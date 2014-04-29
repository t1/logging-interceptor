package com.github.t1.log;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.slf4j.impl.StaticMDCBinder.*;
import lombok.Value;

import org.junit.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class LoggedConvertingTest extends AbstractLoggedTest {
    @Value
    private static class Pojo {
        String one, two;
    }

    @Before
    @Override
    public void setupConverters() {
        when(converters.convert(isA(Pojo.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                return ((Pojo) invocation.getArguments()[0]).one;
            }
        });
    }

    @Test
    public void shouldConvertParameter() throws Exception {
        @SuppressWarnings("unused")
        class Container {
            @Logged
            public void foo(Pojo pojo) {}
        }
        whenMethod(new Container(), "foo", new Pojo("a", "b"));

        interceptor.aroundInvoke(context);

        verify(logger).debug("foo {}", new Object[] { "a" });
    }

    @Test
    public void shouldConvertReturnValue() throws Exception {
        class Container {
            @Logged
            public Pojo foo() {
                return null;
            }
        }
        whenMethod(new Container(), "foo");
        when(context.proceed()).thenReturn(new Pojo("a", "b"));

        interceptor.aroundInvoke(context);

        verify(logger).debug("return {}", new Object[] { "a" });
    }

    @Test
    public void shouldConvertLogContextParameter() throws Exception {
        @SuppressWarnings("unused")
        class Container {
            @Logged
            public void foo(@LogContext(value = "foobar") Pojo pojo) {}
        }
        whenMethod(new Container(), "foo", new Pojo("a", "b"));

        interceptor.aroundInvoke(context);

        verify(mdc()).put("foobar", "a");
    }
}
