package com.github.t1.log;

import static com.github.t1.log.LogConverters.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import javax.inject.Inject;

import lombok.Value;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LogConvertersTest extends AbstractLoggingInterceptorTests {
    @Value
    public static class SimpleType {
        String value;
    }

    public static class SimpleConverter implements LogConverter<SimpleType> {
        @Override
        public String convert(SimpleType object) {
            return object.getValue();
        }
    }

    @Inject
    LogConverter<SimpleType> simpleConverter;

    @Test
    public void shouldFindSimpleConverter() {
        SimpleType object = new SimpleType("foo");

        Object converted = simpleConverter.convert(object);

        assertEquals("foo", converted);
    }

    // ----------------------------------------------------------------------------------

    @Value
    public static class DoubleType {
        String one, two;
    }

    public static class DoubleConverter implements LogConverter<DoubleType> {
        @Override
        public String convert(DoubleType object) {
            return object.getOne() + ":" + object.getTwo();
        }
    }

    @Inject
    LogConverter<DoubleType> doubleConverter;

    @Test
    public void shouldFindDoubleConverter() {
        DoubleType object = new DoubleType("foo", "bar");

        Object converted = doubleConverter.convert(object);

        assertEquals("foo:bar", converted);
    }

    // ----------------------------------------------------------------------------------

    @Test
    public void shouldInjectListOfConverters() {
        assertEquals(SimpleConverter.class, logConverters.get(SimpleType.class).getClass());
        assertEquals(DoubleConverter.class, logConverters.get(DoubleType.class).getClass());
    }

    // ----------------------------------------------------------------------------------

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Inject
    LogConverters converters;

    @Test
    public void shouldConvertNull() {
        Object converted = converters.convert(null);

        assertNull(converted);
    }

    @Test
    public void shouldConvertString() {
        Object converted = converters.convert("hi");

        assertEquals("hi", converted);
    }

    @Test
    public void shouldConvertBoolean() {
        Object converted = converters.convert(true);

        assertEquals(true, converted);
    }

    static class Pojo {
        String value = "x";
    }

    static class PojoConverter implements LogConverter<Pojo> {
        @Override
        public String convert(Pojo pojo) {
            return pojo.value + "#";
        }
    }

    @Test
    public void shouldConvertPojo() {
        Object converted = converters.convert(new Pojo());

        assertEquals("x#", converted);
    }

    @Value
    static class DupPojo {
        String value;
    }

    static class DuplicatePojoConverter1 implements LogConverter<DupPojo> {
        @Override
        public String convert(DupPojo object) {
            return object.value + "!1";
        }
    }

    static class DuplicatePojoConverter2 implements LogConverter<DupPojo> {
        @Override
        public String convert(DupPojo object) {
            return object.value + "!2";
        }
    }

    @Test
    public void shouldPickOneDuplicateConverter() {
        String converted = (String) converters.convert(new DupPojo("x"));

        assertThat(converted, either(is("x!1")).or(is("x!2")));
    }

    static class SuperPojo {
        String value = "x";
    }

    static class SubPojo extends SuperPojo {}

    static class SuperConverter implements LogConverter<SuperPojo> {
        @Override
        public String convert(SuperPojo object) {
            return object.value + "#";
        }
    }

    @Test
    public void shouldConvertSuperClass() {
        Object converted = converters.convert(new SubPojo());

        assertEquals("x#", converted);
    }

    interface PojInterface {}

    static class PojPojo implements PojInterface {}

    static class PojInterfaceConverter implements LogConverter<PojInterface> {
        @Override
        public String convert(PojInterface object) {
            return "#";
        }
    }

    @Test
    public void shouldConvertSuperInterface() {
        Object converted = converters.convert(new PojPojo());

        assertEquals("#", converted);
    }
}
