package systems;

// TODO (Aeron)
/** Running totals for the current playthrough (bosses defeated, correct and wrong answers, play time). */
public class PlayerStats {

    private int bossesDefeated;  // TODO (Aeron)
    private int correctAnswers;  // TODO (Aeron)
    private int wrongAnswers;    // TODO (Aeron)
    private long secondsPlayed;  // TODO (Aeron)

    public void recordCorrectAnswer() {
        // TODO (Aeron)
    }

    public void recordWrongAnswer() {
        // TODO (Aeron)
    }

    public void recordBossDefeated() {
        // TODO (Aeron)
    }

    public int getBossesDefeated() {
        // TODO (Aeron)
        return 0;
    }
}
