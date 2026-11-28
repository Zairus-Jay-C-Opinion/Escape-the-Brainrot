package world;

import engine.AssetManager;
import engine.GamePanel;
import exception.AssetLoadException;
import exception.LevelLoadException;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** A grid of tiles loaded from a CSV file in res/maps/. Owner: Zrs. */
public class TileMap {

    public static final int FLOOR = 0;
    public static final int WALL = 1;          // solid
    public static final int BOSS_TRIGGER = 2;  // stepping here starts a battle
    public static final int DEFEATED_BOSS = 3; // a beaten boss's tile: walkable and never triggers again
    public static final int PORTAL = 4;        // appears after defeating a boss to return to the TV wall

    /** Visual tile theme for each boss level. */
    public enum Theme {
        PAVEMENT("/sprites/tiles/cracked_floor.jpeg", "/sprites/tiles/wall_brick.jpeg"),
        GRASS("/sprites/tiles/floor_grass.jpeg", "/sprites/tiles/wall_brick.jpeg"),
        WATER("/sprites/tiles/cracked_floor.jpeg", "/sprites/tiles/wall_water.jpeg"),
        LIBRARY("/sprites/tiles/dusty_library.jpeg", "/sprites/tiles/wall_brick.jpeg"),
        CORRUPTED("/sprites/tiles/floor_corrupted.jpeg", "/sprites/tiles/wall_brick.jpeg"),
        FINAL("/sprites/tiles/floor_corrupted.jpeg", "/sprites/tiles/wall_water.jpeg");

        final String floorPath;
        final String wallPath;

        Theme(String floorPath, String wallPath) {
            this.floorPath = floorPath;
            this.wallPath = wallPath;
        }
    }

    private static final Color FLOOR_COLOR = new Color(40, 40, 52);
    private static final Color WALL_COLOR = Color.GRAY;
    private static final Color BOSS_COLOR = Color.RED;
    private static final Color DEFEATED_COLOR = new Color(58, 44, 66);
    private static final Color PORTAL_COLOR = new Color(80, 210, 255);

    private final int[][] tiles; // indexed [row][column]
    private final BufferedImage floorSprite;
    private final BufferedImage wallSprite;
    private final BufferedImage bossSprite;

    public TileMap(String resourcePath) throws LevelLoadException {
        this(resourcePath, Theme.PAVEMENT);
    }

    /** Loads a map such as "/maps/map_chimpanzini.csv" with a themed floor/wall pair. */
    public TileMap(String resourcePath, Theme theme) throws LevelLoadException {
        Theme activeTheme = theme != null ? theme : Theme.PAVEMENT;
        this.floorSprite = loadTile(activeTheme.floorPath);
        this.wallSprite = loadTile(activeTheme.wallPath);
        this.bossSprite = loadTile("/sprites/tiles/brainrot_floor.jpeg");
        List<int[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(AssetManager.open(resourcePath), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                int[] row = parseRow(line, lineNumber);
                if (!rows.isEmpty() && row.length != rows.get(0).length) {
                    throw new LevelLoadException("Map line " + lineNumber + " has " + row.length
                            + " tiles but the first line has " + rows.get(0).length);
                }
                rows.add(row);
            }
        } catch (AssetLoadException | IOException e) {
            throw new LevelLoadException("Could not load map " + resourcePath, e);
        }

        if (rows.isEmpty()) {
            throw new LevelLoadException("Map is empty: " + resourcePath);
        }
        tiles = rows.toArray(new int[0][]);
    }

    private int[] parseRow(String line, int lineNumber) throws LevelLoadException {
        String[] parts = line.split(",");
        int[] row = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                row[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                throw new LevelLoadException("Bad tile '" + parts[i].trim() + "' on map line " + lineNumber, e);
            }
            if (row[i] < FLOOR || row[i] > PORTAL) {
                throw new LevelLoadException("Unknown tile " + row[i] + " on map line " + lineNumber);
            }
        }
        return row;
    }

    public int getWidth() {
        return tiles[0].length;
    }

    public int getHeight() {
        return tiles.length;
    }

    /** Tile at (column, row). Anything outside the map counts as a wall so the player can't leave. */
    public int getTile(int column, int row) {
        if (row < 0 || row >= tiles.length || column < 0 || column >= tiles[row].length) {
            return WALL;
        }
        return tiles[row][column];
    }

    /** Changes one tile, e.g. to mark a boss as defeated. Ignored outside the map. */
    public void setTile(int column, int row, int tile) {
        if (row >= 0 && row < tiles.length && column >= 0 && column < tiles[row].length) {
            tiles[row][column] = tile;
        }
    }

    public boolean isSolid(int column, int row) {
        return getTile(column, row) == WALL;
    }

    private static BufferedImage loadTile(String path) {
        try {
            return AssetManager.getImage(path);
        } catch (AssetLoadException e) {
            return null;
        }
    }

    /** Draws every tile using its themed sprite, falling back to colored squares if missing. */
    public void draw(Graphics2D g) {
        int size = GamePanel.SCALED_TILE_SIZE;
        for (int row = 0; row < tiles.length; row++) {
            for (int column = 0; column < tiles[row].length; column++) {
                int x = column * size;
                int y = row * size;
                int tile = tiles[row][column];
                BufferedImage img = switch (tile) {
                    case WALL -> wallSprite;
                    case BOSS_TRIGGER -> bossSprite;
                    default -> floorSprite;
                };
                if (img != null) {
                    g.drawImage(img, x, y, size, size, null);
                } else {
                    g.setColor(colorFor(tile));
                    g.fillRect(x, y, size, size);
                }
                if (tile == DEFEATED_BOSS) {
                    g.setColor(new Color(20, 12, 28, 120));
                    g.fillRect(x, y, size, size);
                    g.setColor(new Color(140, 110, 165));
                    g.drawLine(x + 12, y + 12, x + size - 12, y + size - 12);
                    g.drawLine(x + size - 12, y + 12, x + 12, y + size - 12);
                } else if (tile == PORTAL) {
                    g.setColor(new Color(10, 25, 45, 160));
                    g.fillRect(x, y, size, size);
                    g.setColor(PORTAL_COLOR);
                    g.drawOval(x + 6, y + 6, size - 12, size - 12);
                    g.setColor(new Color(180, 245, 255));
                    g.fillOval(x + 14, y + 14, size - 28, size - 28);
                }
            }
        }
    }

    private Color colorFor(int tile) {
        return switch (tile) {
            case WALL -> WALL_COLOR;
            case BOSS_TRIGGER -> BOSS_COLOR;
            case DEFEATED_BOSS -> DEFEATED_COLOR;
            default -> FLOOR_COLOR;
        };
    }
}
