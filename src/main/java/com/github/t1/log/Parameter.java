package com.github.t1.log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * The Java reflection api (before Java 8) regards method arguments as second class citizens: They are not represented
 * as objects like Class, Method, Package, etc. are; they are only accessible through helper methods.<br/>
 * This class tries to fill that gap as good as it goes.
 */
public class Parameter {
    public static List<Parameter> allOf(Method method) {
        final List<Parameter> list = new ArrayList<>();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            list.add(new Parameter(method, i));
        }
        return Collections.unmodifiableList(list);
    }

    private final Method method;

    private final int index;

    public Parameter(Method method, int index) {
        this.method = method;
        this.index = index;
    }

    public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType) {
        return getAnnotation(annotationType) != null;
    }

    /**
     * @return the annotation of that type or <code>null</code> if the parameter is not annotated with that type.
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (final Annotation annotation : getAnnotations()) {
            if (annotationType.isInstance(annotation)) {
                return annotationType.cast(annotation);
            }
        }
        return null;
    }

    public Annotation[] getAnnotations() {
        return method.getParameterAnnotations()[index];
    }

    public int getIndex() {
        return index;
    }
}
