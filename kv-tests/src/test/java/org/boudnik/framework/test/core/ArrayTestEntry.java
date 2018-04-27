package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

import java.util.Arrays;

public class ArrayTestEntry extends OBJ.Implementation<String[]> {
    private String[] url;

    public ArrayTestEntry() {
    }

    public ArrayTestEntry(String... url) {
        super(url);
    }

//    @Override
//    public String[] getKey() {
//        return url;
//    }

    public String[] getUrl() {
        return url;
    }

    public void setUrl(String[] url) {
        this.url = url;
    }
//
//    public void setKey(String[] url) {
//        this.url = url;
//    }

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
