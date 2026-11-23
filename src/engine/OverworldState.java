package engine;

import battle.BattleState;
import battle.Boss;
import battle.ChimpanziniBananini;
import battle.TralaleroTralala;
import engine.InputHandler.Action;
import entity.Player;
import exception.LevelLoadException;
import ui.Hud;
import world.TileMap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** The walking screen: draws the tile map and player, and starts a battle on a boss tile. Owner: Zrs. */
public class OverworldState extends GameState {

    private final TileMap map;
    private final Player player;
    private final Hud hud;
    // One Boss object per boss tile, kept for the whole session so a defeated boss stays defeated.
    private final Map<Integer, Boss> bosses = new TreeMap<>();
    private static final Color HUD_BACKGROUND = new Color(12, 12, 20);
    private static final Font HINT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    public OverworldState(StateManager manager) throws LevelLoadException {
        super(manager);
        map = new TileMap("/maps/test-map.csv");
        player = new Player(1, 1); // start tile (column 1, row 1)
        // Created up front so the HUD can show a Rot segment for every boss on the map.
        for (int row = 0; row < map.getHeight(); row++) {
            for (int column = 0; column < map.getWidth(); column++) {
                if (map.getTile(column, row) == TileMap.BOSS_TRIGGER) {
                    bosses.put(row * map.getWidth() + column, createBossForTile(column, row));
                }
            }
        }
        hud = new Hud(manager.getGauge(), new ArrayList<>(bosses.values()));
    }

    @Override
    public void update() {
        hud.update();
        markDefeatedBosses();

        // Only accept a new step once the previous one has finished sliding.
        if (!player.isMoving()) {
            InputHandler input = manager.getInput();
            int dx = 0;
            int dy = 0;
            if (input.isHeld(Action.UP)) {
                dy = -1;
            } else if (input.isHeld(Action.DOWN)) {
                dy = 1;
            } else if (input.isHeld(Action.LEFT)) {
                dx = -1;
            } else if (input.isHeld(Action.RIGHT)) {
                dx = 1;
            }
            if (dx != 0 || dy != 0) {
                player.tryMove(dx, dy, map);
            }
        }

        boolean justArrived = player.update();
        if (justArrived && map.getTile(player.getTileX(), player.getTileY()) == TileMap.BOSS_TRIGGER) {
            Boss boss = bossAt(player.getTileX(), player.getTileY());
            if (!boss.isDefeated()) {
                // Pass "this" so the battle can hand control back to the same overworld afterwards.
                manager.setState(new BattleState(manager, this, boss, manager.getGauge(), new ArrayList<>(bosses.values())));
            }
        }
    }

    // Which boss lives on a tile. Every boss tile is the placeholder for now; later this reads from the map data.
    private Boss bossAt(int column, int row) {
        return bosses.get(row * map.getWidth() + column);
    }

    private Boss createBossForTile(int column, int row){
        if (column == 14 && row == 1){
            return new TralaleroTralala();
        }
        return new ChimpanziniBananini();
    }

    // The battle only changes the Boss object, so the map is updated here once the player is back.
    private void markDefeatedBosses() {
        for (Map.Entry<Integer, Boss> entry : bosses.entrySet()) {
            int column = entry.getKey() % map.getWidth();
            int row = entry.getKey() / map.getWidth();
            if (entry.getValue().isDefeated() && map.getTile(column, row) == TileMap.BOSS_TRIGGER) {
                map.setTile(column, row, TileMap.DEFEATED_BOSS);
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(HUD_BACKGROUND);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, Hud.HEIGHT);

        // The map is as tall as the whole screen, so it scrolls under the HUD strip to keep the player in view.
        int viewHeight = GamePanel.SCREEN_HEIGHT - Hud.HEIGHT;
        int maxScroll = Math.max(0, map.getHeight() * GamePanel.SCALED_TILE_SIZE - viewHeight);
        int playerCenterY = player.getPixelY() + GamePanel.SCALED_TILE_SIZE / 2;
        int scroll = Math.max(0, Math.min(maxScroll, playerCenterY - viewHeight / 2));

        java.awt.Shape savedClip = g.getClip();
        g.setClip(0, Hud.HEIGHT, GamePanel.SCREEN_WIDTH, viewHeight);
        g.translate(0, Hud.HEIGHT - scroll);
        map.draw(g);
        player.draw(g);
        g.translate(0, scroll - Hud.HEIGHT);
        g.setClip(savedClip);

        hud.draw(g);

        // Sits in the HUD strip between the gauge and the item slots, so the map never covers it.
        g.setColor(Color.WHITE);
        g.setFont(HINT_FONT);
        String hint = "Arrows / WASD to move. Step on the red tile.";
        g.drawString(hint, 447 - g.getFontMetrics().stringWidth(hint) / 2, 46);
    }
}
