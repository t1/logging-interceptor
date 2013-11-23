package com.github.t1.log;

import static java.util.jar.Attributes.Name.*;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.*;
import java.util.regex.*;

import javax.enterprise.inject.Produces;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VersionLogContextVariableProducer {
    private static final Pattern MAIN_MANIFEST = Pattern
            .compile("vfs:/content/(.*).(war|jar|ear)/META-INF/MANIFEST.MF");

    private static String version;
    private static String app;

    static {
        version = null;
        log.trace("scanning for version in manifest");
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            scanManifests(classLoader);
        } catch (RuntimeException | IOException e) {
            log.debug("can't read manifest", e);
        }
    }

    private static void scanManifests(ClassLoader classLoader) throws IOException {
        Enumeration<URL> resources = classLoader.getResources("/META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            URL manifestUrl = resources.nextElement();
            log.trace("matching {}", manifestUrl);
            Matcher matcher = MAIN_MANIFEST.matcher(manifestUrl.toString());
            if (matcher.matches()) {
                log.trace("found manifest at {}", manifestUrl);
                app = matcher.group(1);
                version = readManifest(manifestUrl);
                log.trace("app={} version={}", app, version);
                return;
            }
        }
        log.debug("no manifest found");
    }

    private static String readManifest(URL manifestUrl) throws IOException {
        try (InputStream is = manifestUrl.openStream()) {
            Manifest manifest = new Manifest(is);
            Attributes attributes = manifest.getMainAttributes();
            String value = attributes.getValue(IMPLEMENTATION_VERSION);
            if (value != null)
                return value;
            value = attributes.getValue(SPECIFICATION_VERSION);
            if (value != null)
                return value;
            log.debug("no version found in {}", manifestUrl);
            return null;
        }
    }

    @Produces
    public LogContextVariable version() {
        return (version == null) ? null : new LogContextVariable("version", version);
    }

    @Produces
    public LogContextVariable app() {
        return (version == null) ? null : new LogContextVariable("app", app);
    }
}
