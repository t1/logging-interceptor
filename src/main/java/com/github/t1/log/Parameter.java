package com.github.t1.log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/** a precursor to JDK 1.8 java.lang.Parameter */
class Parameter {
    private final Method method;
    private final int index;

    Parameter(Method method, int index) {
        this.method = method;
        this.index = index;
    }

    public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType) {
        return getAnnotation(annotationType) != null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (final Annotation annotation : method.getParameterAnnotations()[index]) {
            if (annotationType.isInstance(annotation)) {
                return annotationType.cast(annotation);
            }
        }
        return null;
    }

    public Class<?> type() {
        return method.getParameterTypes()[index];
    }

    public int index() {
        return index;
    }

    public boolean isLast() {
        return method.getParameterTypes().length - 1 == index;
    }

    @Override
    public String toString() {
        return method + "#" + index;
    }
}
