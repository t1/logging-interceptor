package com.github.t1.log;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * The Java reflection api (before Java 8) regards method arguments as second class citizens: They are not represented
 * as objects like Class, Method, Package, etc. are; they are only accessible through helper methods.<br/>
 * This class tries to fill that gap as good as it goes.
 */
public class Parameter {
    public static List<Parameter> allOf(Method method) {
        final List<Parameter> list = new ArrayList<Parameter>();
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

    public Method getMethod() {
        return method;
    }

    public Type getGenericType() {
        return method.getGenericParameterTypes()[index];
    }

    public Class<?> getType() {
        return method.getParameterTypes()[index];
    }

    /**
     * Can that value be passed as this parameter? I.e. is it an instance of the correct type or (if it's not really
     * primitive) <code>null</code>; correctly handles primitive types where {@link Class#isInstance(Object)} returns
     * <code>false</code> for.
     */
    public boolean isAssignable(Object value) {
        Class<?> parameterType = getType();
        if (parameterType.isPrimitive()) {
            if (parameterType == Boolean.TYPE)
                return value.getClass() == Boolean.class;
            if (parameterType == Byte.TYPE)
                return value.getClass() == Byte.class;
            if (parameterType == Character.TYPE)
                return value.getClass() == Character.class;
            if (parameterType == Short.TYPE)
                return value.getClass() == Short.class;
            if (parameterType == Integer.TYPE)
                return value.getClass() == Integer.class;
            if (parameterType == Long.TYPE)
                return value.getClass() == Long.class;
            if (parameterType == Float.TYPE)
                return value.getClass() == Float.class;
            if (parameterType == Double.TYPE)
                return value.getClass() == Double.class;
            throw new AssertionError("unsupported primitive type: " + parameterType);
        } else {
            return value == null || parameterType.isInstance(value);
        }
    }

    @Override
    public String toString() {
        return Parameter.class.getSimpleName() + "#" + getIndex() + " of " + getMethod().toGenericString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + index;
        result = prime * result + method.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Parameter that = (Parameter) obj;
        if (index != that.index)
            return false;
        if (!method.equals(that.method))
            return false;
        return true;
    }
}
