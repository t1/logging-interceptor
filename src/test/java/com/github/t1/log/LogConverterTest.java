package com.github.t1.log;

import jakarta.inject.Inject;
import lombok.Value;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.slf4j.impl.StaticMDCBinder.mdc;

@RunWith(Arquillian.class)
public class LogConverterTest extends AbstractLoggingInterceptorTests {
    // ----------------------------------------------------------------------------------

    @Value
    public static class Pojo {
        String one, two;
    }

    private static final Pojo POJO = new Pojo("foo", "bar");

    public static class PojoConverter implements Converter {
        public String convert(Pojo pojo) {
            return pojo.one + "#" + pojo.two;
        }
    }

    // ----------------------------------------------------------------------------------

    public static class PojoParamClass {
        @SuppressWarnings("unused")
        @Logged
        public void pojoParamMethod(Pojo pojo) {}
    }

    @Inject
    PojoParamClass pojoParam;

    @Test
    public void shouldConvertParameter() {
        pojoParam.pojoParamMethod(POJO);

        verify(log).debug("pojo param method {}", "foo#bar");
    }

    // ----------------------------------------------------------------------------------
    public static class PojoReturnClass {
        @Logged
        public Pojo foo() {
            return POJO;
        }
    }

    @Inject
    PojoReturnClass pojoReturn;

    @Test
    public void shouldConvertReturnValue() {
        pojoReturn.foo();

        verify(log).debug("foo"); // consume for better mockito error messages

        String message = captureMessage();

        assertThat(message).startsWith("return foo#bar [time:");
    }

    // ----------------------------------------------------------------------------------
    public static class PojoLogContextClass {
        @SuppressWarnings("unused")
        @Logged
        public void foo(@LogContext("var") Pojo pojo) {}
    }

    @Inject
    PojoLogContextClass pojoLogContext;

    @Test
    public void shouldConvertLogContextParameter() {
        pojoLogContext.foo(POJO);

        verify(mdc()).put("var", "foo#bar");
    }
}
