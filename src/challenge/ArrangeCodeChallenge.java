package challenge;

import java.util.List;

// TODO (Andrew)
/** Challenge where the player puts shuffled lines of code into the right order. */
public class ArrangeCodeChallenge extends Challenge {

    private List<String> correctOrder; // TODO (Andrew): the lines in their correct order

    /** @param shuffledLines the lines as shown to the player; @param correctOrder the solution */
    public ArrangeCodeChallenge(String prompt, List<String> shuffledLines, List<String> correctOrder) {
        super(prompt, shuffledLines);
        // TODO (Andrew)
    }

    /** True if the player's ordering matches the solution. The answer is line numbers in order, e.g. "3,1,2,4". */
    @Override
    public boolean isCorrect(String answer) {
        // TODO (Andrew)
        return false;
    }

    @Override
    public String getTypeLabel() {
        // TODO (Andrew)
        return "Arrange the Code";
    }
}
