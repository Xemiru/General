package com.github.xemiru.general;

import com.github.xemiru.general.exception.CommandException;

/**
 * Functional interface denoting the execution process of a {@link Command}.
 */
public interface CommandExecutor {

    /**
     * Runs this {@link CommandExecutor}.
     *
     * <p>The {@code dry} parameter is important, and determines whether or not the command should actually take effect.
     * If this is true, the command should not actually execute anything and simply declare its syntax through the
     * {@link Arguments} object.</p>
     *
     * <p>{@link CommandException}s are ignored during dry-runs, meaning one can safely perform state checks to prevent
     * further execution when certain conditions aren't met. <strong>Arguments must always be declared first before
     * these checks in order for syntax harvesting to correctly function.</strong></p>
     *
     * @param context the execution context of the command
     * @param args the arguments object
     * @param dry if the command should be dry-run
     */
    void execute(CommandContext context, Arguments args, boolean dry);

}
