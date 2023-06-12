package com.github.t1.log;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import lombok.Value;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static com.github.t1.log.JsonLogDetail.*;
import static com.github.t1.log.LogJsonTest.PojoConverter;
import static mock.logging.MockMDC.givenMdc;
import static mock.logging.MockMDC.mdc;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@AddBeanClasses(PojoConverter.class)
class LogJsonTest extends AbstractLoggingInterceptorTests {
    @Value
    class Pojo {
        String one, two;
    }

    @Value
    class ConvertiblePojo {
        String one, two;
    }

    public static class PojoConverter implements Converter {
        @SuppressWarnings("unused") public String convert(ConvertiblePojo pojo) {
            return pojo.one + "#" + pojo.two;
        }
    }

    @SuppressWarnings("unused")
    @Logged(json = ALL)
    @Dependent
    public static class JsonLoggedClass {
        public void foo() {}

        public void foo(String bar) {}

        public void foo(boolean bar) {}

        public void foo(int bar) {}

        public void foo(long bar) {}

        public void foo(double bar) {}

        public void foo(BigDecimal bar) {}

        public void foo(String one, String two) {}

        public void foo(Pojo pojo) {}

        public void foo(ConvertiblePojo pojo) {}

        public void foo(RuntimeException exception) {}
    }

    @Inject
    JsonLoggedClass jsonLog;

    private JsonObject captureJsonMdc() {
        String json = "{" + captureMdc("json") + "}";
        return Json.createReader(new StringReader(json)).readObject();
    }

