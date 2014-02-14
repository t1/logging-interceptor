package com.github.t1.log;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.enterprise.inject.Instance;

import lombok.experimental.Value;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Logged
public class LogConverterCollectorTest {
    @InjectMocks
    LogConverterCollector collector;
    @Mock
    Instance<LogConverter<Object>> converterInstances;

    Class<?> loggerType;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private void givenConverters(LogConverter<?>... converters) {
        List<LogConverter<Object>> list = new ArrayList<>();
        for (LogConverter<?> converter : converters) {
            @SuppressWarnings("unchecked")
            LogConverter<Object> c = (LogConverter<Object>) converter;
            list.add(c);
        }
        when(converterInstances.iterator()).thenReturn(list.iterator());
    }

    @Value
    static class Pojo {
        String one, two;
    }

    static class UnannotatedPojoConverter implements LogConverter<Pojo> {
        @Override
        public String convert(Pojo object) {
            return object.one;
        }
    }

    @Test
    public void shouldFailToLoadUnannotatedConverter() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("must be annotated as @" + LogConverterType.class.getName());

        givenConverters(new UnannotatedPojoConverter());

        collector.loadConverters();
    }

    @Value
    static class DupPojo {
        String value;
    }

    @LogConverterType(DupPojo.class)
    static class DuplicatePojoConverter1 implements LogConverter<DupPojo> {
        @Override
        public String convert(DupPojo object) {
            return object.value + "!1";
        }
    }

    @LogConverterType(DupPojo.class)
    static class DuplicatePojoConverter2 implements LogConverter<DupPojo> {
        @Override
        public String convert(DupPojo object) {
            return object.value + "!2";
        }
    }

    @Test
    public void shouldPickOneDuplicateConverter() throws Exception {
        givenConverters(new DuplicatePojoConverter1(), new DuplicatePojoConverter2());

        Map<Class<?>, LogConverter<Object>> map = collector.loadConverters();

        assertEquals(DuplicatePojoConverter2.class, map.get(DupPojo.class).getClass());
    }
}
