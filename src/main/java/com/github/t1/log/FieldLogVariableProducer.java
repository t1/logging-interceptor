package com.github.t1.log;

import com.github.t1.stereotypes.Annotations;
import lombok.RequiredArgsConstructor;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Field;

import static com.github.t1.log.LogContext.*;

@RequiredArgsConstructor
public class FieldLogVariableProducer {
    private final Field field;
    private final Converters converters;

    public String name() {
        LogContext logContext = Annotations.on(field).getAnnotation(LogContext.class);
        return VARIABLE_NAME.equals(logContext.value()) ? field.getName() : logContext.value();
    }

    public String value(InvocationContext context) {
        field.setAccessible(true);
        try {
            Object value = field.get(context.getTarget());
            return (value == null) ? "" : converters.convert(value).toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
