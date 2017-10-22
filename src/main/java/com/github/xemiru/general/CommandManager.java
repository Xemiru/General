package com.github.xemiru.general;

import com.github.xemiru.general.exception.CommandException;
import com.github.xemiru.general.exception.SyntaxException;
import com.github.xemiru.general.stock.ParentExecutor;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The main entrypoint for command systems.
 */
public class CommandManager {

    private Consumer<String> sendMessage;
    private Consumer<String> sendError;

    private Set<Command> commands;
    private Function<CommandContext, Optional<String>> preExec;

    public CommandManager() {
        this.sendMessage = System.out::println;
        this.sendError = System.err::println;
        this.commands = new HashSet<>();
        this.setPreExecutor(null);
    }

    /**
     * Returns the underlying set of {@link Command}s registered with this {@link CommandManager}.
     *
     * <p>The set returned is the same set as the one held by this CommandManager; changes to it are reflected in
     * command execution. It is recommended to use the add/remove methods instead of directly modifying the returned
     * set.</p>
     *
     * @return this CommandManager's commands
     */
    public Set<Command> getCommands() {
        return this.commands;
    }

    /**
     * Adds {@link Command}s to choose from when command input is processed.
     *
     * <p>Duplicate command registration fails silently.</p>
     *
     * @param commands the Commands to add
     */
    public void addCommands(Command... commands) {
        this.addCommands(Arrays.asList(commands));
    }

    /**
     * Adds {@link Command}s to choose from when command input is processed.
     *
     * <p>Duplicate command registration fails silently.</p>
     *
     * @param commands the Commands to add
     */
    public void addCommands(Collection<Command> commands) {
        this.commands.addAll(commands);
    }

    /**
     * Removes {@link Command}s to choose from when command input is processed.
     *
     * @param commands the Commands to remove
     */
    public void removeCommands(Command... commands) {
        this.removeCommands(Arrays.asList(commands));
    }

    /**
     * Removes {@link Command}s to choose from when command input is processed.
     *
     * @param commands the Commands to remove
     */
    public void removeCommands(Collection<Command> commands) {
        this.commands.removeAll(commands);
    }

    /**
     * Handles the provided command string using this {@link CommandManager}'s internal command mapping.
     *
     * @param input the input tokens to process
     */
    public void handleCommand(String input) {
        this.handleCommand(input, null);
    }

    public void handleCommand(String input, Function<CommandContext, CommandContext> contextFactory) {
        this.handleCommand(input, contextFactory, this.commands);
    }

    /**
     * Handles the provided command string using the provided collection of commands.
     *
     * @param input the input tokens to process
     */
    public void handleCommand(String input, Function<CommandContext, CommandContext> contextFactory, Collection<Command> commands) {
        this.processCommand(input, contextFactory, commands, false);
    }

    /**
     * Returns completion suggestions for the last argument in the provided command string using commands from this
     * {@link CommandManager}'s internal command mapping.
     *
     * @param input the input tokens to process
     * @return the list of possible completions for the last argument (can be empty)
     */
    public List<String> completeCommand(String input) {
        return this.completeCommand(input, null);
    }

    public List<String> completeCommand(String input, Function<CommandContext, CommandContext> contextFactory) {
        return this.completeCommand(input, contextFactory, this.commands);
    }

    /**
     * Returns completion suggestions for the last argument in the provided command string using the provided collection
     * of commands.
     *
     * @param input the input tokens to process
     * @return the list of possible completions for the last argument (can be empty)
     */
    public List<String> completeCommand(String input, Function<CommandContext, CommandContext> contextFactory, Collection<Command> commands) {
        return this.processCommand(input, contextFactory, commands, true);
    }

    /**
     * Sends a message using this {@link CommandManager}'s message sending handler.
     *
     * @param msg the message to send
     */
    public void sendMessage(String msg) {
        this.sendMessage.accept(msg);
    }

    /**
     * Sets the message handler for this {@link CommandManager}.
     *
     * <p>If null is passed, this defaults to sending messages to the standard output stream ({@link System#out}).</p>
     *
     * @param handler the new message handler
     */
    public void setMessageHandler(Consumer<String> handler) {
        this.sendMessage = handler == null ? System.out::println : handler;
    }

    /**
     * Sends an error message using this {@link CommandManager}'s error message sending handler.
     *
     * @param msg the error message to send
     */
    public void sendError(String msg) {
        this.sendError.accept(msg);
    }

    /**
     * Sets the error message handler for this {@link CommandManager}.
     *
     * <p>If null is passed, this defaults to sending messages to the standard error stream ({@link System#err}).</p>
     *
     * @param handler the new error message handler
     */
    public void setErrorMessageHandler(Consumer<String> handler) {
        this.sendError = handler == null ? System.err::println : handler;
    }

    /**
     * Returns the pre-executor used for {@link Command}s ran with this {@link CommandManager}.
     *
     * <p>The pre-executor is called before calling the executor, allowing one to do checks on the context of the
     * command before allowing it to be ran. Should the pre-executor return a non-empty Optional, it is assumed that the
     * it does not want the command to be ran. A command running in dry mode will silently fail, but would otherwise
     * throw a {@link CommandException} with the message produced by the pre-executor.</p>
     *
     * <p>The default pre-executor always returns an empty Optional. This method cannot return null.</p>
     *
     * @return this CommandManager's pre-executor
     */
    public Function<CommandContext, Optional<String>> getPreExecutor() {
        return this.preExec;
    }

    /**
     * Sets the pre-executor used for {@link Command}s ran with this {@link CommandManager}.
     *
     * <p>Passing null will set the default pre-executor (which always returns an empty Optional).</p>
     *
     * @param preExec the new pre-executor, or null to use default
     */
    public void setPreExecutor(Function<CommandContext, Optional<String>> preExec) {
        this.preExec = preExec == null ? ctx -> Optional.empty() : preExec;
    }

    private List<String> processCommand(String input, Function<CommandContext, CommandContext> contextFactory, Collection<Command> commands, boolean tab) {
        CommandContext ctx = new CommandContext(this, null, null, tab);
        if (contextFactory != null) ctx = contextFactory.apply(ctx);

        String[] rargs = input.split(" ");
        if (tab && input.length() > 0 && input.charAt(input.length() - 1) == ' ') {
            // if ends with space, assume the user wants to tabcomplete the param after
            rargs = (input + "a").trim().split(" ");
            rargs[rargs.length - 1] = ""; // meaning we need the last param to be blank
        }

        Arguments args = new Arguments(ctx, new RawArguments(rargs));

        try {
            new ParentExecutor().addCommands(commands).execute(ctx, args, ctx.isDry());
            if (tab) return args.complete();
        } catch (CommandException e) {
            if(!ctx.isDry()) ctx.sendError(e.getMessage());
        } catch (SyntaxException e) {
            ctx.sendError(e.getMessage());
            ctx.sendError("Syntax: " + e.getSyntax());
        } catch (Throwable e) {
            ctx.sendError("The command has crashed: " + e.getMessage());
            ctx.sendError("Detailed information has been logged.");

            e.printStackTrace();
        }

        return null;
    }

}
