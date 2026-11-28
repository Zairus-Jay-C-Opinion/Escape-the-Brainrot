package ui;

import battle.Boss;
import engine.Config;
import systems.BrainrotGauge;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.util.List;

/**
 * Always-visible heads-up display. Top-left: a red STRAIN bar with a thinner purple ROT bar under it, split into one
 * locked segment per boss. Top-right: three empty item slots. Owner: Zrs.
 */
public class Hud {

    /** Height of the strip the HUD needs, so other screens can keep their content below it. */
    public static final int HEIGHT = 78;

    private static final int GAUGE_X = 10;
    private static final int ICON_Y = 18;
    private static final int BAR_X = 56;
    private static final int BAR_WIDTH = 124;
    private static final int STRAIN_LABEL_Y = 22;
    private static final int STRAIN_BAR_Y = 27;
    private static final int STRAIN_BAR_HEIGHT = 16;
    private static final int ROT_LABEL_Y = 57;
    private static final int ROT_BAR_Y = 61;
    private static final int ROT_BAR_HEIGHT = 9;
    private static final int SEGMENT_GAP = 2;
    private static final double PULSE_ABOVE = 80;
    private static final double EASE = 0.1;           // fraction of the remaining distance covered each update
    private static final double SEGMENT_EASE = 0.08;
    private static final int WARNING_TICKS = 24;

    private static final int SLOT_SIZE = 34;
    private static final int SLOT_GAP = 6;
    private static final int SLOT_COUNT = 3;

    private final BrainrotGauge gauge;
    private final List<Boss> bosses;
    private final double[] segmentFill; // 1 while a boss is alive, easing to 0 once it is defeated
    private final double rotScale;      // Rot value the whole Rot bar stands for
    private double shownRot;
    private double shownStrain;
    private int warningTicksLeft;
    private int tick;

    /** The bosses are the ones on the current level, in the order their segments should appear. */
    public Hud(BrainrotGauge gauge, List<Boss> bosses) {
        this.gauge = gauge;
        this.bosses = bosses;
        this.shownRot = gauge.getRot();
        this.shownStrain = gauge.getStrain();
        this.segmentFill = new double[bosses.size()];
        double shareTotal = 0;
        for (int i = 0; i < bosses.size(); i++) {
            segmentFill[i] = bosses.get(i).isDefeated() ? 0 : 1;
            shareTotal += bosses.get(i).getRotShare();
        }
        this.rotScale = Math.max(Config.getDouble("rot.start"), shareTotal);
    }

    /** Eases the displayed bars toward the real values. Call once per game update. */
    public void update() {
        tick++;
        shownRot = approach(shownRot, gauge.getRot());
        shownStrain = approach(shownStrain, gauge.getStrain());
        for (int i = 0; i < segmentFill.length; i++) {
            double target = bosses.get(i).isDefeated() ? 0 : 1;
            segmentFill[i] += (target - segmentFill[i]) * SEGMENT_EASE;
            if (Math.abs(target - segmentFill[i]) < 0.01) {
                segmentFill[i] = target;
            }
        }
        if (warningTicksLeft > 0) {
            warningTicksLeft--;
        }
    }

    private static double approach(double shown, double target) {
        double next = shown + (target - shown) * EASE;
        return Math.abs(target - next) < 0.05 ? target : next;
    }

    /** Makes the Strain bar flash red for a moment, for a wrong answer or a Strain tick. */
    public void flashWarning() {
        warningTicksLeft = WARNING_TICKS;
    }

    /** Screen position where Strain numbers should float up from. */
    public int getStrainLabelX() {
        return BAR_X;
    }

    public int getStrainLabelY() {
        return HEIGHT + 14;
    }

    public void draw(Graphics2D g) {
        drawStrain(g);
        drawRot(g);
        drawBrainIcon(g);
        drawItemSlots(g);
    }

    private void drawStrain(Graphics2D g) {
        double max = Config.getDouble("gauge.max");
        int fillWidth = (int) Math.round(BAR_WIDTH * Math.min(1, shownStrain / max));
        boolean pulsing = shownStrain > PULSE_ABOVE;
        double pulse = pulsing ? 0.5 + 0.5 * Math.sin(tick * 0.25) : 0;

        if (warningTicksLeft > 0 || pulsing) {
            float glow = warningTicksLeft > 0 ? 0.55f * warningTicksLeft / WARNING_TICKS : (float) (0.15 + 0.3 * pulse);
            g.setComposite(AlphaComposite.SrcOver.derive(glow));
            g.setColor(UiTheme.STRAIN);
            g.fillRect(BAR_X - 6, STRAIN_LABEL_Y - 16, BAR_WIDTH + 12, STRAIN_BAR_HEIGHT + 24);
            g.setComposite(AlphaComposite.SrcOver);
        }

        g.setFont(UiTheme.MONO_MEDIUM);
        Color labelColor = pulsing ? UiTheme.blend(new Color(240, 90, 100), Color.WHITE, (float) pulse * 0.6f) : new Color(240, 90, 100);
        UiTheme.shadowText(g, "STRAIN " + Math.round(shownStrain) + "/" + Math.round(max), BAR_X, STRAIN_LABEL_Y, labelColor);

        g.setColor(new Color(30, 8, 12));
        g.fillRect(BAR_X, STRAIN_BAR_Y, BAR_WIDTH, STRAIN_BAR_HEIGHT);
        g.setColor(UiTheme.blend(UiTheme.STRAIN, new Color(255, 120, 120), (float) pulse * 0.7f));
        g.fillRect(BAR_X, STRAIN_BAR_Y, fillWidth, STRAIN_BAR_HEIGHT);
        g.setColor(UiTheme.CREAM);
        g.drawRect(BAR_X - 1, STRAIN_BAR_Y - 1, BAR_WIDTH + 1, STRAIN_BAR_HEIGHT + 1);
    }

