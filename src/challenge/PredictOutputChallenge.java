package challenge;

import java.util.List;

/** "What does this code print?" multiple-choice challenge. Owner: Andrew (written by Zrs as the working example). */
public class PredictOutputChallenge extends Challenge {

    private final int correctIndex; // 0-based index into the choices

    public PredictOutputChallenge(String code, List<String> choices, int correctIndex) {
        super("What does this print?\n" + code, choices);
        this.correctIndex = correctIndex;
    }

    @Override
    public boolean isCorrect(String answer) {
        // Answers are 1-based ("1" is the first choice) but correctIndex is 0-based.
        return answer.trim().equals(String.valueOf(correctIndex + 1));
    }

    @Override
    public String getTypeLabel() {
        return "Predict the Output";
    }
}
