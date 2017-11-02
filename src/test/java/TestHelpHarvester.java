import com.github.xemiru.general.ArgumentParsers;
import com.github.xemiru.general.Command;
import com.github.xemiru.general.CommandContext;
import com.github.xemiru.general.CommandManager;
import com.github.xemiru.general.exception.CommandException;
import com.github.xemiru.general.misc.HelpGenerator;
import org.junit.Test;

import java.util.Comparator;
import java.util.Optional;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class TestHelpHarvester {

    @Test
    public void testHarvest() {

        System.out.println("\nTEST HARVESTING\n");

        CommandManager cm = new CommandManager();
        cm.setHelpGenerator(new HelpGenerator() {
            @Override
            public int getPageSize(CommandContext context) {
                return 0;
            }

            @Override
            public Comparator<String> getSorter(CommandContext context) {
                return String::compareToIgnoreCase;
            }

            @Override
            public void sendHelp(CommandContext context, TreeMap<String, Optional<String>> help, int page, int maxPage) {
            }

            @Override
            public void sendFullHelp(CommandContext context, HelpInfo info) {
                assertt("test", info.getName());
                assertt("testt, test2, test5", info.getAliases().orElse("(no aliases)"));
                assertt("test <" + ArgumentParsers.INTEGER.getTypename() + ">", info.getSyntax());
                assertt("it do test", info.getDescription().orElse("No description."));
            }

            @Override
            public CommandException createError(CommandContext ctx, String input) {
                return new CommandException("oops");
            }

            @Override
            public CommandException createErrorUnknown(CommandContext ctx, String input) {
                return new CommandException("what");
            }

            private void assertt(Object expected, Object actual) {
                System.out.println(String.format("Testing \"%s\" == \"%s\"", actual, expected));
                assertEquals(expected, actual);
            }
        });

        cm.addCommands(Command.builder()
            .name("test", "testt", "test2", "test5")
            .shortDescription("it do test")
            .executor((ctx, args, dry) -> args.write(ArgumentParsers.INTEGER))
            .build());

        cm.handleCommand("help test");
    }

}
