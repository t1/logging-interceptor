package com.github.t1.log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;

class LogParameter {
    private final Method method;
    private final int index;
    private final String logContextVariableName;
    private final Converters converters;

    LogParameter(Method method, int index, Converters converters) {
        this.method = method;
        this.index = index;
        this.converters = converters;

        this.logContextVariableName = logContextVariableName();
    }

    private String logContextVariableName() {
        if (!isAnnotationPresent(LogContext.class))
            return null;
        LogContext logContext = getAnnotation(LogContext.class);
        return logContext.value();
    }

    public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType) {
        return getAnnotation(annotationType) != null;
    }

    private <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (final Annotation annotation : method.getParameterAnnotations()[index]) {
            if (annotationType.isInstance(annotation)) {
                return annotationType.cast(annotation);
            }
        }
        return null;
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
        Object object = context.getParameters()[index];
        return converters.convert(object);
    }

    public boolean isThrowable() {
        return Throwable.class.isAssignableFrom(type());
    }

    private Class<?> type() {
        return method.getParameterTypes()[index];
    }

    public int index() {
        return index;
    }
}
