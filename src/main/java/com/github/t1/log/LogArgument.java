package com.github.t1.log;

import javax.interceptor.InvocationContext;

/** Something that goes into a log message and/or the {@link org.slf4j.MDC MDC}. */
interface LogArgument {
    /** Used for named arguments in the message or as the name in the MDC. */
    public String name();

    public Object value(InvocationContext context);

    /**
     * Update the MDC with this {@link #name()} to the value (which may be different from what
     * {@link #value(InvocationContext)} returns.
     */
    public void set(RestorableMdc mdc, InvocationContext context);
}
