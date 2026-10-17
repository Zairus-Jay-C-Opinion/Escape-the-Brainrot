package world;

import engine.AssetManager;
import engine.GamePanel;
import exception.AssetLoadException;
import exception.LevelLoadException;

import java.awt.Color;
import java.awt.Graphics2D;
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

    private static final Color FLOOR_COLOR = new Color(40, 40, 52);
    private static final Color WALL_COLOR = Color.GRAY;
    private static final Color BOSS_COLOR = Color.RED;

    private final int[][] tiles; // indexed [row][column]

    /** Loads a map such as "/maps/test-map.csv". Each line is one row of comma-separated tile numbers. */
    public TileMap(String resourcePath) throws LevelLoadException {
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
            if (row[i] < FLOOR || row[i] > BOSS_TRIGGER) {
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

    public boolean isSolid(int column, int row) {
        return getTile(column, row) == WALL;
    }

    /** Draws every tile as a colored square (placeholder art). */
    public void draw(Graphics2D g) {
        int size = GamePanel.SCALED_TILE_SIZE;
        for (int row = 0; row < tiles.length; row++) {
            for (int column = 0; column < tiles[row].length; column++) {
                g.setColor(colorFor(tiles[row][column]));
                g.fillRect(column * size, row * size, size, size);
            }
        }
    }

    private Color colorFor(int tile) {
        return switch (tile) {
            case WALL -> WALL_COLOR;
            case BOSS_TRIGGER -> BOSS_COLOR;
            default -> FLOOR_COLOR;
        };
    }
}
