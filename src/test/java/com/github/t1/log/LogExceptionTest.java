package com.github.t1.log;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static com.github.t1.log.LogLevel.DEBUG;
import static com.github.t1.log.LogLevel.ERROR;
import static com.github.t1.log.LogLevel.INFO;
import static com.github.t1.log.LogLevel.TRACE;
import static com.github.t1.log.LogLevel.WARN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@SuppressWarnings("WeakerAccess")
@RunWith(Arquillian.class)
public class LogExceptionTest extends AbstractLoggingInterceptorTests {
    @SuppressWarnings("UnusedReturnValue")
    public static class ThrowingClass {
        @Logged public String throwRuntimeExceptionWithoutMessage() {
            throw new RuntimeException();
        }

        @Logged public String throwRuntimeExceptionWithMessage() {
            throw new RuntimeException("bar");
        }

        @Logged public String throwRuntimeExceptionWithCausingNpe() {
            throw new RuntimeException("foo", new NullPointerException("bar"));
        }

        @Logged public String throwRuntimeExceptionWithCausingIllegalArgumentAndCausingNpe() {
            throw new RuntimeException("foo", new IllegalArgumentException("bar", new NullPointerException("baz")));
        }
    }

    @Inject
    ThrowingClass throwing;

    private void verifyFailureLogged(String exceptionName) {
        List<Object> event = captureLogEvent(log, "failed with {} [time:{}]");

        assertThat(event.size()).isEqualTo(2);
        assertThat(event.get(0)).isEqualTo(exceptionName);
        assertThat((Long) event.get(1)).isBetween(0L, 20L);
    }

    private List<Object> captureLogEvent(Logger log, @SuppressWarnings("SameParameterValue") String message) {
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
        verify(log).debug(eq(message), captor.capture());
        List<Object[]> args = captor.getAllValues();
        assertThat(args).hasSize(1);
        return Arrays.asList(args.get(0));
    }

    @Test
    public void shouldLogThrownExceptionWithoutMessage() {
        try {
            throwing.throwRuntimeExceptionWithoutMessage();
            fail("expected RuntimeException");
        } catch (RuntimeException ignored) {}

        verify(log).debug("throw runtime exception without message", NO_ARGS);
        verifyFailureLogged("RuntimeException");
    }

    @Test
    public void shouldLogThrownExceptionWithMessage() {
        try {
            throwing.throwRuntimeExceptionWithMessage();
            fail("expected RuntimeException");
        } catch (RuntimeException ignored) {}

        verify(log).debug("throw runtime exception with message", NO_ARGS);
        verifyFailureLogged("RuntimeException(bar)");
    }

    @Test
    public void shouldLogThrownExceptionWithCausingNpe() {
        try {
            throwing.throwRuntimeExceptionWithCausingNpe();
            fail("expected RuntimeException");
        } catch (RuntimeException ignored) {}

        verify(log).debug("throw runtime exception with causing npe", NO_ARGS);
        verifyFailureLogged("RuntimeException(foo) -> NullPointerException(bar)");
    }

    @Test
    public void shouldLogThrownExceptionWithCausingNpeAndCausingIllegalArgument() {
        try {
            throwing.throwRuntimeExceptionWithCausingIllegalArgumentAndCausingNpe();
            fail("expected RuntimeException");
        } catch (RuntimeException ignored) {}

        verify(log).debug("throw runtime exception with causing illegal argument and causing npe", NO_ARGS);
        verifyFailureLogged("RuntimeException(foo)" //
                + " -> IllegalArgumentException(bar)" //
                + " -> NullPointerException(baz)");
    }

    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    public static class ThrowableLogger {
        @Logged
        public void throwing(RuntimeException t) {}

        @Logged
        public void throwing(String param, RuntimeException t) {}

        @Logged
        public void notThrowing(RuntimeException t, String param) {}
    }

    @Inject
    ThrowableLogger throwableLogger;

