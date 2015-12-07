package com.github.t1.log;

import java.util.*;

import javax.interceptor.InvocationContext;

import org.slf4j.helpers.MessageFormatter;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
abstract class LogPoint {
    private static final String INDENT = "indent";

    static class NullLogPoint extends LogPoint {
        public NullLogPoint(LogPointContext context) {
            super(context);
        }

        @Override
        protected void logCallDo(InvocationContext context) {}

        @Override
        public void logCall(InvocationContext invocationContext) {}

        @Override
        public void logResult(Object result, long time) {}

        @Override
        public void logException(Exception e, long time) {}

        @Override
        public void done() {}
    }

    static class StandardLogPoint extends LogPoint {
        public StandardLogPoint(LogPointContext context) {
            super(context);
        }

        @Override
        protected void logCallDo(InvocationContext context) {
            level().log(logger(), message(), parameterValues(context));
        }

        private Object[] parameterValues(InvocationContext context) {
            List<Object> result = new ArrayList<>();
            for (LogArgument parameter : logArguments()) {
                result.add(parameter.value(context));
            }
            return result.toArray();
        }
    }

    static class ThrowableLogPoint extends LogPoint {
        private final LogArgument throwableParameter;

        public ThrowableLogPoint(LogPointContext context, LogArgument throwableParameter) {
            super(context);
            this.throwableParameter = throwableParameter;
        }

        @Override
        protected void logCallDo(InvocationContext invocation) {
            level().log(logger(), message(invocation), (Throwable) throwableParameter.value(invocation));
        }

        private String message(InvocationContext context) {
            List<Object> result = new ArrayList<>();
            for (LogArgument parameter : logArguments()) {
                result.add(parameter.value(context));
            }
            return MessageFormatter.arrayFormat(message(), result.toArray()).getMessage();
        }
    }

    @Delegate
    private final LogPointContext context;
    private final RestorableMdc mdc = new RestorableMdc();

    public void logCall(InvocationContext invocationContext) {
        addLogContextVariables();
        addFieldLogContextVariables(invocationContext);
        addParameterLogContexts(invocationContext);

        if (level().isEnabled(logger()) && repeatController().shouldRepeat()) {
            incrementIndentLogContext();
            logCallDo(invocationContext);
        }
    }

    private void addLogContextVariables() {
        for (LogContextVariable variable : logContextVariables()) {
            if (variable == null) // producers are allowed to return null
                continue;
            String key = variable.key();
            String value = variable.value();
            mdc.put(key, value);
        }
    }

    private void addFieldLogContextVariables(InvocationContext invocationContext) {
        for (FieldLogVariableProducer field : fieldLogContexts()) {
            String name = field.name();
            String value = field.value(invocationContext);
            mdc.put(name, value);
        }
    }

    private void addParameterLogContexts(InvocationContext context) {
        for (LogArgument parameter : logArguments()) {
            parameter.set(mdc, context);
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

    public void logResult(Object result, long time) {
        if (shouldLogResult()) {
            mdc.put("run-time", String.valueOf(time));
            if (shouldLogResultValue())
                level().log(logger(), "return {} [time:{}]", converters().convert(result), time);
            else
                level().log(logger(), "returned [time:{}]", time);
        }
    }

    public void logException(Exception e, long time) {
        level().log(logger(), "failed with {} [time:{}]", toString(e), time);
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
