package com.github.t1.log;

import javax.interceptor.InvocationContext;

import lombok.RequiredArgsConstructor;

import org.slf4j.MDC;

/** Takes a value from the {@link org.slf4j.MDC MDC}. */
@RequiredArgsConstructor
public class MdcLogArgument implements LogArgument {
    private final String variableName;

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
