package com.github.t1.log;

import java.util.*;

import javax.interceptor.InvocationContext;

import lombok.Value;

import org.joda.time.LocalDateTime;
import org.slf4j.MDC;

@Value
public class JsonLogParameter implements LogParameter {
    private static class JsonBuilder {
        StringBuilder out = new StringBuilder();

        public void set(String key, Object value) {
            if (out.length() == 0)
                out.append("{");
            else
                out.append(",");
            out.append("\"").append(key).append("\":");
            appendJson(value);
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

        @Override
        public String toString() {
            out.append("}");
            return out.toString();
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
        Set<String> names = addMethodParams(context, out);
        addMdc(out, names);

        return out.toString();
    }

    private Set<String> addMethodParams(InvocationContext context, JsonBuilder out) {
        Set<String> names = new HashSet<>();
        for (LogParameter parameter : parameters) {
            if (this == parameter)
                continue;
            String name = parameter.name();
            names.add(name);
            out.set(name, parameter.value(context));
        }
        return names;
    }

    private void addMdc(JsonBuilder out, Set<String> skip) {
        @SuppressWarnings("unchecked")
        Set<String> keys = MDC.getCopyOfContextMap().keySet();
        for (String key : keys) {
            if (skip.contains(key))
                continue;
            out.set(key, MDC.get(key));
        }
    }

    @Override
    public void set(RestorableMdc mdc, InvocationContext context) {
        Object value = value(context);
        mdc.put(name(), value.toString());
    }
}
