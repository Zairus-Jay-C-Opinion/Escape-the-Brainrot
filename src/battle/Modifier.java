package battle;

import engine.Config;

/** A twist applied to a Phase: faster Strain ticks and/or shuffled choices. */
public enum Modifier {
    NONE(null, false),
    OVERCLOCK("modifier.overclock.intervalMultiplier", false),
    OVERDRIVE("modifier.overdrive.intervalMultiplier", true);

    private final String intervalKey; // config key, or null when the modifier changes nothing
    private final boolean shufflesChoices;

    Modifier(String intervalKey, boolean shufflesChoices) {
        this.intervalKey = intervalKey;
        this.shufflesChoices = shufflesChoices;
    }

    /** Multiply the Strain interval by this. Below 1 means ticks come sooner. */
    public double getIntervalMultiplier() {
        return intervalKey == null ? 1.0 : Config.getDouble(intervalKey);
    }

    /** Whether multiple-choice options are shuffled during this phase. */
    public boolean shufflesChoices() {
        return shufflesChoices;
    }
}
