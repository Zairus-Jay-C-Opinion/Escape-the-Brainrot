package battle;

import engine.Config;

/** Placeholder boss used to test the battle flow. Owner: Andrew (written by Zrs as the working example). */
public class TestBoss extends Boss {

    public TestBoss() {
        super("Test Boss", Config.getInt("rot.concept.boss")); // balance number comes from config.properties
    }

    @Override
    public String getIntroLine() {
        return "A placeholder brainrot blocks your path!";
    }
}
