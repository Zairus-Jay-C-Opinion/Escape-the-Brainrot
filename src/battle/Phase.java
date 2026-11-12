package battle;

/**
 * Represents boss battle phases and their difficulty modifiers.
 * Owner: Andrew.
 */
public enum Phase {
    NORMAL("Phase 1: Normal", 1.0),
    ENRAGED("Phase 2: OVERCLOCKED", 1.5);

    private final String label;
    private final double strainMultiplier;

    Phase(String label, double strainMultiplier) {
        this.label = label;
        this.strainMultiplier = strainMultiplier;
    }

    public String getLabel() {
        return label;
    }

    public double getStrainMultiplier() {
        return strainMultiplier;
    }
}
