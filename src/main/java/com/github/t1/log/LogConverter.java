package com.github.t1.log;

/**
 * Converts some object to a primitive type or string to be logged, either
 * <ul>
 * <li>a parameter of a logged method,
 * <li>a return value of a method, or
 * <li>a logging context variable (MDC).
 * </ul>
 */
public interface LogConverter<T> {
    Object convert(T object);
}
