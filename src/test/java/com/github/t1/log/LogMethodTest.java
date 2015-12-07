package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

@RunWith(Arquillian.class)
public class LogMethodTest extends AbstractLoggingInterceptorTests {

    static void verifyLoggedResult(Logger log, String message, String... resultMessageArguments) {
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
        verify(log).debug(eq(message), captor.capture());
        // this seems to be a bug in Mockito: doesn't work with object[]
        @SuppressWarnings("unchecked")
        List<Object> args = (List<Object>) (Object) captor.getAllValues();

        verifyResultMessageArguments(resultMessageArguments, args);
        verifyTime(resultMessageArguments, args);
    }

    private static void verifyTime(String[] resultMessageArguments, List<Object> args) {
        assertTrue(((Long) args.get(resultMessageArguments.length)) >= 0);
    }

    private static void verifyResultMessageArguments(String[] resultMessageArguments, List<Object> args) {
        assertEquals(resultMessageArguments.length, args.size() - 1);
        for (int i = 0; i < resultMessageArguments.length - 1; i++) {
            String resultString = resultMessageArguments[i];
            assertEquals(resultString, args.get(i));
        }
    }


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
        assertTrue(LoggingInterceptor.CACHE.isEmpty());

        // the timing stuff is not very robust... but sometimes helpful ;)
        // long t0 = System.nanoTime();
        simpleClass.foo();
        // long t1 = System.nanoTime();
        simpleClass.foo();
        // long t2 = System.nanoTime();

        // long d0 = t1 - t0;
        // long d1 = t2 - t1;

        verify(log, times(2)).info("foo", NO_ARGS); // actually did log twice
        // assertTrue("expected second (cached) call must be faster, but actually " + d0 + " <= " + d1, d0 > d1);
        assertFalse(LoggingInterceptor.CACHE.isEmpty());
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

        verifyLoggedResult(log, "return {} [time:{}]", "bar");
    }

    public static class AnnotatedReturnValueClass {
        @Logged(shouldLogResultValue = false)
        public String foo() {
            return "bar";
        }
    }

    @Inject
    AnnotatedReturnValueClass annotatedReturnValueClass;

    @Test
    public void shouldHideReturnValue() {
        annotatedReturnValueClass.foo();

        verifyLoggedResult(log, "returned [time:{}]");
    }
}