    @Test
    public void shouldLogRuntimeExceptionParamAsThrowable() {
        IllegalArgumentException exception = new IllegalArgumentException("foo");

        throwableLogger.throwing(exception);

        verify(log).debug("throwing", exception);
    }

    @Test
    public void shouldLogRuntimeExceptionParamAsThrowableWithFormattedMessage() {
        IllegalArgumentException exception = new IllegalArgumentException("foo");

        throwableLogger.throwing("foo", exception);

        verify(log).debug("throwing foo", exception);
    }

    @Test
    public void shouldLogRuntimeExceptionParamNormallyWhenNotLast() {
        IllegalArgumentException exception = new IllegalArgumentException("foo");

        throwableLogger.notThrowing(exception, "bar");

        //noinspection RedundantArrayCreation
        verify(log).debug("not throwing {} {}", new Object[] { exception, "bar" });
    }

    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    static class ExceptionLevelLogger {
        @Logged(level = ERROR)
        void error(RuntimeException e) {}

        @Logged(level = WARN)
        void warn(RuntimeException e) {}

        @Logged(level = INFO)
        void info(RuntimeException e) {}

        @Logged(level = DEBUG)
        void debug(RuntimeException e) {}

        @Logged(level = TRACE)
        void trace(RuntimeException e) {}
    }

    @Inject
    ExceptionLevelLogger exceptionLevelLogger;

    @Test
    public void shouldLogExceptionAtErrorLevel() {
        RuntimeException exception = new RuntimeException();

        exceptionLevelLogger.error(exception);

        verify(log).error("error", exception);
    }

    @Test
    public void shouldLogExceptionAtWarnLevel() {
        RuntimeException exception = new RuntimeException();

        exceptionLevelLogger.warn(exception);

        verify(log).warn("warn", exception);
    }

    @Test
    public void shouldLogExceptionAtInfoLevel() {
        RuntimeException exception = new RuntimeException();

        exceptionLevelLogger.info(exception);

        verify(log).info("info", exception);
    }

    @Test
    public void shouldLogExceptionAtDebugLevel() {
        RuntimeException exception = new RuntimeException();

        exceptionLevelLogger.debug(exception);

        verify(log).debug("debug", exception);
    }

    @Test
    public void shouldLogExceptionAtTraceLevel() {
        givenLogLevel(TRACE);
        RuntimeException exception = new RuntimeException();

        exceptionLevelLogger.trace(exception);

        verify(log).trace("trace", exception);
    }

    // ----------------------------------------------------------------------------------

    @SuppressWarnings({"unused", "SameParameterValue"})
    static class ExceptionLogger {
        @Logged(level = ERROR)
        void failed(String operation, RuntimeException e) {}

        @Logged("message with {}")
        void foo(RuntimeException foo) {}

        @Logged("message with {}")
        void foo(String message, RuntimeException foo) {}

        @Logged("message with {} and {}")
        void fooWithTwo(String message, RuntimeException foo) {}
    }

    @Inject
    ExceptionLogger exceptionLogger;

    @Test
    public void shouldFormatDefaultMessageBeforeException() {
        RuntimeException runtimeException = new RuntimeException();

        exceptionLogger.failed("my operation", runtimeException);

        verify(log).error("failed my operation", runtimeException);
    }

    @Test
    public void shouldFormatExplicitMessageUsingException() {
        RuntimeException runtimeException = new RuntimeException("foo");

        exceptionLogger.foo(runtimeException);

        //noinspection PlaceholderCountMatchesArgumentCount
        verify(log).debug("message with {}", runtimeException);
    }

    @Test
    public void shouldFormatExplicitMessageBeforeException() {
        RuntimeException runtimeException = new RuntimeException();

        exceptionLogger.foo("message", runtimeException);

        verify(log).debug("message with message", runtimeException);
    }

    @Test
    public void shouldFormatExplicitMessageAndException() {
        RuntimeException runtimeException = new RuntimeException();

        exceptionLogger.fooWithTwo("message", runtimeException);

        //noinspection PlaceholderCountMatchesArgumentCount
        verify(log).debug("message with message and {}", runtimeException);
    }
}
