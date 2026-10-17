package engine;

import engine.InputHandler.Action;
import exception.LevelLoadException;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** Title screen: waits for Enter, then starts the overworld. Owner: Zrs. */
public class TitleState extends GameState {

    private String errorMessage; // shown if the map fails to load

    public TitleState(StateManager manager) {
        super(manager);
    }

    @Override
    public void update() {
        if (manager.getInput().wasPressed(Action.CONFIRM)) {
            try {
                manager.setState(new OverworldState(manager));
            } catch (LevelLoadException e) {
                errorMessage = e.getMessage();
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        g.setColor(Color.RED);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 44));
        g.drawString("ESCAPE THE BRAINROT", 130, 230);

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 22));
        g.drawString("Reclaim Your Mind", 280, 275);
        g.drawString("Press ENTER to start", 250, 380);

        if (errorMessage != null) {
            g.setColor(Color.ORANGE);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
            g.drawString("Error: " + errorMessage, 40, 540);
        }
    }
}
