package com.github.xemiru.general.exception;

import com.github.xemiru.general.ArgumentParser;

/**
 * Thrown by an {@link ArgumentParser} when receiving invalid tokens.
 */
public class ParseException extends RuntimeException {

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
