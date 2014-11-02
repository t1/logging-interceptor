package com.github.t1.log;


public abstract class RepeatController {
    private static class AllRepeatController extends RepeatController {
        @Override
        public boolean shouldRepeat() {
            return true;
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
        }
        throw new IllegalArgumentException("unsupported log repeat limit: " + repeat);
    }

    public abstract boolean shouldRepeat();
}
