package exception;

// TODO (Aeron)
/** Thrown when a save file cannot be written, read, or is corrupted. For systems/SaveManager. */
public class SaveDataException extends Exception {

    public SaveDataException(String message) {
        super(message);
    }

    public SaveDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
