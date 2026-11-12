package challenge;

import battle.Difficulty;

import java.util.List;

/** Challenge where the player types the word that fills the blank in a piece of code. Owner: Andrew. */
public class FillBlankChallenge extends Challenge {

    private final String expectedAnswer;

    public FillBlankChallenge(String codeWithBlank, String correctWord) {
        super(codeWithBlank, List.of());
        this.expectedAnswer = correctWord != null ? correctWord.trim() : "";
    }

    public FillBlankChallenge(String id, String prompt, String codeSnippet, Difficulty difficulty, String expectedAnswer) {
        super(id, prompt != null && !prompt.isEmpty() ? prompt : "Fill in the blank:", codeSnippet, difficulty);
        this.expectedAnswer = expectedAnswer != null ? expectedAnswer.trim() : "";
    }

    @Override
    public boolean evaluateAnswer(String answer) {
        if (answer == null) {
            return false;
        }
        return answer.trim().equalsIgnoreCase(expectedAnswer);
    }

    @Override
    public boolean isCorrect(String answer) {
        return evaluateAnswer(answer);
    }

    @Override
    public String getExplanation() {
        return "The missing word was: " + expectedAnswer;
    }

    @Override
    public String getTypeLabel() {
        return "Fill in the Blank";
    }
}
