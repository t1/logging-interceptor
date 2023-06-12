package com.github.t1.log;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static mock.logging.MockMDC.verifyMdc;

class LogContextVariableProducerTest extends AbstractLoggingInterceptorTests {
    @Dependent
    public static class SimpleClass {
        @Logged
        public void simple() {}
    }

    @Inject
    SimpleClass simple;

    @Produces
    LogContextVariable fooBarVariable = new LogContextVariable("fooVar", "barVar");

    @Test void shouldAddLogContextVariable() {
        simple.simple();

        verifyMdc("fooVar", "barVar");
    }

    @Produces
    LogContextVariable nullVariable = null;

    @Test void shouldSkipNullLogContextVariable() {
        simple.simple();

        // verify not possible... just check that there's no exception
    }
}
