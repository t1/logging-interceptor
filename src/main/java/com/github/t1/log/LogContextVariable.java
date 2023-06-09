package com.github.t1.log;

import lombok.Value;
import lombok.experimental.Accessors;

/**
 * A variable that can be added to the {@link org.slf4j.MDC}. If a producer returns <code>null</code>, nothing will be
 * added.
 */
@Value
@Accessors(fluent = true)
public class LogContextVariable {
    String key, value;
}
