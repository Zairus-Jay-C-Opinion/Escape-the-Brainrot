package systems;

import engine.Config;

/**
 * The Brainrot Gauge = Rot + Strain. Fried at 100.
 * Rot is locked per undefeated boss and only drops when that boss is defeated.
 * Strain rises with time, wrong answers and AI use, and falls with correct answers.
 * All numbers come from config.properties.
 */
public class BrainrotGauge {

    private double rot; 
    private double strain; 
    private final double maxGauge; 

    public BrainrotGauge() {
        this.rot = Config.getDouble("rot.start");
        this.strain = Config.getDouble("strain.start");
        this.maxGauge = Config.getDouble("gauge.max");
    }

    public double getRot() {
        return rot;
    }

    public double getStrain() {
        return strain;
    }

    /** Rot + Strain. */
    public double getTotal() {
        return Math.min(maxGauge, rot + strain);
    }

    /** Raises Strain by the given amount. */
    public void addStrain(double amount) {
        if (amount <= 0) {
            return;
        }
        strain = Math.min(maxGauge - rot, strain + amount);
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

    /** True once the total reaches gauge.max. */
    public boolean isFried() {
        return getTotal() >= maxGauge;
    }

    // Small test runner for Week 2
    public static void main(String[] args) {
        try {
            Config.load();
            BrainrotGauge gauge = new BrainrotGauge();

            System.out.println("Start  -> Rot: " + gauge.getRot()
                    + ", Strain: " + gauge.getStrain()
                    + ", Total: " + gauge.getTotal()
                    + ", Fried: " + gauge.isFried());

            // 1. Correct answer reduces strain (50 -> 35)
            gauge.reduceStrain(15);
            System.out.println("After -15 Strain -> Strain: " + gauge.getStrain()
                    + ", Total: " + gauge.getTotal());

            // 2. Strain cannot go below 0 (35 - 50 -> 0)
            gauge.reduceStrain(50);
            System.out.println("Floor test (-50) -> Strain: " + gauge.getStrain()
                    + ", Total: " + gauge.getTotal());

            // 3. Defeating a concept boss clears Rot (40 - 6 -> 34)
            gauge.clearRot(Config.getInt("rot.concept.boss"));
            System.out.println("Boss defeated    -> Rot: " + gauge.getRot()
                    + ", Total: " + gauge.getTotal());

            // 4. Adding large strain caps total at 100 and triggers Fried
            gauge.addStrain(80);
            System.out.println("Max cap (+80)    -> Rot: " + gauge.getRot()
                    + ", Strain: " + gauge.getStrain()
                    + ", Total: " + gauge.getTotal()
                    + ", Fried: " + gauge.isFried());

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
        }
    }
}
