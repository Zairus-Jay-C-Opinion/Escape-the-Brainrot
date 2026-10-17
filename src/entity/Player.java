package entity;

import engine.Config;
import engine.GamePanel;
import world.TileMap;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * The player: tile-locked movement. Owner: Zrs.
 * The tile position changes the instant a step starts; the pixel position then slides to catch up.
 */
public class Player extends Entity {

    private static final int MARGIN = 6; // gap so the square doesn't touch the tile edges

    private final int speed = Config.getInt("player.speed"); // pixels per update
    private boolean moving;

    public Player(int tileX, int tileY) {
        super(tileX, tileY);
    }

    public boolean isMoving() {
        return moving;
    }

    /** Starts a one-tile step if the player is idle and the target tile isn't a wall. Returns true if it started. */
    public boolean tryMove(int dx, int dy, TileMap map) {
        if (moving || map.isSolid(getTileX() + dx, getTileY() + dy)) {
            return false;
        }
        setTile(getTileX() + dx, getTileY() + dy);
        moving = true;
        return true;
    }

    /** Slides toward the target tile. Returns true only on the update where the step finishes. */
    public boolean update() {
        if (!moving) {
            return false;
        }
        int targetX = getTileX() * GamePanel.SCALED_TILE_SIZE;
        int targetY = getTileY() * GamePanel.SCALED_TILE_SIZE;
        setPixel(approach(getPixelX(), targetX), approach(getPixelY(), targetY));

        if (getPixelX() == targetX && getPixelY() == targetY) {
            moving = false;
            return true;
        }
        return false;
    }

    // Moves current toward target by at most "speed" pixels, never overshooting.
    private int approach(int current, int target) {
        int distance = target - current;
        return current + Integer.signum(distance) * Math.min(speed, Math.abs(distance));
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.CYAN);
        int size = GamePanel.SCALED_TILE_SIZE - 2 * MARGIN;
        g.fillRect(getPixelX() + MARGIN, getPixelY() + MARGIN, size, size);
    }
}
