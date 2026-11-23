package battle;

import engine.Config;

import java.awt.Color;
import java.util.List;

/**
 * Final Boss: 67. Three phases covering all 4 OOP pillars, ending with an existential climax question.
 */
public class Boss67 extends Boss {

    public Boss67() {
        super("67", 7, Config.getInt("rot.final.boss"));
    }

    @Override
    public String getConcept() {
        return "All 4 OOP Pillars";
    }

    @Override
    public String getSpritePath() {
        return "/sprites/bosses/sixty_seven.png";
    }

    @Override
    public String getPhase2SpritePath() {
        return "/sprites/bosses/sixty_seven_phase2.png";
    }

    @Override
    public String getPhase3SpritePath() {
        return "/sprites/bosses/sixty_seven_phase3.png";
    }

    @Override
    public List<String> getChallengeFiles() {
        return List.of("/challenges/sixty_seven.txt");
    }

    @Override
    public String getClimaxChallengeFile() {
        return "/challenges/sixty_seven_climax.txt";
    }

    @Override
    public boolean hasPhase2() {
        return true;
    }

    @Override
    public int getPhase2Hp() {
        return 6;
    }

    @Override
    public boolean hasPhase3() {
        return true;
    }

    @Override
    public int getPhase3Hp() {
        return 4;
    }

    @Override
    public String getIntroLine() {
        return "67... 67... Endless feeds, zero meaning. Can your code even think?";
    }

    @Override
    public String getHitLine() {
        return "Signal static... a crack in the algorithm!";
    }

    @Override
    public String getWrongLine() {
        return "Scroll deeper. 67 consumes another thought.";
    }

    @Override
    public String getPhase2Line() {
        return "PHASE 2: FEED ACCELERATION! Four pillars, zero mercy!";
    }

    @Override
    public String getPhase3Line() {
        return "PHASE 3: SINGULARITY 67! Choices scrambled, mind on overdrive!";
    }

    @Override
    public String getDefeatLine() {
        return "67... offline. The feed goes quiet. Your mind is yours again.";
    }

    @Override
    public PhaseStyle getPhase1Style() {
        return new PhaseStyle(new Color(14, 18, 30), new Color(130, 210, 255));
    }

    @Override
    public PhaseStyle getPhase2Style() {
        return new PhaseStyle(new Color(55, 8, 22), new Color(255, 70, 90));
    }

    @Override
    public PhaseStyle getPhase3Style() {
        return new PhaseStyle(new Color(38, 0, 28), new Color(255, 35, 130));
    }

    @Override
    public void onDefeat() { }
}
