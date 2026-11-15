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
    private static final int GRAIN_MAX_ALPHA = 38; // all strengths below are at intensity 1
    private static final int GRAIN_SWAP_TICKS = 3;       // updates between grain frames
    private static final int SCANLINE_SPACING = 3;       // one dark line every N pixel rows
    private static final int SCANLINE_ALPHA = 60;
    private static final int VIGNETTE_EDGE_ALPHA = 200;
    private static final int TRACKING_BAND_HEIGHT = 40;
    private static final float TRACKING_SPEED = 0.6f;    // pixels per update, so the band crawls
    private static final float TRACKING_ALPHA = 0.2f;
    private static final int COLOR_SPLIT_PIXELS = 2;     // how far red and blue drift apart, always at least this
    private static final double JITTER_CHANCE = 0.004;   // per update
    private static final int JITTER_MAX_PIXELS = 6;
    private static final int JITTER_MAX_TICKS = 5;
    private static final int TICKS_PER_SECOND = 60;
    private static final int GLITCH_BANDS = 14;
    private static final int GLITCH_MAX_SHIFT_PIXELS = 70;
    private static final int GLITCH_EXTRA_SPLIT_PIXELS = 7;

    private final int width;
    private final int height;
    private final float intensity;
    private final boolean showOverlayText;
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
    private int glitchTicksLeft;
    private float glitchStrength;
    private String timestamp = "00:00:00";

    public ScreenEffect(int width, int height) {
        this(width, height, 1f, true);
    }

    /** Intensity 1 is the full look; lower values fade every layer. The PLAY overlay can be turned off. */
    public ScreenEffect(int width, int height, float intensity, boolean showOverlayText) {
        this.width = width;
        this.height = height;
        this.intensity = intensity;
        this.showOverlayText = showOverlayText;
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
        g.setColor(new Color(0, 0, 0, Math.round(SCANLINE_ALPHA * intensity)));
        for (int row = 0; row < height; row += SCANLINE_SPACING) {
            g.fillRect(0, row, width, 1);
        }
        float radius = (float) Math.hypot(width, height) / 2f;
        g.setPaint(new RadialGradientPaint(width / 2f, height / 2f, radius,
                new float[] {0f, 0.55f, 1f},
                new Color[] {new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, Math.round(VIGNETTE_EDGE_ALPHA * intensity))}));
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    private BufferedImage buildGrainFrame() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < pixels.length; i++) {
            int shade = random.nextBoolean() ? 255 : 0;
            int alpha = random.nextInt(Math.max(1, Math.round(GRAIN_MAX_ALPHA * intensity)));
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

        if (glitchTicksLeft > 0) {
            glitchTicksLeft--;
        }

        if (jitterTicksLeft > 0) {
            jitterTicksLeft--;
        } else if (random.nextDouble() < JITTER_CHANCE * intensity) {
            jitterTicksLeft = 1 + random.nextInt(JITTER_MAX_TICKS);
            jitterOffset = (random.nextBoolean() ? 1 : -1) * (2 + random.nextInt(JITTER_MAX_PIXELS - 1));
        }
    }

    /** Tears the picture into shifted bands with a wide colour split. Strength 0 to 1; call every update to hold it. */
    public void glitch(int ticks, float strength) {
        glitchTicksLeft = Math.max(glitchTicksLeft, ticks);
        glitchStrength = strength;
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
        boolean glitching = glitchTicksLeft > 0;
        splitColorChannels(COLOR_SPLIT_PIXELS + (glitching ? Math.round(GLITCH_EXTRA_SPLIT_PIXELS * glitchStrength) : 0));

        if (glitching) {
            drawGlitchedBands(g);
        } else {
            g.drawImage(split, jitterTicksLeft > 0 ? jitterOffset : 0, 0, null);
        }
        g.drawImage(staticOverlay, 0, 0, null);
        g.drawImage(grainFrames[(ticks / GRAIN_SWAP_TICKS) % GRAIN_FRAME_COUNT], 0, 0, null);
        drawTrackingBand(g);
        if (showOverlayText) {
            drawOverlayText(g);
        }
    }

    private void drawGlitchedBands(Graphics2D g) {
        int bandHeight = height / GLITCH_BANDS + 1;
        for (int band = 0; band < GLITCH_BANDS; band++) {
            int top = band * bandHeight;
            int bottom = Math.min(height, top + bandHeight);
            // Roughly half the bands stay put so the picture is torn, not gone.
            int shift = random.nextFloat() < 0.5f * glitchStrength + 0.2f
                    ? Math.round((random.nextFloat() * 2 - 1) * GLITCH_MAX_SHIFT_PIXELS * glitchStrength) : 0;
            g.drawImage(split, shift, top, width + shift, bottom, 0, top, width, bottom, null);
        }
    }

    // Copies scene into split, taking red from a few pixels to the right and blue from a few to the left.
    // Done on the raw int arrays because a per-pixel loop is cheaper than filtering the image twice.
    private void splitColorChannels(int splitPixels) {
        int[] src = ((DataBufferInt) scene.getRaster().getDataBuffer()).getData();
        int[] dst = ((DataBufferInt) split.getRaster().getDataBuffer()).getData();
        int last = width - 1;
        for (int row = 0; row < height; row++) {
            int base = row * width;
            for (int col = 0; col < width; col++) {
                int redSource = base + Math.min(last, col + splitPixels);
                int blueSource = base + Math.max(0, col - splitPixels);
                dst[base + col] = (src[redSource] & 0xFF0000) | (src[base + col] & 0x00FF00) | (src[blueSource] & 0x0000FF);
            }
        }
    }

    private void drawTrackingBand(Graphics2D g) {
        g.setComposite(AlphaComposite.SrcOver.derive(TRACKING_ALPHA * intensity));
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
