package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * Logs the method invocation (the name of the method and the parameter values) and eventually the return value resp.
 * exception thrown. There are helpful defaults for the {@link #level() log-level}, the {@link #logger()}, and even the
 * {@link #value() message}.
 * <p>
 * Note that an interceptor is not called, when you call a method locally (not to mention calling a private method)
 * <p>
 * TODO find out and document how to call through the interceptor stack on self
 */
@InterceptorBinding
@Target({ METHOD, TYPE, ANNOTATION_TYPE, PACKAGE })
@Retention(RUNTIME)
public @interface Logged {
    /**
     * The level of detail to log at. If none is specified, it's derived from the recursively enclosing type's
     * <code>level</code> or finally {@link LogLevel#DEBUG}.
     * 
     * @see org.slf4j.Logger the logging methods for those levels
     */
    @Nonbinding
    public LogLevel level() default _DERIVED_;

    /**
     * The class used to create the logger. Defaults to the top level class containing the method being logged (i.e.
     * nested, inner, local, or anonymous classes are unwrapped).
     * 
     * @see Class#getEnclosingClass() the comment <i>in</i> <code>Class#getEnclosingClass</code>
     */
    @Nonbinding
    public Class<?> logger() default void.class;

    /**
     * The format of the message to log. Defaults to a camel-case-to-space-separated string of the method name with the
     * space separated arguments appended. If you do provide a format, make sure to include enough placeholders ("{}")
     * for the arguments.
     */
    @Nonbinding
    public String value() default "";

    /**
     * The level to log exceptions at. If none is specified, it's derived from the recursively enclosing type's
     * <code>throwLevel</code> or finally {@link LogLevel#ERROR}.
     */
    @Nonbinding
    public LogLevel throwLevel() default _DERIVED_;
}