    private void drawRot(Graphics2D g) {
        g.setFont(UiTheme.MONO_BOLD_SMALL);
        UiTheme.shadowText(g, "ROT " + Math.round(shownRot), BAR_X, ROT_LABEL_Y, new Color(176, 128, 224));

        int x = BAR_X;
        double shareTotal = 0;
        for (int i = 0; i < bosses.size(); i++) {
            int share = bosses.get(i).getRotShare();
            shareTotal += share;
            int width = segmentWidth(share);
            drawSegment(g, x, width, segmentFill[i]);
            x += width + SEGMENT_GAP;
        }

        // Rot that belongs to bosses not on this map yet stays locked as one last segment.
        double leftover = rotScale - shareTotal;
        if (leftover > 0.5) {
            drawSegment(g, x, BAR_X + BAR_WIDTH - x, 1);
        }
    }

    private int segmentWidth(int share) {
        return Math.max(8, (int) Math.round(BAR_WIDTH * share / rotScale) - SEGMENT_GAP);
    }

    private void drawSegment(Graphics2D g, int x, int width, double fill) {
        g.setColor(new Color(24, 14, 34));
        g.fillRect(x, ROT_BAR_Y, width, ROT_BAR_HEIGHT);
        g.setColor(UiTheme.ROT);
        g.fillRect(x, ROT_BAR_Y, (int) Math.round(width * fill), ROT_BAR_HEIGHT);
        g.setColor(new Color(UiTheme.CREAM.getRed(), UiTheme.CREAM.getGreen(), UiTheme.CREAM.getBlue(), 170));
        g.drawRect(x, ROT_BAR_Y, width - 1, ROT_BAR_HEIGHT - 1);
        if (fill > 0.3) {
            drawLock(g, x + width / 2, ROT_BAR_Y + ROT_BAR_HEIGHT / 2, (float) fill);
        }
    }

    // A tiny padlock: Rot stays locked until its boss falls.
    private void drawLock(Graphics2D g, int centerX, int centerY, float alpha) {
        g.setComposite(AlphaComposite.SrcOver.derive(Math.min(1f, alpha)));
        g.setColor(new Color(14, 8, 22));
        g.fillRect(centerX - 3, centerY - 1, 7, 5);
        g.setColor(UiTheme.CREAM);
        g.drawRect(centerX - 3, centerY - 1, 6, 4);
        g.setStroke(new BasicStroke(1f));
        g.draw(new Arc2D.Double(centerX - 2, centerY - 5, 4, 7, 0, 180, Arc2D.OPEN));
        g.setComposite(AlphaComposite.SrcOver);
    }

    private void drawBrainIcon(Graphics2D g) {
        UiTheme.panel(g, GAUGE_X, ICON_Y, 40, 40, new Color(30, 22, 40), UiTheme.CREAM);
        g.setColor(new Color(225, 120, 150));
        g.fillOval(GAUGE_X + 7, ICON_Y + 10, 16, 20);
        g.fillOval(GAUGE_X + 17, ICON_Y + 10, 16, 20);
        g.setColor(new Color(150, 60, 90));
        g.drawLine(GAUGE_X + 20, ICON_Y + 11, GAUGE_X + 20, ICON_Y + 29);
        g.drawArc(GAUGE_X + 10, ICON_Y + 14, 8, 8, 0, 180);
        g.drawArc(GAUGE_X + 22, ICON_Y + 18, 8, 8, 180, 180);
    }

    private void drawItemSlots(Graphics2D g) {
        int totalWidth = SLOT_COUNT * SLOT_SIZE + (SLOT_COUNT - 1) * SLOT_GAP;
        int x = 768 - 10 - totalWidth;
        for (int i = 0; i < SLOT_COUNT; i++) {
            UiTheme.panel(g, x, 8, SLOT_SIZE, SLOT_SIZE, new Color(14, 18, 30, 210), new Color(110, 130, 160));
            x += SLOT_SIZE + SLOT_GAP;
        }
    }
}
