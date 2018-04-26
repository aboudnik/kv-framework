package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

public class ComplexTestEntry2 implements OBJ<OBJ<OBJ<String>>>  {
    private final ComplexTestEntry url;
    private String value;
    public ComplexTestEntry2(ComplexTestEntry url) {
        this.url = url;
    }

    @Override
    public ComplexTestEntry getKey() {
        return url;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}