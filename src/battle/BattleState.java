package battle;

import challenge.AnswerInput;
import challenge.Challenge;
import challenge.ChallengeDeck;
import challenge.ChallengeLoader;
import engine.Config;
import engine.GamePanel;
import engine.GameState;
import engine.MouseInput;
import engine.ScreenEffect;
import engine.StateManager;
import systems.BrainrotGauge;
import ui.BattleBackdrop;
import ui.BossHeader;
import ui.BossView;
import ui.ChoiceBox;
import ui.CodeBox;
import ui.DialogueBox;
import ui.FloatingTexts;
import ui.FriedView;
import ui.Hud;
import ui.SlamBanner;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The boss fight. This class owns the rules (questions, timer, Strain, damage, phases, win and fried);
 * every pixel is drawn by the ui classes. Everything boss-specific comes from the Boss. Owner: Andrew.
 */
public class BattleState extends GameState {

    private enum Stage { INTRO, QUESTION, REVIEW, PHASE_CHANGE, DEFEAT, END_WAIT, FRIED }

    private static final int TERMINAL_X = 20;
    private static final int TERMINAL_Y = 92;
    private static final int TERMINAL_WIDTH = 424;
    private static final int TERMINAL_HEIGHT = 330;
    private static final int CHOICE_ROW_HEIGHT = 26;
    private static final int TYPED_AREA_HEIGHT = 58;

    private static final int BOSS_AREA_X = 456;
    private static final int BOSS_AREA_Y = 84;
    private static final int BOSS_AREA_WIDTH = 300;
    private static final int BOSS_AREA_HEIGHT = 338;

    private static final int PHASE_FREEZE_TICKS = 30;
    private static final int PHASE_TOTAL_TICKS = 150;
    private static final int FRIED_MIN_TICKS = 90;
    private static final float FRIED_GLITCH_BURST = 0.9f;
    private static final float FRIED_GLITCH_IDLE = 0.35f;
    private static final int FRIED_BURST_PERIOD_TICKS = 50;
    private static final int FRIED_BURST_TICKS = 8;
    private static final int SHAKE_TICKS = 18;
    private static final int SHAKE_PIXELS = 6;
    private static final int MAX_TYPED_LENGTH = 28;
    private static final int MAX_NOTE_LENGTH = 110;

    private final GameState returnTo;
    private final Boss boss;
    private final BrainrotGauge gauge;
    private final ChallengeDeck deck;
    private final Challenge climaxChallenge;
    private final BattleTimer timer;
    private final Random random = new Random();

    private final ScreenEffect vhs = new ScreenEffect(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, 0.5f, false);
    private final BattleBackdrop backdrop;
    private final BossView bossView;
    private final BossHeader header = new BossHeader();
    private final Hud hud;
    private final CodeBox codeBox = new CodeBox();
    private final ChoiceBox choiceBox = new ChoiceBox();
    private final DialogueBox dialogue = new DialogueBox();
    private final FloatingTexts floating = new FloatingTexts();
    private final SlamBanner slam = new SlamBanner();
    private final FriedView friedView = new FriedView();

    // Key events arrive on the Swing thread; update() drains them on the game thread so the rules never race.
    private final Queue<int[]> keyQueue = new ConcurrentLinkedQueue<>();
    private final KeyEventDispatcher keyDispatcher;

    private Stage stage = Stage.INTRO;
    private Challenge current;
    private int[] choiceOrder;
    private volatile String typed = "";
    private int tick;
    private int stageTicks;
    private int shakeTicksLeft;
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    public BattleState(StateManager manager, GameState returnTo, Boss boss, BrainrotGauge gauge) {
        this(manager, returnTo, boss, gauge, List.of(boss));
    }

