package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static com.github.t1.log.Logged.*;
import static java.lang.Character.*;
import static java.util.Collections.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import javax.enterprise.inject.Instance;

import org.slf4j.*;

import com.github.t1.log.LogPoint.StandardLogPoint;
import com.github.t1.log.LogPoint.ThrowableLogPoint;
import com.github.t1.stereotypes.Annotations;

class LogPointBuilder {
    private static final Pattern VAR = Pattern.compile("\\{(?<expression>[^}]*)\\}");
    private static final Pattern NUMERIC = Pattern.compile("(\\+|-|)\\d+");

    private final Method method;
    private final Instance<LogContextVariable> variables;
    private final Converters converters;

    private final Logged logged;
    private final List<Parameter> rawParams;

    private final Logger logger;
    private final LogLevel level;
    private final List<LogParameter> logParameters;
    private final LogParameter throwableParameter;

    private int defaultIndex = 0;

    public LogPointBuilder(Method method, Instance<LogContextVariable> variables, Converters converters) {
        this.method = method;
        this.variables = variables;
        this.converters = converters;

        this.logged = Annotations.on(method).getAnnotation(Logged.class);
        this.rawParams = rawParams();

        this.logger = logger();
        this.level = resolveLevel();
        this.logParameters = buildParams();
        this.throwableParameter = throwableParam();
    }

    private Logger logger() {
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
        Logged logged = logged(element);
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

    private Logged logged(AnnotatedElement element) {
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

    private List<LogParameter> buildParams() {
        final List<LogParameter> result = new ArrayList<>();
        if (defaultLogMessage()) {
            for (Parameter parameter : rawParams) {
                if (!parameter.isAnnotationPresent(DontLog.class)) {
                    result.add(new RealLogParameter(parameter, converters, null));
                }
            }
        } else {
            Matcher matcher = VAR.matcher(logged.value());
            while (matcher.find()) {
                String expression = matcher.group("expression");
                result.add(logParameter(expression));
            }
        }
        if (logged.json().length > 0) {
            List<JsonLogDetail> details = Arrays.asList(logged.json());
            result.add(new JsonLogParameter(details, result, converters, logger, level));
        }
        return Collections.unmodifiableList(result);
    }

    private boolean defaultLogMessage() {
        return CAMEL_CASE_METHOD_NAME.equals(logged.value());
    }

    private List<Parameter> rawParams() {
        List<Parameter> list = new ArrayList<>();
        for (int index = 0; index < method.getParameterTypes().length; index++) {
            list.add(new Parameter(method, index));
        }
        return unmodifiableList(list);
    }

    private LogParameter throwableParam() {
        if (rawParams.isEmpty())
            return null;
        Parameter lastParam = rawParams.get(rawParams.size() - 1);
        if (!Throwable.class.isAssignableFrom(lastParam.type()))
            return null;
        return new RealLogParameter(lastParam, converters, null);
    }

    public LogPoint build() {
        String message = parseMessage();
        boolean logResult = method.getReturnType() != void.class;

        if (throwableParameter != null) {
            return new ThrowableLogPoint(logger, level, message, logParameters, throwableParameter, variables,
                    logResult, converters);
        }
        return new StandardLogPoint(logger, level, message, logParameters, variables, logResult, converters);
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
        int n = logParameters.size();
        if (throwableParameter != null)
            --n;
        for (int i = 0; i < n; i++) {
            out.append(" {}");
        }
        return out.toString();
    }

    private String stripPlaceholderBodies(String message) {
        StringBuffer out = new StringBuffer();
        Matcher matcher = VAR.matcher(message);
        while (matcher.find()) {
            matcher.appendReplacement(out, "{}");
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private LogParameter logParameter(String expression) {
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
            return new StaticLogParameter("error", "invalid log parameter expression [" + expression
                    + "] for reference [" + paramRef + "]");
        return new MdcLogParameter(paramRef);
    }

    private LogParameter logParam(int index, String expression) {
        if (index < 0 || index >= rawParams.size())
            return new StaticLogParameter("error", "invalid log parameter index: " + index);
        return new RealLogParameter(rawParams.get(index), converters, expression);
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
}
