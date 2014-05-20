package com.github.t1.log;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.*;
import java.util.regex.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.*;

@RunWith(MockitoJUnitRunner.class)
public class VersionLogContextVariableProducerTest {
    private final Logger log = LoggerFactory.getLogger(VersionLogContextVariableProducer.class);

    private static URL url(String string) {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void printLogs() {
        for (Invocation invocation : new MockUtil().getMockHandler(log).getInvocationContainer().getInvocations()) {
            System.out.println(invocation);
        }
    }

    @Mock
    private ManifestFinder finder;

    @InjectMocks
    private VersionLogContextVariableProducer producer;

    private void givenManifestsAt(URL... urls) throws IOException {
        when(finder.matcher(anyString())).thenAnswer(new Answer<Matcher>() {
            @Override
            public Matcher answer(InvocationOnMock invocation) {
                String string = (String) invocation.getArguments()[0];
                return Pattern.compile("file:.*/(?<path>.*).(?<type>war|jar|ear)/META-INF/MANIFEST.MF").matcher(string);
            }
        });
        when(finder.manifests()).thenReturn(asList(urls));
    }

    private URL testFileManifestUrl(String app) {
        return url("file:src/test/resources/" + app + ".war/META-INF/MANIFEST.MF");
    }

    @Test
    public void shouldFindApplicationName() throws IOException {
        givenManifestsAt(testFileManifestUrl("spec-version"));

        producer.scan();

        assertEquals("spec-version", producer.app().value());
    }

    @Test
    public void shouldFindVersion() throws IOException {
        givenManifestsAt(testFileManifestUrl("spec-version"));

        producer.scan();

        assertEquals("1.2.3", producer.version().value());
    }

    @Test
    public void shouldIgnoreMissingManifest() throws IOException {
        givenManifestsAt(url("file:does/not/exist.war/META-INF/MANIFEST.MF"));

        producer.scan();

        assertEquals("exist", producer.app().value());
        assertNull(producer.version());
    }

    @Test
    public void shouldIgnoreNonMatchingManifest() throws IOException {
        givenManifestsAt(url("mailto:no@where.com"));

        producer.scan();

        assertNull(producer.app());
        assertNull(producer.version());
    }

    @Test
    public void shouldIgnoreEmptyManifest() throws IOException {
        givenManifestsAt(testFileManifestUrl("empty-manifest"));

        producer.scan();

        assertEquals("empty-manifest", producer.app().value());
        assertNull(producer.version());
    }

    @Test
    public void shouldFindSpecVersionManifest() throws IOException {
        givenManifestsAt(testFileManifestUrl("impl-version"));

        producer.scan();

        assertEquals("1.2.4", producer.version().value());
    }
}
