package org.slf4j.impl;

import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.*;

public class MockLoggerFactory implements ILoggerFactory {
    final static MockLoggerFactory INSTANCE = new MockLoggerFactory();

    Map<String, Logger> map = new ConcurrentHashMap<>();

    @Override
    public Logger getLogger(String name) {
        Logger logger = map.get(name);
        if (logger == null) {
            logger = mock(Logger.class);
            map.put(name, logger);
        }
        return logger;
    }
}
