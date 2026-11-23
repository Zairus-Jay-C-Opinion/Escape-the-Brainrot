package battle;

import engine.Config;

import java.util.List;

public class ChimpanziniBananini extends Boss {
    public ChimpanziniBananini(){
        super("Chimpanzini Bananini", 2, Config.getInt("rot.concept.boss"));
    }

    @Override
    public String getConcept(){
        return "Encapsulation";
    }

    @Override
    public String getSpritePath(){
        return "/sprites/bosses/chimpanzini_bananini.png";
    }

    @Override
    public List<String> getChallengeFiles(){
        return List.of("/challenges/chimpanzini.txt");
    }

    @Override
    public String getIntroLine(){
        return "Bananucci time! Monkey-monkey-monkey-ucci!";
    }

    @Override
    public String getHitLine(){
        return "Waa, waa, waa! Bananucci!";
    }

    @Override
    public String getWrongLine(){
        return "Mamma mia, badabim! Wrong peel, monkey-ucci!";
    }

    @Override
    public String getDefeatLine(){
        return "Waa, waa, waa... no bananucci...";
    }

    @Override
    public void onDefeat() { }

}
