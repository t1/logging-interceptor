package org.slf4j.impl;

import static org.mockito.Mockito.*;

import java.util.*;

import org.mockito.Mockito;
import org.slf4j.spi.MDCAdapter;

public class StaticMDCBinder {
    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    public static void verifyMdc(String key, String value) {
        verify(mdc()).put(key, value);
        verify(mdc()).remove(key);
    }

    private static Map<String, String> mdc = new HashMap<>();

    public static void givenMdc(String key, String value) {
        mdc.put(key, value);
        when(mdc().getCopyOfContextMap()).thenReturn(mdc);
        when(mdc().get(key)).thenReturn(value);
    }

    public static void reset() {
        Mockito.reset(mdc());
        mdc.clear();
    }

    public static MDCAdapter mdc() {
        return SINGLETON.getMDCA();
    }

    private final MDCAdapter adapter = mock(MDCAdapter.class);

    private StaticMDCBinder() {}

    public MDCAdapter getMDCA() {
        return adapter;
    }

    public String getMDCAdapterClassStr() {
        return "mock.MDCAdapter";
    }
}
