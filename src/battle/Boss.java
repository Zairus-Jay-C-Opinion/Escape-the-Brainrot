package battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base for every boss. Owner: Andrew (written by Zrs as the working example).
 * Each boss has its own list of phases; subclasses add them in their constructor.
 */
public abstract class Boss {

    private final String name;
    private final int rotValue; // how much Rot is cleared when this boss is defeated
    private final List<Phase> phases = new ArrayList<>();

    protected Boss(String name, int rotValue) {
        this.name = name;
        this.rotValue = rotValue;
    }

    public String getName() {
        return name;
    }

    public int getRotValue() {
        return rotValue;
    }

    public List<Phase> getPhases() {
        return Collections.unmodifiableList(phases);
    }

    protected void addPhase(Phase phase) {
        phases.add(phase);
    }

    /** One line of dialogue shown when the battle starts. Every boss says something different. */
    public abstract String getIntroLine();
}
