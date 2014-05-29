package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static java.lang.Character.*;
import static java.util.Collections.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import javax.enterprise.inject.Instance;

import org.slf4j.*;

import com.github.t1.stereotypes.Annotations;

class LogPointBuilder {
    private static final Pattern VAR = Pattern.compile("\\{(?<var>[^}]*)\\}");
    private static final Pattern NUMERIC = Pattern.compile("(\\+|-|)\\d+");

    private final Method method;
    private final Instance<LogContextVariable> variables;
    private final Converters converters;

    private final Logged logged;
    private final List<LogParameter> rawParams;

    public LogPointBuilder(Method method, Instance<LogContextVariable> variables, Converters converters) {
        this.method = method;
        this.variables = variables;
        this.converters = converters;

        this.logged = Annotations.on(method).getAnnotation(Logged.class);
        this.rawParams = rawParams();
    }

    private List<LogParameter> rawParams() {
        List<LogParameter> list = new ArrayList<>();
        for (int index = 0; index < method.getParameterTypes().length; index++) {
            list.add(new LogParameter(method, index, converters));
        }
        return unmodifiableList(list);
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

    private Class<?> loggerType() {
        Class<?> loggerType = logged.logger();
        if (loggerType == void.class) {
            // the method is declared in the target type, while context.getTarget() is the CDI proxy
            loggerType = method.getDeclaringClass();
            while (loggerType.getEnclosingClass() != null) {
                loggerType = loggerType.getEnclosingClass();
            }
        }
        return loggerType;
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
        for (LogParameter parameter : rawParams) {
            if (!parameter.isAnnotationPresent(DontLog.class) && !isLastThrowable(parameter)) {
                out.append(" {}");
            }
        }
        return out.toString();
    }

    private boolean isLastThrowable(LogParameter parameter) {
        return method.getParameterTypes().length - 1 == parameter.index() && parameter.isThrowable();
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

    private List<LogParameter> buildParameters() {
        final List<LogParameter> list = new ArrayList<>();
        int defaultIndex = 0;
        if (defaultLogMessage()) {
            for (LogParameter parameter : rawParams) {
                if (!parameter.isAnnotationPresent(DontLog.class)) {
                    list.add(parameter);
                }
            }
        } else {
            Matcher matcher = VAR.matcher(logged.value());
            while (matcher.find()) {
                int index = index(matcher, defaultIndex++);
                list.add(rawParams.get(index));
            }
        }
        return Collections.unmodifiableList(list);
    }

    private boolean defaultLogMessage() {
        return "".equals(logged.value());
    }

    private int index(Matcher matcher, int defaultIndex) {
        String expression = matcher.group("var");
        if (expression.isEmpty())
            return defaultIndex;
        if (isNumeric(expression))
            return Integer.parseInt(expression);
        return -1;
    }

    private boolean isNumeric(String expression) {
        return NUMERIC.matcher(expression).matches();
    }

    public LogPoint build() {
        Logger logger = LoggerFactory.getLogger(loggerType());
        LogLevel level = resolveLevel();
        String message = parseMessage();
        List<LogParameter> parameters = buildParameters();
        boolean logResult = method.getReturnType() != void.class;

        return new LogPoint(logger, level, message, parameters, variables, logResult, converters);
    }
}
