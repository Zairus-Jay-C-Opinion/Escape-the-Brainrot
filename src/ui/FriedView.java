package ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.util.Random;

/** The "BRAIN FRIED." screen: a hunched kid lit by his phone, walled in by glowing screens. Owner: Zrs. */
public class FriedView {

    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 74);

    private record Screen(int x, int y, int w, int h, float hue, int phase) { }

    private final Screen[] screens;

    public FriedView() {
        Random random = new Random(7);
        screens = new Screen[18];
        for (int i = 0; i < screens.length; i++) {
            boolean left = i < screens.length / 2;
            int column = i % 3;
            int row = (i % (screens.length / 2)) / 3;
            int baseX = left ? 10 + column * 62 : 580 + column * 62;
            screens[i] = new Screen(baseX + random.nextInt(8), 100 + row * 100 + random.nextInt(14),
                    50 + random.nextInt(10), 78 + random.nextInt(14), random.nextFloat(), random.nextInt(60));
        }
    }

    public void draw(Graphics2D g, int tick) {
        g.setPaint(new GradientPaint(0, 0, new Color(8, 4, 14), 0, 576, new Color(20, 8, 28)));
        g.fillRect(0, 0, 768, 576);

        for (Screen s : screens) {
            drawScreen(g, s, tick);
        }

        g.setPaint(new RadialGradientPaint(384, 360, 190, new float[] {0f, 1f},
                new Color[] {new Color(80, 150, 190, 90), new Color(0, 0, 0, 0)}));
        g.fillRect(150, 160, 470, 400);
        drawKid(g);
        drawTitle(g, tick);
    }

    private void drawScreen(Graphics2D g, Screen s, int tick) {
        float flicker = 0.55f + 0.45f * (float) Math.sin((tick + s.phase) * 0.17);
        g.setComposite(AlphaComposite.SrcOver.derive(0.45f + 0.4f * flicker));
        g.setColor(new Color(40, 20, 60));
        g.fillRect(s.x, s.y, s.w, s.h);
        g.setColor(new Color(Math.round(120 + 90 * s.hue), 60, 150));
        g.fillRect(s.x + 4, s.y + 6, s.w - 8, s.h - 12);
        g.setColor(new Color(230, 200, 245, 150));
        for (int line = 0; line < 4; line++) {
            g.fillRect(s.x + 8, s.y + 12 + line * 12, s.w - 16 - (line * 7 % 14), 3);
        }
        g.setComposite(AlphaComposite.SrcOver);
    }

    private void drawKid(Graphics2D g) {
        g.setColor(new Color(40, 66, 52));
        g.fillRoundRect(346, 346, 76, 88, 30, 30);
        g.fillRoundRect(330, 388, 108, 40, 24, 24);
        g.setColor(new Color(44, 52, 82));
        g.fillRoundRect(340, 420, 92, 26, 16, 16);
        g.setColor(new Color(150, 175, 195));
        g.fillOval(364, 306, 38, 40);
        g.setColor(new Color(14, 12, 22));
        g.fillArc(361, 302, 44, 34, 0, 180);
        g.setColor(new Color(150, 235, 245));
        g.fillRoundRect(394, 372, 16, 26, 4, 4);
    }

    private void drawTitle(Graphics2D g, int tick) {
        g.setFont(TITLE_FONT);
        String text = "BRAIN FRIED.";
        int width = g.getFontMetrics().stringWidth(text);
        int x = 384 - width / 2 + (tick % 7 == 0 ? 5 : 0);
        int y = 250;
        g.setColor(new Color(60, 220, 230, 140));
        g.drawString(text, x - 4, y + 2);
        g.setColor(new Color(0, 0, 0, 200));
        g.drawString(text, x + 4, y + 4);
        g.setColor(new Color(214, 40, 44));
        g.drawString(text, x, y);
    }
}
