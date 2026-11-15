package engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/** Slanted rain streaks. Drops live in parallel arrays instead of one object each, which keeps updating a few hundred cheap. */
public class RainLayer {

    private static final float SLANT = 0.3f;      // sideways pixels moved per pixel fallen
    private static final float MIN_SPEED = 14f;   // pixels per update
    private static final float MAX_SPEED = 26f;
    private static final float MIN_LENGTH = 10f;
    private static final float MAX_LENGTH = 24f;

    private final int width;
    private final int height;
    private final Random random = new Random();
    private final float[] x;
    private final float[] y;
    private final float[] speed;
    private final float[] length;
    private final Color[] color; // faster drops are drawn brighter, which fakes depth

    public RainLayer(int width, int height, int dropCount) {
        this.width = width;
        this.height = height;
        x = new float[dropCount];
        y = new float[dropCount];
        speed = new float[dropCount];
        length = new float[dropCount];
        color = new Color[dropCount];
        for (int i = 0; i < dropCount; i++) {
            respawn(i);
            y[i] = random.nextFloat() * height; // spread the first frame over the whole screen
        }
    }

    private void respawn(int i) {
        speed[i] = MIN_SPEED + random.nextFloat() * (MAX_SPEED - MIN_SPEED);
        length[i] = MIN_LENGTH + random.nextFloat() * (MAX_LENGTH - MIN_LENGTH);
        // Start left of the screen too, because the slant carries drops to the right as they fall.
        x[i] = -height * SLANT + random.nextFloat() * (width + height * SLANT);
        y[i] = -length[i];
        float brightness = (speed[i] - MIN_SPEED) / (MAX_SPEED - MIN_SPEED);
        color[i] = new Color(190, 210, 240, 35 + (int) (brightness * 65));
    }

    public void update() {
        for (int i = 0; i < x.length; i++) {
            y[i] += speed[i];
            x[i] += speed[i] * SLANT;
            if (y[i] > height || x[i] > width) {
                respawn(i);
            }
        }
    }

    public void draw(Graphics2D g) {
        for (int i = 0; i < x.length; i++) {
            g.setColor(color[i]);
            int headX = Math.round(x[i]);
            int headY = Math.round(y[i]);
            g.drawLine(headX, headY, Math.round(x[i] - length[i] * SLANT), Math.round(y[i] - length[i]));
        }
    }
}
