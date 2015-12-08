package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
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
public class LogExceptionTest extends AbstractLoggingInterceptorTests {
    public static class ThrowingClass {
        @Logged
        public String throwRuntimeExeptionWithoutMessage() {
            throw new RuntimeException();
        }

        @Logged
        public String throwRuntimeExeptionWithMessage() {
            throw new RuntimeException("bar");
        }

        @Logged
        public String throwRuntimeExeptionWithCausingNpe() {
            throw new RuntimeException("foo", new NullPointerException("bar"));
        }

        @Logged
        public String throwRuntimeExeptionWithCausingIllegalArgumentAndCausingNpe() {
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

    private List<Object> captureLogEvent(Logger log, String message) {
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
        verify(log).debug(eq(message), captor.capture());
        // this seems to be a bug in Mockito: doesn't work with object[]
        @SuppressWarnings("unchecked")
        List<Object> args = (List<Object>) (Object) captor.getAllValues();
        return args;
    }

    @Test
    public void shouldLogThrownExeptionWithoutMessage() {
        try {
            throwing.throwRuntimeExeptionWithoutMessage();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).debug("throw runtime exeption without message", NO_ARGS);
        verifyFailureLogged("RuntimeException");
    }

    @Test
    public void shouldLogThrownExeptionWithMessage() {
        try {
            throwing.throwRuntimeExeptionWithMessage();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).debug("throw runtime exeption with message", NO_ARGS);
        verifyFailureLogged("RuntimeException(bar)");
    }

    @Test
    public void shouldLogThrownExeptionWithCausingNpe() {
        try {
            throwing.throwRuntimeExeptionWithCausingNpe();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).debug("throw runtime exeption with causing npe", NO_ARGS);
        verifyFailureLogged("RuntimeException(foo) -> NullPointerException(bar)");
    }

    @Test
    public void shouldLogThrownExeptionWithCausingNpeAndCausingIllegalArgument() {
        try {
            throwing.throwRuntimeExeptionWithCausingIllegalArgumentAndCausingNpe();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).debug("throw runtime exeption with causing illegal argument and causing npe", NO_ARGS);
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

    @SuppressWarnings("unused")
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

        verify(log).debug("message with " + runtimeException, runtimeException);
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

        verify(log).debug("message with message and " + runtimeException, runtimeException);
    }
}
