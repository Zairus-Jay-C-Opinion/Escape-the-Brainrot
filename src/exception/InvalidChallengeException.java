package exception;

// TODO (Aeron)
/** Thrown when a challenge file has a malformed question. Currently used by challenge/ChallengeLoader. */
public class InvalidChallengeException extends Exception {

    public InvalidChallengeException(String message) {
        super(message);
    }
}
