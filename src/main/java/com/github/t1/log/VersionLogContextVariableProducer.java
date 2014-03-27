package com.github.t1.log;

import static java.util.jar.Attributes.Name.*;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.*;
import java.util.regex.*;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class VersionLogContextVariableProducer {
    private static final Pattern MAIN_MANIFEST = Pattern
            .compile("vfs:/content/(.*).(war|jar|ear)/META-INF/MANIFEST.MF");

    private String version;
    private String app;

    public VersionLogContextVariableProducer() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            scanManifests(classLoader);
        } catch (RuntimeException | IOException e) {
            log.debug("can't read manifest", e);
        }
    }

    private void scanManifests(ClassLoader classLoader) throws IOException {
        log.trace("scanning for manifest");
        Enumeration<URL> resources = manifests(classLoader);
        while (resources.hasMoreElements()) {
            URL manifestUrl = resources.nextElement();
            log.trace("matching {}", manifestUrl);
            Matcher matcher = mainManifestPattern().matcher(manifestUrl.toString());
            if (!matcher.matches()) {
                log.trace("manifest url not matching {}", mainManifestPattern());
                continue;
            }
            log.trace("found manifest at {}", manifestUrl);
            app = matcher.group(1);
            version = readManifest(manifestUrl);
            log.debug("app={} version={}", app, version);
            return;
        }
        log.debug("no manifest found");
    }

    // @VisibleForTesting
    Enumeration<URL> manifests(ClassLoader classLoader) throws IOException {
        return classLoader.getResources("/META-INF/MANIFEST.MF");
    }

    // @VisibleForTesting
    Pattern mainManifestPattern() {
        return MAIN_MANIFEST;
    }

    private String readManifest(URL manifestUrl) throws IOException {
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
        return (app == null) ? null : new LogContextVariable("app", app);
    }
}
