package com.github.xemiru.general.stock;

import com.github.xemiru.general.ArgumentParser;
import com.github.xemiru.general.Arguments;
import com.github.xemiru.general.Command;
import com.github.xemiru.general.CommandContext;
import com.github.xemiru.general.CommandExecutor;
import com.github.xemiru.general.exception.CommandException;
import com.github.xemiru.general.misc.HelpGenerator;
import javafx.scene.Parent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import static com.github.xemiru.general.ArgumentParsers.INTEGER;
import static com.github.xemiru.general.ArgumentParsers.alt;
import static com.github.xemiru.general.ArgumentParsers.opt;
import static com.github.xemiru.general.ArgumentParsers.or;

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
     * Returns a {@link List} containing the given page of elements. It is possible to receive an empty list if the
     * provided page is over the max.
     *
     * <p>The {@code page} parameter is 0-based.</p>
     *
     * @param page the page to receive (0-based)
     * @return the page
     */
    private static <T> List<T> getPage(List<T> elements, int page, int pageSize) {
        if (page < 0) throw new IllegalArgumentException("Page cannot be negative");

        List<T> returned = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            int index = i + (pageSize * (page));
            if (index >= elements.size()) break;
            returned.add(elements.get(index));
        }

        return returned;
    }

    private List<Command> commands;

    private HelpExecutor(List<Command> commands) {
        this.commands = commands;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute(CommandContext context, Arguments args, boolean dry) {
        // not supposed to reach this command if helpgen doesn't exist
        HelpGenerator helpGen = context.getManager().getHelpGenerator().orElseThrow(
            () -> new CommandException("Manager did not have a help generator"));
        int pageSize = helpGen.getPageSize(context);

        ArgumentParser<Optional<CommandContext>> cmdMatcher = new ParentExecutor.CommandMatcher(context, this.commands);
        if (pageSize > 1) args.named("command|page", opt(or(INTEGER, cmdMatcher), "1"));
        else args.named("command", alt(cmdMatcher, ParentExecutor.DUMMY));

        if (dry) return;

        Arguments.Parameter<?> param = args.nextParameter();
        Object next = param.getValue();

        if (next == ParentExecutor.DUMMY || next instanceof Integer) { // specified a page
            int page = pageSize == 0 ? 0 : next instanceof Integer ? (int) next : 1;
            int maxPage = pageSize == 0 ? 0 : (int) Math.ceil((double) this.commands.size() / pageSize);

            if (pageSize > 0 && page < 1) page = 1; // replace bad input with default good input

            // gather elements
            Comparator<String> sorter = helpGen.getSorter(context);
            TreeMap<String, Optional<String>> helpMap = new TreeMap<>(sorter);
            List<Command> commands = new ArrayList<>(this.commands);

            // sort commands first to correctly paginate
            commands.sort((a, b) -> sorter.compare(a.getName(), b.getName()));

            // get page
            List<Command> elements = new ArrayList<>(pageSize <= 0 ? commands
                : HelpExecutor.getPage(commands, page - 1, pageSize));

            // push to map and send
            elements.forEach(cmd -> helpMap.put(cmd.getName(), cmd.getShortDescription()));
            helpGen.sendHelp(context, helpMap, page, maxPage);
        } else { // specified a command
            Optional<CommandContext> ctx = (Optional<CommandContext>) next;
            if (ctx.isPresent()) {
                try {
                    helpGen.sendFullHelp(context, new HelpGenerator.HelpInfo(ctx.get()));
                } catch (Throwable e) {
                    throw helpGen.createError(context, param.getToken());
                }
            } else throw helpGen.createErrorUnknown(context, param.getToken());
        }
    }
}
