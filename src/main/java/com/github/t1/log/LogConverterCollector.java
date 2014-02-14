package com.github.t1.log;

import java.util.*;

import javax.enterprise.inject.*;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import com.github.t1.stereotypes.Annotations;

/** Collects all implementations of {@link LogConverter}s and provides them as a {@link Map}. */
@Slf4j
public class LogConverterCollector {
    @Inject
    private Instance<LogConverter<Object>> converterInstances;

    @Produces
    Map<Class<?>, LogConverter<Object>> loadConverters() {
        Map<Class<?>, LogConverter<Object>> converters = new LinkedHashMap<>();
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
        return converters;
    }
}
