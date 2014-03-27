package com.github.t1.log;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.*;

import lombok.extern.slf4j.Slf4j;

import com.github.t1.stereotypes.Annotations;

/** Collects all implementations of {@link Converter}s and delegates {@link #convert(Object)}. */
@Slf4j
@Singleton
public class Converters {
    @Inject
    private Instance<Converter> converterInstances;

    Map<Class<?>, Converter> converters = new HashMap<>();

    private class ConverterLoader {
        private final Converter converter;
        private final String converterType;
        private final ConverterType annotation;

        public ConverterLoader(Converter converter) {
            this.converter = converter;

            converterType = converter.getClass().getName();
            log.debug("  register converter {}", converterType);

            annotation = Annotations.on(converter.getClass()).getAnnotation(ConverterType.class);
            if (annotation == null)
                throw new RuntimeException("converter " + converterType + " must be annotated as @"
                        + ConverterType.class.getName());
        }

        public void run() {
            for (Class<?> type : annotation.value()) {
                add(type);
            }
        }

        private void add(Class<?> type) {
            Converter old = converters.put(type, converter);
            if (old != null) {
                log.error("ambiguous converters for {}: {} and {}", type, converterType, old.getClass().getName());
            }
        }
    }

    @PostConstruct
    void loadConverters() {
        log.debug("loading converters");
        for (Converter converter : converterInstances) {
            new ConverterLoader(converter).run();
        }
    }

    public Object convert(Object value) {
        if (value == null)
            return null;
        Class<?> type = value.getClass();
        Converter converter = findConverter(type);
        if (converter != null)
            return converter.convert(value);
        return value;
    }

    private Converter findConverter(Class<?> type) {
        Converter converter = converters.get(type);
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
