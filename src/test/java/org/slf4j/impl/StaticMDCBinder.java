package org.slf4j.impl;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.slf4j.spi.MDCAdapter;

public class StaticMDCBinder {
    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    public static String captureMdc(String key) {
        verify(mdc()).remove(key);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mdc()).put(eq(key), captor.capture());
        return captor.getValue();
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
