package com.github.t1.log;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.t1.log.LogLevel.DEBUG;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class LogLoggerTest extends AbstractLoggingInterceptorTests {
    private Logger logger(Class<?> type) {
        Logger logger = LoggerFactory.getLogger(type);
        givenLogLevel(DEBUG, logger);
        return logger;
    }

    // ----------------------------------------------------------------------------------

    @Inject
    ImplicitLoggerClass implicitLoggerClass;

    @Test void shouldUseImplicitLoggerClass() {
        Logger logger = logger(ImplicitLoggerClass.class);

        implicitLoggerClass.foo();

        verify(logger).debug("foo", NO_ARGS);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class ExplicitLoggerClass {
        @Logged(logger = Integer.class)
        public void foo() {}
    }

    @Inject
    ExplicitLoggerClass explicitLoggerClass;

    @Test void shouldUseExplicitLoggerClass() {
        Logger logger = logger(Integer.class);

        explicitLoggerClass.foo();

        verify(logger).debug("foo", NO_ARGS);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class ExplicitSelfLoggerClass {
        @Logged(logger = ExplicitSelfLoggerClass.class)
        public void foo() {}
    }

    @Inject
    ExplicitSelfLoggerClass explicitSelfLogger;

    @Test void shouldNotUnwrapUseExplicitLocalLoggerClass() {
        Logger logger = logger(ExplicitSelfLoggerClass.class);

        explicitSelfLogger.foo();

        verify(logger).debug("foo", NO_ARGS);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class Nested {
        @Logged
        public void implicit() {}

        @Logged(logger = Nested.class)
        public void explicit() {}
    }

    @Inject
    Nested nested;

    @Test void shouldNotUnwrapUseExplicitNestedLoggerClass() {
        Logger logger = logger(Nested.class);

        nested.explicit();

        verify(logger).debug("explicit", NO_ARGS);
    }

    @Test void shouldDefaultToContainerOfNestedLoggerClass() {
        Logger logger = logger(LogLoggerTest.class);

        nested.implicit();

        verify(logger).debug("implicit", NO_ARGS);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class Outer {
        @Dependent
        public static class Inner {
            @Logged
            public void foo() {}
        }
    }

    @Inject
    Outer.Inner inner;

    @Test void shouldDefaultToDoubleContainerLoggerClass() {
        Logger logger = logger(LogLoggerTest.class);

        inner.foo();

        verify(logger).debug("foo", NO_ARGS);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class DollarLoggerClass {
        @Logged(logger = Dollar$Type.class)
        public void foo() {}
    }

    @Inject
    DollarLoggerClass dollarLoggerClass;

    @Test void shouldNotUnwrapUseExplicitDollarLoggerClass() {
        Logger logger = logger(Dollar$Type.class);

        dollarLoggerClass.foo();

        verify(logger).debug("foo", NO_ARGS);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class StringLoggerNameClass {
        @Logged(loggerString = "some.logger")
        public void foo() {}
    }

    @Inject
    StringLoggerNameClass stringLoggerNameClass;

    @Test void shouldUseStringLoggerName() {
        Logger logger = LoggerFactory.getLogger("some.logger");
        givenLogLevel(DEBUG, logger);

        stringLoggerNameClass.foo();

        verify(logger).debug("foo", NO_ARGS);
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class SuperNonLoggedClass {
        public void foo() {}
    }

    @Logged
    @Dependent
    public static class SubLoggedClass extends SuperNonLoggedClass {
        public void bar() {}
    }

    @Inject
    SubLoggedClass subLoggedClass;

    @Test void shouldLogInheritedMethod() {
        Logger logger = logger(LogLoggerTest.class);
        givenLogLevel(DEBUG, logger);

        subLoggedClass.foo();

        verify(logger, never()).debug("foo", NO_ARGS);

        subLoggedClass.bar();

        verify(logger).debug("bar", NO_ARGS);
    }
}

class Dollar$Type {
    @Logged
    public void foo() {}
}
