package com.github.t1.log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javassist.*;
import javassist.bytecode.*;
import lombok.extern.slf4j.Slf4j;

/** provide parameter information from {@link java.lang.Parameter jdk8} or debug information. */
@Slf4j
class Parameter {
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
        name = getJdk8ParameterName();
        log.debug("got jdk8 parameter name: {}", name);

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

    private String getJdk8ParameterName() {
        java.lang.reflect.Parameter parameter = method.getParameters()[index];
        if (!parameter.isNamePresent()) {
            log.debug("jdk8 parameter name not present on {}; you can compile with the '-parameters' option", method);
            return null;
        }
        return parameter.getName();
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

        CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName(), classPool.get(classNames(method)));
        CodeAttribute code = (CodeAttribute) ctMethod.getMethodInfo().getAttribute("Code");
        if (code == null) {
            if (!method.getDeclaringClass().isInterface())
                log.debug("no debug information available for {}", method);
            return null;
        }
        return (LocalVariableAttribute) code.getAttribute("LocalVariableTable");
    }

    private static String[] classNames(Method method) {
        String[] args = new String[method.getParameterTypes().length];
        int i = 0;
        for (Class<?> paramType : method.getParameterTypes()) {
            args[i++] = paramType.getName();
        }
        return args;
    }

    /** if the method is not static, the first local variable is "this" */
    private static int thisOffset(Method method) {
        return isStatic(method) ? 0 : 1;
    }

    private static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }
}
