package com.github.t1.log;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Value;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.junit.jupiter.api.Test;

import static com.github.t1.log.LogConverterTest.PojoConverter;
import static mock.logging.MockLoggerProvider.array;
import static mock.logging.MockMDC.verifyMdc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@AddBeanClasses(PojoConverter.class)
class LogConverterTest extends AbstractLoggingInterceptorTests {
    // ----------------------------------------------------------------------------------

    @Value
    public static class Pojo {
        String one, two;
    }

    private static final Pojo POJO = new Pojo("foo", "bar");

    public static class PojoConverter implements Converter {
        @SuppressWarnings("unused")
        public String convert(Pojo pojo) {
            return pojo.one + "#" + pojo.two;
        }
    }

    // ----------------------------------------------------------------------------------

    @Dependent
    public static class PojoParamClass {
        @SuppressWarnings("unused")
        @Logged
        public void pojoParamMethod(Pojo pojo) {}
    }

    @Inject
    PojoParamClass pojoParam;

    @Test void shouldConvertParameter() {
        pojoParam.pojoParamMethod(POJO);

        verify(log).debug("pojo param method {}", array("foo#bar"));
    }

    // ----------------------------------------------------------------------------------
    @Dependent
    public static class PojoReturnClass {
        @Logged
        public Pojo foo() {
            return POJO;
        }
    }

    @Inject
    PojoReturnClass pojoReturn;

    @Test void shouldConvertReturnValue() {
        pojoReturn.foo();

        //noinspection RedundantArrayCreation
        verify(log).debug("foo", new Object[0]); // consume for better mockito error messages

        String message = captureMessage();

        assertThat(message).startsWith("return foo#bar [time:");
    }

    // ----------------------------------------------------------------------------------
    @Dependent
    public static class PojoLogContextClass {
        @SuppressWarnings("unused")
        @Logged
        public void foo(@LogContext("var") Pojo pojo) {}
    }

    @Inject
    PojoLogContextClass pojoLogContext;

    @Test void shouldConvertLogContextParameter() {
        pojoLogContext.foo(POJO);

        verifyMdc("var", "foo#bar");
    }
}
