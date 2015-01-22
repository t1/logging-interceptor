package com.github.t1.log;

import java.lang.reflect.Method;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ExpressionLogArgument implements LogArgument {
    private final String expression;

    protected Object evaluateExpressionOn(Object object) {
        if (expression != null) {
            for (String propertyName : expression.split("\\.")) {
                object = propertyValue(object, propertyName);
            }
        }
        return object;
    }

    private Object propertyValue(Object object, String propertyName) {
        String getterName = "get" + initCap(propertyName);
        try {
            Method method = object.getClass().getMethod(getterName);
            return method.invoke(object);
        } catch (ReflectiveOperationException e) {
            return "can't get " + propertyName;
        }
    }

    private String initCap(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
