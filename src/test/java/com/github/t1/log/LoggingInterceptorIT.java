package com.github.t1.log;

import static com.github.t1.log.PrintInvocation.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.*;

@RunWith(Arquillian.class)
public class LoggingInterceptorIT {
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

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class) //
                .addClass(CustomerService.class) //
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

    @Inject
    CustomerService service;

    Logger log = LoggerFactory.getLogger(CustomerService.class);

    @Test
    public void shouldLogWithMocks() {
        doPrint().when(log).info(anyString());

        log.info("info-log-message");

        verify(log).info("info-log-message");
    }

    @Test
    public void shouldLogUsingInterceptor() {
        when(log.isInfoEnabled()).thenReturn(true);

        service.hello();

        verify(log).info("hello", new Object[0]);
    }
}
