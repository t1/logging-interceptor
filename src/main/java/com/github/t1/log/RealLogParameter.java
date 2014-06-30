package com.github.t1.log;

import static com.github.t1.log.LogContext.*;

import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;

class RealLogParameter implements LogParameter {
    private final String logContextVariableName;
    private final Parameter parameter;
    private final Converters converters;
    private final String expression;

    RealLogParameter(Parameter parameter, Converters converters, String expression) {
        this.parameter = parameter;
        this.converters = converters;
        this.expression = expression;

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

    protected Object evaluateExpressionOn(Object object) {
        if (expression != null) {
            for (String propertyName : expression.split("\\.")) {
                object = propertyValue(object, propertyName);
            }
        }
        return object;
    }

    private Object propertyValue(Object object, String propertyName) {
        String getterName = "get" + initCap(propertyName);
        try {
            Method method = object.getClass().getMethod(getterName);
            return method.invoke(object);
        } catch (ReflectiveOperationException e) {
            return "can't get " + propertyName;
        }
    }

    private String initCap(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
