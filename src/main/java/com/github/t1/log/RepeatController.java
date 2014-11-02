package com.github.t1.log;

import static java.util.concurrent.TimeUnit.*;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;


public abstract class RepeatController {
    private static class AllRepeatController extends RepeatController {
        @Override
        public boolean shouldRepeat() {
            return true;
        }
    }

    @RequiredArgsConstructor
    private static class TimedRepeatController extends RepeatController {
        private final TimeUnit timeUnit;
        private long nextRepeat;

        @Override
        public boolean shouldRepeat() {
            if (System.currentTimeMillis() >= nextRepeat) {
                nextRepeat = System.currentTimeMillis() + timeUnit.toMillis(1);
                return true;
            } else {
                return false;
            }
        }
    }

    private static class OnlyOnceRepeatController extends RepeatController {
        private boolean first = true;

        @Override
        public boolean shouldRepeat() {
            if (first) {
                first = false;
                return true;
            } else {
                return false;
            }
        }
    }

    public static RepeatController createFor(LogRepeatLimit repeat) {
        switch (repeat) {
            case ALL:
                return new AllRepeatController();
            case ONCE:
                return new OnlyOnceRepeatController();
            case ONCE_PER_SECOND:
                return new TimedRepeatController(SECONDS);
            case ONCE_PER_MINUTE:
                return new TimedRepeatController(MINUTES);
            case ONCE_PER_HOUR:
                return new TimedRepeatController(HOURS);
            case ONCE_PER_DAY:
                return new TimedRepeatController(DAYS);
        }
        throw new IllegalArgumentException("unsupported log repeat limit: " + repeat);
    }

    public abstract boolean shouldRepeat();
}
