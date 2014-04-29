package org.slf4j.impl;

import static org.mockito.Mockito.*;

import org.slf4j.IMarkerFactory;
import org.slf4j.spi.MarkerFactoryBinder;

public class StaticMarkerBinder implements MarkerFactoryBinder {
    public static final StaticMarkerBinder SINGLETON = new StaticMarkerBinder();
    private final IMarkerFactory markerFactory = mock(IMarkerFactory.class);

    private StaticMarkerBinder() {}

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public String getMarkerFactoryClassStr() {
        return "mock.IMarkerFactory";
    }
}
