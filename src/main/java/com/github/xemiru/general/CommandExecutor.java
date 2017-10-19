package com.github.xemiru.general;

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
     * @param context the execution context of the command
     * @param args the arguments object
     * @param dry if the command should be dry-run
     */
    void execute(CommandContext context, Arguments args, boolean dry);

}
