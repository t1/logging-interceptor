package com.github.t1.log;

import static javax.interceptor.Interceptor.Priority.*;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.*;

@Logged
@Dependent
@Interceptor
@Priority(LIBRARY_BEFORE)
public class LoggingInterceptor {
    @Inject
    private Instance<LogContextVariable> variables;
    @Inject
    private Converters converters;

    static final Map<Method, LogPoint> CACHE = new ConcurrentHashMap<>();

    @AroundInvoke
    Object aroundInvoke(InvocationContext context) throws Exception {
        LogPoint logPoint = logPoint(context.getMethod());

        logPoint.logCall(context);

        try {
            Object result = context.proceed();
            logPoint.logResult(result);
            return result;
        } catch (Exception e) {
            logPoint.logException(e);
            throw e;
        } finally {
            logPoint.done();
        }
    }

    private LogPoint logPoint(Method method) {
        LogPoint logPoint = CACHE.get(method);
        if (logPoint == null) {
            logPoint = new LogPointBuilder(method, variables, converters).build();
            CACHE.put(method, logPoint);
        }
        return logPoint;
    }
}
