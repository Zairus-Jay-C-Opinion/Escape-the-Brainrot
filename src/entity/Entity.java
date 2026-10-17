package entity;

import engine.GamePanel;

import java.awt.Graphics2D;

/**
 * Anything that stands on the map. Owner: Zrs.
 * Position is stored twice: tile coordinates for game logic, pixel coordinates for drawing.
 */
public abstract class Entity {

    private int tileX;
    private int tileY;
    private int pixelX;
    private int pixelY;

    protected Entity(int tileX, int tileY) {
        placeAtTile(tileX, tileY);
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public int getPixelX() {
        return pixelX;
    }

    public int getPixelY() {
        return pixelY;
    }

    /** Jumps to a tile instantly, with no sliding. */
    protected void placeAtTile(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.pixelX = tileX * GamePanel.SCALED_TILE_SIZE;
        this.pixelY = tileY * GamePanel.SCALED_TILE_SIZE;
    }

    /** Changes only the logical tile; the pixels catch up later (used for sliding). */
    protected void setTile(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
    }

    protected void setPixel(int pixelX, int pixelY) {
        this.pixelX = pixelX;
        this.pixelY = pixelY;
    }

    public abstract void draw(Graphics2D g);
}
