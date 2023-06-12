package com.github.t1.log;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static com.github.t1.log.LogLevel.*;
import static org.mockito.Mockito.*;

@Logged(level = WARN)
class LogLevelTest extends AbstractLoggingInterceptorTests {
    // ----------------------------------------------------------------------------------

    @Inject
    SimpleLoggedClass simpleLoggedClass;

    @Test void shouldNotLogWhenLevelIsNotEnabled() {
        givenLogLevel(INFO);

        simpleLoggedClass.foo();

        verify(log, atLeast(0)).isDebugEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class OffClass {
        @Logged(level = OFF)
        public void foo() {}
    }

    @Inject
    OffClass offClass;

    @Test void shouldNotLogWhenOff() {
        givenLogLevel(TRACE);

        offClass.foo();

        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class TraceClass {
        @Logged(level = TRACE)
        public void foo() {}
    }

    @Inject
    TraceClass traceClass;

    @Test void shouldLogAtTraceLevelIfTraceIsEnabled() {
        givenLogLevel(TRACE);

        traceClass.foo();

        verify(log, atLeast(0)).isTraceEnabled();
        verify(log).trace("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    @Test void shouldNotLogAtTraceLevelIfOnlyDebugIsEnabled() {
        givenLogLevel(DEBUG);

        traceClass.foo();

        verify(log, atLeast(0)).isTraceEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class DebugClass {
        @Logged(level = DEBUG)
        public void foo() {}
    }

    @Inject
    DebugClass debugClass;

    @Test void shouldLogAtDebugLevelIfDebugIsEnabled() {
        givenLogLevel(DEBUG);

        debugClass.foo();

        verify(log, atLeast(0)).isDebugEnabled();
        verify(log).debug("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    @Test void shouldNotLogAtDebugLevelIfOnlyInfoIsEnabled() {
        givenLogLevel(INFO);

        debugClass.foo();

        verify(log, atLeast(0)).isDebugEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class InfoClass {
        @Logged(level = INFO)
        public void foo() {}
    }

    @Inject
    InfoClass infoClass;

    @Test void shouldLogAtInfoLevelIfInfoIsEnabled() {
        givenLogLevel(INFO);

        infoClass.foo();

        verify(log, atLeast(0)).isInfoEnabled();
        verify(log).info("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    @Test void shouldNotLogAtInfoLevelIfOnlyWarnIsEnabled() {
        givenLogLevel(WARN);

        infoClass.foo();

        verify(log, atLeast(0)).isInfoEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class WarnClass {
        @Logged(level = WARN)
        public void foo() {}
    }

    @Inject
    WarnClass warnClass;

    @Test void shouldLogAtWarnLevelIfWarnIsEnabled() {
        givenLogLevel(WARN);

        warnClass.foo();

        verify(log, atLeast(0)).isWarnEnabled();
        verify(log).warn("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    @Test void shouldNotLogAtWarnLevelIfOnlyErrorIsEnabled() {
        givenLogLevel(ERROR);

        warnClass.foo();

        verify(log, atLeast(0)).isWarnEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class ErrorClass {
        @Logged(level = ERROR)
        public void foo() {}
    }

    @Inject
    ErrorClass errorClass;

    @Test void shouldLogAtErrorLevelIfErrorIsEnabled() {
        givenLogLevel(ERROR);

        errorClass.foo();

        verify(log, atLeast(0)).isErrorEnabled();
        verify(log).error("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    @Test void shouldNotLogAtErrorLevelIfOff() {
        givenLogLevel(OFF);

        errorClass.foo();

        verify(log, atLeast(0)).isErrorEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    @Logged(level = INFO)
    @Dependent
    public static class InheritLogLevelClass {
        @Logged
        public void foo() {}
    }

    @Inject
    InheritLogLevelClass inheritLogLevelClass;

    @Test void shouldInheritLogLevelFromClass() {
        givenLogLevel(DEBUG);

        inheritLogLevelClass.foo();

        verify(log).info("foo", NO_ARGS);
    }

    // ----------------------------------------------------------------------------------

    @Logged
    @Dependent
    public static class InheritLogLevelFromEnclosingClassClass {
        @Logged
        public void foo() {}
    }

    @Inject
    InheritLogLevelFromEnclosingClassClass inheritLogLevelFromEnclosingClassClass;

    @Test void shouldInheritLogLevelFromEnclosingClass() {
        givenLogLevel(DEBUG);

        inheritLogLevelFromEnclosingClassClass.foo();

        verify(log).warn("foo", NO_ARGS);
    }
}
