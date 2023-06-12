package com.github.t1.log;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Value;
import org.junit.jupiter.api.Test;

import static mock.logging.MockLoggerProvider.array;
import static mock.logging.MockMDC.givenMdc;
import static org.mockito.Mockito.verify;

class LogParamsTest extends AbstractLoggingInterceptorTests {
    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    @Dependent
    public static class BooleanParamClass {
        @Logged
        public void foo(boolean i) {}
    }

    @Inject
    BooleanParamClass booleanParamClass;

    @Test void shouldLogBooleanParam() {
        booleanParamClass.foo(true);

        verify(log).debug("foo {}", array(true));
    }

    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    @Dependent
    public static class IntParamClass {
        @Logged
        public void foo(int i) {}
    }

    @Inject
    IntParamClass intParamClass;

    @Test void shouldLogIntParam() {
        intParamClass.foo(3);

        verify(log).debug("foo {}", array(3));
    }

    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    @Dependent
    public static class IntegerParamClass {
        @Logged
        public void foo(Integer i) {}
    }

    @Inject
    IntegerParamClass integerParamClass;

    @Test void shouldLogIntegerParam() {
        integerParamClass.foo(3);

        verify(log).debug("foo {}", array(3));
    }

    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    @Dependent
    public static class StringParamClass {
        @Logged
        public void foo(String i) {}
    }

    @Inject
    StringParamClass stringParamClass;

    @Test void shouldLogStringParam() {
        stringParamClass.foo("bar");

        verify(log).debug("foo {}", array("bar"));
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class DontLogClass {
        @Logged
        @SuppressWarnings("unused")
        public void foo(@DontLog String one, String two) {}
    }

    @Inject
    DontLogClass dontLogClass;

    @Test void shouldNotLogArgumentsAnnotatedAsDontLog() {
        dontLogClass.foo("foo", "bar");

        verify(log).debug("foo {}", array("bar"));
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class TwoParamsClass {
        @Logged
        @SuppressWarnings("unused")
        public void foo(String one, String two) {}
    }

    @Inject
    TwoParamsClass twoParamsClass;

    @Test void shouldLogTwoParameters() {
        twoParamsClass.foo("foo", "bar");

        verify(log).debug("foo {} {}", array("foo", "bar"));
    }

    // ----------------------------------------------------------------------------------

    @Dependent
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

    @Test void shouldLogArgumentsWithIndex() {
        paramsWithIndex.withIndex("foo", "bar");

        verify(log).debug("one={}, two={}", array("foo", "bar"));
    }

    @Test void shouldLogArgumentsWithInvertedIndex() {
        paramsWithIndex.withInvertedIndex("foo", "bar");

        verify(log).debug("two={}, one={}", array("bar", "foo"));
    }

    @Test void shouldLogArgumentsWithRepeatedIndex() {
        paramsWithIndex.withRepeatedIndex("foo", "bar");

        verify(log).debug("one={}, again={}", array("foo", "foo"));
    }

    @Test void shouldFailToLogArgumentsWithInvalidIndex() {
        paramsWithIndex.withInvalidIndex("foo", "bar");

        verify(log).debug("one={}", array("invalid log parameter index: 2"));
    }

    @Test void shouldFailToLogArgumentsWithNegativeIndex() {
        paramsWithIndex.withNegativeIndex("foo");

        verify(log).debug("one={}", array("invalid log parameter index: -1"));
    }

    @Test void shouldLogMixedParameters() {
        paramsWithIndex.withMixedIndex("foo", "bar");

        verify(log).debug("one={}, two={}", array("foo", "bar"));
    }

    @Test void shouldLogMixedParameters2() {
        paramsWithIndex.withMixedIndex2("foo", "bar");

        verify(log).debug("one={}, again={}, two={}", array("foo", "foo", "bar"));
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
    @Dependent
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

    @Test void shouldNotLogArgumentsWithInvalidNameWhenNotAnMdc() {
        paramsWithName.withInvalidName("foo");

        verify(log).debug("one={}",
                array("unset mdc log parameter reference (and not a parameter name): invalid"));
    }

    @Test void shouldLogArgumentsWithValidName() {
        paramsWithName.withValidName("foo");

        verify(log).debug("one={}", array("foo"));
    }

    @Test void shouldLogWithProperty() {
        paramsWithName.withProperty(new Pojo("foo", "bar"));

        verify(log).debug(".one={}", array("foo"));
    }

    @Test void shouldLogIndexedWithProperty() {
        paramsWithName.indexedWithProperty(new Pojo("foo", "bar"));

        verify(log).debug("0.one={}", array("foo"));
    }

    @Test void shouldLogNamedWithProperty() {
        paramsWithName.namedWithProperty(new Pojo("foo", "bar"));

        verify(log).debug("p.one={}", array("foo"));
    }

    @Test void shouldLogWrappedProperty() {
        paramsWithName.wrappedWithProperty(new Wrapper(new Pojo("foo", "bar")));

        verify(log).debug("wrapper.pojo.two={}", array("bar"));
    }

    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    @Dependent
    public static class ParamsWithMdcNameClass {
        @Logged("one={one} mdc={mdc-key}")
        public void withMdcName(String one) {}

        @Logged("mdc={mdc-key.invalid}")
        public void withMdcNameAndExpression() {}
    }

    @Inject
    ParamsWithMdcNameClass paramsWithMdcName;

    @Test void shouldLogArgumentsWithNameAndMdcName() {
        givenMdc("mdc-key", "mdc-value");

        paramsWithMdcName.withMdcName("foo");

        verify(log).debug("one={} mdc={}", array("foo", "mdc-value"));
    }

    @Test void shouldNotLogMdcParameterWithExpression() {
        givenMdc("mdc-key", "mdc-value");

        paramsWithMdcName.withMdcNameAndExpression();

        verify(log).debug("mdc={}",
                array("invalid log parameter expression [invalid] for reference [mdc-key]"));
    }
}
