package com.github.t1.log;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * A variable that can be added to the {@link org.slf4j.MDC}. If a producers returns <code>null</code>, nothing will be
 * added.
 */
@Value
@Accessors(fluent = true)
public class LogContextVariable {
    private final String key, value;
}
