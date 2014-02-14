package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.*;

import javax.enterprise.inject.Instance;
import javax.interceptor.InvocationContext;

import lombok.experimental.Value;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.*;

@RunWith(MockitoJUnitRunner.class)
@Logged
public class LoggingInterceptorTest {
    private final class StoreMdcAnswer implements Answer<Void> {
        private final String key;
        private final String[] result;

        private StoreMdcAnswer(String key, String[] result) {
            this.key = key;
            this.result = result;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            result[0] = MDC.get(key);
            return null;
        }
    }

    private class Nested {
        @Logged
        public void implicit() {}

        @Logged(logger = Nested.class)
        public void explicit() {}
    }

    private class Inner {
        @Logged
        public void implicit() {}

        @Logged(logger = Inner.class)
        public void explicit() {}
    }

    @InjectMocks
    LoggingInterceptor interceptor = new LoggingInterceptor() {
        @Override
        protected Logger getLogger(Class<?> loggerType) {
            LoggingInterceptorTest.this.loggerType = loggerType;
            return logger;
        };
    };
    @Mock
    InvocationContext context;
    @Mock
    Logger logger;
    @Mock
    Instance<LogContextVariable> variables;
    @Mock
    Map<Class<?>, LogConverter<Object>> converters;

    Class<?> loggerType;

    @Before
    public void setup() {
        when(logger.isDebugEnabled()).thenReturn(true);
        when(variables.iterator()).thenReturn(Collections.<LogContextVariable> emptyList().iterator());
        givenConverters(new PojoConverter());
    }

