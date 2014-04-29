package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.slf4j.impl.StaticMDCBinder.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.junit.*;
import org.mockito.InOrder;

@Logged(level = WARN)
public class LoggedTest extends AbstractLoggedTest {
    @Test
    public void shouldLogALongMethodNameWithSpaces() throws Exception {
        class Container {
            @Logged
            public void methodWithALongName() {}
        }
        whenMethod(new Container(), "methodWithALongName");

        interceptor.aroundInvoke(context);

        verify(logger).debug("method with a long name", new Object[0]);
    }

    @Before
    public void before() {
        reset(mdc());
    }

    @Test
    public void shouldLogAnAnnotatedMethod() throws Exception {
        class Container {
            @Logged("bar")
            public void foo() {}
        }
        whenMethod(new Container(), "foo");

        interceptor.aroundInvoke(context);

        verify(logger).debug("bar", new Object[0]);
    }

    @Test
    public void shouldLogReturnValue() throws Exception {
        class Container {
            @Logged
            public boolean methodWithReturnType() {
                return true;
            }
        }
        whenMethod(new Container(), "methodWithReturnType");
        when(context.proceed()).thenReturn(true);

        interceptor.aroundInvoke(context);

        verify(logger).debug("return {}", new Object[] { true });
    }

    @Test
    public void shouldLogException() throws Exception {
        class Container {
            @Logged
            public boolean methodThatMightFail() {
                return true;
            }
        }
        whenMethod(new Container(), "methodThatMightFail");
        RuntimeException exception = new RuntimeException("foo");
        when(context.proceed()).thenThrow(exception);

        try {
            interceptor.aroundInvoke(context);
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            // that's okay
        }
        verify(logger).debug("failed", exception);
    }

