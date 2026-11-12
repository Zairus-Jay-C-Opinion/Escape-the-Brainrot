package challenge;

import battle.Difficulty;

/**
 * Challenge where the player arranges scrambled lines of code into the valid order.
 * Owner: Andrew.
 */
public class ArrangeCodeChallenge extends Challenge {
    private final String expectedOrder;

    public ArrangeCodeChallenge(String id, String prompt, String codeSnippet, Difficulty difficulty, String expectedOrder) {
        super(id, prompt, codeSnippet, difficulty);
        this.expectedOrder = normalizeOrder(expectedOrder);
    }

    // Converts inputs like "2, 1, 3" or "2 1 3" into "2,1,3"
    private String normalizeOrder(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("[\\s,]+", ",");
    }

    @Override
    public boolean evaluateAnswer(String playerAnswer) {
        if (playerAnswer == null || playerAnswer.isBlank()) {
            return false;
        }
        return normalizeOrder(playerAnswer).equals(expectedOrder);
    }

    @Override
    public boolean isCorrect(String answer) {
        return evaluateAnswer(answer);
    }

    @Override
    public String getExplanation() {
        return "The correct line sequence is: " + expectedOrder;
    }

    @Override
    public String getTypeLabel() {
        return "Arrange the Code";
    }
}
