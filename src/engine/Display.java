package engine;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/** Switches the game window between fullscreen and a normal window. Owner: Zrs. */
public class Display {

    // A function key, so it never clashes with the typed answers or Enter used by the battle screen.
    public static final int TOGGLE_FULLSCREEN_KEY = KeyEvent.VK_F11;

    private final JFrame frame;
    private final GamePanel panel;
    private boolean fullscreen;
    private boolean toggleKeyDown;

    public Display(JFrame frame, GamePanel panel) {
        this.frame = frame;
        this.panel = panel;
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // toggleKeyDown ignores the repeats sent while the key is held.
                if (e.getKeyCode() == TOGGLE_FULLSCREEN_KEY && !toggleKeyDown) {
                    toggleKeyDown = true;
                    setFullscreen(!fullscreen);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == TOGGLE_FULLSCREEN_KEY) {
                    toggleKeyDown = false;
                }
            }
        });
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
