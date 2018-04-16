package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

public class ComplexTestEntry implements OBJ<OBJ<String>>{

    private final TestEntry url;

    private String value;
    public ComplexTestEntry(TestEntry url) {
        this.url = url;
    }
    @Override
    public TestEntry getKey() {
        return url;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}