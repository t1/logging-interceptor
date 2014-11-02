package com.github.t1.log;

import static com.github.t1.log.LogRepeatLimit.*;
import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LogRepetitionTest extends AbstractLoggingInterceptorTests {
    public static class RepeatedLogClass {
        @Logged(repeat = ALL)
        public void repeatAll() {}

        @Logged(repeat = ONCE)
        public void repeatOnce() {}
    }

    @Inject
    RepeatedLogClass repeatLog;

    @Test
    public void shouldRepeatAll() {
        repeatLog.repeatAll();
        repeatLog.repeatAll();

        verify(log, times(2)).debug("repeat all", NO_ARGS);
    }

    @Test
    public void shouldRepeatOnce() {
        repeatLog.repeatOnce();
        repeatLog.repeatOnce();

        verify(log, times(1)).debug("repeat once", NO_ARGS);
    }
}
