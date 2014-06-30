package com.github.t1.log;

import javax.interceptor.InvocationContext;

import org.slf4j.MDC;

public class MdcLogParameter implements LogParameter {

    private final String variableName;

    public MdcLogParameter(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String name() {
        return variableName;
    }

    @Override
    public Object value(InvocationContext context) {
        String value = MDC.get(variableName);
        return (value == null) //
                ? "unset mdc log parameter reference (and not a parameter name): " + variableName //
                : value;
    }

    @Override
    public void set(RestorableMdc mdc, InvocationContext context) {}
}
