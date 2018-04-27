package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

public class ComplexRefTestEntry extends OBJ.Implementation<String> {
    private final REF<String, RefTestEntry> ref = new REF<>(RefTestEntry.class);

    public ComplexRefTestEntry() {
    }

    private ComplexRefTestEntry(String key) {
        super(key);
    }

    public ComplexRefTestEntry(String key, RefTestEntry test) {
        this(key);
        ref.set(test);
    }

    public RefTestEntry getEntry() {
        return ref.get();
    }
}
