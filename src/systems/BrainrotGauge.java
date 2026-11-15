package systems;

import engine.Config;

/**
 * The Brainrot Gauge: two separate meters.
 * Rot is locked per undefeated boss and only drops when that boss is defeated.
 * Strain rises with time, wrong answers and AI use, falls with correct answers, and fries the player at the threshold.
 * All numbers come from config.properties.
 */
public class BrainrotGauge {

    private double rot;
    private double strain;
    private final double maxStrain;
    private final double friedThreshold;

    public BrainrotGauge() {
        this.rot = Config.getDouble("rot.start");
        this.strain = Config.getDouble("strain.start");
        this.maxStrain = Config.getDouble("gauge.max");
        this.friedThreshold = Config.getDouble("strain.fried.threshold");
    }

    public double getRot() {
        return rot;
    }

    public double getStrain() {
        return strain;
    }

    /** Raises Strain by the given amount. */
    public void addStrain(double amount) {
        if (amount <= 0) {
            return;
        }
        strain = Math.min(maxStrain, strain + amount);
    }

    /** Lowers Strain by the given amount (never below 0). */
    public void reduceStrain(double amount) {
        if (amount <= 0) {
            return;
        }
        strain = Math.max(0, strain - amount);
    }

    /** Called when a boss is defeated: clears that boss's Rot. */
    public void clearRot(int amount) {
        if (amount <= 0) {
            return;
        }
        rot = Math.max(0, rot - amount);
    }

    /** Puts Strain back to its starting value, used after the brain gets fried. */
    public void resetStrain() {
        strain = Config.getDouble("strain.start");
    }

    /** True once Strain reaches the fried threshold. Rot does not count. */
    public boolean isFried() {
        return strain >= friedThreshold;
    }

    // Small test runner for Week 2
    public static void main(String[] args) {
        try {
            Config.load();
            BrainrotGauge gauge = new BrainrotGauge();

            System.out.println("Start  -> Rot: " + gauge.getRot()
                    + ", Strain: " + gauge.getStrain()
                    + ", Fried: " + gauge.isFried());

            // 1. Correct answer reduces strain (50 -> 35)
            gauge.reduceStrain(15);
            System.out.println("After -15 Strain -> Strain: " + gauge.getStrain());

            // 2. Strain cannot go below 0 (35 - 50 -> 0)
            gauge.reduceStrain(50);
            System.out.println("Floor test (-50) -> Strain: " + gauge.getStrain());

            // 3. Defeating a concept boss clears Rot (40 - 6 -> 34) and leaves Strain alone
            gauge.clearRot(Config.getInt("rot.concept.boss"));
            System.out.println("Boss defeated    -> Rot: " + gauge.getRot()
                    + ", Strain: " + gauge.getStrain());

            // 4. Rot never fries on its own, even with Strain at 0
            System.out.println("Rot only         -> Fried: " + gauge.isFried());

            // 5. Strain is capped and fries at the threshold
            gauge.addStrain(150);
            System.out.println("Max cap (+150)   -> Strain: " + gauge.getStrain()
                    + ", Fried: " + gauge.isFried());

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
        }
    }
}
