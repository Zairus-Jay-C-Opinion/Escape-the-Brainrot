package ui;

import systems.BrainrotGauge;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;

/** Always-visible heads-up display: the Brainrot Gauge on the left and three empty item slots on the right. Owner: Zrs. */
public class Hud {

    private static final int GAUGE_X = 10;
    private static final int GAUGE_Y = 8;
    private static final int BAR_X = 58;
    private static final int BAR_Y = 28;
    private static final int BAR_WIDTH = 190;
    private static final int BAR_HEIGHT = 14;
    private static final double EASE = 0.1;           // fraction of the remaining distance covered each update
    private static final int WARNING_TICKS = 24;

    private static final int SLOT_SIZE = 34;
    private static final int SLOT_GAP = 6;
    private static final int SLOT_COUNT = 3;

    private final BrainrotGauge gauge;
    private double shownRot;
    private double shownStrain;
    private int warningTicksLeft;

    public Hud(BrainrotGauge gauge) {
        this.gauge = gauge;
        this.shownRot = gauge.getRot();
        this.shownStrain = gauge.getStrain();
    }

    /** Eases the displayed bar toward the real values. Call once per game update. */
    public void update() {
        shownRot = approach(shownRot, gauge.getRot());
        shownStrain = approach(shownStrain, gauge.getStrain());
        if (warningTicksLeft > 0) {
            warningTicksLeft--;
        }
    }

    private static double approach(double shown, double target) {
        double next = shown + (target - shown) * EASE;
        return Math.abs(target - next) < 0.05 ? target : next;
    }

    /** Makes the gauge flash red for a moment, for a wrong answer or a Strain tick. */
    public void flashWarning() {
        warningTicksLeft = WARNING_TICKS;
    }

    /** Screen position where Strain numbers should float up from. */
    public int getStrainLabelX() {
        return BAR_X + BAR_WIDTH - 30;
    }

    public int getStrainLabelY() {
        return BAR_Y + 34;
    }

    public void draw(Graphics2D g) {
        drawGauge(g);
        drawItemSlots(g);
    }

    private void drawGauge(Graphics2D g) {
        double max = 100;
        int rotWidth = (int) Math.round(BAR_WIDTH * Math.min(1, shownRot / max));
        int strainWidth = (int) Math.round(BAR_WIDTH * Math.min(1 - Math.min(1, shownRot / max), shownStrain / max));

        if (warningTicksLeft > 0) {
            float glow = 0.55f * warningTicksLeft / WARNING_TICKS;
            g.setComposite(AlphaComposite.SrcOver.derive(glow));
            g.setColor(UiTheme.STRAIN);
            g.fillRect(GAUGE_X - 4, GAUGE_Y - 4, BAR_X + BAR_WIDTH - GAUGE_X + 12, 62);
            g.setComposite(AlphaComposite.SrcOver);
        }

        drawBrainIcon(g);

        g.setFont(UiTheme.MONO_MEDIUM);
        UiTheme.shadowText(g, "BRAINROT " + Math.round(shownRot + shownStrain) + "/100", BAR_X, BAR_Y - 6, UiTheme.CREAM);

        g.setColor(new Color(20, 14, 30));
        g.fillRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);
        g.setColor(UiTheme.ROT);
        g.fillRect(BAR_X, BAR_Y, rotWidth, BAR_HEIGHT);
        g.setColor(UiTheme.STRAIN);
        g.fillRect(BAR_X + rotWidth, BAR_Y, strainWidth, BAR_HEIGHT);
        g.setColor(UiTheme.CREAM);
        g.drawRect(BAR_X - 1, BAR_Y - 1, BAR_WIDTH + 1, BAR_HEIGHT + 1);

        if (rotWidth > 34) {
            g.setFont(UiTheme.MONO_BOLD_SMALL);
            g.setColor(new Color(235, 215, 250));
            g.drawString(Math.round(shownRot) + "%", BAR_X + 4, BAR_Y + BAR_HEIGHT - 3);
        }
        drawLock(g, BAR_X + rotWidth, BAR_Y + BAR_HEIGHT / 2);

        g.setFont(UiTheme.MONO_SMALL);
        g.setColor(new Color(150, 100, 190));
        g.drawString("ROT", BAR_X, BAR_Y + BAR_HEIGHT + 14);
        g.setColor(new Color(200, 70, 80));
        g.drawString("STRAIN", BAR_X + BAR_WIDTH - g.getFontMetrics().stringWidth("STRAIN"), BAR_Y + BAR_HEIGHT + 14);
    }

    // Rot is locked until its boss falls, so a padlock sits where Rot ends.
    private void drawLock(Graphics2D g, int centerX, int centerY) {
        g.setColor(new Color(20, 14, 30));
        g.fillRect(centerX - 6, centerY - 4, 12, 10);
        g.setColor(UiTheme.CREAM);
        g.drawRect(centerX - 6, centerY - 4, 11, 9);
        g.setStroke(new BasicStroke(2f));
        g.draw(new Arc2D.Double(centerX - 4, centerY - 11, 8, 14, 0, 180, Arc2D.OPEN));
        g.setStroke(new BasicStroke(1f));
        g.fillRect(centerX - 1, centerY - 1, 2, 4);
    }

    private void drawBrainIcon(Graphics2D g) {
        UiTheme.panel(g, GAUGE_X, GAUGE_Y, 40, 40, new Color(30, 22, 40), UiTheme.CREAM);
        g.setColor(new Color(225, 120, 150));
        g.fillOval(GAUGE_X + 7, GAUGE_Y + 10, 16, 20);
        g.fillOval(GAUGE_X + 17, GAUGE_Y + 10, 16, 20);
        g.setColor(new Color(150, 60, 90));
        g.drawLine(GAUGE_X + 20, GAUGE_Y + 11, GAUGE_X + 20, GAUGE_Y + 29);
        g.drawArc(GAUGE_X + 10, GAUGE_Y + 14, 8, 8, 0, 180);
        g.drawArc(GAUGE_X + 22, GAUGE_Y + 18, 8, 8, 180, 180);
    }

    private void drawItemSlots(Graphics2D g) {
        int totalWidth = SLOT_COUNT * SLOT_SIZE + (SLOT_COUNT - 1) * SLOT_GAP;
        int x = 768 - 10 - totalWidth;
        for (int i = 0; i < SLOT_COUNT; i++) {
            UiTheme.panel(g, x, GAUGE_Y, SLOT_SIZE, SLOT_SIZE, new Color(14, 18, 30, 210), new Color(110, 130, 160));
            x += SLOT_SIZE + SLOT_GAP;
        }
    }
}
