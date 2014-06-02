package com.github.t1.log;

import java.util.*;

import javax.enterprise.inject.Instance;
import javax.interceptor.InvocationContext;

import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

@AllArgsConstructor
abstract class LogPoint {
    private static final String INDENT = "indent";

    static class StandardLogPoint extends LogPoint {
        private final String message;

        public StandardLogPoint(Logger logger, LogLevel level, String message, List<LogParameter> parameters,
                Instance<LogContextVariable> variables, boolean logResult, Converters converters) {
            super(logger, level, parameters, variables, logResult, converters);
            this.message = message;
        }

        @Override
        protected void logCallDo(InvocationContext context) {
            level.log(logger, message, parameterValues(context));
        }

        private Object[] parameterValues(InvocationContext context) {
            List<Object> result = new ArrayList<>();
            for (LogParameter parameter : parameters) {
                result.add(parameter.value(context));
            }
            return result.toArray();
        }
    }

    static class ThrowableLogPoint extends LogPoint {
        private final String message;
        private final List<LogParameter> parameters;
        private final LogParameter throwableParameter;

        public ThrowableLogPoint(Logger logger, LogLevel level, String message, List<LogParameter> parameters,
                LogParameter throwableParameter, Instance<LogContextVariable> variables, boolean logResult,
                Converters converters) {
            super(logger, level, parameters, variables, logResult, converters);
            this.message = message;
            this.parameters = parameters;
            this.throwableParameter = throwableParameter;
        }

        @Override
        protected void logCallDo(InvocationContext context) {
            level.log(logger, message(context), (Throwable) throwableParameter.value(context));
        }

        private String message(InvocationContext context) {
            List<Object> result = new ArrayList<>();
            for (LogParameter parameter : parameters) {
                result.add(parameter.value(context));
            }
            return MessageFormatter.arrayFormat(message, result.toArray()).getMessage();
        }
    }

    protected final Logger logger;
    protected final LogLevel level;
    protected final List<LogParameter> parameters;

    private final Instance<LogContextVariable> variables;
    private final boolean logResult;
    private final Converters converters;

    private final RestorableMdc mdc = new RestorableMdc();

    public void logCall(InvocationContext context) {
        addParameterLogContexts(context);
        addLogContextVariables();

        if (level.isEnabled(logger)) {
            incrementIndentLogContext();
            logCallDo(context);
        }
    }

    private void addParameterLogContexts(InvocationContext context) {
        for (LogParameter parameter : parameters) {
            parameter.set(mdc, context);
        }
    }

    private void addLogContextVariables() {
        for (LogContextVariable variable : variables) {
            if (variable == null) // producers are allowed to return null
                continue;
            String key = variable.key();
            String value = variable.value();
            mdc.put(key, value);
        }
    }

    private void incrementIndentLogContext() {
        int indent = getIndentLogContext();
        setIndentLogContext(indent + 1);
    }

    private int getIndentLogContext() {
        String indent = mdc.get(INDENT);
        return (indent == null) ? -1 : indent.length() / 2;
    }

    private void setIndentLogContext(int indent) {
        mdc.put(INDENT, Indent.of(indent));
    }

    protected abstract void logCallDo(InvocationContext context);

    public void logResult(Object result) {
        if (logResult) {
            level.log(logger, "return {}", converters.convert(result));
        }
    }

    public void logException(Exception e) {
        level.log(logger, "failed with {}", toString(e));
    }

    private String toString(Exception e) {
        StringBuilder out = new StringBuilder();
        toString(out, e);
        return out.toString();
    }

    private void toString(StringBuilder out, Throwable e) {
        out.append(e.getClass().getSimpleName());
        if (e.getMessage() != null) {
            out.append("(").append(e.getMessage()).append(")");
        }
        if (e.getCause() != null) {
            out.append(" -> ");
            toString(out, e.getCause());
        }
    }

    public void done() {
        mdc.restore();
    }
}
