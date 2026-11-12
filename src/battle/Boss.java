package battle;

/**
 * Abstract boss base class with HP, locked Rot, and phase transitions.
 * Owner: Andrew.
 */
public abstract class Boss {
    private final String name;
    private final int maxHp;
    private int currentHp;
    private final int lockedRot;
    private Phase currentPhase;

    public Boss(String name, int maxHp, int lockedRot) {
        this.name = name;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.lockedRot = lockedRot;
        this.currentPhase = Phase.NORMAL;
    }

    public Boss(String name, int lockedRot) {
        this(name, 3, lockedRot);
    }

    public String getName() {
        return name;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getLockedRot() {
        return lockedRot;
    }

    /** Compatibility alias for earlier prototype references. */
    public int getRotValue() {
        return lockedRot;
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public void takeDamage(int dmg) {
        this.currentHp = Math.max(0, currentHp - dmg);
        checkPhaseTransition();
    }

    private void checkPhaseTransition() {
        if (!isDefeated() && currentPhase == Phase.NORMAL && currentHp <= maxHp / 2) {
            currentPhase = Phase.ENRAGED;
            onPhaseChange(currentPhase);
        }
    }

    public boolean isDefeated() {
        return currentHp <= 0;
    }

    /**
     * Hook called when the boss enters a new phase. Subclasses may override.
     */
    public void onPhaseChange(Phase newPhase) {
        System.out.println(name + " entered " + newPhase.getLabel() + "!");
    }

    public String getIntroLine() {
        return "A brainrot entity approaches!";
    }

    public abstract void onDefeat();
}
