package com.github.t1.log;

import java.util.*;

import javax.inject.Singleton;

@Singleton
class LogConverters {
    static final Map<Class<?>, LogConverter<?>> logConverters = new HashMap<>();

    private static final List<Class<?>> PRIMITIVE_WRAPPERS = Arrays.<Class<?>> asList(Boolean.class, Byte.class,
            Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class);

    public Object convert(Object value) {
        if (value == null)
            return null;
        Class<?> type = value.getClass();
        LogConverter<Object> converter = findConverter(type);
        if (converter != null)
            return converter.convert(value);
        if (value instanceof String || value instanceof Throwable || isPrimitive(value.getClass()))
            return value;
        return value.toString();
    }

    private LogConverter<Object> findConverter(Class<?> type) {
        @SuppressWarnings("unchecked")
        LogConverter<Object> converter = (LogConverter<Object>) logConverters.get(type);
        if (converter != null)
            return converter;

        Class<?> superclass = type.getSuperclass();
        if (superclass != null) {
            converter = findConverter(superclass);
            if (converter != null) {
                return converter;
            }
        }

        for (Class<?> implemented : type.getInterfaces()) {
            converter = findConverter(implemented);
            if (converter != null) {
                return converter;
            }
        }

        return null;
    }

    private boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() || PRIMITIVE_WRAPPERS.contains(type);
    }
}
