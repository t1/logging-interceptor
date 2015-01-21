package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.mockito.Mockito.*;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.*;
import org.slf4j.*;
import org.slf4j.impl.StaticMDCBinder;

public abstract class AbstractLoggingInterceptorTests {
    protected static final Object[] NO_ARGS = new Object[0];

    private static final String BEANS_XML = "" //
            + "<beans>\n" //
            + "    <interceptors>\n" //
            + "        <class>com.github.t1.log.LoggingInterceptor</class>\n" //
            + "    </interceptors>\n" //
            + "</beans>" //
    ;

    @Deployment
    public static JavaArchive loggingInterceptorDeployment() {
        return ShrinkWrap.create(JavaArchive.class) //
                .addPackage(LoggingInterceptor.class.getPackage()) //
                .addAsManifestResource(new StringAsset(BEANS_XML), "beans.xml") //
        ;
    }

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Before
    @After
    public void resetMdc() {
        StaticMDCBinder.reset();
    }

    @Before
    public void initLogLevelDebug() {
        givenLogLevel(DEBUG);
    }

    @After
    public void clearLogPointCache() {
        LoggingInterceptor.CACHE.clear();
    }

    protected void givenLogLevel(LogLevel level) {
        givenLogLevel(level, log);
    }

    protected void givenLogLevel(LogLevel level, Logger log) {
        reset(log);
        switch (level) {
            case _DERIVED_:
                throw new IllegalArgumentException("unsupported log level");
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
