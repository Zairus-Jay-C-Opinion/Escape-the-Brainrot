import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * Dev tool: turns a character on a flat magenta background into a cropped transparent PNG.
 * Usage: java tools/CutoutSprite.java <input> <output>
 */
public class CutoutSprite {
    static final int BG_TOLERANCE = 90;   // max color distance from the background to count as background
    static final int FRINGE_PASSES = 3;   // how many pixels deep to peel pink edge pixels
    static final int DESPILL_DEPTH = 2;   // how many pixels in from the edge to remove leftover tint
    static final int PADDING = 4;

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java tools/CutoutSprite.java <input> <output>");
            System.exit(1);
        }
        BufferedImage src = ImageIO.read(new File(args[0]));
        int w = src.getWidth(), h = src.getHeight();
        int[] px = src.getRGB(0, 0, w, h, null, 0, w);
        boolean[] bg = new boolean[w * h];

        int bgColor = estimateBackground(px, w, h);

        // Flood fill from every border pixel: only background connected to the edge is removed,
        // so magenta-ish pixels inside the character survive.
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int x = 0; x < w; x++) { seed(queue, bg, px, bgColor, x, 0, w); seed(queue, bg, px, bgColor, x, h - 1, w); }
        for (int y = 0; y < h; y++) { seed(queue, bg, px, bgColor, 0, y, w); seed(queue, bg, px, bgColor, w - 1, y, w); }
        while (!queue.isEmpty()) {
            int i = queue.poll();
            int x = i % w, y = i / w;
            if (x > 0) seed(queue, bg, px, bgColor, x - 1, y, w);
            if (x < w - 1) seed(queue, bg, px, bgColor, x + 1, y, w);
            if (y > 0) seed(queue, bg, px, bgColor, x, y - 1, w);
            if (y < h - 1) seed(queue, bg, px, bgColor, x, y + 1, w);
        }

        // Peel pink fringe: pixels touching the background that are still pink-tinted.
        for (int pass = 0; pass < FRINGE_PASSES; pass++) {
            boolean[] next = bg.clone();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int i = y * w + x;
                    if (!bg[i] && touchesBackground(bg, x, y, w, h) && isPink(px[i])) next[i] = true;
                }
            }
            bg = next;
        }

        // Despill: the outline pixels that survived still carry a magenta tint from the JPEG blur.
        // Within DESPILL_DEPTH pixels of the edge, pull red and blue back down toward green.
        boolean[] near = bg;
        for (int d = 0; d < DESPILL_DEPTH; d++) {
            boolean[] grown = near.clone();
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    if (!near[y * w + x] && touchesBackground(near, x, y, w, h)) {
                        grown[y * w + x] = true;
                        px[y * w + x] = despill(px[y * w + x]);
                    }
            near = grown;
        }

        // Write result, find bounding box of what is left.
        int minX = w, minY = h, maxX = -1, maxY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                if (bg[i]) { px[i] = 0; continue; }
                px[i] = px[i] | 0xFF000000;
                minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                minY = Math.min(minY, y); maxY = Math.max(maxY, y);
            }
        }
        if (maxX < 0) throw new IllegalStateException("Nothing left after removing background");
        minX = Math.max(0, minX - PADDING); minY = Math.max(0, minY - PADDING);
        maxX = Math.min(w - 1, maxX + PADDING); maxY = Math.min(h - 1, maxY + PADDING);

        BufferedImage out = new BufferedImage(maxX - minX + 1, maxY - minY + 1, BufferedImage.TYPE_INT_ARGB);
        for (int y = minY; y <= maxY; y++)
            for (int x = minX; x <= maxX; x++)
                out.setRGB(x - minX, y - minY, px[y * w + x]);

        File dest = new File(args[1]);
        if (dest.getParentFile() != null) dest.getParentFile().mkdirs();
        ImageIO.write(out, "png", dest);
        System.out.printf("background #%06X -> %dx%d %s%n", bgColor & 0xFFFFFF, out.getWidth(), out.getHeight(), dest);
    }

    /** Median color of the image border; copes with JPEGs whose "#FF00FF" is not exact. */
    static int estimateBackground(int[] px, int w, int h) {
        int n = 2 * (w + h);
        int[] r = new int[n], g = new int[n], b = new int[n];
        int k = 0;
        for (int x = 0; x < w; x++) for (int y : new int[]{0, h - 1}) { int c = px[y * w + x]; r[k] = c >> 16 & 255; g[k] = c >> 8 & 255; b[k++] = c & 255; }
        for (int y = 0; y < h; y++) for (int x : new int[]{0, w - 1}) { int c = px[y * w + x]; r[k] = c >> 16 & 255; g[k] = c >> 8 & 255; b[k++] = c & 255; }
        Arrays.sort(r); Arrays.sort(g); Arrays.sort(b);
        return 0xFF000000 | r[n / 2] << 16 | g[n / 2] << 8 | b[n / 2];
    }

    static void seed(ArrayDeque<Integer> q, boolean[] bg, int[] px, int bgColor, int x, int y, int w) {
        int i = y * w + x;
        if (!bg[i] && distance(px[i], bgColor) <= BG_TOLERANCE) { bg[i] = true; q.add(i); }
    }

    static int distance(int a, int b) {
        int dr = (a >> 16 & 255) - (b >> 16 & 255), dg = (a >> 8 & 255) - (b >> 8 & 255), db = (a & 255) - (b & 255);
        return (int) Math.sqrt(dr * dr + dg * dg + db * db);
    }

    /** Pink = red and blue both well above green and roughly equal (magenta, or magenta blended with black). */
    static boolean isPink(int c) {
        int r = c >> 16 & 255, g = c >> 8 & 255, b = c & 255;
        int lo = Math.min(r, b), hi = Math.max(r, b);
        return lo - g > 35 && lo * 10 > hi * 6;
    }

    static int despill(int c) {
        int r = c >> 16 & 255, g = c >> 8 & 255, b = c & 255;
        int lo = Math.min(r, b), hi = Math.max(r, b);
        if (lo - g > 8 && lo * 10 > hi * 6) {   // looser than isPink: only a tint, not a removal
            r = Math.min(r, g + (r - g) / 4);
            b = Math.min(b, g + (b - g) / 4);
        }
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    static boolean touchesBackground(boolean[] bg, int x, int y, int w, int h) {
        return (x > 0 && bg[y * w + x - 1]) || (x < w - 1 && bg[y * w + x + 1])
            || (y > 0 && bg[(y - 1) * w + x]) || (y < h - 1 && bg[(y + 1) * w + x]);
    }
}
