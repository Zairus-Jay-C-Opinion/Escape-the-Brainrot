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
        if (isDefeated()) {
            return;
        }
        if (hasPhase3() && currentPhase != Phase.FINAL && currentHp <= getPhase3Hp()) {
            currentPhase = Phase.FINAL;
            onPhaseChange(currentPhase);
        } else if (hasPhase2() && currentPhase == Phase.NORMAL && currentHp <= getPhase2Hp()) {
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

    /** Whether this boss enters Phase 2 at low HP. Early bosses stay in Phase 1. */
    public boolean hasPhase2(){
        return false;
    }

    /** HP at or below which Phase 2 begins (equal to the number of Phase 2 questions). */
    public int getPhase2Hp(){
        return maxHp / 2;
    }

    /** Whether this boss enters Phase 3 at critical HP. */
    public boolean hasPhase3() {
        return false;
    }

    /** HP at or below which Phase 3 begins (equal to the number of Phase 3 questions). */
    public int getPhase3Hp() {
        return maxHp / 3;
    }

    /** The OOP concept this boss tests, shown on the code terminal. */
    public String getConcept() {
        return "Java OOP";
    }

    /** Classpath path of the sprite image. A missing file is replaced by a silhouette. */
    public String getSpritePath() {
        return "/sprites/bosses/missing.png";
    }

    public String getPhase2SpritePath() {
        return null;
    }

    public String getPhase3SpritePath() {
        return null;
    }

    public String getPhaseSpritePath(Phase phase) {
        return switch (phase) {
            case NORMAL -> getSpritePath();
            case ENRAGED -> getPhase2SpritePath();
            case FINAL -> getPhase3SpritePath();
        };
    }

    /** Optional challenge file used exclusively for the final HP point (e.g., 67's climax question). */
    public String getClimaxChallengeFile() {
        return null;
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

    public String getPhase3Line() {
        return "This is my final form!";
    }

    public String getPhaseLine(Phase phase) {
        return switch (phase) {
            case NORMAL -> getIntroLine();
            case ENRAGED -> getPhase2Line();
            case FINAL -> getPhase3Line();
        };
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

    public PhaseStyle getPhase3Style() {
        return PhaseStyle.FINAL;
    }

    public PhaseStyle getStyle(Phase phase) {
        return switch (phase) {
            case NORMAL -> getPhase1Style();
            case ENRAGED -> getPhase2Style();
            case FINAL -> getPhase3Style();
        };
    }
}
