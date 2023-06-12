package com.github.t1.log;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.MockUtil.getMockHandler;

@ExtendWith(MockitoExtension.class)
class VersionLogContextVariableProducerTest {
    private final Logger log = LoggerFactory.getLogger(VersionLogContextVariableProducer.class);

    private static URL url(String string) {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void printLogs() {
        ((InvocationContainerImpl) getMockHandler(log).getInvocationContainer()).getInvocations().forEach(System.out::println);
    }

    @Mock
    private ManifestFinder finder;

    @InjectMocks
    private VersionLogContextVariableProducer producer;

    private void givenManifestsAt(URL... urls) throws IOException {
        when(finder.matcher(anyString())).thenAnswer(invocation -> {
            String string = (String) invocation.getArguments()[0];
            return Pattern.compile("file:.*/(?<path>.*).(?<type>war|jar|ear)/META-INF/MANIFEST.MF").matcher(string);
        });
        when(finder.manifests()).thenReturn(asList(urls));
    }

    private URL testFileManifestUrl(String app) {
        return url("file:src/test/resources/" + app + ".war/META-INF/MANIFEST.MF");
    }

    @Test void shouldFindApplicationName() throws IOException {
        givenManifestsAt(testFileManifestUrl("spec-version"));

        producer.scan();

        assertEquals("spec-version", producer.app().value());
    }

    @Test void shouldFindVersion() throws IOException {
        givenManifestsAt(testFileManifestUrl("spec-version"));

        producer.scan();

        assertEquals("1.2.3", producer.version().value());
    }

    @Test void shouldIgnoreMissingManifest() throws IOException {
        givenManifestsAt(url("file:does/not/exist.war/META-INF/MANIFEST.MF"));

        producer.scan();

        assertEquals("exist", producer.app().value());
        assertNull(producer.version());
    }

    @Test void shouldIgnoreNonMatchingManifest() throws IOException {
        givenManifestsAt(url("mailto:no@where.com"));

        producer.scan();

        assertNull(producer.app());
        assertNull(producer.version());
    }

    @Test void shouldIgnoreEmptyManifest() throws IOException {
        givenManifestsAt(testFileManifestUrl("empty-manifest"));

        producer.scan();

        assertEquals("empty-manifest", producer.app().value());
        assertNull(producer.version());
    }

    @Test void shouldFindSpecVersionManifest() throws IOException {
        givenManifestsAt(testFileManifestUrl("impl-version"));

        producer.scan();

        assertEquals("1.2.4", producer.version().value());
    }
}
