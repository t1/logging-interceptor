package com.github.t1.log;

/**
 * Converts some object to a string to be logged, either
 * <ul>
 * <li>a parameter of a logged method,
 * <li>a return value of a method, or
 * <li>a logging context variable (MDC).
 * </ul>
 * Annotate your {@link Converter} as {@link ConverterType}
 * <p/>
 * Can't be generic, or the injection in {@link Converters} won't find any.
 */
public interface Converter {
    String convert(Object object);
}
