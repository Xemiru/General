package com.github.xemiru.general;

import com.github.xemiru.general.exception.CommandException;
import com.github.xemiru.general.misc.CustomAssignable;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains contextual information about the execution of a {@link Command}.
 */
public class CommandContext implements CustomAssignable {

    private boolean dry;
    private String label;
    private Command command;
    private CommandManager manager;
    private Map<String, Object> custom;

    public CommandContext(CommandManager manager, Command command, String label, boolean dry) {
        this.dry = dry;
        this.label = label;
        this.command = command;
        this.manager = manager;
        this.custom = new HashMap<>();
    }

    /**
     * @return whether or not the associated {@link Command} is being dry-ran
     */
    public boolean isDry() {
        return this.dry;
    }

    /**
     * Modifies the dry-run parameter of this {@link CommandContext} and returns it.
     *
     * @param dry if this CommandContext is being dry-ran
     * @return this CommandContext
     */
    public CommandContext setDry(boolean dry) {
        this.dry = dry;
        return this;
    }

    /**
     * @return the {@link CommandManager} that caused the execution of the command
     */
    public CommandManager getManager() {
        return this.manager;
    }

    /**
     * @return the {@link Command} this context was created for
     */
    public Command getCommand() {
        return this.command;
    }

    /**
     * Modifies the command held by this {@link CommandContext} and returns it.
     *
     * <p>Label is automatically set to the return value of {@link Command#getName()}. Call {@link #setLabel(String)}
     * after this method to override.</p>
     *
     * @param cmd the new Command
     * @return this CommandContext
     */
    public CommandContext setCommand(Command cmd) {
        this.command = cmd;
        this.label = cmd.getName();
        return this;
    }

    /**
     * @return the alias that was used to call the command
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Modifies the label held by this {@link CommandContext} and returns it.
     *
     * @param label the new label used to call this context's {@link Command} with
     * @return this CommandContext
     */
    public CommandContext setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Executes the {@link Command} held by this {@link CommandContext} using the given {@link Arguments}.
     *
     * <p>The context of the provided arguments object is automagically set to this context.</p>
     *
     * @param args the arguments to use
     */
    public void execute(Arguments args) {
        args.setContext(this);
        String preExec = this.manager.getPreExecutor().apply(this).orElse(null);
        if (preExec != null) {
            if (!this.dry) throw new CommandException(preExec);
        } else {
            this.command.getExec().execute(this, args, this.dry);
        }
    }

    /**
     * Sends a message to the user.
     *
     * @param msg the message to respond to the user with
     */
    public void sendMessage(String msg) {
        this.manager.sendMessage(msg);
    }

    /**
     * Sends an error message to the user.
     *
     * @param msg the error message to send to the user
     */
    public void sendError(String msg) {
        this.manager.sendError(msg);
    }

    @Override
    public Map<String, Object> getCustomMap() {
        return this.custom;
    }

    @Override
    public CommandContext setCustom(String key, Object value) {
        CustomAssignable.super.setCustom(key, value);
        return this;
    }
}
