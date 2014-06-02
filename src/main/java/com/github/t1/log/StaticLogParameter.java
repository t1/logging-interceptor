package com.github.t1.log;

import javax.interceptor.InvocationContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StaticLogParameter implements LogParameter {

    private final String value;

    @Override
    public void set(RestorableMdc mdc, InvocationContext context) {}

    @Override
    public Object value(InvocationContext context) {
        return value;
    }

    @Override
    public String defaultParamPlaceholder() {
        return null;
    }

    @Override
    public boolean isLastThrowable() {
        return false;
    }

    @Override
    public boolean isThrowable() {
        return false;
    }
}
