package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

public class RefTestEntry extends OBJ.Implementation<String> {
    private final REF<String, TestEntry> ref = new REF<>(TestEntry.class);

    public RefTestEntry() {
    }

    private RefTestEntry(String key) {
        super(key);
    }

    public RefTestEntry(String key, TestEntry test) {
        this(key);
        ref.set(test);
    }

    public TestEntry getEntry() {
        return ref.get();
    }
}