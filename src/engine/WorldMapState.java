package engine;

import battle.Boss;
import battle.Boss67;
import battle.ChimpanziniBananini;
import battle.LiriliLarila;
import battle.TralaleroTralala;
import engine.InputHandler.Action;
import exception.AssetLoadException;
import exception.LevelLoadException;
import ui.Hud;
import ui.UiTheme;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** CRT TV-wall level select screen ("Select a channel."). Owner: Zrs. */
public class WorldMapState extends GameState {

    private static final class Channel {
        final String name;
        final Boss boss;
        final Rectangle screen;
        final int requiresIndex;
        final boolean isFinal;
        final BufferedImage sprite;
        final BufferedImage silhouette;

        Channel(String name, Boss boss, Rectangle screen, int requiresIndex, boolean isFinal) {
            this.name = name;
            this.boss = boss;
            this.screen = screen;
            this.requiresIndex = requiresIndex;
            this.isFinal = isFinal;
            this.sprite = loadSprite(boss != null ? boss.getSpritePath() : null);
            this.silhouette = makeSilhouette(this.sprite);
        }
    }

    private static final Color BG_COLOR = new Color(10, 12, 18);
    private static final Color SCREEN_STATIC_BG = new Color(16, 26, 30);
    private static final Color SCREEN_UNLOCKED_BG = new Color(18, 34, 42);
    private static final Color SCREEN_CLEARED_BG = new Color(16, 36, 24);
    private static final Color SELECT_GLOW = new Color(235, 220, 140);
    private static final Color FINAL_CRIMSON = new Color(220, 40, 55);
    private static final Font LABEL_FONT = new Font(Font.MONOSPACED, Font.BOLD, 10);

    private final BufferedImage background;
    private final List<Channel> channels = new ArrayList<>();
    private final List<Boss> bosses = new ArrayList<>();
    private final Hud hud;
    private final ScreenEffect vhs = new ScreenEffect(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, 0.35f, false);
    private final Random random = new Random();

    private int selected = 0;
    private int tick = 0;
    private String statusText = "Select a channel.";
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    public WorldMapState(StateManager manager) {
        super(manager);
        this.background = loadSprite("/sprites/ui/world_map.jpeg");

        // Screen rectangles aligned to the 6 boss TVs in world_map.jpeg (768x576).
        channels.add(new Channel("CHIMPANZINI BANANINI", new ChimpanziniBananini(), new Rectangle(164, 116, 96, 80), -1, false));
        channels.add(new Channel("BOMBARDIRO CROCODILO", null, new Rectangle(514, 104, 130, 96), 0, false));
        channels.add(new Channel("TRALALERO TRALALA", new TralaleroTralala(), new Rectangle(16, 336, 112, 88), 0, false));
        channels.add(new Channel("LIRILI LARILA", new LiriliLarila(), new Rectangle(180, 344, 104, 82), 2, false));
        channels.add(new Channel("TUNG TUNG TUNG SAHUR", null, new Rectangle(508, 324, 132, 102), 3, false));
        channels.add(new Channel("67", new Boss67(), new Rectangle(298, 38, 170, 134), 4, true));

        for (Channel c : channels) {
            if (c.boss != null) {
                bosses.add(c.boss);
            }
        }
        this.hud = new Hud(manager.getGauge(), bosses);
    }

    private static BufferedImage loadSprite(String path) {
        if (path == null) {
            return null;
        }
        try {
            return AssetManager.getImage(path);
        } catch (AssetLoadException e) {
            return null;
        }
    }

    private static BufferedImage makeSilhouette(BufferedImage src) {
        if (src == null) {
            return null;
        }
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.setComposite(AlphaComposite.SrcIn);
        g.setColor(new Color(8, 12, 16, 235));
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.dispose();
        return copy;
    }

    private boolean isUnlocked(Channel c) {
        if (c.boss == null) {
            return false;
        }
        if (c.requiresIndex < 0) {
            return true;
        }
        Boss prereq = channels.get(c.requiresIndex).boss;
        return prereq != null && prereq.isDefeated();
    }

