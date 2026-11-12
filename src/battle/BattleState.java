package battle;

import challenge.Challenge;
import challenge.ChallengeLoader;
import engine.Config;
import engine.GamePanel;
import engine.GameState;
import engine.InputHandler;
import engine.InputHandler.Action;
import engine.StateManager;
import systems.BrainrotGauge;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Interactive battle screen supporting 4 challenge types, BattleTimer, and boss phases.
 * Owner: Andrew.
 */
public class BattleState extends GameState {

    private final GameState returnTo;
    private final Boss boss;
    private final List<Challenge> challenges;
    private int currentChallengeIndex;
    private final BrainrotGauge gauge;
    private final BattleTimer timer;

    // Balance values loaded from res/config.properties
    private final int baseWrongPenalty;
    private final int correctReward;
    private final int timeoutPenalty;

    private final StringBuilder inputBuffer;
    private String feedbackMessage;
    private Color feedbackColor;
    private boolean isBattleWon;
    private final KeyEventDispatcher keyDispatcher;

    public BattleState(StateManager manager, GameState returnTo, Boss boss, BrainrotGauge gauge) {
        super(manager);
        this.returnTo = returnTo;
        this.boss = boss != null ? boss : new TrollFaceBoss();
        this.gauge = gauge != null ? gauge : new BrainrotGauge();

        List<Challenge> loaded = ChallengeLoader.loadChallenges("/challenges/level1_challenges.txt");
        if (loaded == null || loaded.isEmpty()) {
            loaded = ChallengeLoader.loadChallenges("/challenges/sample.txt");
        }
        this.challenges = loaded;
        this.currentChallengeIndex = 0;

        // Read balance numbers from Config (with fallback defaults)
        int questionSeconds = getConfigInt("battle.questionSeconds", 30);
        this.baseWrongPenalty = getConfigInt("battle.wrongPenalty", 15);
        this.correctReward = getConfigInt("battle.correctReward", 10);
        this.timeoutPenalty = getConfigInt("battle.timeoutPenalty", 10);

        this.timer = new BattleTimer(questionSeconds);
        this.inputBuffer = new StringBuilder();
        this.feedbackMessage = "Type your answer and press ENTER to submit.";
        this.feedbackColor = Color.LIGHT_GRAY;
        this.isBattleWon = false;

        // Dispatcher for typing answers directly without modifying engine.InputHandler
        this.keyDispatcher = e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                handleKeyPressed(e.getKeyCode(), e.getKeyChar());
            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this.keyDispatcher);
    }

    public BattleState(StateManager manager, GameState returnTo, Boss boss) {
        this(manager, returnTo, boss, new BrainrotGauge());
    }

    public BattleState(StateManager manager, BrainrotGauge gauge) {
        this(manager, null, new TrollFaceBoss(), gauge);
    }

    private static int getConfigInt(String key, int defaultValue) {
        try {
            return Config.getInt(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Challenge getCurrentChallenge() {
        if (challenges == null || challenges.isEmpty()) {
            return null;
        }
        return challenges.get(currentChallengeIndex % challenges.size());
    }

    public BattleTimer getTimer() {
        return timer;
    }

    public Boss getBoss() {
        return boss;
    }

    private void cleanup() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this.keyDispatcher);
    }

    private void returnToOverworld() {
        cleanup();
        if (returnTo != null) {
            manager.setState(returnTo);
        }
    }

    public void handleKeyPressed(int keyCode, char keyChar) {
        if (isBattleWon) {
            if (keyCode == KeyEvent.VK_ENTER) {
                returnToOverworld();
            }
            return;
        }

        if (keyCode == KeyEvent.VK_BACK_SPACE) {
            if (inputBuffer.length() > 0) {
                inputBuffer.deleteCharAt(inputBuffer.length() - 1);
            }
        } else if (keyCode == KeyEvent.VK_ENTER) {
            submitAnswer();
        } else if (keyChar >= 32 && keyChar <= 126) {
            inputBuffer.append(keyChar);
        }
    }

    private void submitAnswer() {
        String answer = inputBuffer.toString().trim();
        inputBuffer.setLength(0);

        Challenge current = getCurrentChallenge();
        if (current == null) {
            return;
        }

        if (current.evaluateAnswer(answer)) {
            boss.takeDamage(1);
            if (gauge != null) {
                gauge.reduceStrain(correctReward);
            }
            feedbackColor = new Color(80, 220, 100);
            feedbackMessage = "Correct! " + boss.getName() + " took 1 damage.";
            currentChallengeIndex++;
            timer.reset();

            if (boss.isDefeated()) {
                handleVictory();
            }
        } else {
            int scaledPenalty = (int) Math.round(baseWrongPenalty * boss.getCurrentPhase().getStrainMultiplier());
            if (gauge != null) {
                gauge.addStrain(scaledPenalty);
            }
            feedbackColor = new Color(240, 80, 80);
            String explanation = current.getExplanation();
            String explainSuffix = (explanation != null && !explanation.isEmpty()) ? " (" + explanation + ")" : "";
            feedbackMessage = "Wrong! +" + scaledPenalty + " Strain [" + boss.getCurrentPhase().getLabel() + "]" + explainSuffix;
        }
    }

    private void handleVictory() {
        isBattleWon = true;
        timer.setPaused(true);
        boss.onDefeat();
        if (gauge != null) {
            gauge.clearRot(boss.getLockedRot());
        }
        feedbackColor = Color.CYAN;
        feedbackMessage = "Victory! " + boss.getName() + " defeated. Press ENTER to return.";
    }

    @Override
    public void update() {
        if (isBattleWon) {
            InputHandler input = manager.getInput();
            if (input != null && input.wasPressed(Action.CONFIRM)) {
                returnToOverworld();
            }
            return;
        }

        // Tick the 60 FPS timer; penalize Strain if time runs out
        if (timer.tick()) {
            if (gauge != null) {
                gauge.addStrain(timeoutPenalty);
            }
            feedbackColor = new Color(255, 165, 0);
            feedbackMessage = "Time's up! +" + timeoutPenalty + " Strain. Timer reset!";
            timer.reset();
        }
    }

    @Override
    public void draw(Graphics2D g) {
        render(g);
    }

    public void render(Graphics2D g) {
        int width = GamePanel.SCREEN_WIDTH;
        int height = GamePanel.SCREEN_HEIGHT;

        // 1. Background (shifts slightly crimson during Phase 2 ENRAGED)
        if (boss.getCurrentPhase() == Phase.ENRAGED) {
            g.setColor(new Color(38, 14, 20));
        } else {
            g.setColor(new Color(18, 18, 24));
        }
        g.fillRect(0, 0, width, height);

        // 2. Boss Header, Phase Badge, and HP Bar
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString("BOSS: " + boss.getName(), 40, 38);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        g.setColor(boss.getCurrentPhase() == Phase.ENRAGED ? new Color(255, 90, 90) : new Color(150, 200, 255));
        g.drawString(boss.getCurrentPhase().getLabel(), 40, 58);

        // HP Bar
        g.setColor(new Color(60, 60, 60));
        g.fillRect(40, 68, 260, 16);
        g.setColor(new Color(220, 50, 50));
        double hpPercent = (double) boss.getCurrentHp() / boss.getMaxHp();
        g.fillRect(40, 68, (int) (260 * Math.max(0, hpPercent)), 16);
        g.setColor(Color.WHITE);
        g.drawRect(40, 68, 260, 16);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        g.drawString("HP: " + boss.getCurrentHp() + "/" + boss.getMaxHp(), 310, 81);

        // 3. BattleTimer Display (Top Right)
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        g.setColor(timer.getRemainingSeconds() <= 5 ? Color.RED : Color.YELLOW);
        g.drawString("TIME: " + timer.getRemainingSeconds() + "s", width - 150, 42);

        // 4. Challenge Display Box
        g.setColor(new Color(30, 32, 44));
        g.fillRect(40, 96, width - 80, 290);
        g.setColor(new Color(70, 75, 100));
        g.drawRect(40, 96, width - 80, 290);

        Challenge current = getCurrentChallenge();
        if (current != null) {
            g.setColor(new Color(255, 215, 0));
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
            String badge = "[" + (!current.getId().isEmpty() ? current.getId() + " | " : "")
                    + current.getTypeLabel() + " | " + current.getDifficulty() + "]";
            g.drawString(badge, 55, 120);

            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
            int yPos = 145;
            for (String pLine : current.getPrompt().split("\n")) {
                g.drawString(pLine, 55, yPos);
                yPos += 20;
            }

            if (!current.getCodeSnippet().isEmpty()) {
                yPos += 6;
                g.setColor(new Color(160, 215, 255));
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
                for (String cLine : current.getCodeSnippet().split("\n")) {
                    g.drawString(cLine, 65, yPos);
                    yPos += 18;
                    if (yPos > 375) break; // keep within box
                }
            } else if (!current.getChoices().isEmpty()) {
                yPos += 10;
                List<String> choices = current.getChoices();
                for (int i = 0; i < choices.size(); i++) {
                    g.drawString((i + 1) + ") " + choices.get(i), 65, yPos);
                    yPos += 24;
                }
            }
        }

        // 5. Input Field
        g.setColor(new Color(10, 10, 14));
        g.fillRect(40, 400, width - 80, 45);
        g.setColor(new Color(100, 149, 237));
        g.drawRect(40, 400, width - 80, 45);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        g.drawString("> " + inputBuffer.toString() + "_", 55, 428);

        // 6. Feedback Message
        g.setColor(feedbackColor);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        g.drawString(feedbackMessage, 40, 475);

        // 7. Hint footer
        g.setColor(new Color(120, 120, 140));
        g.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 12));
        if (isBattleWon) {
            g.setColor(Color.GREEN);
            g.drawString("Press ENTER to return to the overworld.", 40, 520);
        } else {
            g.drawString("Type line number (SpotTheBug) or comma-separated order (Arrange) and press ENTER.", 40, 520);
        }
    }
}
