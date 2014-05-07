package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static java.lang.Character.*;

import java.lang.reflect.*;
import java.util.*;

import javax.enterprise.inject.Instance;
import javax.interceptor.InvocationContext;

import org.slf4j.*;
import org.slf4j.helpers.MessageFormatter;

import com.github.t1.stereotypes.Annotations;

class LogPoint {
    private static final String INDENT = "indent";

    private final Method method;
    private final Instance<LogContextVariable> variables;
    private final Converters converters;

    private final Logged logged;
    private final List<LogParameter> parameters;

    private final LogLevel logLevel;
    private final Logger logger;
    private final String logMessage;

    private final RestorableMdc mdc = new RestorableMdc();

    public LogPoint(Method method, Instance<LogContextVariable> variables, Converters converters) {
        this.method = method;
        this.variables = variables;
        this.converters = converters;

        this.logged = Annotations.on(method).getAnnotation(Logged.class);
        this.parameters = LogParameter.allOf(method, converters);

        this.logMessage = logMessage();
        this.logLevel = resolveLevel();
        this.logger = LoggerFactory.getLogger(loggerType());
    }

    private String logMessage() {
        String message = logged.value();
        if ("".equals(message)) {
            return camelToSpaces(method.getName()) + messageParamPlaceholders();
        } else {
            return message;
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
        for (LogParameter parameter : parameters) {
            if (parameter.isLastThrowable())
                continue;
            out.append(" {}");
        }
        return out.toString();
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

    public void logCall(InvocationContext context) {
        addParamaterLogContexts(context);
        addLogContextVariables();

        if (logLevel.isEnabled(logger)) {
            incrementIndentLogContext();
            if (!parameters.isEmpty() && lastParameter().isThrowable()) {
                String message = throwableMessage(context);
                logLevel.log(logger, message, (Throwable) lastParameter().value(context));
            } else {
                logLevel.log(logger, logMessage, parameterValues(context));
            }
        }
    }

    private void addParamaterLogContexts(InvocationContext context) {
        for (LogParameter parameter : parameters) {
            parameter.set(mdc, context);
        }
    }

    private void addLogContextVariables() {
        for (LogContextVariable variable : variables) {
            if (variable == null) // producers are allowed to return null
                continue;
            String key = variable.getKey();
            String value = variable.getValue();
            mdc.put(key, value);
        }
    }

    private void incrementIndentLogContext() {
        int indent = getIndentLogContext();
        setIndentLogContext(indent + 1);
    }

    private int getIndentLogContext() {
        String indent = mdc.get(INDENT);
        return (indent == null) ? -1 : indent.length() / 2;
    }

    private void setIndentLogContext(int indent) {
        mdc.put(INDENT, Indent.of(indent));
    }

    private LogParameter lastParameter() {
        return parameters.get(parameters.size() - 1);
    }

    private String throwableMessage(InvocationContext context) {
        return MessageFormatter.arrayFormat(logMessage, parameterValues(context)).getMessage();
    }

    private Object[] parameterValues(InvocationContext context) {
        List<Object> result = new ArrayList<>();
        for (LogParameter parameter : parameters) {
            if (parameter.isLastThrowable())
                continue;
            result.add(parameter.value(context));
        }
        return result.toArray();
    }

    public void logResult(Object result) {
        if (method.getReturnType() != void.class) {
            logLevel.log(logger, "return {}", converters.convert(result));
        }
    }

    public void logException(Exception e) {
        logLevel.log(logger, "failed with {}", toString(e));
    }

    private String toString(Exception e) {
        StringBuilder out = new StringBuilder();
        toString(out, e);
        return out.toString();
    }

    private void toString(StringBuilder out, Throwable e) {
        out.append(e.getClass().getSimpleName());
        if (e.getMessage() != null) {
            out.append("(").append(e.getMessage()).append(")");
        }
        if (e.getCause() != null) {
            out.append(" -> ");
            toString(out, e.getCause());
        }
    }

    public void done() {
        mdc.restore();
    }
}
