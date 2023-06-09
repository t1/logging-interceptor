package com.github.t1.log;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.github.t1.log.LogLevel._DERIVED_;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Logs the method invocation (the name of the method and the parameter values) and eventually the return value resp.
 * exception thrown. There are helpful defaults for the {@link #level() log-level}, the {@link #logger()}, and even the
 * {@link #value() message}.
 * <p>
 * Note that an interceptor is not called, when you call a method locally (not to mention calling a private method)
 */
@InterceptorBinding
@Target({METHOD, TYPE, ANNOTATION_TYPE, PACKAGE})
@Retention(RUNTIME)
public @interface Logged {
    String USE_CLASS_LOGGER = "###USE_CLASS_LOGGER###";
    String CAMEL_CASE_METHOD_NAME = "###CAMEL_CASE_METHOD_NAME###";

    /**
     * The level of detail to log at. If none is specified, it's derived from the recursively enclosing type's
     * <code>level</code> or finally {@link LogLevel#DEBUG}.
     *
     * @see org.slf4j.Logger the logging methods for those levels
     */
    @Nonbinding LogLevel level() default _DERIVED_;

    /**
     * The class used to create the logger. Defaults to the top level class containing the method being logged (i.e.
     * nested, inner, local, or anonymous classes are unwrapped).
     *
     * @see Class#getEnclosingClass() the comment <i>in</i> <code>Class#getEnclosingClass</code>
     * @see #loggerString()
     */
    @Nonbinding Class<?> logger() default void.class;

    /**
     * The name of the logger, in case you don't want to use the name of a class. Defaults to {@link #logger()}.
     *
     * @see #logger()
     */
    @Nonbinding String loggerString() default USE_CLASS_LOGGER;

    /**
     * The format of the message to log. Defaults to a camel-case-to-space-separated string of the method name with the
     * space separated arguments appended.
     * <p>
     * If you do provide a format, you can either use the slf4j log message format placeholders <code>{}</code>. Or you
     * can use positional indexes (e.g. `{0}`) or parameter names (e.g. `{firstName}`; this requires jdk8 parameter
     * metadata or the normal debug info).
     * <p>
     * And you can use simple expressions, like `person.address.zip`.
     */
    @Nonbinding String value() default CAMEL_CASE_METHOD_NAME;

    /**
     * Set this to have information added to an MDC variable <code>json</code>.
     *
     * @see JsonLogDetail
     */
    @Nonbinding JsonLogDetail[] json() default {};

    /**
     * Some log messages don't have to be repeated over and over again. E.g. some warnings are only interesting once.
     */
    @Nonbinding LogRepeatLimit repeat() default LogRepeatLimit.ALL;

    /**
     * The format for the message logged when the invocation returns.
     * Supported fields: <code>returnValue</code> and <code>time</code>
     */
    @Nonbinding String returnFormat() default "return {returnValue} [time:{time}]";
}
