package com.github.t1.log;

import static org.junit.Assert.*;

import java.net.*;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.junit.*;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.Invocation;
import org.slf4j.*;

public class VersionLogContextVariableProducerTest {
    private final Logger log = LoggerFactory.getLogger(VersionLogContextVariableProducer.class);

    @After
    public void printLogs() {
        for (Invocation invocation : new MockUtil().getMockHandler(log).getInvocationContainer().getInvocations()) {
            System.out.println(invocation);
        }
    }

    private VersionLogContextVariableProducer createVersionProducer() {
        return createVersionProducer(testFileManifestUrl("spec-version"));
    }

    private URL url(String string) {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private VersionLogContextVariableProducer createVersionProducer(final URL url) {
        return new VersionLogContextVariableProducer() {
            @Override
            Enumeration<URL> manifests(ClassLoader classLoader) {
                return new ListEnumeration<>(url);
            }

            @Override
            Pattern mainManifestPattern() {
                return Pattern.compile("file:(.*).(war|jar|ear)/META-INF/MANIFEST.MF");
            }
        };
    }

    private URL testFileManifestUrl(String app) {
        return url("file:src/test/resources/" + app + ".war/META-INF/MANIFEST.MF");
    }

    @Test
    public void shouldFindApplicationName() {
        VersionLogContextVariableProducer producer = createVersionProducer();

        assertEquals("src/test/resources/spec-version", producer.app().getValue());
    }

    @Test
    public void shouldFindVersion() {
        VersionLogContextVariableProducer producer = createVersionProducer();

        assertEquals("1.2.3", producer.version().getValue());
    }

    @Test
    public void shouldIgnoreMissingManifest() {
        createVersionProducer(url("file:does/not/exist.war/META-INF/MANIFEST.MF"));
    }

    @Test
    public void shouldIgnoreNonMatchingManifest() {
        createVersionProducer(url("mailto:no@where.com"));
    }

    @Test
    public void shouldIgnoreEmptyManifest() {
        createVersionProducer(testFileManifestUrl("empty-manifest"));
    }

    @Test
    public void shouldFindSpecVersionManifest() {
        VersionLogContextVariableProducer producer = createVersionProducer(testFileManifestUrl("impl-version"));

        assertEquals("1.2.4", producer.version().getValue());
    }
}
