package com.github.t1.log;

import static org.junit.Assert.*;
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
    @Logged(json = true)
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

    private JsonObject json(String captureMdc) {
        return Json.createReader(new StringReader(captureMdc)).readObject();
    }

    @Test
    public void shouldLogJsonEvent() {
        jsonLog.foo();

        JsonObject json = json(captureMdc("json"));
        assertEquals("foo", json.getString("event"));
    }

    @Test
    public void shouldLogJsonTimestamp() {
        LocalDateTime before = LocalDateTime.now();
        jsonLog.foo();
        LocalDateTime after = LocalDateTime.now();

        JsonObject json = json(captureMdc("json"));
        LocalDateTime timestamp = LocalDateTime.parse(json.getString("timestamp"));
        assertTrue(timestamp.isAfter(before));
        assertTrue(timestamp.isBefore(after));
    }

    @Test
    public void shouldLogJsonStringParameter() {
        jsonLog.foo("baz");

        JsonObject json = json(captureMdc("json"));
        assertEquals("baz", json.getString("bar"));
    }

    @Test
    public void shouldEscapeQuoteInJsonStringParameter() {
        jsonLog.foo("a\"b");

        JsonObject json = json(captureMdc("json"));
        assertEquals("a\"b", json.getString("bar"));
    }

    @Test
    public void shouldEscapeReturnInJsonStringParameter() {
        jsonLog.foo("a\rb");

        JsonObject json = json(captureMdc("json"));
        assertEquals("a\rb", json.getString("bar"));
    }

    @Test
    public void shouldEscapeNewlineInJsonStringParameter() {
        jsonLog.foo("a\nb");

        JsonObject json = json(captureMdc("json"));
        assertEquals("a\nb", json.getString("bar"));
    }

    @Test
    public void shouldEscapeBackslashInJsonStringParameter() {
        jsonLog.foo("a\\b");

        JsonObject json = json(captureMdc("json"));
        assertEquals("a\\b", json.getString("bar"));
    }

    @Test
    public void shouldLogJsonBooleanParameter() {
        jsonLog.foo(true);

        JsonObject json = json(captureMdc("json"));
        assertTrue(json.getBoolean("bar"));
    }

    @Test
    public void shouldLogJsonIntegerParameter() {
        jsonLog.foo(123);

        JsonObject json = json(captureMdc("json"));
        assertEquals(123, json.getInt("bar"));
    }

    @Test
    public void shouldLogJsonLongParameter() {
        jsonLog.foo(1235678901234567890L);

        JsonObject json = json(captureMdc("json"));
        assertEquals(1235678901234567890L, json.getJsonNumber("bar").longValueExact());
    }

    @Test
    public void shouldLogJsonDoubleParameter() {
        jsonLog.foo(1234.5678);

        JsonObject json = json(captureMdc("json"));
        assertEquals(1234.5678, json.getJsonNumber("bar").doubleValue(), 0.0);
    }

    @Test
    public void shouldLogJsonBigDecimalParameter() {
        jsonLog.foo(new BigDecimal("1234.56789"));

        JsonObject json = json(captureMdc("json"));
        assertEquals(1234.56789, json.getJsonNumber("bar").doubleValue(), 0.0);
    }

    @Test
    public void shouldLogJsonTwoStringParameters() {
        jsonLog.foo("1", "2");

        JsonObject json = json(captureMdc("json"));
        assertEquals("1", json.getString("one"));
        assertEquals("2", json.getString("two"));
    }

    @Test
    public void shouldLogJsonToStringPojoParameter() {
        jsonLog.foo(new Pojo("1", "2"));

        JsonObject json = json(captureMdc("json"));
        assertEquals("LogJsonTest.Pojo(one=1, two=2)", json.getString("pojo"));
    }

    @Test
    public void shouldLogJsonConvertablePojoParameter() {
        jsonLog.foo(new ConvertablePojo("1", "2"));

        JsonObject json = json(captureMdc("json"));
        assertEquals("1#2", json.getString("pojo"));
    }

    @Test
    public void shouldLogJsonThrowableParameter() {
        IllegalStateException exception = new IllegalStateException("bar");
        jsonLog.foo(exception);

        JsonObject json = json(captureMdc("json"));
        assertEquals(exception.toString(), json.getString("exception"));
        assertEquals(Arrays.toString(exception.getStackTrace()), json.getString("exception-stacktrace"));
    }

    @Test
    public void shouldLogJsonMdcVariable() {
        givenMdc("foo", "bar");

        jsonLog.foo();

        JsonObject json = json(captureMdc("json"));
        assertEquals("bar", json.getString("foo"));
    }

    @Test
    public void shouldOverrideMdcVariableWithLogParameter() {
        givenMdc("bar", "mdc-value");

        jsonLog.foo("param-value");

        JsonObject json = json(captureMdc("json"));
        assertEquals("param-value", json.getString("bar"));
    }
}
