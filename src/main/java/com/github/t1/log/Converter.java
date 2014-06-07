package com.github.t1.log;

/**
 * Marker interface for classes that contain methods to convert some object to a string or other simple type to be
 * logged, either
 * <ul>
 * <li>a parameter of a logged method,
 * <li>a return value of a method, or
 * <li>a logging context variable (MDC).
 * </ul>
 * All methods named <code>convert</code> taking one argument and returning non-void will be registered as converter
 * methods.
 */
public interface Converter {}
