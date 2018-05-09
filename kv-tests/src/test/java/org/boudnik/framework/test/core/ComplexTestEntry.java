package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

import java.util.Objects;

public class ComplexTestEntry implements OBJ<OBJ<String>> {

    private TestEntry url;

    private String value;

    public ComplexTestEntry(TestEntry url) {
        this.url = url;
    }

    @Override
    public TestEntry getKey() {
        return url;
    }

    @Override
    public void setKey(OBJ<String> key) {
        this.url = (TestEntry) key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexTestEntry that = (ComplexTestEntry) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(url, value);
    }
}