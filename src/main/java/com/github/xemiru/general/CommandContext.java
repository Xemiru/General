package com.github.xemiru.general;

import com.github.xemiru.general.exception.CommandException;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains contextual information about the execution of a {@link Command}.
 */
public class CommandContext {

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
     * Returns the mapping of custom properties held by this {@link CommandContext}.
     *
     * <p>Changes to this map are reflected in the corresponding getter/setter methods. It is recommended to use the
     * former instead of modifying the returned map directly.</p>
     *
     * @return this CommandContext's custom mapping
     */
    public Map<String, Object> getCustomMap() {
        return this.custom;
    }

    /**
     * Returns a custom property set on this {@link CommandContext}.
     *
     * <p>This method has no safety and can throw a {@link NullPointerException} or a {@link ClassCastException} if the
     * value did not exist or is casted into the wrong type by the type parameter.</p>
     *
     * @param key the key of the property
     * @param <T> the type of the property
     * @return the property value
     */
    public <T> T getCustom(String key) {
        return (T) this.custom.get(key);
    }

    /**
     * Sets a custom property on this {@link CommandContext}.
     *
     * @param key the key of the property
     * @param value the value of the property
     * @return this CommandContext
     */
    public CommandContext setCustom(String key, Object value) {
        this.custom.put(key, value);
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
        if(preExec != null) {
            if(!this.dry) throw new CommandException(preExec);
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

}
