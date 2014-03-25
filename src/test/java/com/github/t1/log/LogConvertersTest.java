package com.github.t1.log;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.*;

import javax.enterprise.inject.Instance;

import lombok.experimental.Value;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LogConvertersTest {
    @Mock
    private Instance<LogConverter<Object>> converterInstances;
    @InjectMocks
    private LogConverters converters;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private void givenConverters(LogConverter<?>... converterList) {
        List<LogConverter<Object>> list = new ArrayList<>();
        for (LogConverter<?> converter : converterList) {
            @SuppressWarnings("unchecked")
            LogConverter<Object> c = (LogConverter<Object>) converter;
            list.add(c);
        }
        when(converterInstances.iterator()).thenReturn(list.iterator());

        converters.loadConverters();
    }

    @Test
    public void shouldConvertNull() throws Exception {
        Object converted = converters.convert(null);

        assertNull(converted);
    }

    @Test
    public void shouldConvertString() throws Exception {
        Object converted = converters.convert("hi");

        assertEquals("hi", converted);
    }

    @Test
    public void shouldConvertBoolean() throws Exception {
        Object converted = converters.convert(true);

        assertEquals(true, converted);
    }

    @Test
    public void shouldConvertPojo() throws Exception {
        class Pojo {
            String value = "x";
        }

        @LogConverterType(Pojo.class)
        class Converter implements LogConverter<Pojo> {
            @Override
            public String convert(Pojo object) {
                return object.value + "#";
            }
        }

        givenConverters(new Converter());

        Object converted = converters.convert(new Pojo());

        assertEquals("x#", converted);
    }

    @Test
    public void shouldFailToLoadUnannotatedConverter() throws Exception {
        @Value
        class Pojo {
            String one, two;
        }

        class UnannotatedPojoConverter implements LogConverter<Pojo> {
            @Override
            public String convert(Pojo object) {
                return object.one;
            }
        }

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("must be annotated as @" + LogConverterType.class.getName());

        givenConverters(new UnannotatedPojoConverter());
    }


    @Test
    public void shouldPickOneDuplicateConverter() throws Exception {
        @Value
        class DupPojo {
            String value;
        }

        @LogConverterType(DupPojo.class)
        class DuplicatePojoConverter1 implements LogConverter<DupPojo> {
            @Override
            public String convert(DupPojo object) {
                return object.value + "!1";
            }
        }

        @LogConverterType(DupPojo.class)
        class DuplicatePojoConverter2 implements LogConverter<DupPojo> {
            @Override
            public String convert(DupPojo object) {
                return object.value + "!2";
            }
        }
        givenConverters(new DuplicatePojoConverter1(), new DuplicatePojoConverter2());

        Object converted = converters.convert(new DupPojo("x"));

        assertEquals("x!2", converted);
    }

    @Test
    public void shouldConvertSuperClass() throws Exception {
        class SuperPojo {
            String value = "x";
        }
        class SubPojo extends SuperPojo {}

        @LogConverterType(SuperPojo.class)
        class SuperConverter implements LogConverter<SuperPojo> {
            @Override
            public String convert(SuperPojo object) {
                return object.value + "#";
            }
        }

        givenConverters(new SuperConverter());

        Object converted = converters.convert(new SubPojo());

        assertEquals("x#", converted);
    }

    @Test
    public void shouldConvertSuperInterface() throws Exception {
        class Pojo implements Serializable {
            private static final long serialVersionUID = 1L;
        }

        @LogConverterType(Serializable.class)
        class Converter implements LogConverter<Serializable> {
            @Override
            public String convert(Serializable object) {
                return "#";
            }
        }

        givenConverters(new Converter());

        Object converted = converters.convert(new Pojo());

        assertEquals("#", converted);
    }
}
