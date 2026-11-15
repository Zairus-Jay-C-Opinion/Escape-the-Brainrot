package engine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

/**
 * Reusable old-VHS look for any screen. The screen draws itself into beginScene(), then present()
 * pushes that picture to the real window with colour split, jitter, scanlines, grain, vignette,
 * a rolling tracking band and a PLAY/timestamp overlay. The static layers are built once.
 */
public class ScreenEffect {

    private static final int GRAIN_FRAME_COUNT = 5;
    private static final int GRAIN_MAX_ALPHA = 38;
    private static final int GRAIN_SWAP_TICKS = 3;       // updates between grain frames
    private static final int SCANLINE_SPACING = 3;       // one dark line every N pixel rows
    private static final int SCANLINE_ALPHA = 60;
    private static final int VIGNETTE_EDGE_ALPHA = 200;
    private static final int TRACKING_BAND_HEIGHT = 40;
    private static final float TRACKING_SPEED = 0.6f;    // pixels per update, so the band crawls
    private static final float TRACKING_ALPHA = 0.2f;
    private static final int COLOR_SPLIT_PIXELS = 2;     // how far red and blue drift apart
    private static final double JITTER_CHANCE = 0.004;   // per update
    private static final int JITTER_MAX_PIXELS = 6;
    private static final int JITTER_MAX_TICKS = 5;
    private static final int TICKS_PER_SECOND = 60;

    private final int width;
    private final int height;
    private final Random random = new Random();
    private final BufferedImage scene;
    private final BufferedImage split;
    private final BufferedImage staticOverlay; // scanlines and vignette merged into one image
    private final BufferedImage[] grainFrames = new BufferedImage[GRAIN_FRAME_COUNT];
    private final BufferedImage trackingBand;
    private final Font osdFont = new Font(Font.MONOSPACED, Font.BOLD, 20);

    private Graphics2D sceneGraphics;
    private int ticks;
    private float trackingY;
    private int jitterTicksLeft;
    private int jitterOffset;
    private String timestamp = "00:00:00";

    public ScreenEffect(int width, int height) {
        this.width = width;
        this.height = height;
        scene = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        split = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        staticOverlay = buildStaticOverlay();
        for (int i = 0; i < GRAIN_FRAME_COUNT; i++) {
            grainFrames[i] = buildGrainFrame();
        }
        trackingBand = buildTrackingBand();
        trackingY = height / 3f;
    }

