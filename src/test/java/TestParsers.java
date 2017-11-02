import com.github.xemiru.general.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.github.xemiru.general.ArgumentParsers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestParsers {

    @Test
    public void parsers() {
        System.out.println("\nTEST PARSERS\n");

        assertEquals("pop", test(STRING, "pop"));
        assertEquals("pop", test(STRING, "pop py"));
        assertEquals("pop '", test(STRING, "\"pop '\""));
        assertEquals("pop \"", test(STRING, "'pop \"'"));
        assertEquals("pop py", test(REMAINING_STRING, "pop py"));

        assertEquals(2.4, test(NUMBER, "2.4"));
        assertEquals(-12, (int) test(INTEGER, "-12"));

        System.out.println("\nTEST GENERATORS\n");

        assertEquals(2, (int) test(anyOf(INTEGER, "2", "4"), "2"));
        assertEquals(3.5, test(or(INTEGER, NUMBER), "3.5"));
        assertEquals(Arrays.asList(1, 2, 3, 4), test(remain(INTEGER), "1 2 3 4"));
        assertEquals("poopy", test(opt(STRING, "poopy"), ""));
        assertEquals(4, (int) test(alt(INTEGER, 4), ""));
        assertEquals("poopy", test(lenient(anyOf(STRING, "poopy", "b", "c"), "poopy"), ""));
        assertEquals(3, (int) test(fallback(anyOf(INTEGER, "1", "2", "3"), 3), ""));
        assertEquals("waht", rename(STRING, "waht").getTypename());

        System.out.println("\nTEST BAD VALUES\n");

        testEx(STRING, "\"pop"); // incomplete quote
        testEx(STRING, "\"pop\"ds"); // invalid ending
        testEx(NUMBER, "2.49823.983"); // not a number
        testEx(INTEGER, "4-4"); // not a number
        testEx(INTEGER, "4.0"); // not an integer
    }

    private void testEx(ArgumentParser<?> parser, String input) {
        try {
            test(parser, input);
            fail("Parser did not throw exception");
        } catch (RuntimeException e) {
            System.out.println("ex = " + e.getCause().getMessage());
        }
    }

    private CommandManager mgr;

    private <T> T test(ArgumentParser<T> parser, String input) {
        if (mgr == null) this.mgr = new CommandManager();

        String[] inputArr = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String str : inputArr) sb.append(str).append(",");

        System.out.println(String.format("parser = %s | input = {%s}", parser.getTypename(),
            sb.toString().substring(0, sb.length() - 1)));

        Set<Command> commands = new HashSet<>();
        HoldingExecutor exec = new HoldingExecutor() {

            private Object value = null;
            private Throwable heldErr = null;

            @Override
            public Object getHeld() {
                return value;
            }

            @Override
            public Throwable getHeldError() {
                return this.heldErr;
            }

            @Override
            public void execute(CommandContext context, Arguments args, boolean dry) {
                try {
                    args.write(parser);
                    if (dry) return;
                    this.value = args.next();
                } catch (Throwable err) {
                    this.heldErr = err;
                }
            }
        };

        commands.add(Command.builder()
            .name("test")
            .executor(exec).build());

        mgr.handleCommand("test " + input, null, commands);
        if (exec.getHeldError() != null) throw new RuntimeException(exec.getHeldError());

        T value = (T) exec.getHeld();
        System.out.println("output = " + value);
        return value;
    }

}
