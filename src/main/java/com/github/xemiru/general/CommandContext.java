package com.github.xemiru.general;

/**
 * Contains contextual information about the execution of a {@link Command}.
 */
public class CommandContext {

    private boolean dry;
    private String label;
    private Command command;
    private CommandManager manager;

    public CommandContext(CommandManager manager, Command command, String label, boolean dry) {
        this.dry = dry;
        this.label = label;
        this.command = command;
        this.manager = manager;
    }

    /**
     * @return whether or not the associated {@link Command} is being dry-ran
     */
    public boolean isDry() {
        return this.dry;
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
     * @return the alias that was used to call the command
     */
    public String getLabel() {
        return this.label;
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
