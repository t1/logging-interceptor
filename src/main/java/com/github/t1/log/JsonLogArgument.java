package com.github.t1.log;

import static com.github.t1.log.JsonLogDetail.*;

import java.util.*;

import javax.interceptor.InvocationContext;

import lombok.Value;

import org.joda.time.LocalDateTime;
import org.slf4j.*;

/**
 * Produces a JSON string, using other {@link LogArgument}s.
 * 
 * @see JsonLogDetail
 */
@Value
public class JsonLogArgument implements LogArgument {
    private static class JsonBuilder {
        private final Map<String, Object> map = new HashMap<>();

        public void set(String key, Object value) {
            if (value == null)
                return;
            map.put(key, value);
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (out.length() != 0)
                    out.append(",");
                out.append("\"").append(entry.getKey()).append("\":");
                appendJson(out, entry.getValue());
            }
            return out.toString();
        }

        private void appendJson(StringBuilder out, Object value) {
            if (isJsonValue(value)) {
                out.append(value);
            } else {
                appendString(out, value);
            }
        }

        private boolean isJsonValue(Object value) {
            return value instanceof Boolean || value instanceof Number;
        }

        private void appendString(StringBuilder out, Object value) {
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

    private final List<JsonLogDetail> jsonLogDetail;
    private final List<LogArgument> parameters;
    private final Converters converters;
    private final Logger logger;
    private final LogLevel level;

    @Override
    public String name() {
        return "json";
    }

    @Override
    public Object value(InvocationContext context) {
        return null;
    }

    @Override
    public void set(RestorableMdc mdc, InvocationContext context) {
        Object value = mdcValue(context);
        mdc.put(name(), value.toString());
    }

    private String mdcValue(InvocationContext context) {
        JsonBuilder out = new JsonBuilder();

        if (isJsonLogDetail(EVENT)) {
            out.set("timestamp", LocalDateTime.now());
            out.set("event", context.getMethod().getName());
            out.set("logger", logger.getName());
            out.set("level", level.name().toLowerCase());
        }

        if (isJsonLogDetail(CONTEXT))
            addMdc(out);
        if (isJsonLogDetail(PARAMETERS))
            addMethodParams(context, out);

        return out.toString();
    }

    private boolean isJsonLogDetail(JsonLogDetail detail) {
        return jsonLogDetail.contains(ALL) || jsonLogDetail.contains(detail);
    }

    private void addMethodParams(InvocationContext context, JsonBuilder out) {
        for (LogArgument parameter : parameters) {
            if (this == parameter)
                continue;
            String name = parameter.name();
            Object value = convert(out, name, parameter.value(context));
            out.set(name, value);
        }
    }

    private Object convert(JsonBuilder out, String name, Object value) {
        value = converters.convert(value);
        if (value instanceof Throwable)
            out.set(name + "-stacktrace", Arrays.toString(((Throwable) value).getStackTrace()));
        return value;
    }

    private void addMdc(JsonBuilder out) {
        Set<String> keys = MDC.getCopyOfContextMap().keySet();
        for (String key : keys) {
            out.set(key, MDC.get(key));
        }
    }
}
