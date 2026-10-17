package systems;

// TODO (Aeron)
/**
 * The Brainrot Gauge = Rot + Strain. Fried at 100.
 * Rot is locked per undefeated boss and only drops when that boss is defeated.
 * Strain rises with time, wrong answers and AI use, and falls with correct answers.
 * All numbers come from config.properties.
 */
public class BrainrotGauge {

    private double rot;    // TODO (Aeron): starts at rot.start
    private double strain; // TODO (Aeron): starts at strain.start

    public BrainrotGauge() {
        // TODO (Aeron)
    }

    /** Rot + Strain. */
    public double getTotal() {
        // TODO (Aeron)
        return 0;
    }

    /** Raises Strain by the given amount. */
    public void addStrain(double amount) {
        // TODO (Aeron)
    }

    /** Lowers Strain by the given amount (never below 0). */
    public void reduceStrain(double amount) {
        // TODO (Aeron)
    }

    /** Called when a boss is defeated: clears that boss's Rot. */
    public void clearRot(int amount) {
        // TODO (Aeron)
    }

    /** True once the total reaches gauge.max. */
    public boolean isFried() {
        // TODO (Aeron)
        return false;
    }
}
