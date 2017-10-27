package com.github.xemiru.general.stock;

import com.github.xemiru.general.*;
import com.github.xemiru.general.exception.CommandException;
import com.github.xemiru.general.exception.SyntaxException;

import java.util.*;

import static com.github.xemiru.general.ArgumentParsers.STRING;
import static com.github.xemiru.general.ArgumentParsers.alt;

/**
 * An implementation of {@link CommandExecutor} that acts as a parent command to numerous subcommands.
 *
 * <p>Comes with a built-in help command, generated from {@link HelpExecutor#create(List)}.</p>
 *
 * <p>If you need to build a command using this executor, it may be better to make use of {@link Command#parent()}.</p>
 */
public class ParentExecutor implements CommandExecutor {

    /**
     * Dummy return value used to signify that the user didn't even try to input anything.
     */
    private static final Optional<CommandContext> DUMMY = Optional.of(new CommandContext(null, null, null, false));

    public static class CommandMatcher implements ArgumentParser<Optional<CommandContext>> {

        private CommandContext parent;
        private List<Command> commands;

        CommandMatcher(CommandContext parent, List<Command> commands) {
            this.parent = parent;
            this.commands = commands;
        }

        @Override
        public String getTypename() {
            return "command";
        }

        @Override
        public Optional<CommandContext> parse(RawArguments args) {
            String name = STRING.parse(args);
            for (Command cmd : this.commands) {
                if (cmd.hasName(name)) {
                    CommandContext rctx = new CommandContext(this.parent.getManager(), cmd, name, this.parent.isDry());
                    parent.getCustomMap().forEach(rctx::setCustom);
                    return Optional.of(rctx);
                }
            }

            return Optional.empty();
        }

        @Override
        public Set<String> getSuggestions() {
            Set<String> returned = new LinkedHashSet<>();
            for (Command cmd : this.commands) returned.add(cmd.getName());
            return returned;
        }
    }

    private List<Command> commands;
    private CommandExecutor fallback;

    public ParentExecutor() {
        this.fallback = null;
        this.commands = new ArrayList<>();
        this.commands.add(HelpExecutor.create(this.commands));
    }

    /**
     * Adds subcommands to this {@link ParentExecutor}'s selection.
     *
     * @param commands commands to add
     * @return this ParentExecutor
     */
    public ParentExecutor addCommands(Command... commands) {
        return this.addCommands(Arrays.asList(commands));
    }

    /**
     * Adds subcommands to this {@link ParentExecutor}'s selection.
     *
     * @param commands commands to add
     * @return this ParentExecutor
     */
    public ParentExecutor addCommands(Collection<Command> commands) {
        this.commands.addAll(commands);
        return this;
    }

    /**
     * Sets the fallback {@link CommandExecutor} used when this {@link ParentExecutor} is called without attempting to
     * specify a subcommand.
     *
     * @param exec the fallback executor, or null to remove
     * @return this ParentExecutor
     */
    public ParentExecutor setFallback(CommandExecutor exec) {
        this.fallback = exec;
        return this;
    }

    @Override
    public void execute(CommandContext context, Arguments args, boolean dry) {
        args.write(alt(new CommandMatcher(context, this.commands), DUMMY));

        if (dry) {
            // be a little hacky
            // we need to call the command dry to harvest the syntax, but we also need the matched cmd
            // so we temporarily set its context to one that lies about being dry-ran
            // this is reset when we receive the matched command's context
            Optional<CommandContext> ctx;

            try {
                // we're also breaking rules here by calling next() during dry
                ctx = args.setContext(context.setDry(false)).next();
            } catch(SyntaxException e) {
                // so we need to ignore the syntax error for command completion ourselves
                ctx = Optional.empty();
            }

            if (ctx == DUMMY) return;
            ctx.ifPresent(ctxx -> ctxx.setDry(true).execute(args.drop(1)));
        } else {
            Optional<CommandContext> ctx = args.next();
            if (!ctx.isPresent() || (ctx == DUMMY && this.fallback == null)) {
                String suggest = context.getLabel() == null ? "help" : context.getLabel() + " help";
                throw new CommandException(String.format("Unknown command. Try \"%s\".", suggest));
            }

            if (ctx == DUMMY) fallback.execute(context, args, false);
            else ctx.get().setDry(false).execute(args.drop(1));
        }
    }

}
