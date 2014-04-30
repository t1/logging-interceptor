package com.github.t1.log;

import static org.mockito.Mockito.*;
import static org.slf4j.impl.StaticMDCBinder.*;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

@RunWith(Arquillian.class)
public class LogContextTest extends AbstractLoggingInterceptorTests {
    @Deployment
    public static JavaArchive createDeployment() {
        return loggingInterceptorDeployment();
    }

    // ----------------------------------------------------------------------------------

    public static class LogContextParameterClass {
        @Logged
        @SuppressWarnings("unused")
        public void methodWithLogContextParameter(@LogContext("var") String one, @Deprecated String two) {}
    }

    @Inject
    LogContextParameterClass logContextParameterClass;

    @Test
    public void shouldLogContextParameter() {
        logContextParameterClass.methodWithLogContextParameter("foo", "bar");

        verifyMdc("var", "foo");
    }

    @Test
    public void shouldRestoreMdcValue() {
        when(mdc().get("var")).thenReturn("oldvalue");

        logContextParameterClass.methodWithLogContextParameter("newvalue", "bar");

        InOrder inOrder = inOrder(mdc());
        inOrder.verify(mdc()).put("var", "newvalue");
        inOrder.verify(mdc()).put("var", "oldvalue");
    }

    @Test
    public void shouldRestoreNullMdcValue() {
        logContextParameterClass.methodWithLogContextParameter("newvalue", "bar");

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
    public static class ConcatenateLogContextParams {
        @Logged
        @SuppressWarnings("unused")
        public void methodWithLogContextParameter(@LogContext("var") String one, @LogContext("var") String two) {}
    }

    @Inject
    ConcatenateLogContextParams concatenateLogContextParams;

    @Test
    public void shouldConcatenateTwoContextParametersWithTheSameName() {
        concatenateLogContextParams.methodWithLogContextParameter("foo", "bar");

        verifyMdc("var", "foo bar");
    }


    // ----------------------------------------------------------------------------------

    public static class SimpleClass {
        @Logged
        public void simple() {}
    }

    @Inject
    SimpleClass simple;

    @Produces
    LogContextVariable fooBarVariable = new LogContextVariable("foo", "bar");

    @Test
    public void shouldAddLogContextVariable() {
        simple.simple();

        verifyMdc("foo", "bar");
    }

    @Produces
    LogContextVariable nullVariable = null;

    @Test
    public void shouldSkipNullLogContextVariable() {
        simple.simple();

        // verify not possible... just check that there's no exception
    }
}
