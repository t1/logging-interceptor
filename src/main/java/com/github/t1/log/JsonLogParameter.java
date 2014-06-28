package com.github.t1.log;

import java.util.*;

import javax.interceptor.InvocationContext;

import lombok.Value;

import org.joda.time.LocalDateTime;
import org.slf4j.MDC;

@Value
public class JsonLogParameter implements LogParameter {
    private static class JsonBuilder {
        private final StringBuilder out = new StringBuilder();
        private final Map<String, Object> map = new HashMap<>();

        public void set(String key, Object value) {
            map.put(key, value);
        }

        @Override
        public String toString() {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (out.length() == 0)
                    out.append("{");
                else
                    out.append(",");
                out.append("\"").append(entry.getKey()).append("\":");
                appendJson(entry.getValue());
            }
            out.append("}");
            return out.toString();
        }

        private void appendJson(Object value) {
            if (isJsonValue(value)) {
                out.append(value);
            } else {
                appendString(value);
            }
        }

        private boolean isJsonValue(Object value) {
            return value instanceof Boolean || value instanceof Number;
        }

        private void appendString(Object value) {
            out.append('\"');
            String string = value.toString();
            for (int i = 0; i < string.length(); i++) {
                char c = string.charAt(i);
                if (mustEscape(c))
                    out.append('\\');
                out.append(mapped(c));
            }
            out.append('\"');
        }

        private boolean mustEscape(char c) {
            return c == '\\' || c == '\"' || c == '\n' || c == '\r';
        }

        private char mapped(char c) {
            if (c == '\n')
                return 'n';
            if (c == '\r')
                return 'r';
            return c;
        }
    }

    protected final List<LogParameter> parameters;

    @Override
    public String name() {
        return "json";
    }

    @Override
    public Object value(InvocationContext context) {
        JsonBuilder out = new JsonBuilder();

        out.set("timestamp", LocalDateTime.now());
        out.set("event", context.getMethod().getName());
        addMdc(out);
        addMethodParams(context, out);

        return out.toString();
    }

    private void addMethodParams(InvocationContext context, JsonBuilder out) {
        for (LogParameter parameter : parameters) {
            if (this == parameter)
                continue;
            String name = parameter.name();
            out.set(name, parameter.value(context));
        }
    }

    private void addMdc(JsonBuilder out) {
        @SuppressWarnings("unchecked")
        Set<String> keys = MDC.getCopyOfContextMap().keySet();
        for (String key : keys) {
            out.set(key, MDC.get(key));
        }
    }

    @Override
    public void set(RestorableMdc mdc, InvocationContext context) {
        Object value = value(context);
        mdc.put(name(), value.toString());
    }
}
