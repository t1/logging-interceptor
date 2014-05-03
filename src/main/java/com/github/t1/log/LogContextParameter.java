package com.github.t1.log;

import java.util.*;

import javax.interceptor.InvocationContext;

class LogContextParameter {
    public static List<LogContextParameter> list(List<Parameter> parameters, Converters converters) {
        List<LogContextParameter> result = new ArrayList<>();
        for (Parameter parameter : parameters) {
            if (!parameter.isAnnotationPresent(LogContext.class))
                continue;
            LogContext logContext = parameter.getAnnotation(LogContext.class);
            String key = logContext.value();
            result.add(new LogContextParameter(key, parameter.getIndex(), converters));
        }
        return result;
    }

    private final String key;
    private final int parameterIndex;
    private final Converters converters;

    public LogContextParameter(String key, int parameterIndex, Converters converters) {
        this.key = key;
        this.parameterIndex = parameterIndex;
        this.converters = converters;
    }

    public void set(RestorableMdc mdc, InvocationContext context) {
        mdc.put(key, value(context));
    }

    private String value(InvocationContext context) {
        Object object = context.getParameters()[parameterIndex];
        Object converted = converters.convert(object);
        if (converted == null)
            return null;
        return converted.toString();
    }
}
