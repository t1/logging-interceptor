package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.*;

@RunWith(Arquillian.class)
public class LoggerClassTest extends AbstractLoggingInterceptorTests {
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

        verify(logger).debug("foo", NO_ARGS);
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

        verify(logger).debug("foo", NO_ARGS);
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

        verify(logger).debug("foo", NO_ARGS);
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

        verify(logger).debug("explicit", NO_ARGS);
    }

    @Test
    public void shouldDefaultToContainerOfNestedLoggerClass() {
        Logger logger = logger(LoggerClassTest.class);

        nested.implicit();

        verify(logger).debug("implicit", NO_ARGS);
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

        verify(logger).debug("foo", NO_ARGS);
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

        verify(logger).debug("foo", NO_ARGS);
    }
}

class Dollar$Type {
    @Logged
    public void foo() {}
}