    @Override
    public void update() {
        tick++;
        hud.update();
        vhs.update();
        InputHandler input = manager.getInput();
        MouseInput mouse = manager.getMouse();

        if (input.wasPressed(Action.LEFT) || input.wasPressed(Action.UP)) {
            selected = (selected - 1 + channels.size()) % channels.size();
            statusText = "Select a channel.";
        } else if (input.wasPressed(Action.RIGHT) || input.wasPressed(Action.DOWN)) {
            selected = (selected + 1) % channels.size();
            statusText = "Select a channel.";
        }

        int mx = mouse.getX();
        int my = mouse.getY();
        boolean mouseMoved = mx != lastMouseX || my != lastMouseY;
        lastMouseX = mx;
        lastMouseY = my;

        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).screen.contains(mx, my)) {
                if (mouseMoved) {
                    selected = i;
                }
                if (mouse.wasClicked()) {
                    selected = i;
                    activateSelected();
                }
            }
        }

        if (input.wasPressed(Action.CONFIRM)) {
            activateSelected();
        }
    }

    private void activateSelected() {
        Channel c = channels.get(selected);
        if (c.boss != null && c.boss.isDefeated()) {
            statusText = c.name + " is already CLEARED.";
        } else if (!isUnlocked(c)) {
            statusText = "NO SIGNAL — Channel locked.";
        } else {
            try {
                manager.setState(new OverworldState(manager, this, "/maps/test-map.csv", c.boss, bosses));
            } catch (LevelLoadException e) {
                statusText = "Failed to load level: " + e.getMessage();
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        Graphics2D scene = vhs.beginScene();
        if (background != null) {
            scene.drawImage(background, 0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, null);
        } else {
            scene.setColor(BG_COLOR);
            scene.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        }

        for (int i = 0; i < channels.size(); i++) {
            drawChannelTv(scene, channels.get(i), i == selected, i);
        }

        scene.setFont(UiTheme.MONO_MEDIUM);
        UiTheme.centeredText(scene, statusText, GamePanel.SCREEN_WIDTH / 2,
                GamePanel.SCREEN_HEIGHT - 16, UiTheme.CREAM);

        hud.draw(scene);
        vhs.present(g);
    }

    private void drawChannelTv(Graphics2D g, Channel c, boolean isSelected, int index) {
        Rectangle r = c.screen;
        boolean defeated = c.boss != null && c.boss.isDefeated();
        boolean unlocked = isUnlocked(c);

        // Screen background
        if (defeated) {
            g.setColor(SCREEN_CLEARED_BG);
        } else if (unlocked) {
            g.setColor(c.isFinal ? new Color(46, 10, 16) : SCREEN_UNLOCKED_BG);
        } else {
            g.setColor(SCREEN_STATIC_BG);
        }
        g.fillRect(r.x, r.y, r.width, r.height);

        if (defeated) {
            drawFitted(g, c.sprite, r, 6);
            g.setFont(LABEL_FONT);
            UiTheme.centeredText(g, "CLEARED", r.x + r.width / 2, r.y + r.height - 6, new Color(160, 255, 175));
        } else if (unlocked) {
            drawStaticLines(g, r, c.isFinal ? 10 : 5, c.isFinal ? new Color(255, 70, 90, 60) : new Color(150, 210, 220, 45));
            drawFitted(g, c.silhouette, r, 6);
            if (c.isFinal) {
                float pulse = (float) (0.5 + 0.5 * Math.sin(tick * 0.1));
                g.setColor(new Color(220, 40, 55, Math.round(50 + 70 * pulse)));
                g.drawRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4);
            }
        } else {
            // Stuttering NO SIGNAL static
            drawStaticLines(g, r, 14, new Color(180, 215, 220, 65));
            if ((tick / 6 + index) % 5 != 0) {
                g.setFont(UiTheme.MONO_BOLD_SMALL);
                Color sigColor = c.isFinal ? new Color(220, 120, 130) : new Color(170, 200, 205);
                UiTheme.centeredText(g, "NO SIGNAL", r.x + r.width / 2, r.y + r.height / 2 + 4, sigColor);
            }
        }

        // Selection highlight around the TV screen
        if (isSelected) {
            g.setStroke(new BasicStroke(2.5f));
            g.setColor(c.isFinal && unlocked ? FINAL_CRIMSON : SELECT_GLOW);
            g.drawRect(r.x - 2, r.y - 2, r.width + 4, r.height + 4);
            g.setStroke(new BasicStroke(1f));
        }

        // Nameplate below the TV: "???" until defeated, then the boss's real name.
        String displayName = defeated ? c.name : "???";
        int plateY = r.y + r.height + 10;
        int plateW = Math.max(r.width + 24, 96);
        int plateX = r.x + r.width / 2 - plateW / 2;
        g.setColor(new Color(10, 12, 18, 230));
        g.fillRect(plateX, plateY, plateW, 18);
        g.setFont(LABEL_FONT);
        UiTheme.centeredText(g, displayName, r.x + r.width / 2, plateY + 13,
                isSelected ? SELECT_GLOW : UiTheme.CREAM);
    }

    private void drawStaticLines(Graphics2D g, Rectangle r, int count, Color color) {
        g.setColor(color);
        for (int i = 0; i < count; i++) {
            int ly = r.y + random.nextInt(Math.max(1, r.height));
            int lw = Math.max(8, random.nextInt(Math.max(9, r.width)));
            int lx = r.x + random.nextInt(Math.max(1, r.width - lw + 1));
            g.fillRect(lx, ly, lw, 2);
        }
    }

    private static void drawFitted(Graphics2D g, BufferedImage img, Rectangle r, int pad) {
        if (img == null) {
            return;
        }
        int maxW = r.width - pad * 2;
        int maxH = r.height - pad * 2;
        double scale = Math.min((double) maxW / img.getWidth(), (double) maxH / img.getHeight());
        int w = (int) Math.round(img.getWidth() * scale);
        int h = (int) Math.round(img.getHeight() * scale);
        int x = r.x + (r.width - w) / 2;
        int y = r.y + (r.height - h) / 2;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, x, y, w, h, null);
    }
}
