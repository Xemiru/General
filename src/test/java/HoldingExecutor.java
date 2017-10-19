import com.github.xemiru.general.CommandExecutor;

public interface HoldingExecutor extends CommandExecutor {

    Object getHeld();

    Throwable getHeldError();

}
