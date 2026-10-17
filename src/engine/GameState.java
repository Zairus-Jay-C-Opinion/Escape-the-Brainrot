package engine;

import java.awt.Graphics2D;

/** Abstract base for every screen (title, overworld, battle...). Owner: Zrs. */
public abstract class GameState {

    // Subclasses use this to read input and to switch to another screen.
    protected final StateManager manager;

    protected GameState(StateManager manager) {
        this.manager = manager;
    }

    /** Called 60 times per second. Game logic only, no drawing. */
    public abstract void update();

    /** Called whenever the panel repaints. Drawing only, no game logic. */
    public abstract void draw(Graphics2D g);
}
