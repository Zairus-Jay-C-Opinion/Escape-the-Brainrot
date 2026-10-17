package challenge;

import java.util.List;

/**
 * Abstract base for every kind of code challenge. Owner: Andrew (written by Zrs as the working example).
 * Subclasses decide how an answer is checked (polymorphism); callers only use this class.
 */
public abstract class Challenge {

    private final String prompt;
    private final List<String> choices;

    protected Challenge(String prompt, List<String> choices) {
        this.prompt = prompt;
        this.choices = List.copyOf(choices); // unmodifiable copy so outside code can't change the answers
    }

    /** The question text. May contain several lines separated by \n. */
    public String getPrompt() {
        return prompt;
    }

    public List<String> getChoices() {
        return choices;
    }

    /**
     * Checks the player's answer, always passed as a String so every challenge type shares one signature:
     * a multiple-choice pick is the choice number counted from 1 ("2"), an arrange-the-code ordering is
     * the line numbers in order ("3,1,2,4"), and a fill-in-the-blank answer is the typed word.
     */
    public abstract boolean isCorrect(String answer);

    /** Short name of the challenge type, shown on the battle screen. */
    public abstract String getTypeLabel();
}
