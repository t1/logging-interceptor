package com.github.t1.log;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static com.github.t1.log.LogRepeatLimit.*;
import static mock.logging.MockLoggerProvider.array;
import static org.mockito.Mockito.*;

class LogRepetitionTest extends AbstractLoggingInterceptorTests {
    @SuppressWarnings("unused")
    @Dependent
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

    @Test void shouldRepeatAll() {
        repeatLog.repeatAll();
        repeatLog.repeatAll();

        verify(log, times(2)).debug("repeat all", NO_ARGS);
    }

    @Test void shouldRepeatOnce() {
        repeatLog.repeatOnce();
        repeatLog.repeatOnce();

        verify(log, times(1)).debug("repeat once", NO_ARGS);
    }

    @Test void shouldRepeatOncePerSecond() throws Exception {
        repeatLog.repeatOncePerSecond(1);
        repeatLog.repeatOncePerSecond(2);
        Thread.sleep(1_020);
        repeatLog.repeatOncePerSecond(3);
        repeatLog.repeatOncePerSecond(4);

        verify(log).debug("repeat once per second {}", array(1));
        verify(log, never()).debug("repeat once per second {}", array(2));
        verify(log).debug("repeat once per second {}", array(3));
        verify(log, never()).debug("repeat once per second {}", array(4));
    }
}
