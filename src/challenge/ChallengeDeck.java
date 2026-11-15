package challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Hands out challenges in shuffled order, with no repeats until every one has been used. */
public class ChallengeDeck {

    private final List<Challenge> all;
    private final List<Challenge> remaining = new ArrayList<>();
    private final Random random = new Random();
    private Challenge last;

    public ChallengeDeck(List<Challenge> challenges) {
        this.all = new ArrayList<>(challenges);
    }

    public boolean isEmpty() {
        return all.isEmpty();
    }

    /** The next challenge. Only call when the deck is not empty. */
    public Challenge draw() {
        if (remaining.isEmpty()) {
            remaining.addAll(all);
            Collections.shuffle(remaining, random);
            // The same question twice in a row would feel like a bug, so swap it away from the front.
            if (remaining.size() > 1 && remaining.get(remaining.size() - 1) == last) {
                Collections.swap(remaining, 0, remaining.size() - 1);
            }
        }
        last = remaining.remove(remaining.size() - 1);
        return last;
    }
}
