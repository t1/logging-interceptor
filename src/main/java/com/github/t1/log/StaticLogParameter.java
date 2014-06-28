package com.github.t1.log;

import javax.interceptor.InvocationContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StaticLogParameter implements LogParameter {

    private final String name;
    private final String value;

    @Override
    public String name() {
        return name;
    }

    @Override
    public Object value(InvocationContext context) {
        return value;
    }

    @Override
    public void set(RestorableMdc mdc, InvocationContext context) {
        mdc.put(name, value);
    }
}
