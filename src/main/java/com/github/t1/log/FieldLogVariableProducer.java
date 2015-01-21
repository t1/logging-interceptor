package com.github.t1.log;

import static com.github.t1.log.LogContext.*;

import java.lang.reflect.Field;

import javax.interceptor.InvocationContext;

import lombok.RequiredArgsConstructor;

import com.github.t1.stereotypes.Annotations;

@RequiredArgsConstructor
public class FieldLogVariableProducer {
    private final Field field;

    public String name() {
        LogContext logContext = Annotations.on(field).getAnnotation(LogContext.class);
        return VARIABLE_NAME.equals(logContext.value()) ? field.getName() : logContext.value();
    }

    public String value(InvocationContext context) {
        field.setAccessible(true);
        try {
            Object value = field.get(context.getTarget());
            return (value == null) ? "" : value.toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
