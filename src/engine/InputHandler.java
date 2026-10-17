package engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Turns raw key presses into game Actions using a key-binding table. Owner: Zrs.
 * Key events arrive on the Swing thread but the game loop reads on its own thread, so access is synchronized.
 */
public class InputHandler implements KeyListener {

    /** What the player wants to do, independent of which key they pressed. */
    public enum Action { UP, DOWN, LEFT, RIGHT, CONFIRM, CHOICE_1, CHOICE_2, CHOICE_3 }

    private final Map<Integer, Action> bindings = new HashMap<>();
    private final Set<Action> held = EnumSet.noneOf(Action.class);    // keys currently down
    private final Set<Action> pressed = EnumSet.noneOf(Action.class); // newly pressed since the last update

    public InputHandler() {
        bind(KeyEvent.VK_UP, Action.UP);
        bind(KeyEvent.VK_W, Action.UP);
        bind(KeyEvent.VK_DOWN, Action.DOWN);
        bind(KeyEvent.VK_S, Action.DOWN);
        bind(KeyEvent.VK_LEFT, Action.LEFT);
        bind(KeyEvent.VK_A, Action.LEFT);
        bind(KeyEvent.VK_RIGHT, Action.RIGHT);
        bind(KeyEvent.VK_D, Action.RIGHT);
        bind(KeyEvent.VK_ENTER, Action.CONFIRM);
        bind(KeyEvent.VK_1, Action.CHOICE_1);
        bind(KeyEvent.VK_NUMPAD1, Action.CHOICE_1);
        bind(KeyEvent.VK_2, Action.CHOICE_2);
        bind(KeyEvent.VK_NUMPAD2, Action.CHOICE_2);
        bind(KeyEvent.VK_3, Action.CHOICE_3);
        bind(KeyEvent.VK_NUMPAD3, Action.CHOICE_3);
    }

    private void bind(int keyCode, Action action) {
        bindings.put(keyCode, action);
    }

    /** True while the key is held down (good for walking). */
    public synchronized boolean isHeld(Action action) {
        return held.contains(action);
    }

    /** True once per key press (good for menus and answers). */
    public synchronized boolean wasPressed(Action action) {
        return pressed.contains(action);
    }

    /** The game loop calls this after every update so "pressed" only lasts one update. */
    public synchronized void endFrame() {
        pressed.clear();
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        Action action = bindings.get(e.getKeyCode());
        // held.add returns false for key-repeat events, so holding a key does not re-trigger "pressed".
        if (action != null && held.add(action)) {
            pressed.add(action);
        }
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        Action action = bindings.get(e.getKeyCode());
        if (action != null) {
            held.remove(action);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) { }
}
