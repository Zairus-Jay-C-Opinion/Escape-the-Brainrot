package battle;

import engine.Config;
import java.util.List;

/**
 * Boss #2: Bombardiro Crocodilo
 * Focuses on Java Inheritance. It has 1 phase and 4 HP.
 * Owner: Andrew.
 */
public class BombardiroCrocodilo extends Boss {

    public BombardiroCrocodilo() {
        // Name, 4 HP (1 Phase), locked Rot from Config
        super("Bombardiro Crocodilo", 4, Config.getInt("rot.concept.boss"));
    }

    @Override
    public String getConcept() {
        return "Inheritance";
    }

    @Override
    public String getSpritePath() {
        return "/sprites/bosses/bombardiro_crocodilo.png";
    }

    @Override
    public List<String> getChallengeFiles() {
        return List.of("/challenges/bombardiro.txt");
    }

    @Override
    public String getIntroLine() {
        return "Bombardiro Crocodilo dives from the sky! Inherit or be bombed!";
    }

    @Override
    public String getHitLine() {
        return "Direct hit! Armor plating breached!";
    }

    @Override
    public String getWrongLine() {
        return "Target missed! Your superclass failed you!";
    }

    @Override
    public String getDefeatLine() {
        return "Mayday... Mayday... Bombardiro Crocodilo is going down...";
    }

    @Override
    public void onDefeat() {
        System.out.println(getName() + " defeated! Inheritance Rot unlocked.");
    }
}

