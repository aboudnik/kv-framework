package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

public class ComplexRefTestEntry extends OBJ.Implementation<Integer> {
    private final REF<Integer, RefTestEntry> ref = new REF<>(RefTestEntry.class);

    private ComplexRefTestEntry(Integer key) {
        super(key);
    }

    public ComplexRefTestEntry(Integer key, RefTestEntry test) {
        this(key);
        ref.set(test);
    }

    public RefTestEntry getEntry() {
        return ref.get();
    }
}
