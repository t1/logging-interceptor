package com.github.t1.log;

import javax.interceptor.InvocationContext;

interface LogParameter {
    public String name();

    public Object value(InvocationContext context);

    public void set(RestorableMdc mdc, InvocationContext context);
}
