package com.github.t1.log;

import javax.interceptor.InvocationContext;

/** a precursor to JDK 1.8 java.lang.Parameter */
class LogParameter {
    private final String logContextVariableName;
    private final Converters converters;
    private final Parameter parameter;

    LogParameter(Parameter parameter, Converters converters) {
        this.parameter = parameter;
        this.converters = converters;

        this.logContextVariableName = logContextVariableName();
    }

    private String logContextVariableName() {
        if (!parameter.isAnnotationPresent(LogContext.class))
            return null;
        LogContext logContext = parameter.getAnnotation(LogContext.class);
        return logContext.value();
    }

    public void set(RestorableMdc mdc, InvocationContext context) {
        if (logContextVariableName != null) {
            Object value = value(context);
            if (value != null) {
                mdc.put(logContextVariableName, value.toString());
            }
        }
    }

    public Object value(InvocationContext context) {
        Object object = context.getParameters()[parameter.index()];
        return converters.convert(object);
    }

    public String defaultParamPlaceholder() {
        return (parameter.isAnnotationPresent(DontLog.class) || isLastThrowable()) ? "" : " {}";
    }

    public boolean isLastThrowable() {
        return parameter.isLast() && isThrowable();
    }

    public boolean isThrowable() {
        return Throwable.class.isAssignableFrom(parameter.type());
    }
}
