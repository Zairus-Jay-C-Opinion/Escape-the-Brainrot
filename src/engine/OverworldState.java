package engine;

import battle.BattleState;
import battle.TestBoss;
import engine.InputHandler.Action;
import entity.Player;
import exception.LevelLoadException;
import world.TileMap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** The walking screen: draws the tile map and player, and starts a battle on a boss tile. Owner: Zrs. */
public class OverworldState extends GameState {

    private final TileMap map;
    private final Player player;

    public OverworldState(StateManager manager) throws LevelLoadException {
        super(manager);
        map = new TileMap("/maps/test-map.csv");
        player = new Player(1, 1); // start tile (column 1, row 1)
    }

    @Override
    public void update() {
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
            // Pass "this" so the battle can hand control back to the same overworld afterwards.
            manager.setState(new BattleState(manager, this, new TestBoss()));
        }
    }

    @Override
    public void draw(Graphics2D g) {
        map.draw(g);
        player.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        g.drawString("Arrows / WASD to move. Step on the red tile.", 10, 20);
    }
}
