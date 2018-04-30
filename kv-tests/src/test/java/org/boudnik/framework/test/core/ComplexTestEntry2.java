package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

public class ComplexTestEntry2 implements OBJ<OBJ<OBJ<String>>>  {
    private ComplexTestEntry url;
    private String value;

    public ComplexTestEntry2() {
    }

    public ComplexTestEntry2(ComplexTestEntry url) {
        this.url = url;
    }

    @Override
    public ComplexTestEntry getKey() {
        return url;
    }

    @Override
    public void setKey(OBJ<OBJ<String>> key) {
        this.url = (ComplexTestEntry) key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}