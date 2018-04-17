package org.boudnik.framework.test.core;

import org.boudnik.framework.OBJ;

import java.util.HashMap;
import java.util.Map;

public class RefTestEntry implements OBJ<Integer> {
    Map<Integer, String> m = new HashMap<>();

    private final Integer key;
    public final OBJ.REF<String, TestEntry> ref = new OBJ.REF<>(TestEntry.class);

    public RefTestEntry(Integer key, TestEntry test) {
        this.key = key;
        this.ref.set(test);
    }

    @Override
    public Integer getKey() {
        return key;
    }
}