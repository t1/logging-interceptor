package com.github.t1.log;

import static com.github.t1.log.LogLevel.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.slf4j.impl.StaticMDCBinder.*;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LogIndentTest extends AbstractLoggingInterceptorTests {
    // ----------------------------------------------------------------------------------

    public static class SimpleClass {
        @Logged
        public void foo() {}
    }

    @Inject
    SimpleClass simpleClass;

    @Test
    public void shouldIndentFromNull() {
        simpleClass.foo();

        verifyMdc("indent", "");
    }

    @Test
    public void shouldIndentFrom0() {
        when(mdc().get("indent")).thenReturn("");

        simpleClass.foo();

        verify(mdc()).put("indent", "  ");
        verify(mdc()).put("indent", "");
    }

    @Test
    public void shouldIndentFrom1() {
        when(mdc().get("indent")).thenReturn("  ");

        simpleClass.foo();

        verify(mdc()).put("indent", "    ");
        verify(mdc()).put("indent", "  ");
    }

    @Test
    public void shouldNotIndentWhenDisabled() {
        givenLogLevel(INFO);
        when(mdc().get("indent")).thenReturn("  ");

        simpleClass.foo();

        verify(mdc(), never()).put(eq("indent"), anyString());
    }
}
