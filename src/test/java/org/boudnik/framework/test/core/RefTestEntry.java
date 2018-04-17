package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

public class RefTestEntry implements OBJ<OBJ.Implementation<String>.REF<String, TestEntry>> {
    private Implementation<String>.REF<String, TestEntry> key;

    public RefTestEntry(Implementation<String>.REF<String, TestEntry> key) {
        this.key = key;
    }

    @Override
    public Implementation<String>.REF<String, TestEntry> getKey() {
        return key;
    }
}