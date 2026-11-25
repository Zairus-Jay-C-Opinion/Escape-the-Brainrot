package battle;

import engine.Config;
import java.awt.Color;
import java.util.List;

/**
 * Boss #5: Tung Tung Tung Sahur
 * Tests All 4 OOP Pillars across 3 phases (6 HP).
 * Owner: Andrew.
 */
public class TungTungTungSahur extends Boss {

    public TungTungTungSahur() {
        // Boss #5: All 4 Pillars, 6 HP (2 P1 + 3 P2 + 1 P3), concept boss Rot
        super("Tung Tung Tung Sahur", 6, Config.getInt("rot.concept.boss"));
    }

    @Override
    public String getConcept() {
        return "All 4 OOP Pillars";
    }

    @Override
    public String getSpritePath() {
        return "/sprites/bosses/tung_tung_tung_sahur.png";
    }

    @Override
    public List<String> getChallengeFiles() {
        return List.of("/challenges/tung_tung_tung_sahur.txt");
    }

    @Override
    public boolean hasPhase2() {
        return true;
    }

    @Override
    public int getPhase2Hp() {
        // 3 Phase-2 questions + 1 Phase-3 question remaining = triggers at 4 HP
        return 4;
    }

    @Override
    public String getPhase2SpritePath() {
        return "/sprites/bosses/tung_tung_tung_sahur_p2.png";
    }

    @Override
    public String getPhase2Line() {
        return "TUNG TUNG TUNG! Wake up—the real exception begins!";
    }

    @Override
    public boolean hasPhase3() {
        return true;
    }

    @Override
    public int getPhase3Hp() {
        // 1 Phase-3 question remaining = triggers at 1 HP
        return 1;
    }

    @Override
    public String getPhase3SpritePath() {
        return "/sprites/bosses/tung_tung_tung_sahur_p3.png";
    }

    @Override
    public String getPhase3Line() {
        return "TUNG OVERDRIVE! Core splintering, uncatchable exception!";
    }

    @Override
    public PhaseStyle getPhase3Style() {
        return new PhaseStyle(new Color(55, 14, 8), new Color(255, 100, 25));
    }

    @Override
    public String getIntroLine() {
        return "Tung... tung... tung... Answer before dawn or face an unhandled exception.";
    }

    @Override
    public String getHitLine() {
        return "Crack! Splinters fly off the trunk!";
    }

    @Override
    public String getWrongLine() {
        return "Unhandled exception! Stack trace overflowing!";
    }

    @Override
    public String getDefeatLine() {
        return "Exception caught... finally... silence...";
    }

    @Override
    public void onDefeat() {
        System.out.println(getName() + " defeated! All 4 Pillars Rot unlocked.");
    }
}

