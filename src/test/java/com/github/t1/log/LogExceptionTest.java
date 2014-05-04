package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.Invocation;

@RunWith(Arquillian.class)
public class LogExceptionTest extends AbstractLoggingInterceptorTests {
    @After
    public void printLogs() {
        for (Invocation invocation : new MockUtil().getMockHandler(log).getInvocationContainer().getInvocations()) {
            System.out.println(invocation);
        }
    }

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

        throwableLogger.notThrowing(exception, "foo");

        verify(log).debug("not throwing {} {}", new Object[] { exception, "foo" });
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
