package challenge;

import java.util.List;

// TODO (Andrew)
/** Challenge where the player types the word that fills the blank in a piece of code. */
public class FillBlankChallenge extends Challenge {

    private String correctWord; // TODO (Andrew): the word that fills the blank

    /** @param codeWithBlank code containing a blank marker; @param correctWord the word that belongs there */
    public FillBlankChallenge(String codeWithBlank, String correctWord) {
        super(codeWithBlank, List.of()); // no choices: the player types the answer
        // TODO (Andrew)
    }

    /** True if the typed word matches the correct one. */
    @Override
    public boolean isCorrect(String answer) {
        // TODO (Andrew)
        return false;
    }

    @Override
    public String getTypeLabel() {
        // TODO (Andrew)
        return "Fill in the Blank";
    }
}
