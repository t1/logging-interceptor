package com.github.t1.log;

import lombok.Value;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import javax.enterprise.inject.Instance;
import java.io.Serializable;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unused")
public class ConvertersTest {
    @Mock
    private Instance<Converter> converterInstances;
    @InjectMocks
    private Converters converters;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private void givenConverters(Converter... converterList) {
        when(converterInstances.iterator()).thenReturn(asList(converterList).iterator());

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

        class PojoConverter implements Converter {
            public String convert(Pojo pojo) {
                return pojo.value + "#";
            }
        }

        givenConverters(new PojoConverter());

        Object converted = converters.convert(new Pojo());

        assertEquals("x#", converted);
    }

    @Test
    public void shouldIgnoreConverterMethodWithWrongName() {
        @Value
        class Pojo {
            String value;
        }

        givenConverters(new Converter() {
            public String convertX(Pojo pojo) {
                return pojo.value + "!1";
            }
        });
        Pojo pojo = new Pojo("x");

        Object converted = converters.convert(pojo);

        assertEquals(pojo, converted);
    }

    @Test
    public void shouldIgnoreConverterMethodReturningVoid() {
        @Value
        class Pojo {
            String value;
        }

        givenConverters(new Converter() {
            public void convert(Pojo pojo) {
                throw new RuntimeException();
            }
        });
        Pojo pojo = new Pojo("x");

        Object converted = converters.convert(pojo);

        assertEquals(pojo, converted);
    }

    @Test
    public void shouldIgnoreConverterMethodWithNoArguments() {
        @Value
        class Pojo {
            String value;
        }

        givenConverters(new Converter() {
            public String convert() {
                return "unexpected";
            }
        });
        Pojo pojo = new Pojo("x");

        Object converted = converters.convert(pojo);

        assertEquals(pojo, converted);
    }

    @Test
    public void shouldIgnoreConverterMethodWithTwoArguments() {
        @Value
        class Pojo {
            String value;
        }

        givenConverters(new Converter() {
            public String convert(Pojo one, String two) {
                return "unexpected";
            }
        });
        Pojo pojo = new Pojo("x");

        Object converted = converters.convert(pojo);

        assertEquals(pojo, converted);
    }


    @Test
    public void shouldPickOneDuplicateConverter() {
        @Value
        class DupPojo {
            String value;
        }

        class DuplicatePojoConverter1 implements Converter {
            public String convert(DupPojo pojo) {
                return pojo.value + "!1";
            }
        }
        class DuplicatePojoConverter2 implements Converter {
            public String convert(DupPojo pojo) {
                return pojo.value + "!2";
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

        class SuperConverter implements Converter {
            public String convert(SuperPojo object) {
                return object.value + "#";
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

        class InterfaceConverter implements Converter {
            public String convert(Serializable object) {
                return "#";
            }
        }

        givenConverters(new InterfaceConverter());

        Object converted = converters.convert(new Pojo());

        assertEquals("#", converted);
    }

    @Test
    public void shouldNotConvertIfTheConverterThrowsRuntimeExeption() {
        class Pojo {}

        class FailingConverter implements Converter {
            public String convert(Pojo object) {
                throw new RuntimeException("dummy");
            }
        }

        givenConverters(new FailingConverter());

        Pojo pojo = new Pojo();
        Object converted = converters.convert(pojo);

        assertTrue(pojo == converted);
    }

    @Test
    public void shouldNotConvertIfTheConverterThrowsLinkageError() {
        class Pojo {}

        class FailingConverter implements Converter {
            public String convert(Pojo object) {
                throw new NoSuchMethodError("dummy");
            }
        }

        givenConverters(new FailingConverter());

        Pojo pojo = new Pojo();
        Object converted = converters.convert(pojo);

        assertTrue(pojo == converted);
    }

    @Test
    public void shouldNotConvertIfTheConverterThrowsAssertionError() {
        class Pojo {}

        class FailingConverter implements Converter {
            public String convert(Pojo object) {
                throw new AssertionError("dummy");
            }
        }

        givenConverters(new FailingConverter());

        Pojo pojo = new Pojo();
        Object converted = converters.convert(pojo);

        assertTrue(pojo == converted);
    }
}
