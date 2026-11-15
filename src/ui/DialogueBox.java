package ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

/** Full-width text box with a name tab and typewriter text, for boss and system lines. Owner: Zrs. */
public class DialogueBox {

    private static final int BOX_X = 12;
    private static final int BOX_Y = 462;
    private static final int BOX_WIDTH = 744;
    private static final int BOX_HEIGHT = 96;
    private static final int TAB_X = 36;
    private static final int TAB_HEIGHT = 32;
    private static final int PADDING = 22;
    private static final int LINE_HEIGHT = 22;
    private static final double CHARS_PER_UPDATE = 1.0;

    private String speaker = "";
    private String text = "";
    private double revealed;
    private boolean waiting;
    private int ticks;
    private List<String> wrapped; // rebuilt lazily because wrapping needs the font metrics from a Graphics2D

    /** Shows a new line of dialogue with no name tab. */
    public void show(String text) {
        show("", text);
    }

    public void show(String speaker, String text) {
        this.speaker = speaker;
        this.text = text;
        this.revealed = 0;
        this.wrapped = null;
        this.waiting = false;
    }

    /** When true, a blinking arrow tells the player the box is waiting for a key or click. */
    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    public void update() {
        ticks++;
        revealed += CHARS_PER_UPDATE;
    }

    /** Reveals the rest of the line immediately. */
    public void skip() {
        revealed = text.length() + 1;
    }

    public boolean isFinished() {
        return revealed >= text.length();
    }

    public void draw(Graphics2D g) {
        g.setFont(UiTheme.MONO_MEDIUM);
        FontMetrics metrics = g.getFontMetrics();
        if (wrapped == null) {
            wrapped = wrap(text, BOX_WIDTH - 2 * PADDING, metrics);
        }

        UiTheme.panel(g, BOX_X, BOX_Y, BOX_WIDTH, BOX_HEIGHT, UiTheme.PANEL, UiTheme.CREAM);
        g.setColor(new Color(UiTheme.CREAM.getRed(), UiTheme.CREAM.getGreen(), UiTheme.CREAM.getBlue(), 90));
        g.drawRect(BOX_X + 4, BOX_Y + 4, BOX_WIDTH - 9, BOX_HEIGHT - 9);

        if (!speaker.isEmpty()) {
            g.setFont(UiTheme.MONO_LARGE);
            int tabWidth = Math.max(150, g.getFontMetrics().stringWidth(speaker) + 40);
            UiTheme.panel(g, TAB_X, BOX_Y - TAB_HEIGHT, tabWidth, TAB_HEIGHT + 1, UiTheme.PANEL, UiTheme.CREAM);
            UiTheme.centeredText(g, speaker, TAB_X + tabWidth / 2, BOX_Y - 10, UiTheme.CREAM);
        }

        g.setFont(UiTheme.MONO_MEDIUM);
        int remaining = (int) revealed;
        int y = BOX_Y + 30;
        for (String line : wrapped) {
            int shown = Math.min(line.length(), Math.max(0, remaining));
            g.setColor(UiTheme.CREAM);
            g.drawString(line.substring(0, shown), BOX_X + PADDING, y);
            remaining -= line.length();
            y += LINE_HEIGHT;
        }

        if (waiting && isFinished() && (ticks / 20) % 2 == 0) {
            int ax = BOX_X + BOX_WIDTH - 36;
            int ay = BOX_Y + BOX_HEIGHT - 26;
            g.setColor(UiTheme.CREAM);
            g.fillPolygon(new Polygon(new int[] {ax, ax + 14, ax + 7}, new int[] {ay, ay, ay + 9}, 3));
        }
    }

    private static List<String> wrap(String text, int maxWidth, FontMetrics metrics) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n", -1)) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (line.length() > 0 && metrics.stringWidth(candidate) > maxWidth) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            lines.add(line.toString());
        }
        return lines;
    }
}
