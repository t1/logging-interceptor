package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.*;

@Logged(level = WARN)
@RunWith(Arquillian.class)
public class LoggerClassTest extends AbstractLoggingInterceptorTests {
    @Deployment
    public static JavaArchive createDeployment() {
        return loggingInterceptorDeployment().addClass(ImplicitLoggerClass.class);
    }

    private Logger logger(Class<?> type) {
        Logger logger = LoggerFactory.getLogger(type);
        givenLogLevel(DEBUG, logger);
        return logger;
    }

    // ----------------------------------------------------------------------------------

    @Inject
    ImplicitLoggerClass implicitLoggerClass;

    @Test
    public void shouldUseImplicitLoggerClass() {
        Logger logger = logger(ImplicitLoggerClass.class);

        implicitLoggerClass.foo();

        verify(logger).debug("foo", new Object[0]);
    }

    // ----------------------------------------------------------------------------------

    public static class ExplicitLoggerClass {
        @Logged(logger = Integer.class)
        public void foo() {}
    }

    @Inject
    ExplicitLoggerClass explicitLoggerClass;

    @Test
    public void shouldUseExplicitLoggerClass() {
        Logger logger = logger(Integer.class);

        explicitLoggerClass.foo();

        verify(logger).debug("foo", new Object[0]);
    }

    // ----------------------------------------------------------------------------------

    public static class ExplicitSelfLoggerClass {
        @Logged(logger = ExplicitSelfLoggerClass.class)
        public void foo() {}
    }

    @Inject
    ExplicitSelfLoggerClass explicitSelfLogger;

    @Test
    public void shouldNotUnwrapUseExplicitLocalLoggerClass() {
        Logger logger = logger(ExplicitSelfLoggerClass.class);

        explicitSelfLogger.foo();

        verify(logger).debug("foo", new Object[0]);
    }

    // ----------------------------------------------------------------------------------

    public static class Nested {
        @Logged
        public void implicit() {}

        @Logged(logger = Nested.class)
        public void explicit() {}
    }

    @Inject
    Nested nested;

    @Test
    public void shouldNotUnwrapUseExplicitNestedLoggerClass() {
        Logger logger = logger(Nested.class);

        nested.explicit();

        verify(logger).debug("explicit", new Object[0]);
    }

    @Test
    public void shouldDefaultToContainerOfNestedLoggerClass() {
        Logger logger = logger(LoggerClassTest.class);

        nested.implicit();

        verify(logger).debug("implicit", new Object[0]);
    }

    // ----------------------------------------------------------------------------------

    public static class Outer {
        public static class Inner {
            @Logged
            public void foo() {}
        }
    }

    @Inject
    Outer.Inner inner;

    @Test
    public void shouldDefaultToDoubleContainerLoggerClass() {
        Logger logger = logger(LoggerClassTest.class);

        inner.foo();

        verify(logger).debug("foo", new Object[0]);
    }

    // ----------------------------------------------------------------------------------

    public static class DollarLoggerClass {
        @Logged(logger = Dollar$Type.class)
        public void foo() {}
    }

    @Inject
    DollarLoggerClass dollarLoggerClass;

    @Test
    public void shouldNotUnwrapUseExplicitDollarLoggerClass() {
        Logger logger = logger(Dollar$Type.class);

        dollarLoggerClass.foo();

        verify(logger).debug("foo", new Object[0]);
    }
}

class Dollar$Type {
    @Logged
    public void foo() {}
}
