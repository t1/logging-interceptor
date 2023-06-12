package com.github.t1.log;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static com.github.t1.log.LogLevel.INFO;
import static mock.logging.MockLoggerProvider.array;
import static mock.logging.MockMDC.verifyMdc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("WeakerAccess")
class LogMethodTest extends AbstractLoggingInterceptorTests {
    // ----------------------------------------------------------------------------------

    @Test void shouldLogWithMocks() {
        log.info("info-log-message");

        verify(log).info("info-log-message");
    }

    // ----------------------------------------------------------------------------------

    @Logged(level = INFO)
    @Dependent
    public static class SimpleClass {
        public void foo() {}
    }

    @Inject
    SimpleClass simpleClass;

    @Test void shouldLogUsingInterceptor() {
        givenLogLevel(INFO);

        simpleClass.foo();

        verify(log).info("foo", NO_ARGS);
    }

    @Test void shouldCacheLogPoint() {
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

    @Dependent
    public static class CamelCaseClass {
        @Logged public void camelCaseMethod() {}
    }

    @Inject
    CamelCaseClass camelCaseClass;

    @Test void shouldConvertCamelCaseToSpaces() {
        camelCaseClass.camelCaseMethod();

        verify(log).debug("camel case method", NO_ARGS);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class LogMessageClass {
        @Logged("log message") public void foo() {}

        @Logged("") public void empty() {}
    }

    @Inject
    LogMessageClass logMessageClass;

    @Test void shouldLogExplicitMessage() {
        logMessageClass.foo();

        verify(log).debug("log message", NO_ARGS);
    }

    @Test void shouldNotLogEmptyMessage() {
        logMessageClass.empty();

        verify(log, atLeast(0)).isDebugEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class ReturnVoidClass {
        @Logged public void foo() {}
    }

    @Inject
    ReturnVoidClass returnVoidClass;

    @Test void shouldNotLogVoidReturnValue() {
        returnVoidClass.foo();

        verify(log, atLeast(0)).isDebugEnabled();
        verify(log).debug("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class ReturnValueClass {
        @Logged public String foo() {
            return "bar";
        }
    }

    @Inject
    ReturnValueClass returnValueClass;

    @Test void shouldLogReturnValue() {
        returnValueClass.foo();

        String message = captureMessage();

        assertThat(message).startsWith("return bar [time:").endsWith("]");
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class ReturnFormatClass {
        @Logged(returnFormat = "my-{returnValue} in: {time}")
        public String foo(String result) {return result;}

        @Logged(returnFormat = "") public String bar(String result) {return result;}
    }

    @Inject
    ReturnFormatClass returnFormatClass;

    @Test void shouldLogReturnFormat() {
        returnFormatClass.foo("bar");

        String message = captureMessage();

        assertThat(message).startsWith("my-bar in: ");
    }

    @Test void shouldSetReturnTimeMdc() {
        returnFormatClass.foo("bar");

        String[] messageWords = captureMessage().split(" ");
        String time = messageWords[messageWords.length - 1]; // last word in format
        verifyMdc("time", time);
    }

    @Test void shouldFormatNullReturnValue() {
        returnFormatClass.foo(null);

        String message = captureMessage();

        assertThat(message).startsWith("my-null in: ");
    }

    @Test void shouldNotLogEmptyReturnFormat() {
        var baz = returnFormatClass.bar("baz");

        then(baz).isEqualTo("baz");
        verify(log).debug("bar {}", array("baz"));
        verify(log, atLeast(0)).isDebugEnabled();
        verifyNoMoreInteractions(log);
    }

    // TODO inherit returnFormat from class/package
}
