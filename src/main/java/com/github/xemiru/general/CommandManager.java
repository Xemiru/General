package com.github.xemiru.general;

import com.github.xemiru.general.exception.CommandException;
import com.github.xemiru.general.exception.SyntaxException;
import com.github.xemiru.general.misc.HelpGenerator;
import com.github.xemiru.general.stock.DefaultHelpGenerator;
import com.github.xemiru.general.stock.ParentExecutor;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The main entrypoint for command systems.
 */
public class CommandManager {

    private BiConsumer<CommandContext, String> sendMessage;
    private BiConsumer<CommandContext, String> sendError;

    private Set<Command> commands;
    private HelpGenerator helpGen;
    private Function<CommandContext, Optional<String>> preExec;

    public CommandManager() {
        this.setMessageHandler(null);
        this.setErrorMessageHandler(null); // methods will set a default
        this.commands = new HashSet<>();
        this.helpGen = new DefaultHelpGenerator();
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
     * @param input the input string to process
     */
    public void handleCommand(String input) {
        this.handleCommand(input, null);
    }

    /**
     * Handles the provided command string using this {@link CommandManager}'s internal command mapping.
     *
     * <p>The given context factory is applied to create the {@link CommandContext} to be received by executed
     * {@link Command}s.</p>
     *
     * @param input the input string to process
     * @param contextFactory the context factory used to modify or create the CommandContext object to be received by
     *     commands
     */
    public void handleCommand(String input, Function<CommandContext, CommandContext> contextFactory) {
        this.handleCommand(input, contextFactory, this.commands);
    }

    /**
     * Handles the provided command string using the provided collection of commands.
     *
     * <p>The given context factory is applied to create the {@link CommandContext} to be received by executed
     * {@link Command}s.</p>
     *
     * @param input the input string to process
     * @param contextFactory the context factory used to modify or create the CommandContext object to be received by
     *     commands
     * @param commands the Set of Commands to choose from while processing the provided input string
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

    /**
     * Returns completion suggestions for the last argument in the provided command string using commands from this
     * {@link CommandManager}'s internal command mapping.
     *
     * <p>The given context factory is applied to create the {@link CommandContext} to be received by executed
     * {@link Command}s.</p>
     *
     * @param input the input tokens to process
     * @param contextFactory the context factory used to modify or create the CommandContext object to be received by
     *     commands
     * @return the list of possible completions for the last argument (can be empty)
     */
    public List<String> completeCommand(String input, Function<CommandContext, CommandContext> contextFactory) {
        return this.completeCommand(input, contextFactory, this.commands);
    }

    /**
     * Returns completion suggestions for the last argument in the provided command string using the provided collection
     * of commands.
     *
     * <p>The given context factory is applied to create the {@link CommandContext} to be received by executed
     * {@link Command}s.</p>
     *
     * @param input the input tokens to process
     * @param contextFactory the context factory used to modify or create the CommandContext object to be received by
     *     commands
     * @param commands the Set of Commands to choose from while processing the provided input string
     * @return the list of possible completions for the last argument (can be empty)
     */
    public List<String> completeCommand(String input, Function<CommandContext, CommandContext> contextFactory, Collection<Command> commands) {
        return this.processCommand(input, contextFactory, commands, true);
    }

    /**
     * Sends a message using this {@link CommandManager}'s message sending handler.
     *
     * @param context the context the message is coming from
     * @param msg the message to send
     */
    public void sendMessage(CommandContext context, String msg) {
        this.sendMessage.accept(context, msg);
    }

    /**
     * Sets the message handler for this {@link CommandManager}.
     *
     * <p>If null is passed, this defaults to sending messages to the standard output stream ({@link System#out}).</p>
     *
     * @param handler the new message handler
     */
    public void setMessageHandler(BiConsumer<CommandContext, String> handler) {
        this.sendMessage = handler == null ? (ctx, msg) -> System.out.println(msg) : handler;
    }

    /**
     * Sends an error message using this {@link CommandManager}'s error message sending handler.
     *
     * @param context the context the message is coming from
     * @param msg the message to send
     */
    public void sendError(CommandContext context, String msg) {
        this.sendError.accept(context, msg);
    }

    /**
     * Sets the error message handler for this {@link CommandManager}.
     *
     * <p>If null is passed, this defaults to sending messages to the standard error stream ({@link System#err}).</p>
     *
     * @param handler the new error message handler
     */
    public void setErrorMessageHandler(BiConsumer<CommandContext, String> handler) {
        this.sendError = handler == null ? (ctx, msg) -> System.err.println(msg) : handler;
    }

    /**
     * Returns the {@link HelpGenerator} used by help commands registered under this {@link CommandManager}.
     *
     * <p>If no HelpGenerator is assigned, commands executed will not have a help command. Help registration is
     * dynamic, however; commands will receive their help command as soon as the CommandManager that owns them receives
     * a HelpGenerator.</p>
     *
     * @return the HelpGenerator used by help commands registered under this CommandManager
     */
    public Optional<HelpGenerator> getHelpGenerator() {
        return Optional.ofNullable(this.helpGen);
    }

    /**
     * Sets the {@link HelpGenerator} used by help commands registered under this {@link CommandManager}.
     *
     * <p>Can be set to null to disable help commands.</p>
     *
     * @param helpGen the HelpGenerator used by help commands registered under this CommandManager
     */
    public void setHelpGenerator(HelpGenerator helpGen) {
        this.helpGen = helpGen;
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

        String[] rargs = input.trim().split(" ");
        if (tab && input.length() > 0 && input.charAt(input.length() - 1) == ' ') {
            // if ends with space, assume the user wants to tabcomplete the param after
            rargs = Arrays.copyOf(rargs, rargs.length + 1);
            rargs[rargs.length - 1] = ""; // meaning we need the last param to be blank
        }

        Arguments args = new Arguments(ctx, new RawArguments(rargs));

        try {
            new ParentExecutor().addCommands(commands).execute(ctx, args, ctx.isDry());
        } catch (CommandException e) {
            if (!tab) ctx.sendError(e.getMessage());
        } catch (SyntaxException e) {
            if (!tab) {
                ctx.sendError(e.getMessage());
                ctx.sendError("Syntax: " + e.getSyntax());
            }
        } catch (Throwable e) {
            ctx.sendError("The command has crashed: " + e.getMessage());
            ctx.sendError("Detailed information has been logged.");

            e.printStackTrace();
            return new ArrayList<>();
        }

        if(tab) return args.complete();
        return new ArrayList<>();
    }

}
