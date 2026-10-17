package exception;

/** Thrown when a map file is missing or malformed. Owner: Zrs (used by world/TileMap). */
public class LevelLoadException extends Exception {

    public LevelLoadException(String message) {
        super(message);
    }

    public LevelLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
