package com.github.t1.log;

import jakarta.inject.Inject;
import lombok.Value;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.verify;
import static org.slf4j.impl.StaticMDCBinder.givenMdc;

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

        verify(log).debug("foo {}", true);
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

        verify(log).debug("foo {}", 3);
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

        verify(log).debug("foo {}", 3);
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

        verify(log).debug("foo {}", "bar");
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
    public void shouldNotLogArgumentsAnnotatedAsDontLog() {
        dontLogClass.foo("foo", "bar");

        verify(log).debug("foo {}", "bar");
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

        verify(log).debug("foo {} {}", "foo", "bar");
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
    public void shouldLogArgumentsWithIndex() {
        paramsWithIndex.withIndex("foo", "bar");

        verify(log).debug("one={}, two={}", "foo", "bar");
    }

    @Test
    public void shouldLogArgumentsWithInvertedIndex() {
        paramsWithIndex.withInvertedIndex("foo", "bar");

        verify(log).debug("two={}, one={}", "bar", "foo");
    }

    @Test
    public void shouldLogArgumentsWithRepeatedIndex() {
        paramsWithIndex.withRepeatedIndex("foo", "bar");

        verify(log).debug("one={}, again={}", "foo", "foo");
    }

    @Test
    public void shouldFailToLogArgumentsWithInvalidIndex() {
        paramsWithIndex.withInvalidIndex("foo", "bar");

        verify(log).debug("one={}", "invalid log parameter index: 2");
    }

    @Test
    public void shouldFailToLogArgumentsWithNegativeIndex() {
        paramsWithIndex.withNegativeIndex("foo");

        verify(log).debug("one={}", "invalid log parameter index: -1");
    }

    @Test
    public void shouldLogMixedParameters() {
        paramsWithIndex.withMixedIndex("foo", "bar");

        verify(log).debug("one={}, two={}", "foo", "bar");
    }

    @Test
    public void shouldLogMixedParameters2() {
        paramsWithIndex.withMixedIndex2("foo", "bar");

        verify(log).debug("one={}, again={}, two={}", "foo", "foo", "bar");
    }

    // ----------------------------------------------------------------------------------

    @Value
    public static class Pojo {
        String one, two;
    }

    @Value
    public static class Wrapper {
        Pojo pojo;
    }

    @SuppressWarnings("unused")
    public static class ParamsWithNameClass {
        @Logged("one={invalid}")
        public void withInvalidName(String one) {}

        @Logged("one={one}")
        public void withValidName(String one) {}

        @Logged(".one={.one}")
        public void withProperty(Pojo p) {}

        @Logged("0.one={0.one}")
        public void indexedWithProperty(Pojo p) {}

        @Logged("p.one={p.one}")
        public void namedWithProperty(Pojo p) {}

        @Logged("wrapper.pojo.two={wrapper.pojo.two}")
        public void wrappedWithProperty(Wrapper wrapper) {}
    }

    @Inject
    ParamsWithNameClass paramsWithName;

    @Test
    public void shouldNotLogArgumentsWithInvalidNameWhenNotAnMdc() {
        paramsWithName.withInvalidName("foo");

        verify(log).debug("one={}",
            "unset mdc log parameter reference (and not a parameter name): invalid");
    }

    @Test
    public void shouldLogArgumentsWithValidName() {
        paramsWithName.withValidName("foo");

        verify(log).debug("one={}", "foo");
    }

    @Test
    public void shouldLogWithProperty() {
        paramsWithName.withProperty(new Pojo("foo", "bar"));

        verify(log).debug(".one={}", "foo");
    }

    @Test
    public void shouldLogIndexedWithProperty() {
        paramsWithName.indexedWithProperty(new Pojo("foo", "bar"));

        verify(log).debug("0.one={}", "foo");
    }

    @Test
    public void shouldLogNamedWithProperty() {
        paramsWithName.namedWithProperty(new Pojo("foo", "bar"));

        verify(log).debug("p.one={}", "foo");
    }

    @Test
    public void shouldLogWrappedProperty() {
        paramsWithName.wrappedWithProperty(new Wrapper(new Pojo("foo", "bar")));

        verify(log).debug("wrapper.pojo.two={}", "bar");
    }

    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    public static class ParamsWithMdcNameClass {
        @Logged("one={one} mdc={mdc-key}")
        public void withMdcName(String one) {}

        @Logged("mdc={mdc-key.invalid}")
        public void withMdcNameAndExpression() {}
    }

    @Inject
    ParamsWithMdcNameClass paramsWithMdcName;

    @Test
    public void shouldLogArgumentsWithNameAndMdcName() {
        givenMdc("mdc-key", "mdc-value");

        paramsWithMdcName.withMdcName("foo");

        verify(log).debug("one={} mdc={}", "foo", "mdc-value");
    }

    @Test
    public void shouldNotLogMdcParameterWithExpression() {
        givenMdc("mdc-key", "mdc-value");

        paramsWithMdcName.withMdcNameAndExpression();

        verify(log).debug("mdc={}",
            "invalid log parameter expression [invalid] for reference [mdc-key]");
    }
}
