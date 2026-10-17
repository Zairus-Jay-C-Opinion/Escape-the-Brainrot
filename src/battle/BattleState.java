package battle;

import challenge.Challenge;
import challenge.ChallengeLoader;
import engine.GamePanel;
import engine.GameState;
import engine.InputHandler;
import engine.InputHandler.Action;
import engine.StateManager;
import exception.AssetLoadException;
import exception.InvalidChallengeException;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

/**
 * Battle screen STUB: shows one random question, takes keys 1-3, shows Correct/Wrong, then returns.
 * Owner: Andrew (skeleton written by Zrs). Andrew takes this over: phases, timer, gauge, items.
 */
public class BattleState extends GameState {

    private static final String CHALLENGE_FILE = "/challenges/sample.txt";
    private static final Random random = new Random();

    private final GameState returnTo; // the screen to go back to (the overworld)
    private final Boss boss;
    private Challenge challenge;
    private String errorMessage;   // set if the question file could not be loaded
    private boolean answered;
    private boolean answeredCorrectly;

    public BattleState(StateManager manager, GameState returnTo, Boss boss) {
        super(manager);
        this.returnTo = returnTo;
        this.boss = boss;
        loadChallenge();
    }

    private void loadChallenge() {
        try {
            List<Challenge> all = ChallengeLoader.load(CHALLENGE_FILE);
            if (all.isEmpty()) {
                errorMessage = "No questions found in " + CHALLENGE_FILE;
            } else {
                challenge = all.get(random.nextInt(all.size()));
            }
        } catch (AssetLoadException | InvalidChallengeException e) {
            errorMessage = e.getMessage();
        }
    }

    @Override
    public void update() {
        InputHandler input = manager.getInput();

        // Nothing to answer (error) or already answered: Enter goes back to the map.
        if (challenge == null || answered) {
            if (input.wasPressed(Action.CONFIRM)) {
                manager.setState(returnTo);
            }
            return;
        }

        int choice = -1;
        if (input.wasPressed(Action.CHOICE_1)) {
            choice = 0;
        } else if (input.wasPressed(Action.CHOICE_2)) {
            choice = 1;
        } else if (input.wasPressed(Action.CHOICE_3)) {
            choice = 2;
        }

        if (choice >= 0 && choice < challenge.getChoices().size()) {
            answered = true;
            // Challenges take the 1-based choice number as text, e.g. "2".
            answeredCorrectly = challenge.isCorrect(String.valueOf(choice + 1));
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(20, 0, 0));
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        g.setColor(Color.RED);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        g.drawString("BATTLE: " + boss.getName(), 40, 60);

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 16));
        g.drawString(boss.getIntroLine(), 40, 90);

        if (challenge == null) {
            g.setColor(Color.ORANGE);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
            g.drawString("Error: " + errorMessage, 40, 160);
            g.drawString("Press ENTER to return", 40, 200);
            return;
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
        int y = 150;
        for (String line : challenge.getPrompt().split("\n")) {
            g.drawString(line, 40, y);
            y += 28;
        }

        y += 20;
        List<String> choices = challenge.getChoices();
        for (int i = 0; i < choices.size(); i++) {
            g.drawString((i + 1) + ") " + choices.get(i), 60, y);
            y += 32;
        }

        y += 30;
        if (answered) {
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 32));
            g.setColor(answeredCorrectly ? Color.GREEN : Color.RED);
            g.drawString(answeredCorrectly ? "Correct" : "Wrong", 40, y);
            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
            g.drawString("Press ENTER to return to the map", 40, y + 40);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
            g.drawString("Press 1-" + choices.size() + " to answer", 40, y);
        }
    }
}
