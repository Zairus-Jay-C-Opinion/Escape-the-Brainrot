package challenge;

import battle.Difficulty;

import java.util.List;

/** "What does this code print?" challenge. Owner: Andrew. */
public class PredictOutputChallenge extends Challenge {

    private final String expectedOutput;
    private final int correctIndex; // 0-based index for multiple-choice

    public PredictOutputChallenge(String code, List<String> choices, int correctIndex) {
        super("", "What does this print?", code, Difficulty.EASY, choices);
        this.correctIndex = correctIndex;
        this.expectedOutput = String.valueOf(correctIndex + 1);
    }

    public PredictOutputChallenge(String id, String prompt, String codeSnippet, Difficulty difficulty, String expectedOutput) {
        this(id, prompt, codeSnippet, difficulty, expectedOutput, List.of(), -1);
    }

    /** Multiple-choice version with a difficulty, used by the loader. correctIndex is 0-based. */
    public PredictOutputChallenge(String id, String prompt, String codeSnippet, Difficulty difficulty,
                                  String expectedOutput, List<String> choices, int correctIndex) {
        super(id, prompt != null && !prompt.isEmpty() ? prompt : "What does this print?", codeSnippet, difficulty, choices);
        this.expectedOutput = expectedOutput != null ? expectedOutput.trim() : "";
        this.correctIndex = correctIndex;
    }

    @Override
    public boolean evaluateAnswer(String answer) {
        if (answer == null) {
            return false;
        }
        String clean = answer.trim();
        if (correctIndex >= 0 && clean.equals(String.valueOf(correctIndex + 1))) {
            return true;
        }
        return clean.equalsIgnoreCase(expectedOutput);
    }

    @Override
    public boolean isCorrect(String answer) {
        return evaluateAnswer(answer);
    }

    @Override
    public String getExplanation() {
        return "Expected output: " + expectedOutput;
    }

    @Override
    protected String getDefaultHint() {
        return "Trace the code one line at a time and track every variable.";
    }

    @Override
    public AnswerInput getAnswerInput() {
        return getChoices().isEmpty() ? AnswerInput.TYPED : AnswerInput.CHOICE;
    }

    @Override
    public String getInputHint() {
        return getChoices().isEmpty() ? "Type the exact output" : "Pick an answer";
    }

    @Override
    public String getTypeLabel() {
        return "Predict the Output";
    }
}
