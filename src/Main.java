// Main: program entry point. Loads config, opens the window, starts the game loop. Owner: Zrs.

import engine.Config;
import engine.Display;
import engine.GamePanel;
import exception.AssetLoadException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        try {
            Config.load();
        } catch (AssetLoadException e) {
            // Without config.properties nothing works, so tell the user and stop.
            JOptionPane.showMessageDialog(null, e.getMessage(), "Could not start", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Swing windows must be created on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Escape the Brainrot: Reclaim Your Mind");
            GamePanel panel = new GamePanel();
            frame.add(panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            new Display(frame, panel).setFullscreen(true);
            panel.requestFocusInWindow();
            panel.start();
        });
    }
}
