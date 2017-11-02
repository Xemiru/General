import com.github.xemiru.general.Command;
import com.github.xemiru.general.CommandManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestRegistry {

    public class Dummy {
        public boolean value = false;
    }

    @Test
    public void testRegistry() {

        System.out.println("\nTEST REGISTRY\n");

        CommandManager cm = new CommandManager();
        Dummy dummy = new Dummy();

        cm.addCommands(Command.builder()
            .name("test")
            .executor((ctx, args, dry) -> {
                dummy.value = true;
                System.out.println("Command changed dummy value");
            })
            .build());

        System.out.println("Registering and running command test");
        cm.handleCommand("test");

        assertEquals(true, dummy.value);
    }

}
