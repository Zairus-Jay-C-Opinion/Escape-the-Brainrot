package battle;

import java.util.Set;

// TODO (Andrew)
/**
 * One stage of a boss fight. Each boss owns a list of these.
 * A phase starts once the boss's HP drops to hpThreshold, and sets the difficulty and modifiers for its questions.
 */
public class Phase {

    private int hpThreshold;          // TODO (Andrew): HP at or below which this phase begins
    private Difficulty difficulty;    // TODO (Andrew)
    private Set<Modifier> modifiers;  // TODO (Andrew)

    public Phase(int hpThreshold, Difficulty difficulty, Set<Modifier> modifiers) {
        // TODO (Andrew)
    }

    public int getHpThreshold() {
        // TODO (Andrew)
        return 0;
    }

    public Difficulty getDifficulty() {
        // TODO (Andrew)
        return null;
    }

    public Set<Modifier> getModifiers() {
        // TODO (Andrew)
        return null;
    }
}
