package challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import battle.Difficulty;
import battle.Phase;

/** Hands out challenges in shuffled order, with no repeats until every one has been used. */
public class ChallengeDeck {

    private final List<Challenge> all;
    private final List<Challenge> remaining = new ArrayList<>();
    private final Random random = new Random();
    private Challenge last;

    public ChallengeDeck(List<Challenge> challenges){
        this.all = new ArrayList<>(challenges);
    }

    public boolean isEmpty(){
        return all.isEmpty();
    }

    public Challenge draw(){
        refillIfNeeded();
        last = remaining.remove(remaining.size() - 1);
        return last;
    }

    /** The next challenge. Only call when the deck is not empty. */
    public Challenge draw(Phase phase) {
        refillIfNeeded();
        Difficulty firstChoice = phase == Phase.NORMAL ? Difficulty.EASY : Difficulty.HARD;
        Challenge picked = takeDifficulty(firstChoice);

        if (picked == null){
            picked = takeDifficulty(Difficulty.MEDIUM);
        }
        if (picked != null){
            last = picked;
            return picked;
        }
        return draw();
    }

    private void refillIfNeeded(){
        if (remaining.isEmpty()){
            remaining.addAll(all);
            Collections.shuffle(remaining, random);

            if (remaining.size() > 1 && remaining.get(remaining.size() - 1) == last){
                Collections.swap(remaining, 0, remaining.size() - 1);
            }
        }
    }

    private Challenge takeDifficulty(Difficulty target){
        for (int i = remaining.size() - 1; i >= 0; i--){
            if (remaining.get(i).getDifficulty() == target){
                return remaining.remove(i);
            }
        }
        return null;
    }
}
