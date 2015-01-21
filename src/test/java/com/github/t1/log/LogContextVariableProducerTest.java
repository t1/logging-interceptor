package com.github.t1.log;

import static org.slf4j.impl.StaticMDCBinder.*;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LogContextVariableProducerTest extends AbstractLoggingInterceptorTests {
    public static class SimpleClass {
        @Logged
        public void simple() {}
    }

    @Inject
    SimpleClass simple;

    @Produces
    LogContextVariable fooBarVariable = new LogContextVariable("fooVar", "barVar");

    @Test
    public void shouldAddLogContextVariable() {
        simple.simple();

        verifyMdc("fooVar", "barVar");
    }

    @Produces
    LogContextVariable nullVariable = null;

    @Test
    public void shouldSkipNullLogContextVariable() {
        simple.simple();

        // verify not possible... just check that there's no exception
    }
}
