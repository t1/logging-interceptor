package com.github.t1.log;

public interface LogContextConverter<T> {
    String convert(T object);
}
