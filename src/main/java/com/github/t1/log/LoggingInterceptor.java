package com.github.t1.log;

import static java.lang.Character.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.*;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.*;

import com.github.t1.stereotypes.Annotations;

@Logged
@Interceptor
@Slf4j
public class LoggingInterceptor {
    private static final LogConverter<Object> DEFAULT_CONVERTER = new LogConverter<Object>() {
        @Override
        public String convert(Object object) {
            return Objects.toString(object);
        }
    };

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
            addParamaterLogContexts();
            addLogContextVariables();

            if (logLevel.isEnabled(logger)) {
                logLevel.log(logger, message(), context.getParameters());
            }
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
                        String valueString = converter(object.getClass()).convert(object);
                        mdc.put(key, valueString);
                    }
                }
            }
        }

        private Method method() {
            return context.getMethod();
        }

        private LogConverter<Object> converter(Class<?> type) {
            LogConverter<Object> converter = converters.get(type);
            return (converter != null) ? converter : DEFAULT_CONVERTER;
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
                logLevel.log(logger, "return {}", result);
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
    Instance<LogContextVariable> variables;

    private final Map<Class<?>, LogConverter<Object>> converters = new HashMap<>();

    @Inject
    @PostConstruct
    void loadConverters(Instance<LogConverter<Object>> converterInstances) {
        for (LogConverter<Object> converter : converterInstances) {
            String converterType = converter.getClass().getName();
            log.debug("register converter {}", converterType);
            LogConverterType annotation = Annotations.on(converter.getClass()).getAnnotation(LogConverterType.class);
            if (annotation == null)
                throw new RuntimeException("converter " + converterType + " must be annotated as @"
                        + LogConverterType.class.getName());
            for (Class<?> type : annotation.value()) {
                LogConverter<Object> old = converters.put(type, converter);
                if (old != null) {
                    log.error("duplicate converters for {}: {} and {}", type, converterType, old.getClass().getName());
                }
            }
        }
    }

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
