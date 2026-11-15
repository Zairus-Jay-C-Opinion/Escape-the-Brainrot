package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/** Colors, fonts and small drawing helpers shared by every UI class so the screens look like one game. Owner: Zrs. */
public final class UiTheme {

    public static final Color CREAM = new Color(232, 222, 188);
    public static final Color CREAM_DIM = new Color(150, 142, 120);
    public static final Color PANEL = new Color(12, 14, 24, 236);
    public static final Color TEAL = new Color(70, 200, 200);
    public static final Color TEAL_PANEL = new Color(8, 34, 40, 232);
    public static final Color ROT = new Color(150, 80, 200);
    public static final Color STRAIN = new Color(225, 50, 60);

    public static final Font MONO_SMALL = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    public static final Font MONO_BOLD_SMALL = new Font(Font.MONOSPACED, Font.BOLD, 12);
    public static final Font MONO_MEDIUM = new Font(Font.MONOSPACED, Font.BOLD, 15);
    public static final Font MONO_LARGE = new Font(Font.MONOSPACED, Font.BOLD, 18);

    private UiTheme() { }

    public static void panel(Graphics2D g, int x, int y, int w, int h, Color fill, Color border) {
        g.setColor(fill);
        g.fillRect(x, y, w, h);
        g.setColor(border);
        g.drawRect(x, y, w - 1, h - 1);
    }

    /** Text with a one-pixel dark shadow so it stays readable on any background. */
    public static void shadowText(Graphics2D g, String text, int x, int y, Color color) {
        g.setColor(Color.BLACK);
        g.drawString(text, x + 1, y + 1);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    public static void centeredText(Graphics2D g, String text, int centerX, int baselineY, Color color) {
        FontMetrics metrics = g.getFontMetrics();
        shadowText(g, text, centerX - metrics.stringWidth(text) / 2, baselineY, color);
    }

    /** The largest size of the base font, down to a minimum, whose text still fits maxWidth. */
    public static Font fit(Graphics2D g, Font base, String text, int maxWidth) {
        Font font = base;
        while (font.getSize() > 10 && g.getFontMetrics(font).stringWidth(text) > maxWidth) {
            font = font.deriveFont((float) font.getSize() - 1);
        }
        return font;
    }

    public static Color blend(Color from, Color to, float amount) {
        float t = Math.max(0f, Math.min(1f, amount));
        return new Color(
                Math.round(from.getRed() + (to.getRed() - from.getRed()) * t),
                Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * t),
                Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * t),
                Math.round(from.getAlpha() + (to.getAlpha() - from.getAlpha()) * t));
    }
}
