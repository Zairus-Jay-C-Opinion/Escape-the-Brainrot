package exception;

/** Thrown when a resource (image, config, map, text file) cannot be found or read. Owner: Zrs (used by engine/). */
public class AssetLoadException extends Exception {

    public AssetLoadException(String message) {
        super(message);
    }

    public AssetLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