    /** levelBosses are the bosses the HUD shows as locked Rot segments. */
    public BattleState(StateManager manager, GameState returnTo, Boss boss, BrainrotGauge gauge, List<Boss> levelBosses) {
        super(manager);
        this.returnTo = returnTo;
        this.boss = boss;
        this.gauge = gauge;
        if (!boss.isDefeated()) {
            boss.reset();
        }

        this.deck = new ChallengeDeck(loadChallenges(boss));
        String climaxFile = boss.getClimaxChallengeFile();
        List<Challenge> climaxList = climaxFile != null ? ChallengeLoader.loadChallenges(climaxFile) : List.of();
        this.climaxChallenge = climaxList.isEmpty() ? null : climaxList.get(0);
        this.timer = new BattleTimer(Difficulty.EASY.getStrainIntervalSeconds());
        timer.setPaused(true);
        this.hud = new Hud(gauge, levelBosses);
        this.backdrop = new BattleBackdrop(boss.getStyle(boss.getCurrentPhase()));
        this.bossView = new BossView(boss.getSpritePath());

        if (deck.isEmpty()) {
            stage = Stage.END_WAIT;
            dialogue.show("SYSTEM", "No questions were found for " + boss.getName() + ".\nPress Enter to go back.");
        } else {
            loadNextQuestion();
            dialogue.show(boss.getName(), boss.getIntroLine());
        }

        this.keyDispatcher = e -> {
            // Cmd or Ctrl means a shortcut such as fullscreen, not an answer character.
            if (e.getID() == KeyEvent.KEY_PRESSED && !e.isMetaDown() && !e.isControlDown()) {
                keyQueue.add(new int[] {e.getKeyCode(), e.getKeyChar()});
            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyDispatcher);
    }

    private static List<Challenge> loadChallenges(Boss boss) {
        List<Challenge> all = new ArrayList<>();
        for (String path : boss.getChallengeFiles()) {
            all.addAll(ChallengeLoader.loadChallenges(path));
        }
        return all;
    }

    public Boss getBoss() {
        return boss;
    }

    public Challenge getCurrentChallenge() {
        return current;
    }

    private void leave() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyDispatcher);
        if (stage == Stage.FRIED) {
            gauge.resetStrain();
        }
        if (returnTo != null) {
            manager.setState(returnTo);
        }
    }

    // ---------------------------------------------------------------- questions

    private void loadNextQuestion() {
        if (climaxChallenge != null && boss.getCurrentHp() <= Config.getInt("battle.damagePerCorrect")) {
            current = climaxChallenge;
        } else {
            current = deck.draw(boss.getCurrentPhase());
        }
        typed = "";
        choiceOrder = null;
        // Anything said about the previous question must not sit under the new one.
        dialogue.show("");

        boolean choices = current.getAnswerInput() == AnswerInput.CHOICE;
        int answerHeight = choices ? current.getChoices().size() * CHOICE_ROW_HEIGHT + 12 : TYPED_AREA_HEIGHT;
        codeBox.setBounds(TERMINAL_X, TERMINAL_Y, TERMINAL_WIDTH, TERMINAL_HEIGHT, answerHeight);
        codeBox.setTitle(current.getTypeLabel() + " | " + current.getDifficulty() + " | " + boss.getConcept());
        codeBox.setContent(current.getPrompt(), current.getCodeSnippet());
        if (choices) {
            List<String> rawChoices = current.getChoices();
            choiceOrder = buildChoiceOrder(rawChoices.size(), boss.getCurrentPhase().getModifier().shufflesChoices());
            List<String> displayed = new ArrayList<>(rawChoices.size());
            for (int idx : choiceOrder) {
                displayed.add(rawChoices.get(idx));
            }
            choiceBox.setChoices(displayed);
            choiceBox.setBounds(TERMINAL_X + 8, codeBox.getAnswerAreaTop() + 4, TERMINAL_WIDTH - 16);
        }

        timer.reset();
        timer.setIntervalSeconds(strainIntervalSeconds());
    }

    private int[] buildChoiceOrder(int size, boolean shuffle) {
        int[] order = new int[size];
        for (int i = 0; i < size; i++) {
            order[i] = i;
        }
        if (shuffle) {
            for (int i = size - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int tmp = order[i];
                order[i] = order[j];
                order[j] = tmp;
            }
        }
        return order;
    }

    private int strainIntervalSeconds() {
        double seconds = current.getDifficulty().getStrainIntervalSeconds()
                * boss.getCurrentPhase().getModifier().getIntervalMultiplier();
        return (int) Math.max(1, Math.round(seconds));
    }

    /** Returns true if the answer was right. */
    private boolean submit(String answer) {
        boolean correct = current.evaluateAnswer(answer);
        if (correct) {
            onCorrect();
        } else {
            onWrong();
        }
        return correct;
    }

    private void onCorrect() {
        Phase before = boss.getCurrentPhase();
        typed = "";
        int damage = Config.getInt("battle.damagePerCorrect");
        boss.takeDamage(damage);
        gauge.reduceStrain(Config.getInt("battle.correctStrainReward"));
        bossView.hit();
        floating.add("-" + damage, BOSS_AREA_X + BOSS_AREA_WIDTH / 2 + 40, BOSS_AREA_Y + 70, new Color(255, 235, 140));

        String note = noteFor(current);
        if (boss.isDefeated()) {
            startDefeat();
        } else if (boss.getCurrentPhase() != before) {
            startPhaseChange();
        } else {
            // The solved question stays on screen with its note until the player is ready for the next one.
            dialogue.show(boss.getName(), boss.getHitLine() + note);
            stage = Stage.REVIEW;
            timer.setPaused(true);
        }
    }

    // The explanation can give the answer away, which is fine here because the question is already solved.
    private static String noteFor(Challenge solved) {
        String explanation = solved.getExplanation();
        if (explanation == null || explanation.isBlank()) {
            return "";
        }
        String shortened = explanation.length() > MAX_NOTE_LENGTH
                ? explanation.substring(0, MAX_NOTE_LENGTH - 3) + "..." : explanation;
        return "\nNote: " + shortened;
    }

    private void onWrong() {
        int cost = (int) Math.round(current.getDifficulty().getStrainPenalty()
                * boss.getCurrentPhase().getStrainMultiplier());
        gauge.addStrain(cost);
        hud.flashWarning();
        floating.add("+" + cost + " STRAIN", hud.getStrainLabelX(), hud.getStrainLabelY(), new Color(255, 90, 100));
        shakeTicksLeft = SHAKE_TICKS;
        typed = "";
        // The hint comes from getHint, never getExplanation, so the answer stays hidden.
        dialogue.show(boss.getName(), boss.getWrongLine() + "\nHint: " + current.getHint());
        checkFried();
    }

    private void onStrainTick() {
        int penalty = current.getDifficulty().getStrainPenalty();
        gauge.addStrain(penalty);
        hud.flashWarning();
        floating.add("+" + penalty + " STRAIN", hud.getStrainLabelX(), hud.getStrainLabelY(), new Color(255, 90, 100));
        checkFried();
    }

    // ---------------------------------------------------------------- stage changes

    private void checkFried() {
        if (gauge.isFried()) {
            stage = Stage.FRIED;
            stageTicks = 0;
            timer.setPaused(true);
            dialogue.show("SYSTEM", "Strain overload. The feed swallowed your mind.\nReturning to the overworld...");
        }
    }

    private void startPhaseChange() {
        stage = Stage.PHASE_CHANGE;
        stageTicks = 0;
        timer.setPaused(true);
    }

    private void startDefeat() {
        stage = Stage.DEFEAT;
        timer.setPaused(true);
        gauge.clearRot(boss.getRotShare());
        boss.onDefeat();
        bossView.startDefeat();
        dialogue.show(boss.getName(), boss.getHitLine());
    }

    // ---------------------------------------------------------------- update

    @Override
    public void update() {
        tick++;
        vhs.update();
        backdrop.update();
        hud.update();
        dialogue.update();
        floating.update();
        slam.update();
        header.update((double) boss.getCurrentHp() / boss.getMaxHp());
        if (stage != Stage.PHASE_CHANGE || stageTicks >= PHASE_FREEZE_TICKS) {
            bossView.update();
        }
        if (shakeTicksLeft > 0) {
            shakeTicksLeft--;
        }

        List<int[]> keys = drainKeys();
        MouseInput mouse = manager.getMouse();
        boolean clicked = mouse.wasClicked();
        switch (stage) {
            case INTRO, END_WAIT -> updateWaiting(keys, clicked);
            case REVIEW -> updateReview(keys, clicked);
            case QUESTION -> updateQuestion(keys, clicked, mouse);
            case PHASE_CHANGE -> updatePhaseChange();
            case DEFEAT -> updateDefeat();
            case FRIED -> updateFried(keys, clicked);
        }
        lastMouseX = mouse.getX();
        lastMouseY = mouse.getY();
    }

    private List<int[]> drainKeys() {
        List<int[]> keys = new ArrayList<>();
        int[] key;
        while ((key = keyQueue.poll()) != null) {
            keys.add(key);
        }
        return keys;
    }

    private static boolean wantsContinue(List<int[]> keys, boolean clicked) {
        if (clicked) {
            return true;
        }
        return keys.stream().anyMatch(k -> k[0] == KeyEvent.VK_ENTER || k[0] == KeyEvent.VK_SPACE);
    }

    // First press finishes the typewriter line, the next one moves on.
    private boolean continuePressed(List<int[]> keys, boolean clicked, String prompt) {
        dialogue.setPrompt(prompt);
        if (!wantsContinue(keys, clicked)) {
            return false;
        }
        if (!dialogue.isFinished()) {
            dialogue.skip();
            return false;
        }
        return true;
    }

    private void updateWaiting(List<int[]> keys, boolean clicked) {
        String prompt = stage == Stage.INTRO ? "Press ENTER to fight" : "Press ENTER to continue";
        if (!continuePressed(keys, clicked, prompt)) {
            return;
        }
        if (stage == Stage.INTRO) {
            stage = Stage.QUESTION;
            dialogue.setPrompt("");
            timer.setPaused(false);
        } else {
            leave();
        }
    }

    private void updateReview(List<int[]> keys, boolean clicked) {
        if (continuePressed(keys, clicked, "Press ENTER for the next question")) {
            loadNextQuestion();
            stage = Stage.QUESTION;
            timer.setPaused(false);
        }
    }

    private void updateQuestion(List<int[]> keys, boolean clicked, MouseInput mouse) {
        int mouseX = mouse.getX();
        int mouseY = mouse.getY();
        boolean mouseMoved = mouseX != lastMouseX || mouseY != lastMouseY;

        switch (current.getAnswerInput()) {
            case CHOICE -> updateChoiceInput(keys, clicked, mouseMoved, mouseX, mouseY);
            case LINE -> {
                codeBox.setHoverLine(codeBox.lineAt(mouseX, mouseY));
                updateTypedInput(keys);
                int line = codeBox.lineAt(mouseX, mouseY);
                if (stage == Stage.QUESTION && clicked && line >= 0) {
                    submit(String.valueOf(line));
                }
            }
            case TYPED -> updateTypedInput(keys);
        }

        if (stage == Stage.QUESTION && timer.tick()) {
            onStrainTick();
        }
    }

    private void updateChoiceInput(List<int[]> keys, boolean clicked, boolean mouseMoved, int mouseX, int mouseY) {
        for (int[] key : keys) {
            int code = key[0];
            int shortcut = choiceForKey(code);
            if (shortcut >= 0 && shortcut < current.getChoices().size() && !choiceBox.isEliminated(shortcut)) {
                submitChoice(shortcut);
                return;
            }
            if (code == KeyEvent.VK_UP) {
                choiceBox.move(-1);
            } else if (code == KeyEvent.VK_DOWN) {
                choiceBox.move(1);
            } else if (code == KeyEvent.VK_ENTER) {
                submitChoice(choiceBox.getSelected());
                return;
            }
        }

        int hovered = choiceBox.choiceAt(mouseX, mouseY);
        if (hovered >= 0 && mouseMoved) {
            choiceBox.setSelected(hovered);
        }
        if (hovered >= 0 && clicked) {
            submitChoice(hovered);
        }
    }

    // 1-4 and A-D both pick a choice; the number keys on the keypad work too.
    private static int choiceForKey(int code) {
        if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_4) {
            return code - KeyEvent.VK_1;
        }
        if (code >= KeyEvent.VK_NUMPAD1 && code <= KeyEvent.VK_NUMPAD4) {
            return code - KeyEvent.VK_NUMPAD1;
        }
        if (code >= KeyEvent.VK_A && code <= KeyEvent.VK_D) {
            return code - KeyEvent.VK_A;
        }
        return -1;
    }

    private void submitChoice(int index) {
        int originalIndex = (choiceOrder != null && index >= 0 && index < choiceOrder.length)
                ? choiceOrder[index] : index;
        if (!submit(String.valueOf(originalIndex + 1))) {
            choiceBox.eliminate(index);
        }
    }

    private void updateTypedInput(List<int[]> keys) {
        for (int[] key : keys) {
            int code = key[0];
            char typedChar = (char) key[1];
            if (code == KeyEvent.VK_BACK_SPACE) {
                if (!typed.isEmpty()) {
                    typed = typed.substring(0, typed.length() - 1);
                }
            } else if (code == KeyEvent.VK_ENTER) {
                if (!typed.isBlank()) {
                    submit(typed.trim());
                    return;
                }
            } else if (typedChar >= 32 && typedChar <= 126 && typed.length() < MAX_TYPED_LENGTH) {
                typed = typed + typedChar;
            }
        }
    }

    private void updatePhaseChange() {
        stageTicks++;
        if (stageTicks == PHASE_FREEZE_TICKS) {
            PhaseStyle style = boss.getStyle(boss.getCurrentPhase());
            vhs.glitch(40, 0.8f);
            bossView.glitchBurst();

            String phaseSprite = boss.getPhaseSpritePath(boss.getCurrentPhase());

            if (phaseSprite != null){
                bossView.setSprite(phaseSprite);
            }

            bossView.setEnraged(style.accent());
            slam.start(boss.getCurrentPhase().getLabel(), style.accent());
            backdrop.setStyle(style);
            dialogue.show(boss.getName(), boss.getPhaseLine(boss.getCurrentPhase()));
            shakeTicksLeft = SHAKE_TICKS;
        }
        if (stageTicks >= PHASE_TOTAL_TICKS) {
            stage = Stage.QUESTION;
            loadNextQuestion();
            timer.setPaused(false);
        }
    }

    private void updateDefeat() {
        if (bossView.isDefeatFinished()) {
            stage = Stage.END_WAIT;
            dialogue.show(boss.getName(), boss.getDefeatLine());
        }
    }

    private void updateFried(List<int[]> keys, boolean clicked) {
        stageTicks++;
        // Bursts of heavy tearing with calmer frames between, so the title stays readable.
        boolean burst = stageTicks % FRIED_BURST_PERIOD_TICKS < FRIED_BURST_TICKS;
        vhs.glitch(2, burst ? FRIED_GLITCH_BURST : FRIED_GLITCH_IDLE);
        if (stageTicks >= FRIED_MIN_TICKS && continuePressed(keys, clicked, "Press ENTER to continue")) {
            leave();
        }
    }

    // ---------------------------------------------------------------- draw

    @Override
    public void draw(Graphics2D g) {
        Graphics2D scene = vhs.beginScene();
        if (stage == Stage.FRIED) {
            friedView.draw(scene, tick);
        } else {
            drawFight(scene);
        }
        hud.draw(scene);
        floating.draw(scene);
        slam.draw(scene);
        dialogue.draw(scene);
        vhs.present(g);
    }

    private void drawFight(Graphics2D g) {
        AffineTransform saved = g.getTransform();
        if (shakeTicksLeft > 0) {
            // Seeded by tick so the shake looks the same however often the screen repaints inside one update.
            Random shake = new Random(tick * 17L);
            double strength = SHAKE_PIXELS * (double) shakeTicksLeft / SHAKE_TICKS;
            g.translate((shake.nextDouble() * 2 - 1) * strength, (shake.nextDouble() * 2 - 1) * strength);
        }

        backdrop.draw(g);

        if (stage == Stage.PHASE_CHANGE){
            bossView.draw(g, 164, 76, 440, 356);
        }
        else{
            bossView.draw(g, BOSS_AREA_X, BOSS_AREA_Y, BOSS_AREA_WIDTH, BOSS_AREA_HEIGHT);
        }
        
        if (current != null && stage != Stage.INTRO && stage != Stage.PHASE_CHANGE) {
            codeBox.draw(g);
            if (current.getAnswerInput() == AnswerInput.CHOICE) {
                choiceBox.draw(g);
            } else {
                codeBox.drawTypedInput(g, current.getInputHint(), typed, (tick / 30) % 2 == 0);
            }
        }
        header.draw(g, boss.getName(), boss.getCurrentPhase().getNumber(), timer.getElapsedSeconds(),
                timer.getIntervalProgress(), backdrop.getAccent());
        g.setTransform(saved);
    }
}
