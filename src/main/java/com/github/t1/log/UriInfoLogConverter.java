package com.github.t1.log;

import javax.ws.rs.core.UriInfo;

public class UriInfoLogConverter implements Converter {
    public String convert(UriInfo info) {
        return info.getRequestUri().toASCIIString();
    }
}
