package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.mockito.Mockito.*;
import static org.slf4j.impl.StaticMDCBinder.*;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.slf4j.*;

public abstract class AbstractLogTests {
    protected static final Object[] NO_ARGS = new Object[0];

    private static final String BEANS_XML = //
            "<beans " //
                    + "xmlns=\"http://java.sun.com/xml/ns/javaee\" " //
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " //
                    + "xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee " //
                    + "http://java.sun.com/xml/ns/javaee/beans_1_0.xsd\">\n" //
                    + "    <interceptors>\n" //
                    + "        <class>com.github.t1.log.LoggingInterceptor</class>\n" //
                    + "    </interceptors>\n" //
                    + "</beans>" //
    ;

    protected static JavaArchive loggingInterceptorDeployment() {
        return ShrinkWrap.create(JavaArchive.class) //
                .addClasses(//
                        Converter.class, //
                        Converters.class, //
                        ConverterType.class, //
                        DontLog.class, //
                        Indent.class, //
                        LogContext.class, //
                        LogContextVariable.class, //
                        Logged.class, //
                        LoggingInterceptor.class, //
                        LogLevel.class, //
                        Parameter.class, //
                        RestorableMdc.class, //
                        VersionLogContextVariableProducer.class //
                ) //
                .addAsManifestResource(new StringAsset(BEANS_XML), "beans.xml") //
        ;
    }

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Before
    public void resetMdc() {
        reset(mdc());
    }

    @Before
    public void givenLogLevelDebug() {
        givenLogLevel(DEBUG);
    }

    protected void givenLogLevel(LogLevel level) {
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
