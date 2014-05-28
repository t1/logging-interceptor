package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    @Test
    public void shouldLogThrownExeptionWithoutMessage() {
        try {
            throwing.throwRuntimeExeptionWithoutMessage();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).debug("throw runtime exeption without message", NO_ARGS);
        verify(log).debug("failed with {}", new Object[] { "RuntimeException" });
    }

    @Test
    public void shouldLogThrownExeptionWithMessage() {
        try {
            throwing.throwRuntimeExeptionWithMessage();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).debug("throw runtime exeption with message", NO_ARGS);
        verify(log).debug("failed with {}", new Object[] { "RuntimeException(bar)" });
    }

    @Test
    public void shouldLogThrownExeptionWithCausingNpe() {
        try {
            throwing.throwRuntimeExeptionWithCausingNpe();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).debug("throw runtime exeption with causing npe", NO_ARGS);
        verify(log).debug("failed with {}", new Object[] { "RuntimeException(foo) -> NullPointerException(bar)" });
    }

    @Test
    public void shouldLogThrownExeptionWithCausingNpeAndCausingIllegalArgument() {
        try {
            throwing.throwRuntimeExeptionWithCausingIllegalArgumentAndCausingNpe();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).debug("throw runtime exeption with causing illegal argument and causing npe", NO_ARGS);
        verify(log).debug("failed with {}", new Object[] { "RuntimeException(foo)" //
                + " -> IllegalArgumentException(bar)" //
                + " -> NullPointerException(baz)" });
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
    }

    @Inject
    ExceptionLogger exceptionLogger;

    @Test
    public void example() {
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            exceptionLogger.failed("my operation", e);
        }
    }
}
