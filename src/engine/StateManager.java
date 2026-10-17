package engine;

import java.awt.Graphics2D;

/** Holds the current screen and switches between screens. Owner: Zrs. */
public class StateManager {

    private final InputHandler input;
    private volatile GameState current; // volatile: set by the game thread, read by the painting thread

    public StateManager(InputHandler input) {
        this.input = input;
    }

    public InputHandler getInput() {
        return input;
    }

    public void setState(GameState state) {
        current = state;
    }

    public void update() {
        GameState state = current;
        if (state != null) {
            state.update();
        }
    }

    public void draw(Graphics2D g) {
        GameState state = current;
        if (state != null) {
            state.draw(g);
        }
    }
}
