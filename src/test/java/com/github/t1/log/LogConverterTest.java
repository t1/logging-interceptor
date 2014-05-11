package com.github.t1.log;

import static org.mockito.Mockito.*;
import static org.slf4j.impl.StaticMDCBinder.*;

import javax.inject.Inject;

import lombok.Value;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LogConverterTest extends AbstractLoggingInterceptorTests {
    // ----------------------------------------------------------------------------------

    @Value
    public static class Pojo {
        String one, two;
    }

    private static final Pojo POJO = new Pojo("foo", "bar");

    public static class PojoConverter implements LogConverter<Pojo> {
        @Override
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

        verify(log).debug("pojo param method {}", new Object[] { "foo#bar" });
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

        verify(log).debug("return {}", new Object[] { "foo#bar" });
    }

    // ----------------------------------------------------------------------------------
    public static class PojoLogContextClass {
        @SuppressWarnings("unused")
        @Logged
        public void foo(@LogContext(value = "var") Pojo pojo) {}
    }

    @Inject
    PojoLogContextClass pojoLogContext;

    @Test
    public void shouldConvertLogContextParameter() {
        pojoLogContext.foo(POJO);

        verify(mdc()).put("var", "foo#bar");
    }
}
