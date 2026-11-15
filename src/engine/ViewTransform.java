package engine;

/**
 * Where the game picture sits inside the window: scaled to fit, centered, black bars around it.
 * The single place that converts between window pixels and game pixels, for drawing and for the mouse.
 */
public final class ViewTransform {

    private final int gameWidth;
    private final int gameHeight;
    private final int offsetX;
    private final int offsetY;
    private final int drawWidth;
    private final int drawHeight;

    private ViewTransform(int gameWidth, int gameHeight, int offsetX, int offsetY, int drawWidth, int drawHeight) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.drawWidth = drawWidth;
        this.drawHeight = drawHeight;
    }

    /** Largest centered rectangle with the game's aspect ratio that fits in the window. */
    public static ViewTransform fit(int windowWidth, int windowHeight, int gameWidth, int gameHeight) {
        double scale = Math.min((double) windowWidth / gameWidth, (double) windowHeight / gameHeight);
        int drawWidth = Math.max(1, (int) Math.round(gameWidth * scale));
        int drawHeight = Math.max(1, (int) Math.round(gameHeight * scale));
        return new ViewTransform(gameWidth, gameHeight,
                (windowWidth - drawWidth) / 2, (windowHeight - drawHeight) / 2, drawWidth, drawHeight);
    }

    /** Game x for a window x. Outside 0 to game width means the point is over a black bar. */
    public int toGameX(int windowX) {
        return (int) Math.floor((windowX - offsetX) * (double) gameWidth / drawWidth);
    }

    public int toGameY(int windowY) {
        return (int) Math.floor((windowY - offsetY) * (double) gameHeight / drawHeight);
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getDrawWidth() {
        return drawWidth;
    }

    public int getDrawHeight() {
        return drawHeight;
    }
}
