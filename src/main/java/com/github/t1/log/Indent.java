package com.github.t1.log;

import java.util.*;

public class Indent {
    private static final List<String> INDENT_STRINGS = new ArrayList<>();

    private static String spaces(int length) {
        char[] chars = new char[length];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }

    public static String of(int indent) {
        for (int i = INDENT_STRINGS.size(); i <= indent; i++)
            INDENT_STRINGS.add(spaces(i * 2));
        return INDENT_STRINGS.get(indent);
    }
}
