package com.github.t1.log;

import jakarta.inject.Inject;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.t1.log.LogLevel.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.slf4j.impl.StaticMDCBinder.verifyMdc;

@SuppressWarnings("WeakerAccess")
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
        @Logged public void camelCaseMethod() {}
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
        @Logged("log message") public void foo() {}

        @Logged("") public void empty() {}
    }

    @Inject
    LogMessageClass logMessageClass;

    @Test
    public void shouldLogExplicitMessage() {
        logMessageClass.foo();

        verify(log).debug("log message", NO_ARGS);
    }

    @Test
    public void shouldNotLogEmptyMessage() {
        logMessageClass.empty();

        verify(log, atLeast(0)).isDebugEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    public static class ReturnVoidClass {
        @Logged public void foo() {}
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
        @Logged public String foo() {
            return "bar";
        }
    }

    @Inject
    ReturnValueClass returnValueClass;

    @Test
    public void shouldLogReturnValue() {
        returnValueClass.foo();

        String message = captureMessage();

        assertThat(message).startsWith("return bar [time:").endsWith("]");
    }

    // ----------------------------------------------------------------------------------

    public static class ReturnFormatClass {
        @Logged(returnFormat = "my-{returnValue} in: {time}")
        public String foo(String result) {return result;}

        @Logged(returnFormat = "") public String bar(String result) {return result;}
    }

    @Inject
    ReturnFormatClass returnFormatClass;

    @Test
    public void shouldLogReturnFormat() {
        returnFormatClass.foo("bar");

        String message = captureMessage();

        assertThat(message).startsWith("my-bar in: ");
    }

    @Test
    public void shouldSetReturnTimeMdc() {
        returnFormatClass.foo("bar");

        String[] messageWords = captureMessage().split(" ");
        String time = messageWords[messageWords.length - 1]; // last word in format
        verifyMdc("time", time);
    }

    @Test
    public void shouldFormatNullReturnValue() {
        returnFormatClass.foo(null);

        String message = captureMessage();

        assertThat(message).startsWith("my-null in: ");
    }

    @Test
    public void shouldNotLogEmptyReturnFormat() {
        var baz = returnFormatClass.bar("baz");

        then(baz).isEqualTo("baz");
        verify(log).debug("bar {}", "baz");
        verify(log, atLeast(0)).isDebugEnabled();
        verifyNoMoreInteractions(log);
    }

    // TODO inherit returnFormat from class/package
}
