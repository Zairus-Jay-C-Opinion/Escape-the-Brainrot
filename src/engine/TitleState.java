package engine;

import engine.InputHandler.Action;
import exception.AssetLoadException;
import exception.LevelLoadException;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.util.Map;
import java.util.Random;

/** Animated horror title screen: a dark room lit by a phone, lightning, rain, VHS effects and the main menu. Owner: Zrs. */
public class TitleState extends GameState {

    // Darkness: where the lit hole sits (fractions of the screen) and how big it is (pixels).
    private static final float HOLE_CENTER_X = 0.50f;
    private static final float HOLE_CENTER_Y = 0.50f;
    private static final float HOLE_RADIUS = 200f;
    private static final float DARKNESS_OUTSIDE = 0.97f; // 1 is pitch black outside the hole

    // Lightning: a double flash, then he keeps staring for a moment.
    private static final int MIN_SECONDS_BETWEEN_FLASHES = 6;
    private static final int MAX_SECONDS_BETWEEN_FLASHES = 12;
    private static final int FIRST_PULSE_DECAY_TICKS = 10;
    private static final int SECOND_PULSE_START_TICK = 14;
    private static final float SECOND_PULSE_PEAK = 1f;
    private static final int SECOND_PULSE_DECAY_TICKS = 45;
    private static final int STARE_HOLD_TICKS = 50;       // stare continues this long after the light fades
    private static final float FLASH_DARKNESS_REMOVED = 1f;   // 1 means a full flash strips away all the darkness
    private static final float FLASH_WHITE_ALPHA = 0.12f;
    private static final float FLASH_LIT_BRIGHTNESS = 5f;     // how steeply the dark parts of the room are lifted in a flash
    private static final int FLASH_LIT_CEILING = 235;         // brightest value the lit copy can reach, so he never turns pure white
    private static final double MIN_THUNDER_DELAY_SECONDS = 0.5;
    private static final double MAX_THUNDER_DELAY_SECONDS = 1.5;

    private static final int RAIN_DROP_COUNT = 300;
    private static final int FADE_OUT_TICKS = 45;
    private static final int TICKS_PER_SECOND = 60;

