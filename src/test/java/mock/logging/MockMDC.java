package mock.logging;

import org.mockito.Mockito;
import org.slf4j.spi.MDCAdapter;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class MockMDC {
    public static final MockMDC SINGLETON = new MockMDC();

    public static void verifyMdc(String key, String value) {
        verify(SINGLETON.adapter).put(key, value);
        verify(SINGLETON.adapter).remove(key);
    }

    private static Map<String, String> mdc = new HashMap<>();

    public static void givenMdc(String key, String value) {
        mdc.put(key, value);
        when(SINGLETON.adapter.getCopyOfContextMap()).thenReturn(mdc);
        when(SINGLETON.adapter.get(key)).thenReturn(value);
    }

    public static void reset() {
        Mockito.reset(SINGLETON.adapter);
        mdc.clear();
    }

    public static MDCAdapter mdc() {return SINGLETON.adapter;}

    final MDCAdapter adapter = mock(MDCAdapter.class);
}
