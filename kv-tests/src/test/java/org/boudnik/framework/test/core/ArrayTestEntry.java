package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

import java.util.Arrays;

public class ArrayTestEntry extends OBJ.Implementation<String> {
    private int[] url;

    public ArrayTestEntry() {
    }

    public ArrayTestEntry(String key) {
        super(key);
    }

    public int[] getUrl() {
        return url;
    }

    public void setUrl(int[] url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayTestEntry that = (ArrayTestEntry) o;
        return Arrays.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(url);
    }
}
