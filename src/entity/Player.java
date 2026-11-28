package entity;

import engine.AssetManager;
import engine.Config;
import engine.GamePanel;
import exception.AssetLoadException;
import world.TileMap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * The player: tile-locked movement with directional walking sprites. Owner: Zrs.
 * The tile position changes the instant a step starts; the pixel position then slides to catch up.
 */
public class Player extends Entity {

    private enum Direction { DOWN, UP, LEFT, RIGHT }

    private static final int MARGIN = 6; // fallback square margin if sprites are missing
    private static final int WALK_FRAME_TICKS = 6;

    private final int speed = Config.getInt("player.speed"); // pixels per update
    private final BufferedImage[][] sprites = new BufferedImage[4][3]; // [direction][0=idle, 1=walk1, 2=walk2]
    private Direction facing = Direction.DOWN;
    private boolean moving;
    private int animTick;

    public Player(int tileX, int tileY) {
        super(tileX, tileY);
        String[] dirs = {"down", "up", "left", "right"};
        String[] poses = {"idle", "walk1", "walk2"};
        for (int d = 0; d < dirs.length; d++) {
            for (int p = 0; p < poses.length; p++) {
                try {
                    sprites[d][p] = AssetManager.getImage("/sprites/player/" + dirs[d] + "_" + poses[p] + ".png");
                } catch (AssetLoadException ignored) {
                    // Falls back to the cyan square if any sprite file is missing.
                }
            }
        }
    }

    public boolean isMoving() {
        return moving;
    }

    public void setTilePosition(int tileX, int tileY) {
        placeAtTile(tileX, tileY);
        moving = false;
        animTick = 0;
    }

    /** Starts a one-tile step if the player is idle and the target tile isn't a wall. Returns true if it started. */
    public boolean tryMove(int dx, int dy, TileMap map) {
        if (moving) {
            return false;
        }
        if (dy > 0) {
            facing = Direction.DOWN;
        } else if (dy < 0) {
            facing = Direction.UP;
        } else if (dx < 0) {
            facing = Direction.LEFT;
        } else if (dx > 0) {
            facing = Direction.RIGHT;
        }
        if (map.isSolid(getTileX() + dx, getTileY() + dy)) {
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
        animTick++;
        int targetX = getTileX() * GamePanel.SCALED_TILE_SIZE;
        int targetY = getTileY() * GamePanel.SCALED_TILE_SIZE;
        setPixel(approach(getPixelX(), targetX), approach(getPixelY(), targetY));

        if (getPixelX() == targetX && getPixelY() == targetY) {
            moving = false;
            animTick = 0;
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
        int frame = moving ? (animTick / WALK_FRAME_TICKS) % 2 + 1 : 0;
        BufferedImage sprite = sprites[facing.ordinal()][frame];
        if (sprite == null) {
            sprite = sprites[facing.ordinal()][0];
        }
        int tileSize = GamePanel.SCALED_TILE_SIZE;
        if (sprite == null) {
            g.setColor(Color.CYAN);
            int size = tileSize - 2 * MARGIN;
            g.fillRect(getPixelX() + MARGIN, getPixelY() + MARGIN, size, size);
            return;
        }
        double scale = Math.min((double) tileSize / sprite.getWidth(), (double) tileSize / sprite.getHeight());
        int w = (int) Math.round(sprite.getWidth() * scale);
        int h = (int) Math.round(sprite.getHeight() * scale);
        int drawX = getPixelX() + (tileSize - w) / 2;
        int drawY = getPixelY() + (tileSize - h);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(sprite, drawX, drawY, w, h, null);
    }
}
