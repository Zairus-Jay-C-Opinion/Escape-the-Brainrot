package systems;

import exception.ToolLockedException;

// TODO (Aeron)
/** Item that reveals the correct answer to the current question. */
public class AnswerKey extends Item implements Usable {

    public AnswerKey() {
        super("Answer Key", "Reveals the correct answer."); // TODO (Aeron): final wording
    }

    @Override
    public void use(BrainrotGauge gauge) throws ToolLockedException {
        // TODO (Aeron)
    }
}
