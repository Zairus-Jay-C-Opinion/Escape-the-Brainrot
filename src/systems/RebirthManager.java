package systems;

import engine.Config;

/**
 * Handles brain-fried rebirths up to the configured maximum.
 */
public class RebirthManager {

    private int rebirthCount;

    public RebirthManager() {
        this(0);
    }

    public RebirthManager(int initialRebirths) {
        this.rebirthCount = Math.max(0, initialRebirths);
    }

    public int getRebirthCount() {
        return rebirthCount;
    }

    public boolean canRebirth() {
        return rebirthCount < Config.getMaxRebirths();
    }

    /**
     * Applies a rebirth when Strain hits the fried threshold.
     * Returns true if rebirth succeeded, or false if max rebirths were already used.
     */
    public boolean triggerRebirth(BrainrotGauge gauge) {
        if (!canRebirth()) {
            return false;
        }
        rebirthCount++;
        if (gauge != null) {
            gauge.setStrain(Config.getStartStrain());
        }
        return true;
    }
}
