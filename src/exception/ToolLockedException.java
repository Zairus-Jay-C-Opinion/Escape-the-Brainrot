package exception;

// TODO (Aeron)
/** Thrown when the player tries to use a tool or item that is still locked. For systems/Usable. */
public class ToolLockedException extends Exception {

    public ToolLockedException(String message) {
        super(message);
    }
}
