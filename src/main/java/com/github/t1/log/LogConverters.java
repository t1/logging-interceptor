package com.github.t1.log;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import com.github.t1.stereotypes.Annotations;

/** Collects all implementations of {@link LogConverter}s and provides them as a {@link Map}. */
@Slf4j
public class LogConverters {
    private static final LogConverter<Object> DEFAULT_CONVERTER = new ToStringLogConverter();

    /** The default converter; we still need the annotation, and Void is a good value for that. */
    @LogConverterType(Void.class)
    private static final class ToStringLogConverter implements LogConverter<Object> {
        @Override
        public String convert(Object object) {
            return Objects.toString(object);
        }
    }

    @Inject
    private Instance<LogConverter<Object>> converterInstances;

    Map<Class<?>, LogConverter<Object>> converters = new HashMap<>();

    @PostConstruct
    void loadConverters() {
        for (LogConverter<Object> converter : converterInstances) {
            String converterType = converter.getClass().getName();
            log.debug("register converter {}", converterType);
            LogConverterType annotation = Annotations.on(converter.getClass()).getAnnotation(LogConverterType.class);
            if (annotation == null)
                throw new RuntimeException("converter " + converterType + " must be annotated as @"
                        + LogConverterType.class.getName());
            for (Class<?> type : annotation.value()) {
                LogConverter<Object> old = converters.put(type, converter);
                if (old != null) {
                    log.error("duplicate converters for {}: {} and {}", type, converterType, old.getClass().getName());
                }
            }
        }
    }

    public LogConverter<Object> of(Class<?> type) {
        LogConverter<Object> converter = converters.get(type);
        return (converter != null) ? converter : DEFAULT_CONVERTER;
    }
}
