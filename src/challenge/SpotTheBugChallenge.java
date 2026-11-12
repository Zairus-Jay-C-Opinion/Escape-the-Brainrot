package challenge;

import battle.Difficulty;

/**
 * Challenge where the player identifies the line number containing a bug.
 * Owner: Andrew.
 */
public class SpotTheBugChallenge extends Challenge {
    private final String buggyLine;
    private final String explanation;

    public SpotTheBugChallenge(String id, String prompt, String codeSnippet, Difficulty difficulty, String buggyLine, String explanation) {
        super(id, prompt, codeSnippet, difficulty);
        this.buggyLine = buggyLine != null ? buggyLine.trim() : "";
        this.explanation = explanation;
    }

    @Override
    public boolean evaluateAnswer(String playerAnswer) {
        if (playerAnswer == null) {
            return false;
        }
        // Normalize inputs so both "3" and "line 3" are accepted
        String cleaned = playerAnswer.trim().toLowerCase().replace("line", "").trim();
        return cleaned.equals(buggyLine);
    }

    @Override
    public boolean isCorrect(String answer) {
        return evaluateAnswer(answer);
    }

    @Override
    public String getExplanation() {
        if (explanation == null || explanation.isEmpty()) {
            return "The bug was on line " + buggyLine + ".";
        }
        return explanation;
    }

    @Override
    public String getTypeLabel() {
        return "Spot the Bug";
    }
}
