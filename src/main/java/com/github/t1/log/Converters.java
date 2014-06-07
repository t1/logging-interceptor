package com.github.t1.log;

import java.lang.reflect.Method;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.*;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Collects all implementations of {@link Converter}s and delegates {@link #convert(Object)}. */
@Slf4j
@Singleton
public class Converters {
    @AllArgsConstructor
    private static class ConverterMethod {
        private final Converter converterInstance;
        private final Method method;

        public Object convert(Object value) {
            try {
                return method.invoke(converterInstance, value);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("can't convert", e);
            }
        }
    }

    @Inject
    private Instance<Converter> converterInstances;

    Map<Class<?>, ConverterMethod> converters = new HashMap<>();

    @PostConstruct
    void loadConverters() {
        log.debug("loading converters");
        for (Converter converterInstance : converterInstances) {
            Class<? extends Converter> converterType = converterInstance.getClass();
            log.debug("register converters in {}", converterType);
            int count = 0;
            for (Method method : converterType.getMethods()) {
                if (isConverterMethod(method)) {
                    ConverterMethod converterMethod = new ConverterMethod(converterInstance, method);
                    ConverterMethod old = converters.put(method.getParameterTypes()[0], converterMethod);
                    if (old != null)
                        log.error("ambiguous converters for {}: {} and {}", converterType, converterMethod, old);
                    count++;
                }
            }
            log.debug("registered {} converter methods in {}", count, converterType);
        }
        log.debug("converters loaded");
    }

    private boolean isConverterMethod(Method method) {
        return "convert".equals(method.getName()) && method.getReturnType() != void.class
                && method.getParameterTypes().length == 1;
    }

    public Object convert(Object value) {
        if (value == null)
            return null;
        Class<?> type = value.getClass();
        ConverterMethod converter = findConverter(type);
        if (converter != null) {
            try {
                return converter.convert(value);
            } catch (RuntimeException | LinkageError | AssertionError e) {
                log.debug("failed to convert " + value, e);
            }
        }
        return value;
    }

    private ConverterMethod findConverter(Class<?> type) {
        ConverterMethod converter = converters.get(type);
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
}
