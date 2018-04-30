package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

public class MutableTestEntry implements OBJ<String> {
    private String url;

    private String value;

    public MutableTestEntry() {
    }

    public MutableTestEntry(String url) {
        this.url = url;
    }

    @Override
    public String getKey() {
        return url;
    }

    @Override
    public void setKey(String key) {
        this.url = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}