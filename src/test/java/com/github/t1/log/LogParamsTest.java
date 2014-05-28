package com.github.t1.log;

import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LogParamsTest extends AbstractLoggingInterceptorTests {
    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    public static class BooleanParamClass {
        @Logged
        public void foo(boolean i) {}
    }

    @Inject
    BooleanParamClass booleanParamClass;

    @Test
    public void shouldLogBooleanParam() {
        booleanParamClass.foo(true);

        verify(log).debug("foo {}", new Object[] { true });
    }

    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    public static class IntParamClass {
        @Logged
        public void foo(int i) {}
    }

    @Inject
    IntParamClass intParamClass;

    @Test
    public void shouldLogIntParam() {
        intParamClass.foo(3);

        verify(log).debug("foo {}", new Object[] { 3 });
    }

    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    public static class IntegerParamClass {
        @Logged
        public void foo(Integer i) {}
    }

    @Inject
    IntegerParamClass integerParamClass;

    @Test
    public void shouldLogIntegerParam() {
        integerParamClass.foo(3);

        verify(log).debug("foo {}", new Object[] { 3 });
    }

    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    public static class StringParamClass {
        @Logged
        public void foo(String i) {}
    }

    @Inject
    StringParamClass stringParamClass;

    @Test
    public void shouldLogStringParam() {
        stringParamClass.foo("bar");

        verify(log).debug("foo {}", new Object[] { "bar" });
    }

    // ----------------------------------------------------------------------------------

    public static class DontLogClass {
        @Logged
        @SuppressWarnings("unused")
        public void foo(@DontLog String one, String two) {}
    }

    @Inject
    DontLogClass dontLogClass;

    @Test
    public void shouldNotLogParametersAnnotatedAsDontLog() {
        dontLogClass.foo("foo", "bar");

        verify(log).debug("foo {}", new Object[] { "bar" });
    }

    // ----------------------------------------------------------------------------------

    public static class TwoParamsClass {
        @Logged
        @SuppressWarnings("unused")
        public void foo(String one, String two) {}
    }

    @Inject
    TwoParamsClass twoParamsClass;

    @Test
    public void shouldLogTwoParameters() {
        twoParamsClass.foo("foo", "bar");

        verify(log).debug("foo {} {}", new Object[] { "foo", "bar" });
    }

    // ----------------------------------------------------------------------------------

    public static class TwoParamsByIndexClass {
        @Logged("two={1}, one={0}")
        @SuppressWarnings("unused")
        public void foo(String one, String two) {}
    }

    @Inject
    TwoParamsByIndexClass twoParamsByIndexClass;

    @Test
    public void shouldLogTwoParametersByIndex() {
        twoParamsByIndexClass.foo("foo", "bar");

        verify(log).debug("two={}, one={}", new Object[] { "bar", "foo" });
    }
}
