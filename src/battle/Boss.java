package battle;

import java.util.List;

/**
 * Abstract boss: HP, locked Rot, phase changes, and all the data a fight needs (sprite, questions, dialogue, look).
 * Subclasses override the getters to make a boss their own, so the battle screen never needs to know which boss it has.
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

    /** The share of the player's Rot that disappears when this boss is beaten. */
    public int getRotShare() {
        return lockedRot;
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

    /** Back to full HP and Phase 1, for a retry after the brain got fried. Never call on a defeated boss. */
    public void reset() {
        currentHp = maxHp;
        currentPhase = Phase.NORMAL;
    }

    /** Hook called when the boss enters a new phase. Subclasses may override. */
    public void onPhaseChange(Phase newPhase) { }

    public abstract void onDefeat();

    /** The OOP concept this boss tests, shown on the code terminal. */
    public String getConcept() {
        return "Java OOP";
    }

    /** Classpath path of the sprite image. A missing file is replaced by a silhouette. */
    public String getSpritePath() {
        return "/sprites/bosses/missing.png";
    }

    /** Classpath paths of the challenge files this boss draws its questions from. */
    public List<String> getChallengeFiles() {
        return List.of("/challenges/sample.txt");
    }

    public String getIntroLine() {
        return "A brainrot entity approaches!";
    }

    public String getHitLine() {
        return "Ow.";
    }

    public String getWrongLine() {
        return "Wrong. Try again.";
    }

    public String getPhase2Line() {
        return "You have made me angry.";
    }

    public String getDefeatLine() {
        return "Impossible...";
    }

    public PhaseStyle getPhase1Style() {
        return PhaseStyle.CALM;
    }

    public PhaseStyle getPhase2Style() {
        return PhaseStyle.ENRAGED;
    }

    public PhaseStyle getStyle(Phase phase) {
        return phase == Phase.NORMAL ? getPhase1Style() : getPhase2Style();
    }
}
