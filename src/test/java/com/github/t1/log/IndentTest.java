package com.github.t1.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndentTest {
    @Test void shouldIndent0() {
        assertEquals("", Indent.of(0));
    }

    @Test void shouldIndent1() {
        assertEquals("  ", Indent.of(1));
    }

    @Test void shouldIndent2() {
        assertEquals("    ", Indent.of(2));
    }

    @Test void shouldIndent3() {
        assertEquals("      ", Indent.of(3));
    }
}
