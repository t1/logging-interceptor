package com.github.t1.log;

import javax.interceptor.InvocationContext;

interface LogParameter {
    public void set(RestorableMdc mdc, InvocationContext context);

    public Object value(InvocationContext context);

    public String defaultParamPlaceholder();

    public boolean isLastThrowable();

    public boolean isThrowable();
}
