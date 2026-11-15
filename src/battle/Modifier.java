package battle;

import engine.Config;

/** A twist applied to a Phase. For now the only real one makes Strain tick faster. */
public enum Modifier {
    NONE(null),
    OVERCLOCK("modifier.overclock.intervalMultiplier");

    private final String intervalKey; // config key, or null when the modifier changes nothing

    Modifier(String intervalKey) {
        this.intervalKey = intervalKey;
    }

    /** Multiply the Strain interval by this. Below 1 means ticks come sooner. */
    public double getIntervalMultiplier() {
        return intervalKey == null ? 1.0 : Config.getDouble(intervalKey);
    }
}
