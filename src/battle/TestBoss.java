package battle;

import engine.Config;

import java.awt.Color;
import java.util.List;

/** Placeholder boss used to test the battle flow. Owner: Andrew. */
public class TestBoss extends Boss {

    public TestBoss() {
        super("Test Boss", 4, Config.getInt("rot.concept.boss"));
    }

    @Override
    public String getConcept() {
        return "Placeholder";
    }

    @Override
    public String getSpritePath() {
        return "/sprites/bosses/test_boss.png";
    }

    @Override
    public List<String> getChallengeFiles() {
        return List.of("/challenges/level1_challenges.txt", "/challenges/sample.txt", "/challenges/test_extra.txt");
    }

    @Override
    public String getIntroLine() {
        return "A placeholder brainrot blocks your path!";
    }

    @Override
    public String getHitLine() {
        return "Ouch! That was a real answer!";
    }

    @Override
    public String getWrongLine() {
        return "Wrong! Scroll a little more...";
    }

    @Override
    public String getPhase2Line() {
        return "Now I am OVERCLOCKED!";
    }

    @Override
    public boolean hasPhase2(){
        return true;
    }

    @Override
    public String getDefeatLine() {
        return "Placeholder... deleted...";
    }

    @Override
    public PhaseStyle getPhase2Style() {
        return new PhaseStyle(new Color(60, 10, 40), new Color(255, 80, 160));
    }

    @Override
    public void onDefeat() { }
}
