package mock.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;

public class MockLoggerProvider implements SLF4JServiceProvider {
    /** we need to verify the Object[]-log method, not the one-arg method. */
    public static Object[] array(Object... args) {return args;}

    private final Map<String, Logger> map = new ConcurrentHashMap<>();
    private final IMarkerFactory markerFactory = new BasicMarkerFactory();
    private final MDCAdapter mdcAdapter = MockMDC.SINGLETON.adapter;

    @Override public String getRequestedApiVersion() {return "2.0.99";}

    @Override public void initialize() {}

    @Override public ILoggerFactory getLoggerFactory() {
        return name -> map.computeIfAbsent(name, n -> mock(Logger.class));
    }

    @Override public IMarkerFactory getMarkerFactory() {return markerFactory;}

    @Override public MDCAdapter getMDCAdapter() {return mdcAdapter;}
}
