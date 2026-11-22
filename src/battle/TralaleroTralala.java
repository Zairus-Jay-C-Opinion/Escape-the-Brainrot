package battle;

import engine.Config;

import java.util.List;
import java.awt.Color;

public class TralaleroTralala extends Boss {
    public TralaleroTralala(){
        super("Tralalero Tralala", 5, Config.getInt("rot.concept.boss"));
    }

    @Override
    public String getConcept(){
        return "Polymorphism";
    }

    @Override
    public String getSpritePath(){
        return "/sprites/bosses/tralalero_tralala.png";
    }

    @Override
    public List<String> getChallengeFiles(){
        return List.of("/challenges/tralalero.txt");
    }

    @Override
    public String getIntroLine(){
        return "Tralalero Tralala! Orcalero Orcala!";
    }

    @Override
    public String getHitLine(){
        return "Tralala... no, no, no! Scuffed the sneakers!";
    }

    @Override
    public String getWrongLine(){
        return "Taste the blue Nikes! Tralalero Tralala!";
    }

    @Override
    public String getDefeatLine(){
        return "Tralalero... trala... lost my drift...";
    }

    @Override
    public PhaseStyle getPhase2Style() {
        return new PhaseStyle(new Color(12, 28, 52), new Color(80, 210, 255));
    }

    @Override
    public String getPhase2Line() {
        return "TRALALERO TRALALA! Shoes on turbo, mercy off!";
    }

    @Override
    public boolean hasPhase2(){
        return true;
    }

    @Override
    public int getPhase2Hp(){
        return 2;
    }

    @Override
    public void onDefeat() { }

}
