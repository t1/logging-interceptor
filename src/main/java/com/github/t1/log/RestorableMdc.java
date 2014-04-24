package com.github.t1.log;

import java.util.*;

import org.slf4j.MDC;

class RestorableMdc {
    private Map<String, String> memento;

    public String get(String indent) {
        return MDC.get(indent);
    }

    public void put(String key, String value) {
        String oldValue = MDC.get(key);
        memento().put(key, oldValue);
        MDC.put(key, value);
    }

    private Map<String, String> memento() {
        if (memento == null)
            memento = new HashMap<>();
        return memento;
    }

    public void restore() {
        if (memento == null)
            return;
        for (Map.Entry<String, String> entry : memento.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, value);
            }
        }
    }
}
