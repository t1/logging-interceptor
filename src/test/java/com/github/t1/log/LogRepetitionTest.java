package com.github.t1.log;

import static com.github.t1.log.LogRepeatLimit.*;
import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LogRepetitionTest extends AbstractLoggingInterceptorTests {
    @SuppressWarnings("unused")
    public static class RepeatedLogClass {
        @Logged(repeat = ALL)
        public void repeatAll() {}

        @Logged(repeat = ONCE)
        public void repeatOnce() {}

        @Logged(repeat = ONCE_PER_SECOND)
        public void repeatOncePerSecond(int n) {}
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

    @Test
    public void shouldRepeatOncePerSecond() throws Exception {
        repeatLog.repeatOncePerSecond(1);
        repeatLog.repeatOncePerSecond(2);
        Thread.sleep(1_020);
        repeatLog.repeatOncePerSecond(3);
        repeatLog.repeatOncePerSecond(4);

        verify(log).debug("repeat once per second {}", new Object[] { 1 });
        verify(log, never()).debug("repeat once per second {}", new Object[] { 2 });
        verify(log).debug("repeat once per second {}", new Object[] { 3 });
        verify(log, never()).debug("repeat once per second {}", new Object[] { 4 });
    }
}
