package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

public class MutableTestEntry implements OBJ<String> {
    private final String url;

    private String value;

    public MutableTestEntry(String url) {
        this.url = url;
    }

    @Override
    public String getKey() {
        return url;
    }

    public String getUrl() {
        return url;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}