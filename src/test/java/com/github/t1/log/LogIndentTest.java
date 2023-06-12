package com.github.t1.log;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static com.github.t1.log.LogLevel.INFO;
import static mock.logging.MockMDC.mdc;
import static mock.logging.MockMDC.verifyMdc;
import static org.mockito.Mockito.*;

class LogIndentTest extends AbstractLoggingInterceptorTests {
    // ----------------------------------------------------------------------------------

    @Dependent
    public static class SimpleClass {
        @Logged
        public void foo() {}
    }

    @Inject
    SimpleClass simpleClass;

    @Test void shouldIndentFromNull() {
        simpleClass.foo();

        verifyMdc("indent", "");
    }

    @Test void shouldIndentFrom0() {
        when(mdc().get("indent")).thenReturn("");

        simpleClass.foo();

        verify(mdc()).put("indent", "  ");
        verify(mdc()).put("indent", "");
    }

    @Test void shouldIndentFrom1() {
        when(mdc().get("indent")).thenReturn("  ");

        simpleClass.foo();

        verify(mdc()).put("indent", "    ");
        verify(mdc()).put("indent", "  ");
    }

    @Test void shouldNotIndentWhenDisabled() {
        givenLogLevel(INFO);
        when(mdc().get("indent")).thenReturn("  ");

        simpleClass.foo();

        verify(mdc(), never()).put(eq("indent"), anyString());
    }
}
