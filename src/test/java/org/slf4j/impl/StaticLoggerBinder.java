package org.slf4j.impl;

import static lombok.AccessLevel.*;
import lombok.NoArgsConstructor;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

@NoArgsConstructor(access = PRIVATE)
public class StaticLoggerBinder implements LoggerFactoryBinder {
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    public static final StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.6";

    private final ILoggerFactory loggerFactory = new MockLoggerFactory();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return MockLoggerFactory.class.getName();
    }
}
