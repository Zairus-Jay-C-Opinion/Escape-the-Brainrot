package battle;

import engine.Config;

/** Question difficulty. It decides how often Strain ticks up and by how much. Numbers come from config.properties. */
public enum Difficulty {
    EASY,
    MEDIUM,
    HARD;

    /** Seconds between automatic Strain increases. */
    public int getStrainIntervalSeconds() {
        return Config.getInt("strain." + configName() + ".interval");
    }

    /** Strain added per automatic tick. Wrong answers use it as their base cost too. */
    public int getStrainPenalty() {
        return Config.getInt("strain." + configName() + ".penalty");
    }

    private String configName() {
        return name().toLowerCase();
    }
}
