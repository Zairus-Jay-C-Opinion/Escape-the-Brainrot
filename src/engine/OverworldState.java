package engine;

import battle.BattleState;
import battle.Boss;
import battle.TestBoss;
import engine.InputHandler.Action;
import entity.Player;
import exception.LevelLoadException;
import ui.Hud;
import world.TileMap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

/** The walking screen: draws the tile map and player, and starts a battle on a boss tile. Owner: Zrs. */
public class OverworldState extends GameState {

    private final TileMap map;
    private final Player player;
    private final Hud hud;
    // One Boss object per boss tile, kept for the whole session so a defeated boss stays defeated.
    private final Map<Integer, Boss> bosses = new HashMap<>();

    public OverworldState(StateManager manager) throws LevelLoadException {
        super(manager);
        map = new TileMap("/maps/test-map.csv");
        player = new Player(1, 1); // start tile (column 1, row 1)
        hud = new Hud(manager.getGauge());
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
                manager.setState(new BattleState(manager, this, boss, manager.getGauge()));
            }
        }
    }

    // Which boss lives on a tile. Every boss tile is the placeholder for now; later this reads from the map data.
    private Boss bossAt(int column, int row) {
        return bosses.computeIfAbsent(row * map.getWidth() + column, key -> new TestBoss());
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
        map.draw(g);
        player.draw(g);
        hud.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        g.drawString("Arrows / WASD to move. Step on the red tile.", 10, GamePanel.SCREEN_HEIGHT - 10);
    }
}
