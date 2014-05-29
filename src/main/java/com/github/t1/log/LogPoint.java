package com.github.t1.log;

import java.util.*;

import javax.enterprise.inject.Instance;
import javax.interceptor.InvocationContext;

import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

@AllArgsConstructor
class LogPoint {
    private static final String INDENT = "indent";

    private final Logger logger;
    private final LogLevel level;
    private final String message;
    private final List<LogParameter> parameters;
    private final Instance<LogContextVariable> variables;
    private final boolean logResult;
    private final Converters converters;

    private final RestorableMdc mdc = new RestorableMdc();

    public void logCall(InvocationContext context) {
        addParamaterLogContexts(context);
        addLogContextVariables();

        if (level.isEnabled(logger)) {
            incrementIndentLogContext();
            if (isLogThrowable()) {
                String message = throwableMessage(context);
                level.log(logger, message, (Throwable) lastParameter().value(context));
            } else {
                level.log(logger, message, parameterValues(context));
            }
        }
    }

    private void addParamaterLogContexts(InvocationContext context) {
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

    private boolean isLogThrowable() {
        return !parameters.isEmpty() && lastParameter().isThrowable();
    }

    private LogParameter lastParameter() {
        return parameters.get(parameters.size() - 1);
    }

    private String throwableMessage(InvocationContext context) {
        return MessageFormatter.arrayFormat(message, parameterValues(context)).getMessage();
    }

    private Object[] parameterValues(InvocationContext context) {
        List<Object> result = new ArrayList<>();
        for (LogParameter parameter : parameters) {
            result.add(parameter.value(context));
        }
        return result.toArray();
    }

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
