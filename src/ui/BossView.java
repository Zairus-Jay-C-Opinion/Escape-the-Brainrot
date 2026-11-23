package ui;

import engine.AssetManager;
import exception.AssetLoadException;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Draws a boss sprite and animates it in code: idle bob, breathing and sway, hit flash and knockback,
 * glitch bursts, a phase tint, and a glitch-out defeat. No sprite sheets needed. Owner: Zrs.
 */
public class BossView {

    private static final int PLACEHOLDER_WIDTH = 200;
    private static final int PLACEHOLDER_HEIGHT = 280;
    private static final int HIT_TICKS = 14;
    private static final int GLITCH_TICKS = 40;
    private static final int DEFEAT_TICKS = 100;
    private static final int STRIPS = 16;
    private static final float TINT_ALPHA = 0.3f;

    private BufferedImage sprite;
    private BufferedImage whiteSprite;
    private BufferedImage tintSprite;
    private int tick;
    private int hitTicks;
    private int glitchTicks;
    private int defeatTicks;
    private boolean defeating;
    private boolean enraged;
    private float tintAlpha;

    /** Loads the sprite, or builds a silhouette if the file is missing so the fight still works. */
    public BossView(String spritePath) {
        sprite = load(spritePath);
        whiteSprite = solidCopy(sprite, Color.WHITE);
    }

    private static BufferedImage load(String path) {
        try {
            return AssetManager.getImage(path);
        } catch (AssetLoadException e) {
            return placeholder();
        }
    }

    private static BufferedImage placeholder() {
        BufferedImage image = new BufferedImage(PLACEHOLDER_WIDTH, PLACEHOLDER_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(22, 18, 32));
        g.fillRoundRect(30, 100, 140, 170, 50, 50);
        g.fillRoundRect(2, 118, 40, 124, 30, 30);
        g.fillRoundRect(158, 118, 40, 124, 30, 30);
        g.fillOval(50, 8, 100, 104);
        g.setColor(new Color(64, 52, 96));
        g.setStroke(new java.awt.BasicStroke(3f));
        g.drawArc(50, 8, 100, 104, 30, 120);
        g.drawRoundRect(30, 100, 140, 170, 50, 50);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 72));
        g.setColor(new Color(48, 38, 72));
        g.drawString("?", 74, 214);
        g.setColor(new Color(255, 225, 110));
        g.fillOval(72, 46, 20, 20);
        g.fillOval(110, 46, 20, 20);
        g.setColor(new Color(40, 10, 10));
        g.fillOval(79, 53, 7, 7);
        g.fillOval(117, 53, 7, 7);
        g.dispose();
        return image;
    }

    // Same shape as the image, filled with one color. Used for the hit flash and the phase tint.
    private static BufferedImage solidCopy(BufferedImage source, Color color) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int rgb = color.getRGB() & 0xFFFFFF;
        for (int py = 0; py < source.getHeight(); py++) {
            for (int px = 0; px < source.getWidth(); px++) {
                copy.setRGB(px, py, (source.getRGB(px, py) & 0xFF000000) | rgb);
            }
        }
        return copy;
    }

    public void setSprite(String spritePath){
        sprite = load(spritePath);
        whiteSprite = solidCopy(sprite, Color.WHITE);
    }

    public void hit() {
        hitTicks = HIT_TICKS;
    }

    /** A short tear through the sprite, used when the phase changes. */
    public void glitchBurst() {
        glitchTicks = GLITCH_TICKS;
    }

    public void setEnraged(Color accent) {
        enraged = true;
        tintSprite = solidCopy(sprite, accent);
    }

    public void startDefeat() {
        defeating = true;
        defeatTicks = 0;
    }

    public boolean isDefeatFinished() {
        return defeating && defeatTicks >= DEFEAT_TICKS;
    }

    public void update() {
        tick++;
        if (hitTicks > 0) {
            hitTicks--;
        }
        if (glitchTicks > 0) {
            glitchTicks--;
        }
        if (defeating && defeatTicks < DEFEAT_TICKS) {
            defeatTicks++;
        }
        tintAlpha += ((enraged ? TINT_ALPHA : 0f) - tintAlpha) * 0.05f;
    }

    /** Draws the boss standing on the bottom-center of the given area, scaled to fit it. */
    public void draw(Graphics2D g, int areaX, int areaY, int areaWidth, int areaHeight) {
        if (isDefeatFinished()) {
            return;
        }
        int w = sprite.getWidth();
        int h = sprite.getHeight();
        float fit = Math.min((float) areaWidth / w, (float) areaHeight / h);

        double bob = Math.sin(tick * 0.05) * 5;
        double breath = 1 + 0.018 * Math.sin(tick * 0.07);
        double sway = Math.toRadians(1.4 * Math.sin(tick * 0.031));
        double knockback = hitTicks > 0 ? Math.sin(hitTicks * 2.2) * hitTicks * 0.9 : 0;

        AffineTransform saved = g.getTransform();
        g.translate(areaX + areaWidth / 2.0 + knockback, areaY + areaHeight + bob);
        g.rotate(sway);
        g.scale(fit * (1 - (breath - 1) * 0.5), fit * breath);
        g.translate(-w / 2.0, -h);

        float defeatProgress = defeating ? (float) defeatTicks / DEFEAT_TICKS : 0f;
        float glitchStrength = defeating ? defeatProgress : (glitchTicks > 0 ? 0.6f : 0f);
        float alpha = 1f - defeatProgress * defeatProgress;

        g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        drawBody(g, sprite, glitchStrength, defeating);
        if (hitTicks > 0) {
            g.setComposite(AlphaComposite.SrcOver.derive(alpha * hitTicks / HIT_TICKS));
            g.drawImage(whiteSprite, 0, 0, null);
        }
        if (tintAlpha > 0.01f && tintSprite != null) {
            g.setComposite(AlphaComposite.SrcOver.derive(alpha * tintAlpha));
            g.drawImage(tintSprite, 0, 0, null);
        }
        g.setComposite(AlphaComposite.SrcOver);
        g.setTransform(saved);
    }

    // With glitch strength above 0 the sprite is drawn as horizontal strips, each shifted sideways.
    private void drawBody(Graphics2D g, BufferedImage image, float glitchStrength, boolean dropStrips) {
        if (glitchStrength <= 0f) {
            g.drawImage(image, 0, 0, null);
            return;
        }
        // Seeding by tick keeps the tear the same however often the screen repaints within one update.
        Random strip = new Random(tick * 31L);
        int w = image.getWidth();
        int h = image.getHeight();
        for (int k = 0; k < STRIPS; k++) {
            int top = k * h / STRIPS;
            int bottom = (k + 1) * h / STRIPS;
            if (dropStrips && strip.nextFloat() < glitchStrength * 0.6f) {
                continue;
            }
            int shift = strip.nextFloat() < 0.6f ? Math.round((strip.nextFloat() * 2 - 1) * 45 * glitchStrength) : 0;
            g.drawImage(image, shift, top, shift + w, bottom, 0, top, w, bottom, null);
        }
    }
}
