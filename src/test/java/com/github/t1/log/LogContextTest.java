package com.github.t1.log;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static mock.logging.MockLoggerProvider.array;
import static mock.logging.MockMDC.mdc;
import static mock.logging.MockMDC.verifyMdc;
import static org.mockito.Mockito.*;

@SuppressWarnings("WeakerAccess")
class LogContextTest extends AbstractLoggingInterceptorTests {
    @Dependent
    public static class LogContextParameterClass {
        @Logged
        @SuppressWarnings("unused")
        public void methodWithLogContextParameter(@LogContext("var") String one, @Deprecated String two) {}

        @Logged("[{two}]")
        @SuppressWarnings("unused")
        public void methodWithLogContextParameterNotInMessage(@LogContext String one, String two) {}
    }

    @Inject
    LogContextParameterClass logContextParameterClass;

    @Test void shouldSetLogContextParameter() {
        logContextParameterClass.methodWithLogContextParameter("foo", "bar");

        verifyMdc("var", "foo");
        verify(log).debug("method with log context parameter {} {}", array("foo", "bar"));
    }

    @Disabled("not yet implemented")
    @Test void shouldSetLogContextParameterNotInMessage() {
        logContextParameterClass.methodWithLogContextParameterNotInMessage("foo", "bar");

        verifyMdc("one", "foo");
        verify(log).debug("[{}]", "bar");
    }

    @Test void shouldNotSetNullMdcParameter() {
        logContextParameterClass.methodWithLogContextParameter(null, "bar");

        verify(mdc(), never()).put(eq("var"), anyString());
    }

    @Test void shouldRestoreMdcValue() {
        when(mdc().get("var")).thenReturn("oldValue");

        logContextParameterClass.methodWithLogContextParameter("newValue", "bar");

        InOrder inOrder = inOrder(mdc());
        inOrder.verify(mdc()).put("var", "newValue");
        inOrder.verify(mdc()).put("var", "oldValue");
    }

    @Test void shouldRestoreNullMdcValue() {
        logContextParameterClass.methodWithLogContextParameter("newValue", "bar");

        verify(mdc()).remove("var");
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class TwoContextVariables {
        @Logged
        @SuppressWarnings("unused")
        public void methodWithLogContextParameter(@LogContext("var1") String one, @LogContext("var2") String two) {}
    }

    @Inject
    TwoContextVariables twoContextVariables;

    @Test void shouldLogTwoDifferentContextParameters() {
        twoContextVariables.methodWithLogContextParameter("foo", "bar");

        verifyMdc("var1", "foo");
        verifyMdc("var2", "bar");
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class LogContextParameterVariableClass {
        @Logged
        @SuppressWarnings("unused")
        public void methodWithLogContextParameter(@LogContext String one) {}
    }

    @Inject
    LogContextParameterVariableClass logContextParameterVariableClass;

    @Test void shouldLogContextParameterWithName() {
        logContextParameterVariableClass.methodWithLogContextParameter("foo");

        verifyMdc("one", "foo");
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class LogContextFieldClass {
        @SuppressWarnings("unused")
        @LogContext
        String one = "foo";

        @Logged
        public void methodWithLogContextField() {}

        @Logged("[{one}]")
        public void methodWithLogContextFieldInMessage() {}
    }

    @Inject
    LogContextFieldClass logContextFieldClass;

    @Test void shouldLogContextField() {
        logContextFieldClass.methodWithLogContextField();

        verifyMdc("one", "foo");
    }

    @Test void shouldLogContextFieldArgument() {
        when(mdc().get("one")).thenReturn(null, "foo");

        logContextFieldClass.methodWithLogContextFieldInMessage();

        verifyMdc("one", "foo");
        verify(log).debug("[{}]", array("foo"));
    }
}
