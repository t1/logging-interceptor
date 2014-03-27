package com.github.t1.log;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

/**
 * Prevent this parameter from being logged. This is necessary for, e.g. passwords, that must not turn up in log files.
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface DontLog {}
