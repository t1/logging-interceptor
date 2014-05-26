package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LogMethodTest extends AbstractLoggingInterceptorTests {
    // ----------------------------------------------------------------------------------

    @Test
    public void shouldLogWithMocks() {
        log.info("info-log-message");

        verify(log).info("info-log-message");
    }

    // ----------------------------------------------------------------------------------

    @Logged(level = INFO)
    public static class SimpleClass {
        public void foo() {}
    }

    @Inject
    SimpleClass simpleClass;

    @Test
    public void shouldLogUsingInterceptor() {
        givenLogLevel(INFO);

        simpleClass.foo();

        verify(log).info("foo", NO_ARGS);
    }

    @Test
    public void shouldCacheLogPoint() {
        givenLogLevel(INFO);

        long t0 = System.nanoTime();
        simpleClass.foo();
        long t1 = System.nanoTime();
        simpleClass.foo();
        long t2 = System.nanoTime();

        long d0 = t1 - t0;
        long d1 = t2 - t1;

        verify(log, times(2)).info("foo", NO_ARGS); // actually did log twice
        assertTrue("second call must be faster, but " + d0 + " <= " + d1, d0 > d1);
    }

    // ----------------------------------------------------------------------------------

    public static class CamelCaseClass {
        @Logged
        public void camelCaseMethod() {}
    }

    @Inject
    CamelCaseClass camelCaseClass;

    @Test
    public void shouldConvertCamelCaseToSpaces() {
        camelCaseClass.camelCaseMethod();

        verify(log).debug("camel case method", NO_ARGS);
    }

    // ----------------------------------------------------------------------------------

    public static class LogMessageClass {
        @Logged("log message")
        public void foo() {}
    }

    @Inject
    LogMessageClass logMessageClass;

    @Test
    public void shouldLogExplicitMessage() {
        logMessageClass.foo();

        verify(log).debug("log message", NO_ARGS);
    }

    // ----------------------------------------------------------------------------------

    public static class ReturnVoidClass {
        @Logged
        public void foo() {}
    }

    @Inject
    ReturnVoidClass returnVoidClass;

    @Test
    public void shouldNotLogVoidReturnValue() {
        returnVoidClass.foo();

        verify(log, atLeast(0)).isDebugEnabled();
        verify(log).debug("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    public static class ReturnValueClass {
        @Logged
        public String foo() {
            return "bar";
        }
    }

    @Inject
    ReturnValueClass returnValueClass;

    @Test
    public void shouldLogReturnValue() {
        returnValueClass.foo();

        verify(log).debug("return {}", new Object[] { "bar" });
    }
}
