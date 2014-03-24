package com.github.t1.log;

import static java.util.Arrays.*;

import java.io.Serializable;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.*;

import lombok.extern.slf4j.Slf4j;

import com.github.t1.stereotypes.Annotations;

/** Collects all implementations of {@link LogConverter}s and provides them as a {@link Map}. */
@Slf4j
@Singleton
public class LogConverters {
    @Inject
    private Instance<LogConverter<Object>> converterInstances;

    Map<Class<?>, LogConverter<Object>> converters = new HashMap<>();

    private class ConverterLoader {
        private final LogConverter<Object> converter;
        private final String converterType;
        private final LogConverterType annotation;

        public ConverterLoader(LogConverter<Object> converter) {
            this.converter = converter;

            converterType = converter.getClass().getName();
            log.debug("register converter {}", converterType);

            annotation = Annotations.on(converter.getClass()).getAnnotation(LogConverterType.class);
            if (annotation == null)
                throw new RuntimeException("converter " + converterType + " must be annotated as @"
                        + LogConverterType.class.getName());
        }

        public void run() {
            for (Class<?> type : annotation.value()) {
                add(type);
            }
        }

        private void add(Class<?> type) {
            LogConverter<Object> old = converters.put(type, converter);
            if (old != null)
                log.error("ambiguous converters for {}: {} and {}", type, converterType, old.getClass().getName());
            Class<?> superclass = type.getSuperclass();
            if (superclass != Object.class)
                add(superclass);
            for (Class<?> implemented : type.getInterfaces()) {
                if (WELL_KNOWN_INERFACES.contains(implemented))
                    continue;
                add(implemented);
            }
        }
    }

    private static final List<Class<?>> WELL_KNOWN_INERFACES = asList(Serializable.class, Runnable.class);

    @PostConstruct
    void loadConverters() {
        for (LogConverter<Object> converter : converterInstances) {
            new ConverterLoader(converter).run();
        }
    }

    public Object convert(Object value) {
        if (value == null)
            return null;
        Class<?> type = value.getClass();
        LogConverter<Object> converter = converters.get(type);
        if (converter != null)
            return converter.convert(value);
        return value;
    }
}
