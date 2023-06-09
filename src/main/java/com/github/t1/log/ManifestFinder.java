package com.github.t1.log;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManifestFinder {
    private static final Pattern MAIN_MANIFEST =
        Pattern.compile("vfs:/content/(?<path>.*)\\.(?<type>war|jar|ear)/META-INF/MANIFEST.MF");

    public Iterable<URL> manifests() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final Enumeration<URL> enumeration = classLoader.getResources("/META-INF/MANIFEST.MF");
        return () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            @Override
            public URL next() {
                return enumeration.nextElement();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Matcher matcher(String string) {
        return MAIN_MANIFEST.matcher(string);
    }
}
