package com.github.t1.log;

import jakarta.enterprise.context.Dependent;

@Dependent
public class ImplicitLoggerClass {
    @Logged
    public void foo() {}
}
