import com.github.xemiru.general.Command;
import com.github.xemiru.general.CommandContext;
import com.github.xemiru.general.CommandExecutor;
import com.github.xemiru.general.CommandManager;
import com.github.xemiru.general.misc.CustomKey;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.github.xemiru.general.misc.CustomKey.key;
import static org.junit.Assert.assertEquals;

public class TestCustoms {

    private CommandManager cm;

    @Test
    public void testCustoms() {
        this.cm = new CommandManager();
        System.out.println("\nTEST CUSTOMS\n");

        testCustomContext(key(), "string boi");
        testCustomCommand(key(), 495);
    }

    private <T> void testCustomContext(CustomKey<T> key, T value) {
        exec(ctx -> ctx.setCustom(key, value), null, (ctx, args, dry) -> {
            System.out.println(String.format("Checking context for custom value %s", value));
            assertEquals(value, ctx.getCustom(key));
        });
    }

    private <T> void testCustomCommand(CustomKey<T> key, T value) {
        exec(null, cmd -> cmd.setCustom(key, value), (ctx, args, dry) -> {
            System.out.println(String.format("Checking command for custom value %s", value));
            assertEquals(value, ctx.getCommand().getCustom(key));
        });
    }

    private void exec(Function<CommandContext, CommandContext> contextFactory,
                      Function<Command.Builder, Command.Builder> commandMod,
                      CommandExecutor exec) {
        if (commandMod == null) commandMod = cmd -> cmd;
        List<Command> commands = new ArrayList<>();
        commands.add(commandMod.apply(Command.builder())
            .name("test")
            .executor(exec)
            .build());

        cm.handleCommand("test", contextFactory, commands);
    }

}
