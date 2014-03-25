package com.github.t1.log;

import static java.util.Arrays.*;

import java.util.*;

final class ListEnumeration<T> implements Enumeration<T> {
    private final Iterator<T> iterator;

    @SafeVarargs
    public ListEnumeration(T... elements) {
        this(asList(elements));
    }

    public ListEnumeration(List<T> list) {
        this(list.iterator());
    }

    private ListEnumeration(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    @Override
    public T nextElement() {
        return iterator.next();
    }
}