package com.github.t1.log;

import com.github.t1.log.LogPoint.NullLogPoint;
import com.github.t1.log.LogPoint.StandardLogPoint;
import com.github.t1.log.LogPoint.ThrowableLogPoint;
import com.github.t1.stereotypes.Annotations;
import lombok.experimental.Delegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.t1.log.LogLevel.DEBUG;
import static com.github.t1.log.LogLevel._DERIVED_;
import static com.github.t1.log.Logged.CAMEL_CASE_METHOD_NAME;
import static com.github.t1.log.Logged.USE_CLASS_LOGGER;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;
import static java.util.Collections.unmodifiableList;

class LogPointBuilder {
    private static final Pattern VAR = Pattern.compile("\\{(?<expression>[^}]*)}");
    private static final Pattern NUMERIC = Pattern.compile("(\\+|-|)\\d+");

    private final Method method;
    private final Logged logged;

    @Delegate
    private final LogPointContext context;

    private LogArgument throwableParameter;
    private List<Parameter> rawParams;

    private int defaultIndex = 0;

    public LogPointBuilder(Method method, Logged logged, LogPointContext context) {
        this.method = method;
        this.logged = logged;
        this.context = context;
    }

    public LogPoint build() {
        if (logged == null)
            return new NullLogPoint(context);

        this.rawParams = rawParams();
        this.throwableParameter = throwableParam();

        this.context //
            .logger(buildLogger()) //
            .level(resolveLevel()) //
            .fieldLogContexts(buildFieldLogContextVariables()) //
            .logArguments(buildLogArguments()) //
            .messageFormat(parseMessage()) //
            .voidMethod(method.getReturnType() == void.class) //
            .returnFormat(loggedAnnotationOn(method).returnFormat()) //
            .repeatController(RepeatController.createFor(logged.repeat())) //
        ;

        if (throwableParameter != null)
            return new ThrowableLogPoint(context, throwableParameter);
        return new StandardLogPoint(context);
    }

    private List<Parameter> rawParams() {
        List<Parameter> list = new ArrayList<>();
        for (int index = 0; index < method.getParameterTypes().length; index++) {
            list.add(new Parameter(method, index));
        }
        return unmodifiableList(list);
    }

    private Logger buildLogger() {
        String loggerString = logged.loggerString();
        if (!USE_CLASS_LOGGER.equals(loggerString))
            return LoggerFactory.getLogger(loggerString);
        Class<?> loggerType = logged.logger();
        if (loggerType == void.class) {
            // the method is declared in the target type, while context.getTarget() is the CDI proxy
            loggerType = method.getDeclaringClass();
            while (loggerType.getEnclosingClass() != null) {
                loggerType = loggerType.getEnclosingClass();
            }
        }
        return LoggerFactory.getLogger(loggerType);
    }

    private LogLevel resolveLevel() {
        return resolveLevel(method);
    }

    private LogLevel resolveLevel(AnnotatedElement element) {
        Logged logged = loggedAnnotationOn(element);
        if (logged != null) {
            LogLevel level = logged.level();
            if (level != _DERIVED_)
                return level;
        }
        Class<?> container = container(element);
        if (container != null)
            return resolveLevel(container);
        return DEBUG;
    }

    private Logged loggedAnnotationOn(AnnotatedElement element) {
        return annotations(element).getAnnotation(Logged.class);
    }

    private AnnotatedElement annotations(AnnotatedElement element) {
        if (element instanceof Method)
            return Annotations.on((Method) element);
        return Annotations.on((Class<?>) element);
    }

    private Class<?> container(AnnotatedElement element) {
        if (element instanceof Member)
            return ((Member) element).getDeclaringClass();
        return ((Class<?>) element).getEnclosingClass();
    }

