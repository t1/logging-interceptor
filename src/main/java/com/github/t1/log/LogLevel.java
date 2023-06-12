package com.github.t1.log;

import org.slf4j.Logger;

public enum LogLevel {
    /** effectively a synonym for {@link #TRACE} */
    ALL {
        @Override public boolean isEnabled(Logger logger) {return true;}

        @Override public void log(Logger logger, String message, Object... args) {logger.trace(message, args);}

        @Override
        public void log(Logger logger, String message, Throwable throwable) {logger.trace(message, throwable);}
    },
    TRACE {
        @Override public boolean isEnabled(Logger logger) {return logger.isTraceEnabled();}

        @Override public void log(Logger logger, String message, Object... args) {logger.trace(message, args);}

        @Override
        public void log(Logger logger, String message, Throwable throwable) {logger.trace(message, throwable);}
    },
    DEBUG {
        @Override public boolean isEnabled(Logger logger) {return logger.isDebugEnabled();}

        @Override public void log(Logger logger, String message, Object... args) {
            logger.debug(message, args);
        }

        @Override
        public void log(Logger logger, String message, Throwable throwable) {logger.debug(message, throwable);}
    },
    INFO {
        @Override public boolean isEnabled(Logger logger) {return logger.isInfoEnabled();}

        @Override public void log(Logger logger, String message, Object... args) {logger.info(message, args);}

        @Override
        public void log(Logger logger, String message, Throwable throwable) {logger.info(message, throwable);}
    },
    WARN {
        @Override public boolean isEnabled(Logger logger) {return logger.isWarnEnabled();}

        @Override public void log(Logger logger, String message, Object... args) {logger.warn(message, args);}

        @Override
        public void log(Logger logger, String message, Throwable throwable) {logger.warn(message, throwable);}
    },
    ERROR {
        @Override public boolean isEnabled(Logger logger) {return logger.isErrorEnabled();}

        @Override public void log(Logger logger, String message, Object... args) {logger.error(message, args);}

        @Override
        public void log(Logger logger, String message, Throwable throwable) {logger.error(message, throwable);}
    },
    OFF {
        @Override public boolean isEnabled(Logger logger) {return false;}

        @Override public void log(Logger logger, String message, Object... args) {}

        @Override
        public void log(Logger logger, String message, Throwable throwable) {}
    },
    /** This log level is only for the default of Logged#level(). The actual default is {@link #DEBUG}. */
    _DERIVED_ {
        @Override public boolean isEnabled(Logger logger) {throw new UnsupportedOperationException();}

        @Override
        public void log(Logger logger, String message, Object... args) {throw new UnsupportedOperationException();}

        @Override
        public void log(Logger logger, String message, Throwable throwable) {
            throw new UnsupportedOperationException();
        }
    };

    public abstract boolean isEnabled(Logger logger);

    public abstract void log(Logger logger, String message, Object... args);

    public abstract void log(Logger logger, String message, Throwable throwable);
}