    @Test
    public void shouldNotLogVoidReturnValue() throws Exception {
        class Container {
            @Logged
            public void voidReturnType() {}
        }
        whenMethod(new Container(), "voidReturnType");

        interceptor.aroundInvoke(context);

        verify(logger).debug("void return type", new Object[0]);
        verify(logger, atLeast(0)).isDebugEnabled();
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void shouldLogIntParameter() throws Exception {
        class Container {
            @Logged
            public void methodWithIntArgument(@SuppressWarnings("unused") int i) {}
        }
        Method method = Container.class.getMethod("methodWithIntArgument", int.class);
        whenMethod(method, new Container(), 3);

        interceptor.aroundInvoke(context);

        verify(logger).debug("method with int argument {}", new Object[] { 3 });
    }

    @Test
    public void shouldLogIntegerParameter() throws Exception {
        class Container {
            @Logged
            public void methodWithIntegerArgument(@SuppressWarnings("unused") Integer i) {}
        }
        whenMethod(new Container(), "methodWithIntegerArgument", 3);

        interceptor.aroundInvoke(context);

        verify(logger).debug("method with integer argument {}", new Object[] { 3 });
    }

    @Test
    public void shouldLogTwoParameters() throws Exception {
        class Container {
            @Logged
            @SuppressWarnings("unused")
            public void methodWithTwoParameters(String one, String two) {}
        }
        whenMethod(new Container(), "methodWithTwoParameters", "foo", "bar");

        interceptor.aroundInvoke(context);

        verify(logger).debug("method with two parameters {} {}", new Object[] { "foo", "bar" });
    }

    @Test
    public void shouldNotLogParametersAnnotatedAsDontLog() throws Exception {
        class Container {
            @Logged
            @SuppressWarnings("unused")
            public void methodWithDontLogParameter(@DontLog String one, String two) {}
        }
        whenMethod(new Container(), "methodWithDontLogParameter", "foo", "bar");

        interceptor.aroundInvoke(context);

        verify(logger).debug("method with dont log parameter {}", new Object[] { "bar" });
    }

    @Test
    public void shouldNotLogWhenOff() throws Exception {
        class Container {
            @Logged(level = OFF)
            public void atOff() {}
        }
        whenMethod(new Container(), "atOff");

        interceptor.aroundInvoke(context);

        verifyNoMoreInteractions(logger);
    }

    @Test
    public void shouldNotLogWhenDebugIsNotEnabled() throws Exception {
        class Container {
            @Logged(level = DEBUG)
            public void atDebug() {}
        }
        when(logger.isDebugEnabled()).thenReturn(false);
        whenMethod(new Container(), "atDebug");

        interceptor.aroundInvoke(context);

        verify(logger, atLeast(0)).isDebugEnabled();
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void shouldLogInfoWhenInfoIsEnabled() throws Exception {
        class Container {
            @Logged(level = INFO)
            public void atInfo() {}
        }
        when(logger.isInfoEnabled()).thenReturn(true);
        whenMethod(new Container(), "atInfo");

        interceptor.aroundInvoke(context);

        verify(logger).info("at info", new Object[0]);
    }

    @Test
    public void shouldInheritLogLevelFromContainer() throws Exception {
        @Logged(level = INFO)
        class Container {
            @Logged
            public void method() {}
        }
        when(logger.isInfoEnabled()).thenReturn(true);
        whenMethod(new Container(), "method");

        interceptor.aroundInvoke(context);

        verify(logger).info("method", new Object[0]);
    }

    @Test
    public void shouldInheritLogLevelFromEnclosingClass() throws Exception {
        @Logged
        class Container {
            @Logged
            public void method() {}
        }
        when(logger.isWarnEnabled()).thenReturn(true);
        whenMethod(new Container(), "method");

        interceptor.aroundInvoke(context);

        verify(logger).warn("method", new Object[0]);
    }

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

        assertEquals(LoggedTest.class, loggerType);
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

        assertEquals(LoggedTest.class, loggerType);
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
        whenMethod(new LoggedTest(), "someMethod");

        interceptor.aroundInvoke(context);

        assertEquals(LoggedTest.class, loggerType);
    }

    @Test
    public void shouldDefaultToContainerOfLocalLoggerClass() throws Exception {
        class Container {
            @Logged
            public void foo() {}
        }
        whenMethod(new Container(), "foo");

        interceptor.aroundInvoke(context);

        assertEquals(LoggedTest.class, loggerType);
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

        assertEquals(LoggedTest.class, loggerType);
    }

    @Test
    public void shouldDefaultToAnonymousLoggerClass() throws Exception {
        whenMethod(new Runnable() {
            @Override
            @Logged
            public void run() {}
        }, "run");

        interceptor.aroundInvoke(context);

        assertEquals(LoggedTest.class, loggerType);
    }

    @Test
    public void shouldDefaultToDollarLoggerClass() throws Exception {
        whenMethod(new Dollar$Type(), "foo");

        interceptor.aroundInvoke(context);

        assertEquals(Dollar$Type.class, loggerType);
    }

    @Test
    public void shouldLogContextParameter() throws Exception {
        class Container {
            @Logged
            @SuppressWarnings("unused")
            public void methodWithLogContextParameter(@LogContext("var") String one, @Deprecated String two) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "foo", "bar");

        interceptor.aroundInvoke(context);

        verifyMdc("var", "foo");
    }

    @Test
    public void shouldLogTwoDifferentContextParameters() throws Exception {
        class Container {
            @Logged
            @SuppressWarnings("unused")
            public void methodWithLogContextParameter(@LogContext("var1") String one, @LogContext("var2") String two) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "foo", "bar");

        interceptor.aroundInvoke(context);

        verifyMdc("var1", "foo");
        verifyMdc("var2", "bar");
    }

    @Test
    public void shouldConcatenateTwoContextParametersWithTheSameName() throws Exception {
        class Container {
            @Logged
            @SuppressWarnings("unused")
            public void methodWithLogContextParameter(@LogContext("var") String one, @LogContext("var") String two) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "foo", "bar");

        interceptor.aroundInvoke(context);

        verifyMdc("var", "foo bar");
    }

    @Test
    public void shouldRestoreMdcValue() throws Exception {
        when(mdc().get("foo")).thenReturn("oldvalue");
        class Container {
            @Logged
            public void methodWithLogContextParameter(@SuppressWarnings("unused") @LogContext("foo") String foo) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "newvalue");

        interceptor.aroundInvoke(context);

        InOrder inOrder = inOrder(mdc());
        inOrder.verify(mdc()).put("foo", "newvalue");
        inOrder.verify(mdc()).put("foo", "oldvalue");
    }

    @Test
    public void shouldRestoreNullMdcValue() throws Exception {
        class Container {
            @Logged
            @SuppressWarnings("unused")
            public void methodWithLogContextParameter(@LogContext("var") String one, String two) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "foo", "bar");

        interceptor.aroundInvoke(context);
        verify(mdc()).remove("var");
    }

    @Test
    public void shouldAddLogContextVariable() throws Exception {
        class Container {
            @Logged
            public void foo() {}
        }
        whenMethod(new Container(), "foo");

        LogContextVariable variable = new LogContextVariable("foo", "baz");
        when(variables.iterator()).thenReturn(asList(variable).iterator());

        interceptor.aroundInvoke(context);

        verify(logger).debug("foo", new Object[0]);
        verifyMdc("foo", "baz");
    }

    @Test
    public void shouldSkipNullLogContextVariable() throws Exception {
        class Container {
            @Logged
            public void foo() {}
        }
        whenMethod(new Container(), "foo");

        when(variables.iterator()).thenReturn(asList((LogContextVariable) null).iterator());

        interceptor.aroundInvoke(context);

        verify(logger).debug("foo", new Object[0]);
    }

    @Test
    public void shouldProduceVersionLogContextVariable() {
        VersionLogContextVariableProducer producer = new VersionLogContextVariableProducer() {
            @Override
            Enumeration<URL> manifests(ClassLoader classLoader) throws IOException {
                return new ListEnumeration<>(new URL("file:target/test-classes/TEST-MANIFEST.MF"));
            }

            @Override
            Pattern mainManifestPattern() {
                return Pattern.compile("file:.*/(.*)");
            }
        };

        assertNotNull(producer.app());
        assertEquals("TEST-MANIFEST.MF", producer.app().getValue());
        assertNotNull(producer.version());
        assertEquals("1.0", producer.version().getValue());
    }

    @Test
    public void shouldIndentFromNull() throws Exception {
        class Container {
            @Logged
            public void method() {}
        }
        whenMethod(new Container(), "method");

        interceptor.aroundInvoke(context);

        verifyMdc("indent", "");
    }

    @Test
    public void shouldIndentFrom0() throws Exception {
        when(mdc().get("indent")).thenReturn("");
        class Container {
            @Logged
            public void method() {}
        }
        whenMethod(new Container(), "method");

        interceptor.aroundInvoke(context);

        verify(mdc()).put("indent", "  ");
        verify(mdc()).put("indent", "");
    }

    @Test
    public void shouldIndentFrom1() throws Exception {
        when(mdc().get("indent")).thenReturn("  ");
        class Container {
            @Logged
            public void method() {}
        }
        whenMethod(new Container(), "method");

        interceptor.aroundInvoke(context);

        verify(mdc()).put("indent", "    ");
        verify(mdc()).put("indent", "  ");
    }

    @Test
    public void shouldNotIndentWhenDisabled() throws Exception {
        when(mdc().get("indent")).thenReturn("  ");
        class Container {
            @Logged(level = OFF)
            public void method() {}
        }
        whenMethod(new Container(), "method");

        interceptor.aroundInvoke(context);

        verify(mdc(), never()).put("indent", "    ");
    }
}

class Dollar$Type {
    @Logged
    public void foo() {}
}
