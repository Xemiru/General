package com.github.xemiru.general;

import com.github.xemiru.general.exception.CommandException;

/**
 * Interface denoting the execution runtime of a {@link Command}.
 */
public interface FullCommandExecutor extends CommandExecutor {

    /**
     * Called to declare syntax within the passed {@link Arguments} object.
     *
     * <p>Argument syntax should always be declared first prior to anything else. You cannot call
     * {@link Arguments#next()} in this method, as this will cause {@link IllegalStateException}s during dry-runs
     * (the help command and parameter completion will break).</p>
     *
     * <p>{@link CommandException}s are ignored during dry-runs, meaning one can safely perform state checks to prevent
     * {@link #execute(CommandContext, Arguments)} from being called when certain conditions aren't met. Be warned that
     * these checks will be performed every time the help command or parameter completion requests syntax declarations,
     * meaning that state checks performed in this method should be relatively lightweight.</p>
     *
     * @param context the {@link CommandContext} the command is being ran with
     * @param args the Arguments object given to the command
     */
    void initialize(CommandContext context, Arguments args);

    /**
     * Runs this {@link FullCommandExecutor}.
     *
     * <p>{@link #initialize(CommandContext, Arguments)} will have been called prior to this method, meaning parameters
     * are ready for retrieval and usage.</p>
     *
     * <p>This will never be called during dry runs.</p>
     *
     * @param context the {@link CommandContext} of the {@link Command}'s execution
     * @param args the {@link Arguments} object given to the command
     */
    void execute(CommandContext context, Arguments args);

    @Override
    default void execute(CommandContext context, Arguments args, boolean dry) {
        this.initialize(context, args);
        if(!dry) this.execute(context, args);
    }

}
