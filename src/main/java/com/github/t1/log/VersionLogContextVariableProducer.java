package com.github.t1.log;

import static java.util.jar.Attributes.Name.*;

import java.io.*;
import java.net.URL;
import java.util.jar.*;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class VersionLogContextVariableProducer {
    @Inject
    private ManifestFinder manifestFinder;

    private String version;
    private String app;

    @PostConstruct
    public void scan() {
        log.debug("scan for manifests");
        try {
            scanManifests();
        } catch (RuntimeException | IOException e) {
            log.debug("can't read manifest", e);
        }
    }

    private void scanManifests() throws IOException {
        log.trace("scanning for manifest");
        for (URL manifestUrl : manifestFinder.manifests()) {
            log.trace("matching {}", manifestUrl);
            Matcher matcher = manifestFinder.matcher(manifestUrl.toString());
            if (matcher.matches()) {
                String path = matcher.group("path");
                log.trace("found potential match of type {} at {}", matcher.group("type"), path);
                if (!isNested(path)) {
                    log.trace("found non-nested manifest at {}", manifestUrl);
                    app = matcher.group("path");
                    version = readManifest(manifestUrl);
                    log.debug("app={} version={}", app, version);
                    return;
                }
            }
        }
        log.debug("no manifest found");
    }

    private boolean isNested(String path) {
        return path.contains(".war/") || path.contains(".ear/") || path.contains(".jar/");
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
