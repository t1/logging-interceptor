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

    public static class ParamsWithIndexClass {
        @Logged("one={0}, two={1}")
        @SuppressWarnings("unused")
        public void withIndex(String one, String two) {}

        @Logged("two={1}, one={0}")
        @SuppressWarnings("unused")
        public void withInvertedIndex(String one, String two) {}

        @Logged("one={0}, again={0}")
        @SuppressWarnings("unused")
        public void withRepeatedIndex(String one, String two) {}

        @Logged("one={2}")
        @SuppressWarnings("unused")
        public void withInvalidIndex(String one, String two) {}

        @Logged("one={-1}")
        @SuppressWarnings("unused")
        public void withNegativeIndex(String one) {}

        @Logged("one={}, two={1}")
        @SuppressWarnings("unused")
        public void withMixedIndex(String one, String two) {}

        @Logged("one={0}, again={}, two={}")
        @SuppressWarnings("unused")
        public void withMixedIndex2(String one, String two) {}
    }

    @Inject
    ParamsWithIndexClass paramsWithIndex;

    @Test
    public void shouldLogParametersWithIndex() {
        paramsWithIndex.withIndex("foo", "bar");

        verify(log).debug("one={}, two={}", new Object[] { "foo", "bar" });
    }

    @Test
    public void shouldLogParametersWithInvertedIndex() {
        paramsWithIndex.withInvertedIndex("foo", "bar");

        verify(log).debug("two={}, one={}", new Object[] { "bar", "foo" });
    }

    @Test
    public void shouldLogParametersWithRepeatedIndex() {
        paramsWithIndex.withRepeatedIndex("foo", "bar");

        verify(log).debug("one={}, again={}", new Object[] { "foo", "foo" });
    }

    @Test
    public void shouldFailToLogParametersWithInvalidIndex() {
        paramsWithIndex.withInvalidIndex("foo", "bar");

        verify(log).debug("one={}", new Object[] { "invalid log parameter index: 2" });
    }

    @Test
    public void shouldFailToLogParametersWithNegativeIndex() {
        paramsWithIndex.withNegativeIndex("foo");

        verify(log).debug("one={}", new Object[] { "invalid log parameter index: -1" });
    }

    @Test
    public void shouldLogMixedParameters() {
        paramsWithIndex.withMixedIndex("foo", "bar");

        verify(log).debug("one={}, two={}", new Object[] { "foo", "bar" });
    }

    @Test
    public void shouldLogMixedParameters2() {
        paramsWithIndex.withMixedIndex2("foo", "bar");

        verify(log).debug("one={}, again={}, two={}", new Object[] { "foo", "foo", "bar" });
    }

    // ----------------------------------------------------------------------------------

    public static class ParamsWithNameClass {
        @Logged("one={invalid}")
        @SuppressWarnings("unused")
        public void withInvalidName(String one) {}
    }

    @Inject
    ParamsWithNameClass paramsWithName;

    @Test
    public void shouldNotLogParametersWithInvalidName() {
        paramsWithName.withInvalidName("foo");

        verify(log).debug("one={}", new Object[] { "invalid log parameter expression: invalid" });
    }
}
