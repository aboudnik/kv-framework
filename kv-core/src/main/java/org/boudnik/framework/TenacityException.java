package org.boudnik.framework;

/**
 * @author Alexandre_Boudnik
 * @since 05/01/2018
 */
@SuppressWarnings("WeakerAccess")
public class TenacityException extends RuntimeException {
    public TenacityException(Throwable cause) {
        super(cause);
    }

    public TenacityException(String message) {
        super(message);
    }
}