    // sometimes I hate java generics
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void givenConverters(LogConverter... defined) {
        for (LogConverter converter : defined) {
            Class<?>[] types = converter.getClass().getAnnotation(LogConverterType.class).value();
            for (Class<?> type : types) {
                when(converters.get(type)).thenReturn(converter);
            }
        }
    }

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private void whenMethod(Object target, String methodName, Object... args) throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(methodName, types(args));
        whenMethod(method, target, args);
    }

    private void whenMethod(Method method, Object target, Object... args) {
        when(context.getMethod()).thenReturn(method);
        when(context.getTarget()).thenReturn(target);
        when(context.getParameters()).thenReturn(args);
    }

    private Class<?>[] types(Object[] objects) {
        Class<?>[] result = new Class[objects.length];
        for (int i = 0; i < objects.length; i++) {
            result[i] = objects[i].getClass();
        }
        return result;
    }

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
            public void methodWithIntArgument(int i) {}
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
            public void methodWithIntegerArgument(Integer i) {}
        }
        whenMethod(new Container(), "methodWithIntegerArgument", 3);

        interceptor.aroundInvoke(context);

        verify(logger).debug("method with integer argument {}", new Object[] { 3 });
    }

    @Test
    public void shouldLogTwoParameters() throws Exception {
        class Container {
            @Logged
            public void methodWithTwoParameters(String one, String two) {}
        }
        whenMethod(new Container(), "methodWithTwoParameters", "foo", "bar");

        interceptor.aroundInvoke(context);

        verify(logger).debug("method with two parameters {} {}", new Object[] { "foo", "bar" });
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

    @Test
    public void shouldNotUnwrapUseExplicitNestedLoggerClass() throws Exception {
        whenMethod(new Nested(), "explicit");

        interceptor.aroundInvoke(context);

        assertEquals(Nested.class, loggerType);
    }

    @Test
    public void shouldNotUnwrapUseExplicitInnerLoggerClass() throws Exception {
        whenMethod(new Inner(), "explicit");

        interceptor.aroundInvoke(context);

        assertEquals(Inner.class, loggerType);
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

    @Test
    public void shouldDefaultToLoggerClass() throws Exception {
        whenMethod(new LoggingInterceptorTest(), "shouldDefaultToLoggerClass");

        interceptor.aroundInvoke(context);

        assertEquals(LoggingInterceptorTest.class, loggerType);
    }

    @Test
    public void shouldDefaultToContainerOfLocalLoggerClass() throws Exception {
        class Container {
            @Logged
            public void foo() {}
        }
        whenMethod(new Container(), "foo");

        interceptor.aroundInvoke(context);

        assertEquals(LoggingInterceptorTest.class, loggerType);
    }

    @Test
    public void shouldDefaultToContainerOfNestedLoggerClass() throws Exception {
        whenMethod(new Nested(), "implicit");

        interceptor.aroundInvoke(context);

        assertEquals(LoggingInterceptorTest.class, loggerType);
    }

    @Test
    public void shouldDefaultToContainerOfInnerLoggerClass() throws Exception {
        whenMethod(new Inner(), "implicit");

        interceptor.aroundInvoke(context);

        assertEquals(LoggingInterceptorTest.class, loggerType);
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

        assertEquals(LoggingInterceptorTest.class, loggerType);
    }

    @Test
    public void shouldDefaultToAnonymousLoggerClass() throws Exception {
        whenMethod(new Runnable() {
            @Override
            @Logged
            public void run() {}
        }, "run");

        interceptor.aroundInvoke(context);

        assertEquals(LoggingInterceptorTest.class, loggerType);
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
            public void methodWithLogContextParameter(@LogContext("var") String one, @Deprecated String two) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "foo", "bar");
        final String[] result = new String[1];
        when(context.proceed()).then(new StoreMdcAnswer("var", result));

        MDC.put("var", "bar");
        interceptor.aroundInvoke(context);
        assertEquals("bar", MDC.get("var"));

        verify(logger).debug("method with log context parameter {} {}", new Object[] { "foo", "bar" });
        assertEquals("foo", result[0]);
    }

    @Test
    public void shouldLogTwoContextParameters() throws Exception {
        class Container {
            @Logged
            public void methodWithLogContextParameter(@LogContext("var1") String one, @LogContext("var2") String two) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "foo", "bar");
        final String[] result1 = new String[1];
        final String[] result2 = new String[1];
        when(context.proceed()).then(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                result1[0] = MDC.get("var1");
                result2[0] = MDC.get("var2");
                return null;
            }
        });

        MDC.put("var1", "old1");
        MDC.put("var2", "old2");
        interceptor.aroundInvoke(context);
        assertEquals("old1", MDC.get("var1"));
        assertEquals("old2", MDC.get("var2"));

        verify(logger).debug("method with log context parameter {} {}", new Object[] { "foo", "bar" });
        assertEquals("foo", result1[0]);
        assertEquals("bar", result2[0]);
    }

    @Test
    public void shouldRestoreMdcValue() throws Exception {
        class Container {
            @Logged
            public void methodWithLogContextParameter(@LogContext("foo") String foo) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "newvalue");

        MDC.put("foo", "oldvalue");
        interceptor.aroundInvoke(context);
        assertEquals("oldvalue", MDC.get("foo"));
    }

    @Test
    public void shouldRestoreNullMdcValue() throws Exception {
        class Container {
            @Logged
            public void methodWithLogContextParameter(@LogContext("var") String one, String two) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "foo", "bar");

        MDC.remove("var"); // just to make sure with other tests running before
        interceptor.aroundInvoke(context);
        assertEquals(null, MDC.get("var"));
    }

    @Value
    static class Pojo {
        String one, two;
    }

    @LogConverterType(Pojo.class)
    static class PojoConverter implements LogConverter<Pojo> {
        @Override
        public String convert(Pojo object) {
            return object.one;
        }
    }

    @Test
    public void shouldConvertLogContextParameter() throws Exception {
        class Container {
            @Logged
            public void foo(@LogContext(value = "foobar") Pojo pojo) {}
        }
        whenMethod(new Container(), "foo", new Pojo("a", "b"));
        final String[] result = new String[1];
        when(context.proceed()).then(new StoreMdcAnswer("foobar", result));

        MDC.put("foobar", "bar");
        interceptor.aroundInvoke(context);
        assertEquals("bar", MDC.get("foobar"));

        verify(logger).debug("foo {}", new Object[] { new Pojo("a", "b") });
        assertEquals("a", result[0]);
    }

    @Test
    public void shouldFindAddALogContextVariable() throws Exception {
        class Container {
            @Logged
            public void foo() {}
        }
        whenMethod(new Container(), "foo");
        final String[] result = new String[1];
        when(context.proceed()).then(new StoreMdcAnswer("foo", result));

        LogContextVariable variable = new LogContextVariable("foo", "baz");
        when(variables.iterator()).thenReturn(Collections.<LogContextVariable> singletonList(variable).iterator());

        MDC.put("foo", "bar");
        interceptor.aroundInvoke(context);
        assertEquals("bar", MDC.get("foo"));

        verify(logger).debug("foo", new Object[0]);
        assertEquals("baz", result[0]);
    }

    @Test
    public void shouldSkipNullLogContextVariable() throws Exception {
        class Container {
            @Logged
            public void foo() {}
        }
        whenMethod(new Container(), "foo");
        final String[] result = new String[1];
        when(context.proceed()).then(new StoreMdcAnswer("foo", result));

        when(variables.iterator()).thenReturn(Collections.<LogContextVariable> singletonList(null).iterator());

        MDC.put("foo", "bar");
        interceptor.aroundInvoke(context);
        assertEquals("bar", MDC.get("foo"));

        verify(logger).debug("foo", new Object[0]);
        assertEquals("bar", result[0]);
    }
}

class Dollar$Type {
    @Logged
    public void foo() {}
}
