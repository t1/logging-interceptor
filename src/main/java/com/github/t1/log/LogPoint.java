package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static java.lang.Character.*;

import java.lang.reflect.*;
import java.util.*;

import javax.enterprise.inject.Instance;
import javax.interceptor.InvocationContext;

import org.slf4j.*;

import com.github.t1.stereotypes.Annotations;

class LogPoint {
    private static final String INDENT = "indent";

    private final Method method;
    private final Instance<LogContextVariable> variables;
    private final Converters converters;

    private final List<Parameter> parameters;
    private final List<LogContextParameter> logContextParameters;

    private final LogLevel logLevel;
    private final LogLevel throwLevel;
    private final Logger logger;
    private final String logMessage;

    private final RestorableMdc mdc = new RestorableMdc();

    public LogPoint(Method method, Instance<LogContextVariable> variables, Converters converters) {
        this.method = method;
        this.variables = variables;
        this.converters = converters;

        this.parameters = Parameter.allOf(method);
        this.logContextParameters = LogContextParameter.list(parameters, converters);

        this.logMessage = logMessage(loggedAnnotation().value());
        this.logLevel = resolveLevel(false);
        this.throwLevel = resolveLevel(true);
        this.logger = getLogger(resolveLogger());
    }

    private Logged loggedAnnotation() {
        return Annotations.on(method).getAnnotation(Logged.class);
    }

    private String logMessage(String message) {
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
        for (Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(DontLog.class))
                continue;
            out.append(" {}");
        }
        return out.toString();
    }

    private LogLevel resolveLevel(boolean throwing) {
        return resolveLevel(method, throwing);
    }

    private LogLevel resolveLevel(AnnotatedElement element, boolean throwing) {
        Logged logged = logged(element);
        if (logged != null) {
            LogLevel level = throwing ? logged.throwLevel() : logged.level();
            if (level != _DERIVED_)
                return level;
            if (container(element) != null) {
                return resolveLevel(container(element), throwing);
            }
        }
        return throwing ? ERROR : DEBUG;
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

    private Class<?> resolveLogger() {
        Class<?> loggerType = loggedAnnotation().logger();
        if (loggerType == void.class) {
            // the method is declared in the target type, while context.getTarget() is the CDI proxy
            loggerType = method.getDeclaringClass();
            while (loggerType.getEnclosingClass() != null) {
                loggerType = loggerType.getEnclosingClass();
            }
        }
        return loggerType;
    }

    private Logger getLogger(Class<?> loggerType) {
        return LoggerFactory.getLogger(loggerType);
    }

    public void logCall(InvocationContext context) {
        // System.out.println("--------------- log call do");
        addParamaterLogContexts(context);
        addLogContextVariables();

        // System.out.println("logger " + logger.getName());
        // System.out.println("level " + logLevel + ": " + logLevel.isEnabled(logger));
        if (logLevel.isEnabled(logger)) {
            incrementIndentLogContext();
            // System.out.println("message " + logMessage + ": " + message());
            logLevel.log(logger, logMessage, parameterValues(context));
        }
        // System.out.println("--------------- log call done");
    }

    private void addParamaterLogContexts(InvocationContext context) {
        for (LogContextParameter logContextParameter : logContextParameters) {
            logContextParameter.set(mdc, context);
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

    private Object[] parameterValues(InvocationContext context) {
        List<Object> result = new ArrayList<>();
        for (Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(DontLog.class))
                continue;
            Object value = context.getParameters()[parameter.getIndex()];
            result.add(converters.convert(value));
        }
        return result.toArray();
    }

    public void logResult(Object result) {
        if (method.getReturnType() != void.class) {
            logLevel.log(logger, "return {}", converters.convert(result));
        }
    }

    public void logException(Exception e) {
        throwLevel.log(logger, "failed", e);
    }

    public void done() {
        mdc.restore();
    }
}