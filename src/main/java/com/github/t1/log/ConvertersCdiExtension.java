package com.github.t1.log;

import static com.github.t1.log.LogConverters.*;

import java.lang.reflect.*;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvertersCdiExtension implements Extension {
    public <X extends LogConverter<?>> void processAnnotatedType(@Observes ProcessAnnotatedType<X> pat) {
        javax.enterprise.inject.spi.AnnotatedType<X> annotatedType = pat.getAnnotatedType();
        System.out.println("######### scan " + annotatedType.getJavaClass().getSimpleName());
        log.debug("scan {}", annotatedType.getJavaClass().getSimpleName());
        // double check for CDI 1.0:
        if (isLogConverter(annotatedType)) {
            Class<Object> convertedType = genericType(annotatedType);
            log.debug("register converter {} for {}", annotatedType.getJavaClass(), convertedType);
            LogConverter<?> converter = instantiate(annotatedType);
            if (converter != null) {
                LogConverter<?> old = logConverters.put(convertedType, converter);
                if (old != null) {
                    String oldType = old.getClass().getName();
                    log.error("ambiguous converters for {}: {} and {}", convertedType, annotatedType, oldType);
                }
            }
        }
    }

    private <X> boolean isLogConverter(javax.enterprise.inject.spi.AnnotatedType<X> annotatedType) {
        Class<X> javaClass = annotatedType.getJavaClass();
        return LogConverter.class.isAssignableFrom(javaClass) && LogConverter.class != javaClass;
    }

    @SuppressWarnings("unchecked")
    private Class<Object> genericType(javax.enterprise.inject.spi.AnnotatedType<?> annotatedType) {
        for (Type type : annotatedType.getTypeClosure()) {
            if (isLogConverter(type)) {
                return (Class<Object>) ((ParameterizedType) type).getActualTypeArguments()[0];
            }
        }
        return null;
    }

    private boolean isLogConverter(Type type) {
        return LogConverter.class.equals(rawType(type));
    }

    private Type rawType(Type type) {
        if (type instanceof ParameterizedType)
            return ((ParameterizedType) type).getRawType();
        return null;
    }

    private LogConverter<?> instantiate(javax.enterprise.inject.spi.AnnotatedType<?> annotatedType) {
        try {
            return (LogConverter<?>) annotatedType.getJavaClass().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("could not instantiate " + annotatedType, e);
        }
    }
}
