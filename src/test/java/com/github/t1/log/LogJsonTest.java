package com.github.t1.log;

import static com.github.t1.log.JsonLogDetail.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.slf4j.impl.StaticMDCBinder.*;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Arrays;

import javax.inject.Inject;
import javax.json.*;

import lombok.Value;

import org.jboss.arquillian.junit.Arquillian;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
public class LogJsonTest extends AbstractLoggingInterceptorTests {
    @Value
    class Pojo {
        String one, two;
    }

    @Value
    class ConvertablePojo {
        String one, two;
    }

    public static class PojoConverter implements Converter {
        public String convert(ConvertablePojo pojo) {
            return pojo.one + "#" + pojo.two;
        }
    }

    @SuppressWarnings("unused")
    @Logged(json = ALL)
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

        public void foo(ConvertablePojo pojo) {}

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

    @Test
    public void shouldLogJsonTimestamp() throws InterruptedException {
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

    @Test
    public void shouldLogJsonEvent() {
        jsonLog.foo();

        JsonObject json = captureJsonMdc();
        assertEquals("foo", json.getString("event"));
    }

    @Test
    public void shouldLogJsonLogger() {
        when(LoggerFactory.getLogger(LogJsonTest.class).getName()).thenReturn("logger-name");

        jsonLog.foo();

        JsonObject json = captureJsonMdc();
        assertEquals("logger-name", json.getString("logger"));
    }

    @Test
    public void shouldLogJsonLevel() {
        jsonLog.foo();

        JsonObject json = captureJsonMdc();
        assertEquals("debug", json.getString("level"));
    }

    @Test
    public void shouldLogJsonStringParameter() {
        jsonLog.foo("baz");

        JsonObject json = captureJsonMdc();
        assertEquals("baz", json.getString("bar"));
    }

    @Test
    public void shouldLogJsonNullParameter() {
        jsonLog.foo((String) null);

        JsonObject json = captureJsonMdc();
        assertNull(json.get("bar"));
    }

    @Test
    public void shouldEscapeQuoteInJsonStringParameter() {
        jsonLog.foo("a\"b");

        JsonObject json = captureJsonMdc();
        assertEquals("a\"b", json.getString("bar"));
    }

    @Test
    public void shouldEscapeReturnInJsonStringParameter() {
        jsonLog.foo("a\rb");

        JsonObject json = captureJsonMdc();
        assertEquals("a\rb", json.getString("bar"));
    }

    @Test
    public void shouldEscapeNewlineInJsonStringParameter() {
        jsonLog.foo("a\nb");

        JsonObject json = captureJsonMdc();
        assertEquals("a\nb", json.getString("bar"));
    }

    @Test
    public void shouldEscapeBackslashInJsonStringParameter() {
        jsonLog.foo("a\\b");

        JsonObject json = captureJsonMdc();
        assertEquals("a\\b", json.getString("bar"));
    }

    @Test
    public void shouldLogJsonBooleanParameter() {
        jsonLog.foo(true);

        JsonObject json = captureJsonMdc();
        assertTrue(json.getBoolean("bar"));
    }

    @Test
    public void shouldLogJsonIntegerParameter() {
        jsonLog.foo(123);

        JsonObject json = captureJsonMdc();
        assertEquals(123, json.getInt("bar"));
    }

    @Test
    public void shouldLogJsonLongParameter() {
        jsonLog.foo(1235678901234567890L);

        JsonObject json = captureJsonMdc();
        assertEquals(1235678901234567890L, json.getJsonNumber("bar").longValueExact());
    }

    @Test
    public void shouldLogJsonDoubleParameter() {
        jsonLog.foo(1234.5678);

        JsonObject json = captureJsonMdc();
        assertEquals(1234.5678, json.getJsonNumber("bar").doubleValue(), 0.0);
    }

    @Test
    public void shouldLogJsonBigDecimalParameter() {
        jsonLog.foo(new BigDecimal("1234.56789"));

        JsonObject json = captureJsonMdc();
        assertEquals(1234.56789, json.getJsonNumber("bar").doubleValue(), 0.0);
    }

    @Test
    public void shouldLogJsonTwoStringParameters() {
        jsonLog.foo("1", "2");

        JsonObject json = captureJsonMdc();
        assertEquals("1", json.getString("one"));
        assertEquals("2", json.getString("two"));
    }

    @Test
    public void shouldLogJsonToStringPojoParameter() {
        jsonLog.foo(new Pojo("1", "2"));

        JsonObject json = captureJsonMdc();
        assertEquals("LogJsonTest.Pojo(one=1, two=2)", json.getString("pojo"));
    }

    @Test
    public void shouldLogJsonConvertablePojoParameter() {
        jsonLog.foo(new ConvertablePojo("1", "2"));

        JsonObject json = captureJsonMdc();
        assertEquals("1#2", json.getString("pojo"));
    }

    @Test
    public void shouldLogJsonThrowableParameter() {
        IllegalStateException exception = new IllegalStateException("bar");
        jsonLog.foo(exception);

        JsonObject json = captureJsonMdc();
        assertEquals(exception.toString(), json.getString("exception"));
        assertEquals(Arrays.toString(exception.getStackTrace()), json.getString("exception-stacktrace"));
    }

    @Test
    public void shouldLogJsonMdcVariable() {
        givenMdc("foo", "bar");

        jsonLog.foo();

        JsonObject json = captureJsonMdc();
        assertEquals("bar", json.getString("foo"));
    }

    @Test
    public void shouldOverrideMdcVariableWithLogArgument() {
        givenMdc("bar", "mdc-value");

        jsonLog.foo("param-value");

        JsonObject json = captureJsonMdc();
        assertEquals("param-value", json.getString("bar"));
    }

    @Logged(json = EVENT)
    @SuppressWarnings("unused")
    public static class JsonEventLoggedClass {
        public void foo(String bar) {}
    }

    @Inject
    JsonEventLoggedClass jsonEventLog;

    @Test
    public void shouldLogJsonEventDetails() {
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
    public static class JsonParamsLoggedClass {
        public void foo(String bar) {}
    }

    @Inject
    JsonParamsLoggedClass jsonParamsLog;

    @Test
    public void shouldLogJsonParametersDetails() {
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
    public static class JsonContextLoggedClass {
        public void foo(String bar) {}
    }

    @Inject
    JsonContextLoggedClass jsonContextLog;

    @Test
    public void shouldLogJsonContextDetails() {
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