    private static String captureMdc(String key) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mdc()).put(eq(key), captor.capture());
        String value = captor.getValue();

        verify(mdc()).remove(key);

        return value;
    }

    @Test void shouldLogJsonTimestamp() throws InterruptedException {
        LocalDateTime before = LocalDateTime.now();
        Thread.sleep(3);
        jsonLog.foo();
        Thread.sleep(3);
        LocalDateTime after = LocalDateTime.now();

        JsonObject json = captureJsonMdc();
        LocalDateTime timestamp = LocalDateTime.parse(json.getString("timestamp"));
        assertTrue(timestamp.isAfter(before));
        assertTrue(timestamp.isBefore(after));
    }

    @Test void shouldLogJsonEvent() {
        jsonLog.foo();

        JsonObject json = captureJsonMdc();
        assertEquals("foo", json.getString("event"));
    }

    @Test void shouldLogJsonLogger() {
        when(LoggerFactory.getLogger(LogJsonTest.class).getName()).thenReturn("logger-name");

        jsonLog.foo();

        JsonObject json = captureJsonMdc();
        assertEquals("logger-name", json.getString("logger"));
    }

    @Test void shouldLogJsonLevel() {
        jsonLog.foo();

        JsonObject json = captureJsonMdc();
        assertEquals("debug", json.getString("level"));
    }

    @Test void shouldLogJsonStringParameter() {
        jsonLog.foo("baz");

        JsonObject json = captureJsonMdc();
        assertEquals("baz", json.getString("bar"));
    }

    @Test void shouldLogJsonNullParameter() {
        jsonLog.foo((String) null);

        JsonObject json = captureJsonMdc();
        assertNull(json.get("bar"));
    }

    @Test void shouldEscapeQuoteInJsonStringParameter() {
        jsonLog.foo("a\"b");

        JsonObject json = captureJsonMdc();
        assertEquals("a\"b", json.getString("bar"));
    }

    @Test void shouldEscapeReturnInJsonStringParameter() {
        jsonLog.foo("a\rb");

        JsonObject json = captureJsonMdc();
        assertEquals("a\rb", json.getString("bar"));
    }

    @Test void shouldEscapeNewlineInJsonStringParameter() {
        jsonLog.foo("a\nb");

        JsonObject json = captureJsonMdc();
        assertEquals("a\nb", json.getString("bar"));
    }

    @Test void shouldEscapeBackslashInJsonStringParameter() {
        jsonLog.foo("a\\b");

        JsonObject json = captureJsonMdc();
        assertEquals("a\\b", json.getString("bar"));
    }

    @Test void shouldLogJsonBooleanParameter() {
        jsonLog.foo(true);

        JsonObject json = captureJsonMdc();
        assertTrue(json.getBoolean("bar"));
    }

    @Test void shouldLogJsonIntegerParameter() {
        jsonLog.foo(123);

        JsonObject json = captureJsonMdc();
        assertEquals(123, json.getInt("bar"));
    }

    @Test void shouldLogJsonLongParameter() {
        jsonLog.foo(1235678901234567890L);

        JsonObject json = captureJsonMdc();
        assertEquals(1235678901234567890L, json.getJsonNumber("bar").longValueExact());
    }

    @Test void shouldLogJsonDoubleParameter() {
        jsonLog.foo(1234.5678);

        JsonObject json = captureJsonMdc();
        assertEquals(1234.5678, json.getJsonNumber("bar").doubleValue(), 0.0);
    }

    @Test void shouldLogJsonBigDecimalParameter() {
        jsonLog.foo(new BigDecimal("1234.56789"));

        JsonObject json = captureJsonMdc();
        assertEquals(1234.56789, json.getJsonNumber("bar").doubleValue(), 0.0);
    }

    @Test void shouldLogJsonTwoStringParameters() {
        jsonLog.foo("1", "2");

        JsonObject json = captureJsonMdc();
        assertEquals("1", json.getString("one"));
        assertEquals("2", json.getString("two"));
    }

    @Test void shouldLogJsonToStringPojoParameter() {
        jsonLog.foo(new Pojo("1", "2"));

        JsonObject json = captureJsonMdc();
        assertEquals("LogJsonTest.Pojo(one=1, two=2)", json.getString("pojo"));
    }

    @Test void shouldLogJsonConvertiblePojoParameter() {
        jsonLog.foo(new ConvertiblePojo("1", "2"));

        JsonObject json = captureJsonMdc();
        assertEquals("1#2", json.getString("pojo"));
    }

    @Test void shouldLogJsonThrowableParameter() {
        IllegalStateException exception = new IllegalStateException("bar");
        jsonLog.foo(exception);

        JsonObject json = captureJsonMdc();
        assertEquals(exception.toString(), json.getString("exception"));
        assertEquals(Arrays.toString(exception.getStackTrace()), json.getString("exception-stacktrace"));
    }

    @Test void shouldLogJsonMdcVariable() {
        givenMdc("foo", "bar");

        jsonLog.foo();

        JsonObject json = captureJsonMdc();
        assertEquals("bar", json.getString("foo"));
    }

    @Test void shouldOverrideMdcVariableWithLogArgument() {
        givenMdc("bar", "mdc-value");

        jsonLog.foo("param-value");

        JsonObject json = captureJsonMdc();
        assertEquals("param-value", json.getString("bar"));
    }

    @Logged(json = EVENT)
    @SuppressWarnings("unused")
    @Dependent
    public static class JsonEventLoggedClass {
        public void foo(String bar) {}
    }

    @Inject
    JsonEventLoggedClass jsonEventLog;

    @Test void shouldLogJsonEventDetails() {
        when(LoggerFactory.getLogger(LogJsonTest.class).getName()).thenReturn("logger-name");
        givenMdc("mdc-var", "mdc-value");

        jsonEventLog.foo("baz");

        JsonObject json = captureJsonMdc();
        assertNull(json.get("bar"));
        assertNull(json.get("mdc-var"));

        assertNotNull(json.get("timestamp"));
        assertNotNull(json.get("event"));
        assertNotNull(json.get("logger"));
        assertNotNull(json.get("level"));
    }

    @Logged(json = PARAMETERS)
    @SuppressWarnings("unused")
    @Dependent
    public static class JsonParamsLoggedClass {
        public void foo(String bar) {}
    }

    @Inject
    JsonParamsLoggedClass jsonParamsLog;

    @Test void shouldLogJsonParametersDetails() {
        when(LoggerFactory.getLogger(LogJsonTest.class).getName()).thenReturn("logger-name");
        givenMdc("mdc-var", "mdc-value");

        jsonParamsLog.foo("baz");

        JsonObject json = captureJsonMdc();
        assertEquals("baz", json.getString("bar"));

        assertNull(json.get("mdc-var"));
        assertNull(json.get("timestamp"));
        assertNull(json.get("event"));
        assertNull(json.get("logger"));
        assertNull(json.get("level"));
    }

    @Logged(json = CONTEXT)
    @SuppressWarnings("unused")
    @Dependent
    public static class JsonContextLoggedClass {
        public void foo(String bar) {}
    }

    @Inject
    JsonContextLoggedClass jsonContextLog;

    @Test void shouldLogJsonContextDetails() {
        when(LoggerFactory.getLogger(LogJsonTest.class).getName()).thenReturn("logger-name");
        givenMdc("mdc-var", "mdc-value");

        jsonContextLog.foo("baz");

        JsonObject json = captureJsonMdc();
        assertEquals("mdc-value", json.getString("mdc-var"));

        assertNull(json.get("bar"));
        assertNull(json.get("timestamp"));
        assertNull(json.get("event"));
        assertNull(json.get("logger"));
        assertNull(json.get("level"));
    }
}
