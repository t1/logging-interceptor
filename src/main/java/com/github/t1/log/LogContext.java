package com.github.t1.log;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

/**
 * Parameters of {@link Logged} methods annotated as {@link LogContext} are placed into the {@link org.slf4j.MDC MDC},
 * so they can be used in a log format. This is most useful to add some important business key like the order id to
 * every line logged, so you can easily find all interactions concerning this order. When the logged method returns, the
 * previous value is restored.
 * <p>
 * If <code>toString</code> is not good enough, you can write a {@link LogConverter}.
 * <p>
 * Note that the MDC is set even if the log level is not enabled!
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface LogContext {
    /** The name of the MDC variable to put. */
    String value();
}
