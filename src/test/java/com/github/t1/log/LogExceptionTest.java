package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LogExceptionTest extends AbstractLoggingInterceptorTests {
    // ----------------------------------------------------------------------------------

    public static class DefaultThrowLevelClass {
        @Logged
        public String foo() {
            throw new RuntimeException("bar");
        }
    }

    @Inject
    DefaultThrowLevelClass defaultThrowLevel;

    @Test
    public void shouldLogThrownExeptionByDefaultAtLevel() {
        try {
            defaultThrowLevel.foo();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).error(eq("failed"), any(RuntimeException.class));
    }

    // ----------------------------------------------------------------------------------

    public static class ThrowAtExplicitErrorClass {
        @Logged(throwLevel = ERROR)
        public String foo() {
            throw new RuntimeException("bar");
        }
    }

    @Inject
    ThrowAtExplicitErrorClass throwAtErrorClass;

    @Test
    public void shouldLogThrownExeptionAtErrorLevel() {
        try {
            throwAtErrorClass.foo();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).error(eq("failed"), any(RuntimeException.class));
    }

    // ----------------------------------------------------------------------------------

    public static class ThrowAtWarnClass {
        @Logged(throwLevel = WARN)
        public String foo() {
            throw new RuntimeException("bar");
        }
    }

    @Inject
    ThrowAtWarnClass throwAtWarnClass;

    @Test
    public void shouldLogThrownExeptionAtWarnLevel() {
        try {
            throwAtWarnClass.foo();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).warn(eq("failed"), any(RuntimeException.class));
    }

    // ----------------------------------------------------------------------------------

    public static class ThrowAtInfoClass {
        @Logged(throwLevel = INFO)
        public String foo() {
            throw new RuntimeException("bar");
        }
    }

    @Inject
    ThrowAtInfoClass throwAtInfoClass;

    @Test
    public void shouldLogThrownExeptionAtInfoLevel() {
        try {
            throwAtInfoClass.foo();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).info(eq("failed"), any(RuntimeException.class));
    }

    // ----------------------------------------------------------------------------------

    public static class ThrowAtDebugClass {
        @Logged(throwLevel = DEBUG)
        public String foo() {
            throw new RuntimeException("bar");
        }
    }

    @Inject
    ThrowAtDebugClass throwAtDebugClass;

    @Test
    public void shouldLogThrownExeptionAtDebugLevel() {
        try {
            throwAtDebugClass.foo();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).debug(eq("failed"), any(RuntimeException.class));
    }

    // ----------------------------------------------------------------------------------

    public static class ThrowAtTraceClass {
        @Logged(throwLevel = TRACE)
        public String foo() {
            throw new RuntimeException("bar");
        }
    }

    @Inject
    ThrowAtTraceClass throwAtTraceClass;

    @Test
    public void shouldLogThrownExeptionAtTraceLevel() {
        try {
            throwAtTraceClass.foo();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).trace(eq("failed"), any(RuntimeException.class));
    }

    // ----------------------------------------------------------------------------------

    public static class ThrowAtOffClass {
        @Logged(throwLevel = OFF)
        public String foo() {
            throw new RuntimeException("bar");
        }
    }

    @Inject
    ThrowAtOffClass throwAtOffClass;

    @Test
    public void shouldLogThrownExeptionAtOffLevel() {
        try {
            throwAtOffClass.foo();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).isDebugEnabled();
        verify(log).debug("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    @Logged(throwLevel = DEBUG)
    public static class ThrowAtDerivedLevelClass {
        @Logged("foo-message")
        public String foo() {
            throw new RuntimeException("bar");
        }
    }

    @Inject
    ThrowAtDerivedLevelClass throwAtDerivedLevelClass;

    @Test
    public void shouldLogThrownExeptionAtDerivedLevel() {
        try {
            throwAtDerivedLevelClass.foo();
            fail("expected RuntimeException");
        } catch (RuntimeException e) {}

        verify(log).debug("foo-message", NO_ARGS);
        verify(log).debug(eq("failed"), any(RuntimeException.class));
    }
}
