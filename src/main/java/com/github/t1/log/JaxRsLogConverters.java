package com.github.t1.log;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.StatusType;
import java.util.List;

public class JaxRsLogConverters implements Converter {
    public String convert(UriInfo info) {
        return info.getRequestUri().toString();
    }

    public String convert(Response response) {
        StatusType statusInfo = response.getStatusInfo();
        return statusInfo.getStatusCode() + " " + statusInfo.getReasonPhrase() + entityInfo(response);
    }

    private String entityInfo(Response response) {
        Object entity = response.getEntity();
        if (entity == null)
            return "";
        return ": " + entity.getClass().getSimpleName();
    }

    private static class Printer {
        private final StringBuilder out = new StringBuilder();

        public Printer print(MultivaluedMap<String, ?> map) {
            out.append("{");
            boolean first = true;
            for (String key : map.keySet()) {
                if (first)
                    first = false;
                else
                    out.append(", ");
                out.append(key).append(":");
                printValues(map.get(key));
            }
            out.append("}");
            return this;
        }

        private void printValues(List<?> values) {
            if (values.size() > 1) {
                out.append("[");
            }
            for (Object value : values) {
                out.append(value);
            }
            if (values.size() > 1) {
                out.append("]");
            }
        }

        @Override
        public String toString() {
            return out.toString();
        }
    }

    public String convert(MultivaluedMap<String, ?> map) {
        return new Printer().print(map).toString();
    }
}
