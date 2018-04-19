package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

import java.util.Objects;

/**
 * @author Alexandre_Boudnik
 * @since 03/01/18 14:20
 */
public class TestEntry implements OBJ<String> {
    private final String url;

    public TestEntry(String url) {
        this.url = url;
    }

    @Override
    public String getKey() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestEntry testEntry = (TestEntry) o;
        return Objects.equals(url, testEntry.url);
    }

    @Override
    public int hashCode() {

        return Objects.hash(url);
    }
}
