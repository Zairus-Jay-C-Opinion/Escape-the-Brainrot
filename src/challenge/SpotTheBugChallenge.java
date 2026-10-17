package challenge;

import java.util.List;

// TODO (Andrew)
/** Challenge where the player picks the line of code that contains the bug. */
public class SpotTheBugChallenge extends Challenge {

    private int buggyLineIndex; // TODO (Andrew): which line (0-based) holds the bug

    /** @param codeWithBug the code to show; @param lines the selectable lines; @param buggyLineIndex the right one */
    public SpotTheBugChallenge(String codeWithBug, List<String> lines, int buggyLineIndex) {
        super(codeWithBug, lines);
        // TODO (Andrew)
    }

    /** True if the chosen line number (counted from 1, e.g. "2") is the buggy one. */
    @Override
    public boolean isCorrect(String answer) {
        // TODO (Andrew)
        return false;
    }

    @Override
    public String getTypeLabel() {
        // TODO (Andrew)
        return "Spot the Bug";
    }
}
