package systems;

import exception.ToolLockedException;

// TODO (Aeron)
/** Tool that helps with a question but raises Strain every time it is used. */
public class AiAssistant implements Usable {

    private boolean unlocked; // TODO (Aeron)

    @Override
    public void use(BrainrotGauge gauge) throws ToolLockedException {
        // TODO (Aeron): throw ToolLockedException if not unlocked; otherwise add strain to the gauge
    }
}
