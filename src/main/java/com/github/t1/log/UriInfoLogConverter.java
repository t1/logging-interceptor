package com.github.t1.log;

import javax.ws.rs.core.UriInfo;

@ConverterType(UriInfo.class)
public class UriInfoLogConverter implements Converter {
    @Override
    public String convert(Object o) {
        UriInfo info = (UriInfo) o;
        return info.getRequestUri().toASCIIString();
    }
}
