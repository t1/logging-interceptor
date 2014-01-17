package com.github.t1.log;

/**
 * Converts some object to a string to be logged, either
 * <ul>
 * <li>a parameter of a logged method,
 * <li>a return value of a method, or
 * <li>a logging context variable (MDC).
 * </ul>
 * Annotate your {@link LogConverter} as {@link LogConverterType}
 */
public interface LogConverter<T> {
    String convert(T object);
}
