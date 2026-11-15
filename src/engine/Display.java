package engine;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/** Switches the game window between fullscreen and a normal window. Owner: Zrs. */
public class Display {

    // macOS reserves F11, so it gets the standard Cmd+Ctrl+F. Elsewhere F11 never clashes with typed answers or Enter.
    private static final boolean IS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");
    public static final int TOGGLE_FULLSCREEN_KEY = IS_MAC ? KeyEvent.VK_F : KeyEvent.VK_F11;
    private static final long TOGGLE_COOLDOWN_NANOS = 400_000_000L;

    private final JFrame frame;
    private final GamePanel panel;
    private boolean fullscreen;
    private long lastToggleNanos;

    public Display(JFrame frame, GamePanel panel) {
        this.frame = frame;
        this.panel = panel;
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!isToggleShortcut(e)) {
                    return;
                }
                // A cooldown instead of tracking key release, because macOS drops the release of keys pressed with Cmd.
                long now = System.nanoTime();
                if (now - lastToggleNanos > TOGGLE_COOLDOWN_NANOS) {
                    lastToggleNanos = now;
                    setFullscreen(!fullscreen);
                }
            }
        });
    }

    private static boolean isToggleShortcut(KeyEvent e) {
        if (e.getKeyCode() != TOGGLE_FULLSCREEN_KEY) {
            return false;
        }
        return !IS_MAC || (e.isMetaDown() && e.isControlDown());
    }

    /** Must run on the Swing thread. */
    public void setFullscreen(boolean on) {
        Rectangle screen = frame.getGraphicsConfiguration().getBounds();
        fullscreen = on;
        // A window's decoration can only change while it is not displayable.
        frame.dispose();
        frame.setUndecorated(on);
        frame.setResizable(!on);
        if (on) {
            frame.setBounds(screen);
        } else {
            frame.pack();
            frame.setLocationRelativeTo(null);
        }
        frame.setVisible(true);
        SwingUtilities.invokeLater(panel::requestFocusInWindow);
    }
}
