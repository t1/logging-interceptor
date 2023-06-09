package com.github.t1.log;

import static com.github.t1.log.LogContext.*;

import jakarta.interceptor.InvocationContext;

/** This is the normal case, reading the value from the method parameter. */
class ParameterLogArgument extends ExpressionLogArgument {
    private final String logContextVariableName;
    private final Parameter parameter;
    private final Converters converters;

    ParameterLogArgument(Parameter parameter, Converters converters, String expression) {
        super(expression);

        this.parameter = parameter;
        this.converters = converters;

        this.logContextVariableName = logContextVariableName();
    }

    private String logContextVariableName() {
        if (!parameter.isAnnotationPresent(LogContext.class))
            return null;
        String value = parameter.getAnnotation(LogContext.class).value();
        return (VARIABLE_NAME.equals(value)) ? parameter.getName() : value;
    }

    @Override
    public String name() {
        return parameter.getName();
    }

    @Override
    public Object value(InvocationContext context) {
        Object object = context.getParameters()[parameter.index()];
        object = evaluateExpressionOn(object);
        return converters.convert(object);
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
}
