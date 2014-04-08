package com.github.t1.log;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.*;

import javax.enterprise.inject.Instance;

import lombok.Value;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LogConvertersTest {
    @Mock
    private Instance<Converter> converterInstances;
    @InjectMocks
    private Converters converters;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private void givenConverters(Converter... converterList) {
        List<Converter> list = new ArrayList<>();
        for (Converter converter : converterList) {
            Converter c = converter;
            list.add(c);
        }
        when(converterInstances.iterator()).thenReturn(list.iterator());

        converters.loadConverters();
    }

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

    @Test
    public void shouldConvertPojo() {
        class Pojo {
            String value = "x";
        }

        @ConverterType(Pojo.class)
        class PojoConverter implements Converter {
            @Override
            public String convert(Object pojo) {
                return ((Pojo) pojo).value + "#";
            }
        }

        givenConverters(new PojoConverter());

        Object converted = converters.convert(new Pojo());

        assertEquals("x#", converted);
    }

    @Test
    public void shouldFailToLoadUnannotatedConverter() {
        @Value
        class Pojo {
            String one, two;
        }

        class UnannotatedPojoConverter implements Converter {
            @Override
            public String convert(Object pojo) {
                return ((Pojo) pojo).one;
            }
        }

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("must be annotated as @" + ConverterType.class.getName());

        givenConverters(new UnannotatedPojoConverter());
    }


    @Test
    public void shouldPickOneDuplicateConverter() {
        @Value
        class DupPojo {
            String value;
        }

        @ConverterType(DupPojo.class)
        class DuplicatePojoConverter1 implements Converter {
            @Override
            public String convert(Object object) {
                return ((DupPojo) object).value + "!1";
            }
        }

        @ConverterType(DupPojo.class)
        class DuplicatePojoConverter2 implements Converter {
            @Override
            public String convert(Object object) {
                return ((DupPojo) object).value + "!2";
            }
        }
        givenConverters(new DuplicatePojoConverter1(), new DuplicatePojoConverter2());

        Object converted = converters.convert(new DupPojo("x"));

        assertEquals("x!2", converted);
    }

    @Test
    public void shouldConvertSuperClass() {
        class SuperPojo {
            String value = "x";
        }
        class SubPojo extends SuperPojo {}

        @ConverterType(SuperPojo.class)
        class SuperConverter implements Converter {
            @Override
            public String convert(Object object) {
                return ((SuperPojo) object).value + "#";
            }
        }

        givenConverters(new SuperConverter());

        Object converted = converters.convert(new SubPojo());

        assertEquals("x#", converted);
    }

    @Test
    public void shouldConvertSuperInterface() {
        class Pojo implements Serializable {
            private static final long serialVersionUID = 1L;
        }

        @ConverterType(Serializable.class)
        class InterfaceConverter implements Converter {
            @Override
            public String convert(Object object) {
                return "#";
            }
        }

        givenConverters(new InterfaceConverter());

        Object converted = converters.convert(new Pojo());

        assertEquals("#", converted);
    }
}
