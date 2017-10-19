package com.github.xemiru.general.exception;

import com.github.xemiru.general.Command;

/**
 * Thrown whenever an execution error occurs during the runtime of a {@link Command}.
 */
public class CommandException extends RuntimeException {

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

}
