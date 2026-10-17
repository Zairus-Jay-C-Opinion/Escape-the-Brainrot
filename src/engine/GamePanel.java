package engine;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** The game window's drawing surface and the 60-updates-per-second game loop. Owner: Zrs. */
public class GamePanel extends JPanel implements Runnable {

    public static final int TILE_SIZE = 16;       // size of a tile in the art, in pixels
    public static final int SCALE = 3;            // everything is drawn 3x bigger
    public static final int SCALED_TILE_SIZE = TILE_SIZE * SCALE; // 48 pixels on screen
    public static final int SCREEN_COLUMNS = 16;
    public static final int SCREEN_ROWS = 12;
    public static final int SCREEN_WIDTH = SCREEN_COLUMNS * SCALED_TILE_SIZE;  // 768
    public static final int SCREEN_HEIGHT = SCREEN_ROWS * SCALED_TILE_SIZE;    // 576

    private static final int UPDATES_PER_SECOND = 60;

    private final InputHandler input = new InputHandler();
    private final StateManager stateManager = new StateManager(input);
    private Thread gameThread;
    private volatile boolean running;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true); // needed to receive key events
        addKeyListener(input);
        stateManager.setState(new TitleState(stateManager));
    }

    /** Starts the game loop on its own thread so the window stays responsive. */
    public void start() {
        running = true;
        gameThread = new Thread(this, "game-loop");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    /**
     * Fixed-timestep loop: measure how much time passed, run one update for every 1/60 s owed,
     * then repaint. This keeps game speed the same on slow and fast laptops.
     */
    @Override
    public void run() {
        final double nanosPerUpdate = 1_000_000_000.0 / UPDATES_PER_SECOND;
        long lastTime = System.nanoTime();
        double owedUpdates = 0;

        while (running) {
            long now = System.nanoTime();
            owedUpdates += (now - lastTime) / nanosPerUpdate;
            lastTime = now;

            while (owedUpdates >= 1) {
                stateManager.update();
                input.endFrame();
                owedUpdates--;
            }
            repaint();

            try {
                Thread.sleep(2); // don't burn a whole CPU core spinning
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        stateManager.draw(g2);
    }
}
