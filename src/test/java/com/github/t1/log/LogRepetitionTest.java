package com.github.t1.log;

import jakarta.inject.Inject;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.t1.log.LogRepeatLimit.ALL;
import static com.github.t1.log.LogRepeatLimit.ONCE;
import static com.github.t1.log.LogRepeatLimit.ONCE_PER_SECOND;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

        verify(log).debug("repeat once per second {}", 1);
        verify(log, never()).debug("repeat once per second {}", 2);
        verify(log).debug("repeat once per second {}", 3);
        verify(log, never()).debug("repeat once per second {}", 4);
    }
}
