package engine;

import java.awt.Graphics2D;

import systems.BrainrotGauge;

/** Holds the current screen and switches between screens. Owner: Zrs. */
public class StateManager {

    private final InputHandler input;
    private final MouseInput mouse;
    private volatile GameState current; // volatile: set by the game thread, read by the painting thread
    private BrainrotGauge gauge = new BrainrotGauge();

    public StateManager(InputHandler input, MouseInput mouse) {
        this.input = input;
        this.mouse = mouse;
    }

    public BrainrotGauge getGauge(){
        return gauge;
    }

    public InputHandler getInput() {
        return input;
    }

    public MouseInput getMouse() {
        return mouse;
    }

    public void setState(GameState state) {
        current = state;
        if (state != null) {
            state.onEnter();
        }
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
