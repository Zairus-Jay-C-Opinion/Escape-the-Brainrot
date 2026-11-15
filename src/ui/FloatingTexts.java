package ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Short messages that rise and fade, such as "-1" over the boss or "+9 STRAIN" by the gauge. Owner: Zrs. */
public class FloatingTexts {

    private static final int LIFE_TICKS = 50;
    private static final float RISE_PER_TICK = 0.9f;

    private static final class Floater {
        final String text;
        final float x;
        float y;
        final Color color;
        int age;

        Floater(String text, float x, float y, Color color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    private final List<Floater> floaters = new ArrayList<>();

    public void add(String text, int x, int y, Color color) {
        floaters.add(new Floater(text, x, y, color));
    }

    public void update() {
        Iterator<Floater> it = floaters.iterator();
        while (it.hasNext()) {
            Floater f = it.next();
            f.age++;
            f.y -= RISE_PER_TICK;
            if (f.age >= LIFE_TICKS) {
                it.remove();
            }
        }
    }

    public void draw(Graphics2D g) {
        g.setFont(UiTheme.MONO_LARGE.deriveFont(26f));
        for (Floater f : floaters) {
            float fade = Math.min(1f, 2f * (1f - (float) f.age / LIFE_TICKS));
            g.setComposite(AlphaComposite.SrcOver.derive(fade));
            UiTheme.shadowText(g, f.text, Math.round(f.x), Math.round(f.y), f.color);
        }
        g.setComposite(AlphaComposite.SrcOver);
    }
}
