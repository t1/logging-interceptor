package com.github.t1.log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import javax.interceptor.InvocationContext;

/**
 * The Java reflection api (before Java 8) regards method arguments as second class citizens: They are not represented
 * as objects like Class, Method, Package, etc. are; they are only accessible through helper methods.<br/>
 * This class tries to fill that gap as good as it goes.
 */
public class LogParameter {
    public static List<LogParameter> allOf(Method method, Converters converters) {
        final List<LogParameter> list = new ArrayList<>();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            LogParameter parameter = new LogParameter(method, i, converters);
            if (!parameter.isAnnotationPresent(DontLog.class)) {
                list.add(parameter);
            }
        }
        return Collections.unmodifiableList(list);
    }

    private final Method method;
    private final int index;
    private final String logContextVariableName;
    private final Converters converters;

    private LogParameter(Method method, int index, Converters converters) {
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

    private <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType) {
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
            mdc.put(logContextVariableName, (value == null) ? null : value.toString());
        }
    }

    public Object value(InvocationContext context) {
        Object object = context.getParameters()[index];
        return converters.convert(object);
    }

    public boolean isLastThrowable() {
        return isLast() && isThrowable();
    }

    private boolean isLast() {
        return index == method.getParameterTypes().length - 1;
    }

    public boolean isThrowable() {
        return Throwable.class.isAssignableFrom(type());
    }

    private Class<?> type() {
        return method.getParameterTypes()[index];
    }
}
