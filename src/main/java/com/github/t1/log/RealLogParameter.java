package com.github.t1.log;

import javax.interceptor.InvocationContext;

class RealLogParameter implements LogParameter {
    private final String logContextVariableName;
    private final Converters converters;
    private final Parameter parameter;

    RealLogParameter(Parameter parameter, Converters converters) {
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

    @Override
    public void set(RestorableMdc mdc, InvocationContext context) {
        if (logContextVariableName != null) {
            Object value = value(context);
            if (value != null) {
                mdc.put(logContextVariableName, value.toString());
            }
        }
    }

    @Override
    public Object value(InvocationContext context) {
        Object object = context.getParameters()[parameter.index()];
        return converters.convert(object);
    }
}
