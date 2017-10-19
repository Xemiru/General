package com.github.xemiru.general.exception;

import com.github.xemiru.general.Command;

/**
 * Thrown whenever a syntax error occurs during the execution of a {@link Command}.
 */
public class SyntaxException extends RuntimeException {

    private String syntax;

    public SyntaxException(String syntax, String message) {
        super(message);
        this.syntax = syntax;
    }

    public SyntaxException(String syntax, String message, Throwable cause) {
        super(message, cause);
        this.syntax = syntax;
    }

    public String getSyntax() {
        return this.syntax;
    }

}
