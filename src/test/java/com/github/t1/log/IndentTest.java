package com.github.t1.log;

import static org.junit.Assert.*;

import org.junit.Test;

public class IndentTest {
    @Test
    public void shouldIndent0() {
        assertEquals("", Indent.of(0));
    }

    @Test
    public void shouldIndent1() {
        assertEquals("  ", Indent.of(1));
    }

    @Test
    public void shouldIndent2() {
        assertEquals("    ", Indent.of(2));
    }

    @Test
    public void shouldIndent3() {
        assertEquals("      ", Indent.of(3));
    }
}
