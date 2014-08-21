package net.whistlingfish.harmony.protocol;

import static java.lang.String.format;

public class AuthFailedException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Logitech authentication failed";
    private static final long serialVersionUID = 1L;

    public AuthFailedException() {
        super(DEFAULT_MESSAGE);
    }

    public AuthFailedException(String message) {
        super(format("%s: %s", DEFAULT_MESSAGE, message));
    }

    public AuthFailedException(Throwable cause) {
        this(DEFAULT_MESSAGE, cause);
    }

    public AuthFailedException(String message, Throwable cause) {
        super(format("%s: %s", DEFAULT_MESSAGE, message), cause);
    }
}
