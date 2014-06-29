package com.github.t1.log;

/**
 * The list of types of information added to the <code>json</code> MDC variable.
 * 
 * @see Logged#json()
 */
public enum JsonLogDetail {
    /** things that are normally added by the logging framework: timestamp, event (the method name), logger, and level */
    EVENT,
    /** log the parameters to the logged method */
    PARAMETERS,
    /** log all MDC variables */
    CONTEXT,
    /** pseudo detail for all other details */
    ALL;
}
