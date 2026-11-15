package battle;

import engine.Config;

/** Boss battle phases. Each one carries its own Modifier. */
public enum Phase {
    NORMAL(1, Modifier.NONE),
    ENRAGED(2, Modifier.OVERCLOCK);

    private final int number;
    private final Modifier modifier;

    Phase(int number, Modifier modifier) {
        this.number = number;
        this.modifier = modifier;
    }

    public int getNumber() {
        return number;
    }

    public String getLabel() {
        return "PHASE " + number;
    }

    public Modifier getModifier() {
        return modifier;
    }

    /** How much more a wrong answer costs in this phase. */
    public double getStrainMultiplier() {
        return this == ENRAGED ? Config.getDouble("phase2.wrongMultiplier") : 1.0;
    }
}
