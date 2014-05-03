package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Logged(level = WARN)
public class LogLevelTest extends AbstractLoggingInterceptorTests {
    // ----------------------------------------------------------------------------------

    public static class DefaultClass {
        @Logged
        public void foo() {}
    }

    @Inject
    DefaultClass defaultClass;

    @Test
    public void shouldNotLogWhenLevelIsNotEnabled() {
        givenLogLevel(INFO);

        defaultClass.foo();

        verify(log, atLeast(0)).isDebugEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    public static class OffClass {
        @Logged(level = OFF)
        public void foo() {}
    }

    @Inject
    OffClass offClass;

    @Test
    public void shouldNotLogWhenOff() {
        givenLogLevel(TRACE);

        offClass.foo();

        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    public static class TraceClass {
        @Logged(level = TRACE)
        public void foo() {}
    }

    @Inject
    TraceClass traceClass;

    @Test
    public void shouldLogAtTraceLevelIfTraceIsEnabled() {
        givenLogLevel(TRACE);

        traceClass.foo();

        verify(log, atLeast(0)).isTraceEnabled();
        verify(log).trace("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    @Test
    public void shouldNotLogAtTraceLevelIfOnlyDebugIsEnabled() {
        givenLogLevel(DEBUG);

        traceClass.foo();

        verify(log, atLeast(0)).isTraceEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    public static class DebugClass {
        @Logged(level = DEBUG)
        public void foo() {}
    }

    @Inject
    DebugClass debugClass;

    @Test
    public void shouldLogAtDebugLevelIfDebugIsEnabled() {
        givenLogLevel(DEBUG);

        debugClass.foo();

        verify(log, atLeast(0)).isDebugEnabled();
        verify(log).debug("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    @Test
    public void shouldNotLogAtDebugLevelIfOnlyInfoIsEnabled() {
        givenLogLevel(INFO);

        debugClass.foo();

        verify(log, atLeast(0)).isDebugEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    public static class InfoClass {
        @Logged(level = INFO)
        public void foo() {}
    }

    @Inject
    InfoClass infoClass;

    @Test
    public void shouldLogAtInfoLevelIfInfoIsEnabled() {
        givenLogLevel(INFO);

        infoClass.foo();

        verify(log, atLeast(0)).isInfoEnabled();
        verify(log).info("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    @Test
    public void shouldNotLogAtInfoLevelIfOnlyWarnIsEnabled() {
        givenLogLevel(WARN);

        infoClass.foo();

        verify(log, atLeast(0)).isInfoEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    public static class WarnClass {
        @Logged(level = WARN)
        public void foo() {}
    }

    @Inject
    WarnClass warnClass;

    @Test
    public void shouldLogAtWarnLevelIfWarnIsEnabled() {
        givenLogLevel(WARN);

        warnClass.foo();

        verify(log, atLeast(0)).isWarnEnabled();
        verify(log).warn("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    @Test
    public void shouldNotLogAtWarnLevelIfOnlyErrorIsEnabled() {
        givenLogLevel(ERROR);

        warnClass.foo();

        verify(log, atLeast(0)).isWarnEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    public static class ErrorClass {
        @Logged(level = ERROR)
        public void foo() {}
    }

    @Inject
    ErrorClass errorClass;

    @Test
    public void shouldLogAtErrorLevelIfErrorIsEnabled() {
        givenLogLevel(ERROR);

        errorClass.foo();

        verify(log, atLeast(0)).isErrorEnabled();
        verify(log).error("foo", NO_ARGS);
        verifyNoMoreInteractions(log);
    }

    @Test
    public void shouldNotLogAtErrorLevelIfOff() {
        givenLogLevel(OFF);

        errorClass.foo();

        verify(log, atLeast(0)).isErrorEnabled();
        verifyNoMoreInteractions(log);
    }

    // ----------------------------------------------------------------------------------

    @Logged(level = INFO)
    public static class InheritLogLevelClass {
        @Logged
        public void foo() {}
    }

    @Inject
    InheritLogLevelClass inheritLogLevelClass;

    @Test
    public void shouldInheritLogLevelFromClass() {
        givenLogLevel(DEBUG);

        inheritLogLevelClass.foo();

        verify(log).info("foo", NO_ARGS);
    }

    // ----------------------------------------------------------------------------------

    @Logged
    public static class InheritLogLevelFromEnclosingClassClass {
        @Logged
        public void foo() {}
    }

    @Inject
    InheritLogLevelFromEnclosingClassClass inheritLogLevelFromEnclosingClassClass;

    @Test
    public void shouldInheritLogLevelFromEnclosingClass() {
        givenLogLevel(DEBUG);

        inheritLogLevelFromEnclosingClassClass.foo();

        verify(log).warn("foo", NO_ARGS);
    }
}
