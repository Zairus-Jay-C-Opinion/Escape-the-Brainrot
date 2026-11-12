package challenge;

import battle.Difficulty;

import java.util.Collections;
import java.util.List;

/**
 * Abstract base for every kind of code challenge. Owner: Andrew.
 * Subclasses decide how an answer is checked (polymorphism); callers use evaluateAnswer or isCorrect.
 */
public abstract class Challenge {

    private final String id;
    private final String prompt;
    private final String codeSnippet;
    private final Difficulty difficulty;
    private final List<String> choices;

    /** Constructor matching Andrew's UML architecture. */
    protected Challenge(String id, String prompt, String codeSnippet, Difficulty difficulty) {
        this.id = id != null ? id : "";
        this.prompt = prompt != null ? prompt : "";
        this.codeSnippet = codeSnippet != null ? codeSnippet : "";
        this.difficulty = difficulty != null ? difficulty : Difficulty.EASY;
        this.choices = Collections.emptyList();
    }

    /** Constructor for choice-based challenges. */
    protected Challenge(String prompt, List<String> choices) {
        this.id = "";
        this.prompt = prompt != null ? prompt : "";
        this.codeSnippet = "";
        this.difficulty = Difficulty.EASY;
        this.choices = choices != null ? List.copyOf(choices) : Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    /** The question text. May contain several lines separated by \n. */
    public String getPrompt() {
        return prompt;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public List<String> getChoices() {
        return choices;
    }

    /**
     * Checks the player's answer. Default delegates to isCorrect.
     */
    public boolean evaluateAnswer(String answer) {
        return isCorrect(answer);
    }

    /**
     * Legacy method: checks the player's answer. Default delegates to evaluateAnswer.
     */
    public boolean isCorrect(String answer) {
        return false;
    }

    public String getExplanation() {
        return "";
    }

    /** Short name of the challenge type, shown on the battle screen. */
    public String getTypeLabel() {
        return getClass().getSimpleName();
    }
}
