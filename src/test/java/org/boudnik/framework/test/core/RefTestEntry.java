package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

public class RefTestEntry extends OBJ.Implementation<Integer> {
    private final REF<String, TestEntry> ref = new REF<>(TestEntry.class);

    private RefTestEntry(Integer key) {
        super(key);
    }

    public RefTestEntry(Integer key, TestEntry test) {
        this(key);
        ref.set(test);
    }

    public TestEntry getEntry() {
        return ref.get();
    }
}