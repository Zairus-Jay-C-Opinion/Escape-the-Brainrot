package battle;

// TODO (Andrew)
/**
 * Question difficulty. It decides how fast Strain rises and how much a wrong answer costs.
 * The numbers must come from config.properties (keys strain.easy.interval, strain.easy.penalty, etc.).
 */
public enum Difficulty {
    EASY,
    MEDIUM,
    HARD;

    /** Seconds between automatic Strain increases. */
    public int getStrainIntervalSeconds() {
        // TODO (Andrew): read strain.<name>.interval from Config
        return 0;
    }

    /** Strain added per tick or wrong answer. */
    public int getStrainPenalty() {
        // TODO (Andrew): read strain.<name>.penalty from Config
        return 0;
    }
}
