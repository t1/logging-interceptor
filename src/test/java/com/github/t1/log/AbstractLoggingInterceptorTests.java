package com.github.t1.log;

import mock.logging.MockMDC;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddEnabledInterceptors;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.t1.log.LogLevel.DEBUG;
import static org.mockito.Mockito.*;

@EnableAutoWeld
@AddEnabledInterceptors(LoggingInterceptor.class)
@AddPackages(LoggingInterceptor.class)
@AddBeanClasses(Converters.class)
abstract class AbstractLoggingInterceptorTests {
    static final Object[] NO_ARGS = new Object[0];

    String captureMessage() {
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> objectCaptor = ArgumentCaptor.forClass(Object[].class);

        verify(log, atLeastOnce()).debug(messageCaptor.capture(), objectCaptor.capture());

        return messageCaptor.getValue();
    }

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @BeforeEach
    @AfterEach
    void resetMdc() {
        MockMDC.reset();
    }

    @BeforeEach
    void initLogLevelDebug() {
        givenLogLevel(DEBUG);
    }

    @AfterEach
    void clearLogPointCache() {
        LoggingInterceptor.CACHE.clear();
    }

    void givenLogLevel(LogLevel level) {
        givenLogLevel(level, log);
    }

    void givenLogLevel(LogLevel level, Logger log) {
        reset(log);
        switch (level) {
            case _DERIVED_:
                throw new IllegalArgumentException("unsupported log level");
            case ALL:
            case TRACE:
                when(log.isTraceEnabled()).thenReturn(true);
            case DEBUG:
                when(log.isDebugEnabled()).thenReturn(true);
            case INFO:
                when(log.isInfoEnabled()).thenReturn(true);
            case WARN:
                when(log.isWarnEnabled()).thenReturn(true);
            case ERROR:
                when(log.isErrorEnabled()).thenReturn(true);
            case OFF:
                break;
        }
    }
}
