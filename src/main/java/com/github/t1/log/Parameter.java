package com.github.t1.log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javassist.*;
import javassist.bytecode.*;
import lombok.extern.slf4j.Slf4j;

/** a precursor to JDK 1.8 java.lang.Parameter */
@Slf4j
class Parameter {
    private static final Method isNamePresent = jdk8ParameterMethod("isNamePresent");
    private static final Method getName = jdk8ParameterMethod("getName");

    private static Method jdk8ParameterMethod(String name) {
        try {
            Class<?> parameterClass = Class.forName("java.lang.reflect.Parameter");
            return parameterClass.getMethod(name);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            log.debug("can't access jdk8 Parameter class: " + e);
            return null;
        }
    }

    private final Method method;
    private final int index;

    private String name;

    Parameter(Method method, int index) {
        this.method = method;
        this.index = index;
    }

    public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType) {
        return getAnnotation(annotationType) != null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (final Annotation annotation : method.getParameterAnnotations()[index]) {
            if (annotationType.isInstance(annotation)) {
                return annotationType.cast(annotation);
            }
        }
        return null;
    }

    public Class<?> type() {
        return method.getParameterTypes()[index];
    }

    public int index() {
        return index;
    }

    public boolean isLast() {
        return method.getParameterTypes().length - 1 == index;
    }

    @Override
    public String toString() {
        return method + "#" + index;
    }

    public String getName() {
        if (name == null)
            initName();
        return name;
    }

    private void initName() {
        if (isJdk8()) {
            log.debug("is jdk8; try to get parameter info");
            name = getJdk8ParameterName();
            log.debug("got parameter info: {}", name);
        }
        if (name == null) {
            log.debug("try to get debug info");
            name = getDebugInfoParameterName();
            log.debug("got debug info: {}", name);
        }
        if (name == null) {
            name = "arg" + index;
            log.debug("fall back to {}", name);
        }
    }

    private boolean isJdk8() {
        return isNamePresent != null;
    }

    private String getJdk8ParameterName() {
        try {
            Object parameter = parameter();
            if (!((boolean) isNamePresent.invoke(parameter))) {
                log.debug("jdk8 parameter name not present on {}; you can compile with the '-parameters' option",
                        method);
                return null;
            }
            return (String) getName.invoke(parameter);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private Object parameter() throws ReflectiveOperationException {
        Method getParameters = Method.class.getMethod("getParameters");
        Object[] parameters = (Object[]) getParameters.invoke(method);
        return parameters[index];
    }

    private String getDebugInfoParameterName() {
        try {
            LocalVariableAttribute localVariables = getLocalVariableTable(method);
            if (localVariables == null)
                return null;
            return localVariables.variableName(index + thisOffset(method));
        } catch (NotFoundException e) {
            log.debug("can't load debug info for parameter", e);
            return null;
        }
    }

    private static LocalVariableAttribute getLocalVariableTable(Method method) throws NotFoundException {
        ClassPool classPool = new ClassPool(true);
        classPool.insertClassPath(new LoaderClassPath(method.getDeclaringClass().getClassLoader()));
        CtClass ctClass = classPool.get(method.getDeclaringClass().getName());

        CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName());
        CodeAttribute code = (CodeAttribute) ctMethod.getMethodInfo().getAttribute("Code");
        // TODO if it's not an interface: log a warning: missing debug information; once per jar only!
        if (code == null)
            return null;
        return (LocalVariableAttribute) code.getAttribute("LocalVariableTable");
    }

    /** if the method is not static, the first local variable is "this" */
    private static int thisOffset(Method method) {
        return (Modifier.isStatic(method.getModifiers())) ? 0 : 1;
    }
}
