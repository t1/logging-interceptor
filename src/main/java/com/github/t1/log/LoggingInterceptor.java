package com.github.t1.log;

import com.github.t1.stereotypes.Annotations;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.interceptor.*;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static jakarta.interceptor.Interceptor.Priority.*;

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
        return around(context);
    }

    @AroundTimeout
    Object aroundTimeout(InvocationContext context) throws Exception {
        return around(context);
    }

    private Object around(InvocationContext context) throws Exception {
        LogPoint logPoint = logPoint(context.getMethod());

        logPoint.logCall(context);

        long t0 = System.currentTimeMillis();
        try {
            Object result = context.proceed();
            logPoint.logResult(result, System.currentTimeMillis() - t0);
            return result;
        } catch (Exception e) {
            logPoint.logException(e, System.currentTimeMillis() - t0);
            throw e;
        } finally {
            logPoint.done();
        }
    }

    private LogPoint logPoint(Method method) {
        LogPoint logPoint = CACHE.get(method);
        if (logPoint == null) {
            try {
                Logged logged = Annotations.on(method).getAnnotation(Logged.class);
                LogPointContext context = new LogPointContext(variables, converters);
                logPoint = new LogPointBuilder(method, logged, context).build();
            } catch (RuntimeException e) {
                throw new RuntimeException("can't build log point for " + method, e);
            }
            CACHE.put(method, logPoint);
        }
        return logPoint;
    }
}
