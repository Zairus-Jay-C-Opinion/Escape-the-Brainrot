package battle;

import engine.Config;

import java.awt.Color;
import java.util.List;

public class LiriliLarila extends Boss {
    public LiriliLarila() {
        super("Liril\u00ec Laril\u00e0", 7, Config.getInt("rot.concept.boss"));
    }

    @Override
    public String getConcept() {
        return "Abstraction";
    }

    @Override
    public String getSpritePath() {
        return "/sprites/bosses/lirili_larila.png";
    }

    @Override
    public List<String> getChallengeFiles() {
        return List.of("/challenges/lirili.txt");
    }

    @Override
    public String getIntroLine() {
        return "Liril\u00ec Laril\u00e0! An elephant in the desert walks here and there...";
    }

    @Override
    public String getHitLine() {
        return "Tick-tock... my cactus thorns cracked!";
    }

    @Override
    public String getWrongLine() {
        return "Brrr, patapim! Sandals stomp your sloppy code!";
    }

    @Override
    public String getDefeatLine() {
        return "Liril\u00ec... laril\u00e0... the desert clock stops ticking...";
    }

    @Override
    public PhaseStyle getPhase2Style() {
        return new PhaseStyle(new Color(18, 48, 22), new Color(120, 255, 90));
    }

    @Override
    public String getPhase2Line() {
        return "LIRIL\u00cc LARIL\u00c0! CACTUS OVERGROWTH! TIME TO ABSTRACT YOUR MIND!";
    }

    @Override
    public boolean hasPhase2() {
        return true;
    }

    @Override
    public int getPhase2Hp() {
        return 4;
    }

    @Override
    public String getPhase2SpritePath() {
        return "/sprites/bosses/lirili_larila_phase3.png";
    }

    @Override
    public void onDefeat() { }
}