    private BufferedImage buildStaticOverlay() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(0, 0, 0, SCANLINE_ALPHA));
        for (int row = 0; row < height; row += SCANLINE_SPACING) {
            g.fillRect(0, row, width, 1);
        }
        float radius = (float) Math.hypot(width, height) / 2f;
        g.setPaint(new RadialGradientPaint(width / 2f, height / 2f, radius,
                new float[] {0f, 0.55f, 1f},
                new Color[] {new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, VIGNETTE_EDGE_ALPHA)}));
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    private BufferedImage buildGrainFrame() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < pixels.length; i++) {
            int shade = random.nextBoolean() ? 255 : 0;
            int alpha = random.nextInt(GRAIN_MAX_ALPHA);
            pixels[i] = (alpha << 24) | (shade << 16) | (shade << 8) | shade;
        }
        return image;
    }

    private BufferedImage buildTrackingBand() {
        BufferedImage image = new BufferedImage(width, TRACKING_BAND_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int row = 0; row < TRACKING_BAND_HEIGHT; row++) {
            // The band fades in and out vertically, and each row gets its own noise strength like a torn tape line.
            double fade = Math.sin(Math.PI * row / TRACKING_BAND_HEIGHT);
            double rowStrength = 0.4 + 0.6 * random.nextDouble();
            for (int col = 0; col < width; col++) {
                int shade = random.nextInt(256);
                int alpha = (int) (255 * fade * rowStrength * random.nextDouble());
                pixels[row * width + col] = (alpha << 24) | (shade << 16) | (shade << 8) | shade;
            }
        }
        return image;
    }

    /** Advances the animated parts. Call once per game update. */
    public void update() {
        ticks++;
        if (ticks % TICKS_PER_SECOND == 0) {
            timestamp = formatTimestamp(ticks / TICKS_PER_SECOND);
        }

        trackingY += TRACKING_SPEED;
        if (trackingY > height) {
            trackingY = -TRACKING_BAND_HEIGHT;
        }

        if (jitterTicksLeft > 0) {
            jitterTicksLeft--;
        } else if (random.nextDouble() < JITTER_CHANCE) {
            jitterTicksLeft = 1 + random.nextInt(JITTER_MAX_TICKS);
            jitterOffset = (random.nextBoolean() ? 1 : -1) * (2 + random.nextInt(JITTER_MAX_PIXELS - 1));
        }
    }

    private static String formatTimestamp(int totalSeconds) {
        return String.format("%02d:%02d:%02d", totalSeconds / 3600, totalSeconds / 60 % 60, totalSeconds % 60);
    }

    /** Returns the graphics to draw the screen into. Always follow with present(). */
    public Graphics2D beginScene() {
        sceneGraphics = scene.createGraphics();
        sceneGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        sceneGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return sceneGraphics;
    }

    /** Draws the finished scene onto the real screen with every VHS layer on top. */
    public void present(Graphics2D g) {
        sceneGraphics.dispose();
        splitColorChannels();

        int shift = jitterTicksLeft > 0 ? jitterOffset : 0;
        g.drawImage(split, shift, 0, null);
        g.drawImage(staticOverlay, 0, 0, null);
        g.drawImage(grainFrames[(ticks / GRAIN_SWAP_TICKS) % GRAIN_FRAME_COUNT], 0, 0, null);
        drawTrackingBand(g);
        drawOverlayText(g);
    }

    // Copies scene into split, taking red from a few pixels to the right and blue from a few to the left.
    // Done on the raw int arrays because a per-pixel loop is cheaper than filtering the image twice.
    private void splitColorChannels() {
        int[] src = ((DataBufferInt) scene.getRaster().getDataBuffer()).getData();
        int[] dst = ((DataBufferInt) split.getRaster().getDataBuffer()).getData();
        int last = width - 1;
        for (int row = 0; row < height; row++) {
            int base = row * width;
            for (int col = 0; col < width; col++) {
                int redSource = base + Math.min(last, col + COLOR_SPLIT_PIXELS);
                int blueSource = base + Math.max(0, col - COLOR_SPLIT_PIXELS);
                dst[base + col] = (src[redSource] & 0xFF0000) | (src[base + col] & 0x00FF00) | (src[blueSource] & 0x0000FF);
            }
        }
    }

    private void drawTrackingBand(Graphics2D g) {
        g.setComposite(AlphaComposite.SrcOver.derive(TRACKING_ALPHA));
        g.drawImage(trackingBand, 0, Math.round(trackingY), null);
        g.setComposite(AlphaComposite.SrcOver);
    }

    private void drawOverlayText(Graphics2D g) {
        g.setFont(osdFont);
        int right = width - 24;
        int textX = right - 26 - g.getFontMetrics().stringWidth("PLAY");
        drawShadowedText(g, "PLAY", textX, 34);
        // A drawn triangle instead of the unicode arrow, which not every font has.
        Polygon arrow = new Polygon(
                new int[] {right - 20, right - 20, right},
                new int[] {20, 36, 28}, 3);
        g.setColor(Color.BLACK);
        g.translate(1, 1);
        g.fillPolygon(arrow);
        g.translate(-1, -1);
        g.setColor(Color.WHITE);
        g.fillPolygon(arrow);
        drawShadowedText(g, timestamp, right - g.getFontMetrics().stringWidth(timestamp), 58);
    }

    private void drawShadowedText(Graphics2D g, String text, int x, int y) {
        g.setColor(Color.BLACK);
        g.drawString(text, x + 1, y + 1);
        g.setColor(new Color(235, 235, 235));
        g.drawString(text, x, y);
    }
}