    private static final Color TITLE_RED = new Color(235, 55, 55);
    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 40);
    private static final Font SUBTITLE_FONT = new Font(Font.SERIF, Font.BOLD, 17)
            .deriveFont(Map.of(TextAttribute.TRACKING, 0.15f));
    private static final Font TAGLINE_FONT = new Font(Font.SERIF, Font.BOLD, 20);
    private static final Font MENU_FONT = new Font(Font.SERIF, Font.BOLD, 19);

    private static final int MENU_BUTTON_WIDTH = 150;
    private static final int MENU_BUTTON_HEIGHT = 38;
    private static final int MENU_BUTTON_GAP = 6;
    private static final int MENU_Y = 500;

    /** One row of the menu. Load Game stays disabled until saving exists. */
    private enum MenuItem {
        NEW_GAME("New Game", true),
        LOAD_GAME("Load Game", false),
        EXIT("Exit", true);

        private final String label;
        private final boolean enabled;

        MenuItem(String label, boolean enabled) {
            this.label = label;
            this.enabled = enabled;
        }
    }

    private final Random random = new Random();
    private final ScreenEffect vhs = new ScreenEffect(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
    private final RainLayer rain = new RainLayer(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, RAIN_DROP_COUNT);
    private final Sound rainSound = new Sound("/sounds/rain.wav");
    private final Sound thunderSound = new Sound("/sounds/thunder.wav");

    // Each image can be null if the file is missing; drawing then falls back to plain black.
    private final BufferedImage lookingDown = loadFitted("/sprites/ui/title_down.jpeg");
    private final BufferedImage lookingUp = loadFitted("/sprites/ui/title_up.jpeg");
    private final BufferedImage lookingDownLit = brighten(lookingDown);
    private final BufferedImage lookingUpLit = brighten(lookingUp);
    private final BufferedImage darkness = buildDarkness();

    private final MenuItem[] menu = MenuItem.values();
    private int selected = 0;
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    private int ticksUntilFlash = randomFlashGap();
    private int flashTick = -1;        // -1 means no flash is running
    private int ticksUntilThunder = 0; // 0 means no thunder is waiting
    private float flash;               // current flash brightness, 0 to 1
    private boolean staring;

    private boolean fading;
    private int fadeTicks;
    private String errorMessage;       // shown if the map fails to load

    public TitleState(StateManager manager) {
        super(manager);
        rainSound.loop();
    }

    private static BufferedImage loadFitted(String path) {
        try {
            return fitScale(AssetManager.getImage(path));
        } catch (AssetLoadException e) {
            return null;
        }
    }

    /** Scales the whole image to fit inside the screen, centered, leaving black bars where the aspect ratios differ. */
    private static BufferedImage fitScale(BufferedImage source) {
        int w = GamePanel.SCREEN_WIDTH;
        int h = GamePanel.SCREEN_HEIGHT;
        double scale = Math.min((double) w / source.getWidth(), (double) h / source.getHeight());
        int scaledW = (int) Math.round(source.getWidth() * scale);
        int scaledH = (int) Math.round(source.getHeight() * scale);
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(source, (w - scaledW) / 2, (h - scaledH) / 2, scaledW, scaledH, null);
        g.dispose();
        return result;
    }

    private static BufferedImage brighten(BufferedImage image) {
        if (image == null) {
            return null;
        }
        // A curve instead of a plain multiply: dark values shoot up while bright ones flatten out below the ceiling.
        short[] curve = new short[256];
        for (int value = 0; value < 256; value++) {
            curve[value] = (short) Math.round(FLASH_LIT_CEILING * (1 - Math.exp(-FLASH_LIT_BRIGHTNESS * value / 255.0)));
        }
        return new LookupOp(new ShortLookupTable(0, curve), null).filter(image, null);
    }

    // A black layer with a soft see-through hole, built once and reused every frame.
    private static BufferedImage buildDarkness() {
        int w = GamePanel.SCREEN_WIDTH;
        int h = GamePanel.SCREEN_HEIGHT;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        int outsideAlpha = Math.round(DARKNESS_OUTSIDE * 255);
        g.setPaint(new RadialGradientPaint(HOLE_CENTER_X * w, HOLE_CENTER_Y * h, HOLE_RADIUS,
                new float[] {0f, 0.35f, 0.7f, 1f},
                new Color[] {
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, outsideAlpha / 2),
                    new Color(0, 0, 0, outsideAlpha)}));
        g.fillRect(0, 0, w, h);
        g.dispose();
        return image;
    }

    private int randomFlashGap() {
        int seconds = MIN_SECONDS_BETWEEN_FLASHES
                + random.nextInt(MAX_SECONDS_BETWEEN_FLASHES - MIN_SECONDS_BETWEEN_FLASHES + 1);
        return seconds * TICKS_PER_SECOND;
    }

    @Override
    public void update() {
        vhs.update();
        rain.update();
        updateLightning();

        if (fading) {
            fadeTicks++;
            if (fadeTicks >= FADE_OUT_TICKS) {
                startOverworld();
            }
            return;
        }
        handleMenuInput();
    }

    private void updateLightning() {
        if (flashTick < 0 && --ticksUntilFlash <= 0) {
            flashTick = 0;
            double delaySeconds = MIN_THUNDER_DELAY_SECONDS
                    + random.nextDouble() * (MAX_THUNDER_DELAY_SECONDS - MIN_THUNDER_DELAY_SECONDS);
            ticksUntilThunder = (int) Math.round(delaySeconds * TICKS_PER_SECOND);
        }

        if (flashTick >= 0) {
            flash = Math.max(
                    pulse(flashTick, 0, 1f, FIRST_PULSE_DECAY_TICKS),
                    pulse(flashTick, SECOND_PULSE_START_TICK, SECOND_PULSE_PEAK, SECOND_PULSE_DECAY_TICKS));
            staring = true;
            flashTick++;
            if (flashTick > SECOND_PULSE_START_TICK + SECOND_PULSE_DECAY_TICKS + STARE_HOLD_TICKS) {
                flashTick = -1;
                staring = false;
                flash = 0;
                ticksUntilFlash = randomFlashGap();
            }
        }

        if (ticksUntilThunder > 0 && --ticksUntilThunder == 0) {
            thunderSound.play();
        }
    }

    // Lightning has no fade-in: jump to the peak at startTick, then fade out linearly.
    private static float pulse(int tick, int startTick, float peak, int decayTicks) {
        if (tick < startTick) {
            return 0f;
        }
        return peak * Math.max(0f, 1f - (float) (tick - startTick) / decayTicks);
    }

    private void handleMenuInput() {
        InputHandler input = manager.getInput();
        // Left and Right also work because the buttons sit in a row.
        if (input.wasPressed(Action.UP) || input.wasPressed(Action.LEFT)) {
            moveSelection(-1);
        } else if (input.wasPressed(Action.DOWN) || input.wasPressed(Action.RIGHT)) {
            moveSelection(1);
        }
        if (input.wasPressed(Action.CONFIRM)) {
            activateSelected();
        }

        MouseInput mouse = manager.getMouse();
        int hovered = buttonAt(mouse.getX(), mouse.getY());
        boolean usable = hovered >= 0 && menu[hovered].enabled;
        // Only a real mouse move steals the highlight, so the keyboard still works while the pointer rests on a button.
        boolean moved = mouse.getX() != lastMouseX || mouse.getY() != lastMouseY;
        lastMouseX = mouse.getX();
        lastMouseY = mouse.getY();
        if (usable && (moved || mouse.wasClicked())) {
            selected = hovered;
        }
        if (usable && mouse.wasClicked()) {
            activateSelected();
        }
    }

    private void activateSelected() {
        switch (menu[selected]) {
            case NEW_GAME -> fading = true;
            case EXIT -> System.exit(0);
            default -> { }
        }
    }

    private int buttonX(int index) {
        int totalWidth = menu.length * MENU_BUTTON_WIDTH + (menu.length - 1) * MENU_BUTTON_GAP;
        return (GamePanel.SCREEN_WIDTH - totalWidth) / 2 + index * (MENU_BUTTON_WIDTH + MENU_BUTTON_GAP);
    }

    /** Index of the button under the given game-pixel point, or -1. */
    private int buttonAt(int gameX, int gameY) {
        if (gameY < MENU_Y || gameY >= MENU_Y + MENU_BUTTON_HEIGHT) {
            return -1;
        }
        for (int i = 0; i < menu.length; i++) {
            if (gameX >= buttonX(i) && gameX < buttonX(i) + MENU_BUTTON_WIDTH) {
                return i;
            }
        }
        return -1;
    }

    private void moveSelection(int step) {
        do {
            selected = (selected + step + menu.length) % menu.length;
        } while (!menu[selected].enabled);
    }

    private void startOverworld() {
        try {
            OverworldState next = new OverworldState(manager);
            rainSound.close();
            manager.setState(next);
        } catch (LevelLoadException e) {
            errorMessage = e.getMessage();
            fading = false;
            fadeTicks = 0;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        Graphics2D scene = vhs.beginScene();
        drawRoom(scene);
        drawText(scene);
        drawMenu(scene);
        rain.draw(scene);
        vhs.present(g);

        if (fading) {
            float progress = Math.min(1f, (float) fadeTicks / FADE_OUT_TICKS);
            g.setComposite(AlphaComposite.SrcOver.derive(progress));
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
            g.setComposite(AlphaComposite.SrcOver);
        }
    }

    // Layers, bottom to top: room photo, brighter photo (flash), darkness, white flash.
    private void drawRoom(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        BufferedImage room = staring ? lookingUp : lookingDown;
        BufferedImage lit = staring ? lookingUpLit : lookingDownLit;
        if (room != null) {
            g.drawImage(room, 0, 0, null);
            if (flash > 0) {
                g.setComposite(AlphaComposite.SrcOver.derive(flash));
                g.drawImage(lit, 0, 0, null);
            }
        }

        g.setComposite(AlphaComposite.SrcOver.derive(1f - flash * FLASH_DARKNESS_REMOVED));
        g.drawImage(darkness, 0, 0, null);

        if (flash > 0) {
            g.setComposite(AlphaComposite.SrcOver.derive(flash * FLASH_WHITE_ALPHA));
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        }
        g.setComposite(AlphaComposite.SrcOver);
    }

    private void drawText(Graphics2D g) {
        g.setFont(TITLE_FONT);
        g.setColor(new Color(110, 0, 0, 170));
        g.drawString("ESCAPE THE BRAINROT", 43, 75);
        g.setColor(TITLE_RED);
        g.drawString("ESCAPE THE BRAINROT", 40, 72);

        g.setFont(SUBTITLE_FONT);
        g.setColor(new Color(190, 196, 215));
        g.drawString("Reclaim Your Mind", 46, 98);

        int boxWidth = 520;
        int boxX = (GamePanel.SCREEN_WIDTH - boxWidth) / 2;
        g.setColor(new Color(16, 20, 38, 225));
        g.fillRect(boxX, 432, boxWidth, 46);
        g.fillRect(boxX, 410, 60, 22);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SERIF, Font.BOLD, 14));
        drawCentered(g, "???", boxX, 60, 426);
        g.setFont(TAGLINE_FONT);
        drawCentered(g, "You can't stop scrolling... can you?", boxX, boxWidth, 462);

        if (errorMessage != null) {
            g.setColor(Color.ORANGE);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
            g.drawString("Error: " + errorMessage, 20, 560);
        }
    }

    private void drawMenu(Graphics2D g) {
        g.setFont(MENU_FONT);

        for (int i = 0; i < menu.length; i++) {
            int x = buttonX(i);
            boolean isSelected = i == selected;
            Color top;
            Color bottom;
            Color text;
            if (!menu[i].enabled) {
                top = new Color(70, 70, 76);
                bottom = new Color(48, 48, 54);
                text = new Color(120, 120, 128);
            } else if (isSelected) {
                top = new Color(232, 196, 120);
                bottom = new Color(184, 142, 76);
                text = new Color(25, 15, 5);
            } else {
                top = new Color(140, 112, 70);
                bottom = new Color(98, 76, 46);
                text = new Color(40, 28, 14);
            }

            g.setPaint(new GradientPaint(0, MENU_Y, top, 0, MENU_Y + MENU_BUTTON_HEIGHT, bottom));
            g.fillRect(x, MENU_Y, MENU_BUTTON_WIDTH, MENU_BUTTON_HEIGHT);
            if (isSelected) {
                g.setColor(new Color(255, 236, 170));
                g.drawRect(x, MENU_Y, MENU_BUTTON_WIDTH - 1, MENU_BUTTON_HEIGHT - 1);
                g.drawRect(x + 1, MENU_Y + 1, MENU_BUTTON_WIDTH - 3, MENU_BUTTON_HEIGHT - 3);
            }
            g.setColor(text);
            drawCentered(g, menu[i].label, x, MENU_BUTTON_WIDTH, MENU_Y + 25);
        }
    }

    private static void drawCentered(Graphics2D g, String text, int areaX, int areaWidth, int baselineY) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, areaX + (areaWidth - metrics.stringWidth(text)) / 2, baselineY);
    }
}
