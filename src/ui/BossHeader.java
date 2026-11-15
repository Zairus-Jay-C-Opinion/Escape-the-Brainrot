package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** Top-center strip of the battle: boss name and phase, HP bar that eases, and the question timer. Owner: Zrs. */
public class BossHeader {

    private static final Font NAME_FONT = new Font(Font.MONOSPACED, Font.BOLD, 20);
    private static final int CENTER_X = 384;
    private static final int MAX_NAME_WIDTH = 370;
    private static final int BAR_X = 276;
    private static final int BAR_Y = 34;
    private static final int BAR_WIDTH = 230;
    private static final int BAR_HEIGHT = 12;
    private static final double EASE = 0.12;

    private double shownHpRatio = 1;

    public void update(double hpRatio) {
        shownHpRatio += (hpRatio - shownHpRatio) * EASE;
        if (Math.abs(hpRatio - shownHpRatio) < 0.002) {
            shownHpRatio = hpRatio;
        }
    }

    public void draw(Graphics2D g, String bossName, int phaseNumber, int elapsedSeconds, double intervalProgress, Color accent) {
        String title = bossName.toUpperCase() + " - PHASE " + phaseNumber;
        g.setFont(UiTheme.fit(g, NAME_FONT, title, MAX_NAME_WIDTH));
        UiTheme.centeredText(g, title, CENTER_X, 26, accent);

        g.setFont(UiTheme.MONO_BOLD_SMALL);
        UiTheme.shadowText(g, "HP", BAR_X - 24, BAR_Y + 11, accent);
        g.setColor(new Color(24, 10, 16));
        g.fillRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);
        g.setColor(UiTheme.STRAIN);
        g.fillRect(BAR_X, BAR_Y, (int) Math.round(BAR_WIDTH * Math.max(0, shownHpRatio)), BAR_HEIGHT);
        g.setColor(UiTheme.CREAM);
        g.drawRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);

        g.setFont(UiTheme.MONO_MEDIUM);
        String time = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60);
        UiTheme.centeredText(g, time, CENTER_X, 66, accent);

        // The thin bar under the clock fills up to the next automatic Strain tick.
        int progressWidth = 110;
        g.setColor(new Color(40, 30, 40));
        g.fillRect(CENTER_X - progressWidth / 2, 72, progressWidth, 3);
        g.setColor(UiTheme.STRAIN);
        g.fillRect(CENTER_X - progressWidth / 2, 72, (int) Math.round(progressWidth * intervalProgress), 3);
    }
}
