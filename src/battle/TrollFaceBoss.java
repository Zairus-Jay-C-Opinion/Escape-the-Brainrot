package battle;

/**
 * Concrete boss implementation for the Level 1 vertical slice.
 * Demonstrates inheritance and method overriding.
 * Owner: Andrew.
 */
public class TrollFaceBoss extends Boss {

    public TrollFaceBoss() {
        // Name: Trollface, Max HP: 3 (requires 3 correct answers), Locked Rot: 40
        super("Trollface", 3, 40);
    }

    @Override
    public String getIntroLine() {
        return "Problem? U mad, bro?";
    }

    @Override
    public void onDefeat() {
        // Triggers the Rot reduction hook when the boss reaches 0 HP
        System.out.println(getName() + " has been defeated! Locked Rot is cleared.");
    }
}

