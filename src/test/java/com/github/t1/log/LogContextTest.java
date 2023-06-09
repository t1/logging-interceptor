package com.github.t1.log;

import jakarta.inject.Inject;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.impl.StaticMDCBinder.mdc;
import static org.slf4j.impl.StaticMDCBinder.verifyMdc;

@SuppressWarnings("WeakerAccess")
@RunWith(Arquillian.class)
public class LogContextTest extends AbstractLoggingInterceptorTests {
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

    @Test
    public void shouldSetLogContextParameter() {
        logContextParameterClass.methodWithLogContextParameter("foo", "bar");

        verifyMdc("var", "foo");
        verify(log).debug("method with log context parameter {} {}", "foo", "bar");
    }

    @Test
    @Ignore("not yet implemented")
    public void shouldSetLogContextParameterNotInMessage() {
        logContextParameterClass.methodWithLogContextParameterNotInMessage("foo", "bar");

        verifyMdc("one", "foo");
        verify(log).debug("[{}]", "bar");
    }

    @Test
    public void shouldNotSetNullMdcParameter() {
        logContextParameterClass.methodWithLogContextParameter(null, "bar");

        verify(mdc(), never()).put(eq("var"), anyString());
    }

    @Test
    public void shouldRestoreMdcValue() {
        when(mdc().get("var")).thenReturn("oldValue");

        logContextParameterClass.methodWithLogContextParameter("newValue", "bar");

        InOrder inOrder = inOrder(mdc());
        inOrder.verify(mdc()).put("var", "newValue");
        inOrder.verify(mdc()).put("var", "oldValue");
    }

    @Test
    public void shouldRestoreNullMdcValue() {
        logContextParameterClass.methodWithLogContextParameter("newValue", "bar");

        verify(mdc()).remove("var");
    }

    // ----------------------------------------------------------------------------------

    public static class TwoContextVariables {
        @Logged
        @SuppressWarnings("unused")
        public void methodWithLogContextParameter(@LogContext("var1") String one, @LogContext("var2") String two) {}
    }

    @Inject
    TwoContextVariables twoContextVariables;

    @Test
    public void shouldLogTwoDifferentContextParameters() {
        twoContextVariables.methodWithLogContextParameter("foo", "bar");

        verifyMdc("var1", "foo");
        verifyMdc("var2", "bar");
    }

    // ----------------------------------------------------------------------------------

    public static class LogContextParameterVariableClass {
        @Logged
        @SuppressWarnings("unused")
        public void methodWithLogContextParameter(@LogContext String one) {}
    }

    @Inject
    LogContextParameterVariableClass logContextParameterVariableClass;

    @Test
    public void shouldLogContextParameterWithName() {
        logContextParameterVariableClass.methodWithLogContextParameter("foo");

        verifyMdc("one", "foo");
    }

    // ----------------------------------------------------------------------------------

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

    @Test
    public void shouldLogContextField() {
        logContextFieldClass.methodWithLogContextField();

        verifyMdc("one", "foo");
    }

    @Test
    public void shouldLogContextFieldArgument() {
        when(mdc().get("one")).thenReturn(null, "foo");

        logContextFieldClass.methodWithLogContextFieldInMessage();

        verifyMdc("one", "foo");
        verify(log).debug("[{}]", "foo");
    }
}
