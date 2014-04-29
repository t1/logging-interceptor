package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.junit.Assert.*;

import org.junit.Test;

@Logged(level = WARN)
public class LoggerClassTest extends AbstractLoggedTest {
    @Test
    public void shouldUseExplicitLoggerClass() throws Exception {
        class Container {
            @Logged(logger = Integer.class)
            public void foo() {}
        }
        whenMethod(new Container(), "foo");

        interceptor.aroundInvoke(context);

        assertEquals(Integer.class, loggerType);
    }

    @Test
    public void shouldNotUnwrapUseExplicitLocalLoggerClass() throws Exception {
        class Container {
            @Logged(logger = Container.class)
            public void foo() {}
        }
        whenMethod(new Container(), "foo");

        interceptor.aroundInvoke(context);

        assertEquals(Container.class, loggerType);
    }

    private class Nested {
        @Logged
        public void implicit() {}

        @Logged(logger = Nested.class)
        public void explicit() {}
    }

    @Test
    public void shouldNotUnwrapUseExplicitNestedLoggerClass() throws Exception {
        whenMethod(new Nested(), "explicit");

        interceptor.aroundInvoke(context);

        assertEquals(Nested.class, loggerType);
    }

    @Test
    public void shouldDefaultLoggerToContainerOfNestedLoggerClass() throws Exception {
        whenMethod(new Nested(), "implicit");

        interceptor.aroundInvoke(context);

        assertEquals(LoggerClassTest.class, loggerType);
    }

    private static class Inner {
        @Logged
        public void implicit() {}

        @Logged(logger = Inner.class)
        public void explicit() {}
    }

    @Test
    public void shouldNotUnwrapUseExplicitInnerLoggerClass() throws Exception {
        whenMethod(new Inner(), "explicit");

        interceptor.aroundInvoke(context);

        assertEquals(Inner.class, loggerType);
    }

    @Test
    public void shouldDefaultToContainerOfInnerLoggerClass() throws Exception {
        whenMethod(new Inner(), "implicit");

        interceptor.aroundInvoke(context);

        assertEquals(LoggerClassTest.class, loggerType);
    }

    @Test
    public void shouldNotUnwrapUseExplicitDollarLoggerClass() throws Exception {
        class Container {
            @Logged(logger = Dollar$Type.class)
            public void foo() {}
        }
        whenMethod(new Container(), "foo");

        interceptor.aroundInvoke(context);

        assertEquals(Dollar$Type.class, loggerType);
    }

    public void someMethod() {}

    @Test
    public void shouldDefaultToLoggerClass() throws Exception {
        whenMethod(new LoggerClassTest(), "someMethod");

        interceptor.aroundInvoke(context);

        assertEquals(LoggerClassTest.class, loggerType);
    }

    @Test
    public void shouldDefaultToContainerOfLocalLoggerClass() throws Exception {
        class Container {
            @Logged
            public void foo() {}
        }
        whenMethod(new Container(), "foo");

        interceptor.aroundInvoke(context);

        assertEquals(LoggerClassTest.class, loggerType);
    }

    @Test
    public void shouldDefaultToDoubleContainerLoggerClass() throws Exception {
        class Outer {
            class Inner {
                @Logged
                public void foo() {}
            }
        }
        whenMethod(new Outer().new Inner(), "foo");

        interceptor.aroundInvoke(context);

        assertEquals(LoggerClassTest.class, loggerType);
    }

    @Test
    public void shouldDefaultToAnonymousLoggerClass() throws Exception {
        whenMethod(new Runnable() {
            @Override
            @Logged
            public void run() {}
        }, "run");

        interceptor.aroundInvoke(context);

        assertEquals(LoggerClassTest.class, loggerType);
    }

    @Test
    public void shouldDefaultToDollarLoggerClass() throws Exception {
        whenMethod(new Dollar$Type(), "foo");

        interceptor.aroundInvoke(context);

        assertEquals(Dollar$Type.class, loggerType);
    }
}

class Dollar$Type {
    @Logged
    public void foo() {}
}
