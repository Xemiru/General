package com.github.xemiru.general.stock;

import com.github.xemiru.general.*;
import com.github.xemiru.general.exception.CommandException;

import java.util.List;
import java.util.Optional;

public class HelpExecutor implements CommandExecutor {

    /**
     * Creates a new help {@link Command} using a {@link HelpExecutor} referencing the provided list of commands.
     *
     * @param commands the list of Commands to show help for
     * @return a help Command
     */
    public static Command create(List<Command> commands) {
        return Command.builder()
            .name("help")
            .shortDescription("Shows help text for commands.")
            .executor(new HelpExecutor(commands))
            .build();
    }

    /**
     * Lists a {@link Command}'s aliases in string form.
     *
     * <p>This does not include the command's main name, stored in the 0th index of the aliases array.</p>
     *
     * @param cmd the Command to harvest aliases from
     * @return a String form of a list of the command's aliases, or an empty string if it has none
     */
    private static String aliases(Command cmd) {
        String[] list = new String[cmd.getAliases().length - 1];
        System.arraycopy(cmd.getAliases(), 1, list, 0, cmd.getAliases().length - 1);

        if (list.length <= 0) return "(no aliases)";
        StringBuilder sb = new StringBuilder(list[0]);
        for (int i = 1; i < list.length; i++) sb.append(", ").append(list[i]);
        return sb.toString();
    }

    private List<Command> commands;

    private HelpExecutor(List<Command> commands) {
        this.commands = commands;
    }

    @Override
    public void execute(CommandContext context, Arguments args, boolean dry) {
        args.write(new ParentExecutor.CommandMatcher(context, this.commands));
        if (dry) return;

        Optional<CommandContext> ctx = args.next();
        if (ctx.isPresent()) {
            try {
                String syntax;
                Command cmd = ctx.get().getCommand();
                if (cmd.getSyntax().isPresent()) syntax = cmd.getSyntax().get();
                else {
                    Arguments simArgs = new Arguments(ctx.get(), new RawArguments(new String[0]));
                    cmd.getExec().execute(ctx.get(), simArgs, true);
                    syntax = simArgs.getSyntax();
                }

                context.sendMessage("Help for: " + cmd.getName());
                context.sendMessage("Syntax: " + cmd.getName() + " " + syntax);
                context.sendMessage("Aliases: " + aliases(cmd));
                context.sendMessage(cmd.getDescription().orElse(cmd.getShortDescription()
                    .orElse("This command has no help text.")));
            } catch (Throwable e) {
                throw new CommandException("That command crashed when we tried to ask it about itself. Oops.", e);
            }
        } else {
            throw new CommandException("Couldn't find command.");
        }

        /*
        // no command matched, default to showing everything
        StringBuilder sb = new StringBuilder();
        for (Command cmd : this.commands)
            sb.append(cmd.getName())
                .append(" -- ")
                .append(cmd.getShortDescription().orElse("This command has no short help text."))
                .append('\n');

        context.sendMessage(sb.toString().trim());*/
    }
}
