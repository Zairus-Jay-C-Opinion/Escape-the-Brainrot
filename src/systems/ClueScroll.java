package systems;

import exception.ToolLockedException;

// TODO (Aeron)
/** Item that gives a hint for the current question. */
public class ClueScroll extends Item implements Usable {

    public ClueScroll() {
        super("Clue Scroll", "A hint for the current question."); // TODO (Aeron): final wording
    }

    @Override
    public void use(BrainrotGauge gauge) throws ToolLockedException {
        // TODO (Aeron)
    }
}
