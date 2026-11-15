package ui;

import battle.PhaseStyle;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;

/** The dark room behind a fight. Its tint and glow follow the boss's PhaseStyle and ease when the phase changes. Owner: Zrs. */
public class BattleBackdrop {

    private static final float TINT_ALPHA = 0.55f;
    private static final double EASE = 0.04;
    private static final int GRID = 48;

    private Color tint;
    private Color accent;
    private Color targetTint;
    private Color targetAccent;
    private int tick;

    public BattleBackdrop(PhaseStyle style) {
        tint = targetTint = style.tint();
        accent = targetAccent = style.accent();
    }

    /** Starts easing toward a new look. */
    public void setStyle(PhaseStyle style) {
        targetTint = style.tint();
        targetAccent = style.accent();
    }

    public void update() {
        tick++;
        tint = UiTheme.blend(tint, targetTint, (float) EASE);
        accent = UiTheme.blend(accent, targetAccent, (float) EASE);
    }

    public Color getAccent() {
        return accent;
    }

    public void draw(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, new Color(6, 8, 14), 0, 576, new Color(16, 20, 30)));
        g.fillRect(0, 0, 768, 576);

        g.setComposite(AlphaComposite.SrcOver.derive(TINT_ALPHA));
        g.setColor(tint);
        g.fillRect(0, 0, 768, 576);
        g.setComposite(AlphaComposite.SrcOver);

        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 16));
        for (int x = 0; x < 768; x += GRID) {
            g.drawLine(x, 90, x, 430);
        }
        for (int y = 90; y < 430; y += GRID) {
            g.drawLine(0, y, 768, y);
        }

        int glow = 52 + (int) (10 * Math.sin(tick * 0.04));
        g.setPaint(new RadialGradientPaint(604, 290, 230, new float[] {0f, 1f},
                new Color[] {new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), glow),
                        new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0)}));
        g.fillRect(380, 60, 388, 400);
    }
}
