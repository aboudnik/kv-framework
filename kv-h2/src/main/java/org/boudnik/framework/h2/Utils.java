package org.boudnik.framework.h2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class Utils {

    private Utils() {
    }

    static <K> String encode(K key) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(key);
            out.flush();
            byte[] bytes = bos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        }
    }
}
