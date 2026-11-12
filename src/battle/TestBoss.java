package battle;

import engine.Config;

/** Placeholder boss used to test the battle flow. Owner: Andrew. */
public class TestBoss extends Boss {

    public TestBoss() {
        super("Test Boss", 3, Config.getInt("rot.concept.boss"));
    }

    @Override
    public String getIntroLine() {
        return "A placeholder brainrot blocks your path!";
    }

    @Override
    public void onDefeat() {
        System.out.println(getName() + " has been defeated! Locked Rot is cleared.");
    }
}
