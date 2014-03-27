package com.github.t1.log;

import static java.lang.Character.*;
import static javax.interceptor.Interceptor.Priority.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.*;

import org.slf4j.*;

import com.github.t1.stereotypes.Annotations;

@Logged
@Dependent
@Interceptor
@Priority(LIBRARY_BEFORE)
public class LoggingInterceptor {
    private class Logging {
        private final InvocationContext context;

        private final LogLevel logLevel;
        private final Logger logger;
        private final String logMessage;

        private final RestorableMdc mdc = new RestorableMdc();

        public Logging(InvocationContext context) {
            this.context = context;

            Logged loggedAnnotation = Annotations.on(context.getMethod()).getAnnotation(Logged.class);
            this.logLevel = loggedAnnotation.level();
            this.logMessage = loggedAnnotation.value();
            this.logger = getLogger(resolveLogger(loggedAnnotation.logger()));
        }

        private Class<?> resolveLogger(Class<?> loggerType) {
            if (loggerType == void.class) {
                // the method is declared in the target type, while context.getTarget() is the CDI proxy
                loggerType = context.getMethod().getDeclaringClass();
                while (loggerType.getEnclosingClass() != null) {
                    loggerType = loggerType.getEnclosingClass();
                }
            }
            return loggerType;
        }

        public void logCall() {
            // System.out.println("--------------- log call do");
            addParamaterLogContexts();
            addLogContextVariables();

            // System.out.println("logger " + logger.getName());
            // System.out.println("level " + logLevel + ": " + logLevel.isEnabled(logger));
            if (logLevel.isEnabled(logger)) {
                // System.out.println("message " + logMessage + ": " + message());
                logLevel.log(logger, message(), parameters());
            }
            // System.out.println("--------------- log call done");
        }

        private Object[] parameters() {
            List<Object> result = new ArrayList<>();
            for (Object parameter : context.getParameters()) {
                result.add(converters.convert(parameter));
            }
            return result.toArray();
        }

        private void addParamaterLogContexts() {
            Annotation[][] parameterAnnotations = method().getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                for (Annotation annotation : annotations) {
                    if (annotation instanceof LogContext) {
                        LogContext logContext = (LogContext) annotation;
                        String key = logContext.value();
                        Object object = context.getParameters()[i];
                        Object converted = converters.convert(object);
                        if (converted != null) {
                            mdc.put(key, converted.toString());
                        }
                    }
                }
            }
        }

        private Method method() {
            return context.getMethod();
        }

        private void addLogContextVariables() {
            for (LogContextVariable variable : variables) {
                if (variable == null) // producers are allowed to return null
                    continue;
                String key = variable.getKey();
                String value = variable.getValue();
                mdc.put(key, value);
            }
        }

        private String message() {
            if ("".equals(logMessage)) {
                return camelToSpaces(method().getName())
                        + messageParamPlaceholders(method().getParameterTypes().length);
            } else {
                return logMessage;
            }
        }

        private String camelToSpaces(String string) {
            StringBuilder out = new StringBuilder();
            for (Character c : string.toCharArray()) {
                if (isUpperCase(c)) {
                    out.append(' ');
                    out.append(toLowerCase(c));
                } else {
                    out.append(c);
                }
            }
            return out.toString();
        }

        private String messageParamPlaceholders(int length) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < length; i++)
                out.append(" {}");
            return out.toString();
        }

        public void logResult(Object result) {
            if (method().getReturnType() != void.class) {
                logLevel.log(logger, "return {}", converters.convert(result));
            }
        }

        public void logException(Exception e) {
            logLevel.log(logger, "failed", e);
        }

        public void done() {
            mdc.restore();
        }
    }

    @Inject
    private Instance<LogContextVariable> variables;
    @Inject
    private Converters converters;

    @AroundInvoke
    Object aroundInvoke(InvocationContext context) throws Exception {
        Logging logging = new Logging(context);

        logging.logCall();

        try {
            Object result = context.proceed();
            logging.logResult(result);
            return result;
        } catch (Exception e) {
            logging.logException(e);
            throw e;
        } finally {
            logging.done();
        }
    }

    // @VisibleForTesting
    protected Logger getLogger(Class<?> loggerType) {
        return LoggerFactory.getLogger(loggerType);
    }
}