    private List<LogArgument> buildLogArguments() {
        final List<LogArgument> result = new ArrayList<>();
        if (defaultLogMessage()) {
            buildParamsFromRawParams(result);
        } else {
            buildParamsFromMessage(result);
        }
        if (logged.json().length > 0) {
            List<JsonLogDetail> details = Arrays.asList(logged.json());
            result.add(new JsonLogArgument(details, result, converters(), logger(), level()));
        }
        return Collections.unmodifiableList(result);
    }

    private List<FieldLogVariableProducer> buildFieldLogContextVariables() {
        List<FieldLogVariableProducer> result = new ArrayList<>();
        for (Field field : method.getDeclaringClass().getDeclaredFields()) {
            LogContext logContext = Annotations.on(field).getAnnotation(LogContext.class);
            if (logContext == null)
                continue;
            result.add(new FieldLogVariableProducer(field, converters()));
        }
        return result;
    }

    private void buildParamsFromRawParams(final List<LogArgument> result) {
        for (Parameter parameter : rawParams) {
            if (!parameter.isAnnotationPresent(DontLog.class)) {
                result.add(new ParameterLogArgument(parameter, converters(), null));
            }
        }
    }

    private void buildParamsFromMessage(final List<LogArgument> result) {
        Matcher matcher = VAR.matcher(logged.value());
        while (matcher.find()) {
            String expression = matcher.group("expression");
            result.add(logArgument(expression));
        }
    }

    private LogArgument logArgument(String expression) {
        int dot = expression.indexOf('.');
        String paramRef;
        if (dot < 0) {
            paramRef = expression;
            expression = null;
        } else {
            paramRef = expression.substring(0, dot);
            expression = expression.substring(dot + 1);
        }
        if (paramRef.isEmpty())
            return logParam(defaultIndex++, expression);
        if (isNumeric(paramRef))
            return logParam(Integer.parseInt(paramRef), expression);
        if (isParameterName(paramRef))
            return logParam(parameterNameIndex(paramRef), expression);
        if (expression != null)
            return new StaticLogArgument("error",
                "invalid log parameter expression [" + expression + "] for reference [" + paramRef + "]");
        return new MdcLogArgument(paramRef);
    }

    private LogArgument logParam(int index, String expression) {
        if (index < 0 || index >= rawParams.size())
            return new StaticLogArgument("error", "invalid log parameter index: " + index);
        return new ParameterLogArgument(rawParams.get(index), converters(), expression);
    }

    private boolean isNumeric(String expression) {
        return NUMERIC.matcher(expression).matches();
    }

    private boolean isParameterName(String expression) {
        return parameterNameIndex(expression) >= 0;
    }

    private int parameterNameIndex(String expression) {
        for (Parameter parameter : rawParams) {
            if (expression.equals(parameter.getName())) {
                return parameter.index();
            }
        }
        return -1;
    }

    private LogArgument throwableParam() {
        if (rawParams.isEmpty())
            return null;
        Parameter lastParam = rawParams.get(rawParams.size() - 1);
        if (!Throwable.class.isAssignableFrom(lastParam.type()))
            return null;
        return new ParameterLogArgument(lastParam, converters(), null);
    }

    private boolean defaultLogMessage() {
        return CAMEL_CASE_METHOD_NAME.equals(logged.value());
    }

    private String parseMessage() {
        if (defaultLogMessage()) {
            return camelToSpaces(method.getName()) + messageParamPlaceholders();
        } else {
            return stripPlaceholderBodies(logged.value());
        }
    }

    private String camelToSpaces(String string) {
        StringBuilder out = new StringBuilder();
        for (Character c : string.toCharArray()) {
            if (isUpperCase(c)) {
                out.append(' ');
                out.append(toLowerCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private String messageParamPlaceholders() {
        StringBuilder out = new StringBuilder();
        int n = logArguments().size();
        if (throwableParameter != null)
            --n;
        out.append(" {}".repeat(Math.max(0, n)));
        return out.toString();
    }

    private String stripPlaceholderBodies(String message) {
        StringBuilder out = new StringBuilder();
        Matcher matcher = VAR.matcher(message);
        while (matcher.find()) {
            matcher.appendReplacement(out, "{}");
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
