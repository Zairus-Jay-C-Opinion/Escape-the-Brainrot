package ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/** A band across the screen with big text that slams in, like "PHASE 2". Owner: Zrs. */
public class SlamBanner {

    private static final int TOTAL_TICKS = 110;
    private static final int SLAM_TICKS = 10;
    private static final int FADE_TICKS = 20;
    private static final int BAND_Y = 120;
    private static final int BAND_HEIGHT = 70;
    private static final float START_SCALE = 3.2f;
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 54);

    private String text = "";
    private Color color = Color.RED;
    private int age = TOTAL_TICKS;

    public void start(String text, Color color) {
        this.text = text;
        this.color = color;
        this.age = 0;
    }

    public boolean isActive() {
        return age < TOTAL_TICKS;
    }

    public void update() {
        if (isActive()) {
            age++;
        }
    }

    public void draw(Graphics2D g) {
        if (!isActive()) {
            return;
        }
        float fade = age > TOTAL_TICKS - FADE_TICKS ? (float) (TOTAL_TICKS - age) / FADE_TICKS : 1f;
        g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0f, fade)));

        int width = 768;
        g.setColor(new Color(10, 4, 8, 220));
        g.fillRect(0, BAND_Y, width, BAND_HEIGHT);
        g.setColor(color);
        g.fillRect(0, BAND_Y, width, 2);
        g.fillRect(0, BAND_Y + BAND_HEIGHT - 2, width, 2);

        // Scale shrinks from large to 1 over a few ticks, which reads as the text slamming down onto the band.
        float t = Math.min(1f, (float) age / SLAM_TICKS);
        float scale = START_SCALE - (START_SCALE - 1f) * t * t;
        int shakeX = age < SLAM_TICKS + 6 ? (age % 2 == 0 ? 4 : -4) : 0;

        g.setFont(FONT);
        int textWidth = g.getFontMetrics().stringWidth(text);
        AffineTransform old = g.getTransform();
        g.translate(width / 2 + shakeX, BAND_Y + BAND_HEIGHT / 2 + 19);
        g.scale(scale, scale);
        g.setColor(new Color(0, 0, 0, 200));
        g.drawString(text, -textWidth / 2 + 3, 3);
        g.setColor(color);
        g.drawString(text, -textWidth / 2, 0);
        g.setTransform(old);
        g.setComposite(AlphaComposite.SrcOver);
    }
}
