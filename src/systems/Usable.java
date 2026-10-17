package systems;

import exception.ToolLockedException;

// TODO (Aeron)
/** Anything the player can use during a battle (clue scroll, answer key, AI assistant). */
public interface Usable {

    /** Uses this tool. Throws ToolLockedException if it isn't available yet. */
    void use(BrainrotGauge gauge) throws ToolLockedException;
}
