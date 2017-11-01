package com.github.xemiru.general.stock;

import com.github.xemiru.general.CommandContext;
import com.github.xemiru.general.exception.CommandException;
import com.github.xemiru.general.misc.HelpGenerator;

import java.util.Comparator;
import java.util.Optional;
import java.util.TreeMap;

/**
 * The default implementation of a {@link HelpGenerator}.
 *
 * <p>Pagination is disabled.</p>
 *
 * <p>Command sorting is alphabetical.</p>
 *
 * <p>The list of commands with their short help text is output in the following format:</p>
 * <pre>(command name)\t(command short description)</pre>
 *
 * <p>Full help text for commands is output in the following format:</p>
 * <pre>
 * Command: (command name)
 * Aliases: (aliases OR "(no aliases)")
 * Syntax: (syntax)
 *
 * (description OR "This command has no description.")</pre>
 *
 * <p>The error message for unknown commands is:</p>
 * <pre>"Couldn't find command (input). Try 'help'."</pre>
 *
 * <p>The error message for commands that crash during information harvesting is:</p>
 * <pre>"That command crashed when we tried to ask it about itself. Oops."</pre>
 */
public class DefaultHelpGenerator implements HelpGenerator {

    @Override
    public int getPageSize() {
        return 0;
    }

    @Override
    public Comparator<String> getSorter() {
        return String::compareToIgnoreCase;
    }

    @Override
    public void sendHelp(CommandContext context, TreeMap<String, Optional<String>> help, int page, int maxPage) {
        if (page > 0) context.sendMessage(String.format("Command List (page %d of %d)", page, maxPage));
        for (String cmd : help.keySet())
            context.sendMessage(String.format("%s\t%s", cmd,
                help.get(cmd).orElse("This command has no short description.")));
    }

    @Override
    public void sendFullHelp(CommandContext context, HelpGenerator.HelpInfo info) {
        context.sendMessage(String.format("Command: %s", info.getName()));
        context.sendMessage(String.format("Aliases: %s", info.getAliases().orElse("(no aliases)")));
        context.sendMessage(String.format("Syntax: %s", info.getSyntax()));
        context.sendMessage("");
        context.sendMessage(info.getDescription().orElse("This command has no description."));
    }

    @Override
    public CommandException createError(CommandContext ctx, String input) {
        return new CommandException("That command crashed when we tried to ask it about itself. Oops.");
    }

    @Override
    public CommandException createErrorUnknown(CommandContext ctx, String input) {
        return new CommandException(String.format("Couldn't find command %s. Try 'help'.", input));
    }

}
