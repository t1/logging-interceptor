package com.github.t1.log;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.slf4j.impl.StaticMDCBinder.*;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.junit.*;
import org.mockito.InOrder;

public class LogContextTest extends AbstractLoggedTest {
    @Before
    public void resetMdc() {
        reset(mdc());
    }

    @Test
    public void shouldLogContextParameter() throws Exception {
        class Container {
            @Logged
            @SuppressWarnings("unused")
            public void methodWithLogContextParameter(@LogContext("var") String one, @Deprecated String two) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "foo", "bar");

        interceptor.aroundInvoke(context);

        verifyMdc("var", "foo");
    }

    @Test
    public void shouldLogTwoDifferentContextParameters() throws Exception {
        class Container {
            @Logged
            @SuppressWarnings("unused")
            public void methodWithLogContextParameter(@LogContext("var1") String one, @LogContext("var2") String two) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "foo", "bar");

        interceptor.aroundInvoke(context);

        verifyMdc("var1", "foo");
        verifyMdc("var2", "bar");
    }

    @Test
    public void shouldConcatenateTwoContextParametersWithTheSameName() throws Exception {
        class Container {
            @Logged
            @SuppressWarnings("unused")
            public void methodWithLogContextParameter(@LogContext("var") String one, @LogContext("var") String two) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "foo", "bar");

        interceptor.aroundInvoke(context);

        verifyMdc("var", "foo bar");
    }

    @Test
    public void shouldRestoreMdcValue() throws Exception {
        when(mdc().get("foo")).thenReturn("oldvalue");
        class Container {
            @Logged
            public void methodWithLogContextParameter(@SuppressWarnings("unused") @LogContext("foo") String foo) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "newvalue");

        interceptor.aroundInvoke(context);

        InOrder inOrder = inOrder(mdc());
        inOrder.verify(mdc()).put("foo", "newvalue");
        inOrder.verify(mdc()).put("foo", "oldvalue");
    }

    @Test
    public void shouldRestoreNullMdcValue() throws Exception {
        class Container {
            @Logged
            @SuppressWarnings("unused")
            public void methodWithLogContextParameter(@LogContext("var") String one, String two) {}
        }
        whenMethod(new Container(), "methodWithLogContextParameter", "foo", "bar");

        interceptor.aroundInvoke(context);
        verify(mdc()).remove("var");
    }

    @Test
    public void shouldAddLogContextVariable() throws Exception {
        class Container {
            @Logged
            public void foo() {}
        }
        whenMethod(new Container(), "foo");

        LogContextVariable variable = new LogContextVariable("foo", "baz");
        when(variables.iterator()).thenReturn(asList(variable).iterator());

        interceptor.aroundInvoke(context);

        verify(logger).debug("foo", new Object[0]);
        verifyMdc("foo", "baz");
    }

    @Test
    public void shouldSkipNullLogContextVariable() throws Exception {
        class Container {
            @Logged
            public void foo() {}
        }
        whenMethod(new Container(), "foo");

        when(variables.iterator()).thenReturn(asList((LogContextVariable) null).iterator());

        interceptor.aroundInvoke(context);

        verify(logger).debug("foo", new Object[0]);
    }

    @Test
    public void shouldProduceVersionLogContextVariable() {
        VersionLogContextVariableProducer producer = new VersionLogContextVariableProducer() {
            @Override
            Enumeration<URL> manifests(ClassLoader classLoader) throws IOException {
                return new ListEnumeration<>(new URL("file:target/test-classes/TEST-MANIFEST.MF"));
            }

            @Override
            Pattern mainManifestPattern() {
                return Pattern.compile("file:.*/(.*)");
            }
        };

        assertNotNull(producer.app());
        assertEquals("TEST-MANIFEST.MF", producer.app().getValue());
        assertNotNull(producer.version());
        assertEquals("1.0", producer.version().getValue());
    }
}
