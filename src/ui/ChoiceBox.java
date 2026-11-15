package ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;

/** Lettered answer choices (A, B, C, D) that can be picked by key, arrows or mouse. Owner: Zrs. */
public class ChoiceBox {

    private static final int ROW_HEIGHT = 26;
    private static final String LETTERS = "ABCD";

    private List<String> choices = List.of();
    private boolean[] eliminated = new boolean[0];
    private int selected;
    private int x;
    private int y;
    private int width;

    public void setChoices(List<String> choices) {
        this.choices = choices;
        this.eliminated = new boolean[choices.size()];
        this.selected = 0;
    }

    public void setBounds(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public int getHeight() {
        return choices.size() * ROW_HEIGHT;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int index) {
        if (index >= 0 && index < choices.size() && !eliminated[index]) {
            selected = index;
        }
    }

    public boolean isEliminated(int index) {
        return eliminated[index];
    }

    /** Greys out a wrong choice so it cannot be picked again. */
    public void eliminate(int index) {
        eliminated[index] = true;
        if (selected == index) {
            move(1);
        }
    }

    /** Moves the highlight up or down, skipping eliminated choices. */
    public void move(int step) {
        for (int i = 0; i < choices.size(); i++) {
            selected = (selected + step + choices.size()) % choices.size();
            if (!eliminated[selected]) {
                return;
            }
        }
    }

    /** Index of the choice under a game-pixel point, or -1. Eliminated choices never match. */
    public int choiceAt(int px, int py) {
        if (px < x || px >= x + width || py < y || py >= y + getHeight()) {
            return -1;
        }
        int index = (py - y) / ROW_HEIGHT;
        return eliminated[index] ? -1 : index;
    }

    public void draw(Graphics2D g) {
        g.setFont(UiTheme.MONO_MEDIUM);
        FontMetrics metrics = g.getFontMetrics();
        for (int i = 0; i < choices.size(); i++) {
            int rowY = y + i * ROW_HEIGHT;
            boolean isSelected = i == selected && !eliminated[i];
            if (isSelected) {
                g.setColor(new Color(92, 124, 76));
                g.fillRect(x, rowY, width, ROW_HEIGHT - 2);
            }
            String label = LETTERS.charAt(i) + ".  " + ellipsize(choices.get(i), width - 24, metrics);
            Color color = eliminated[i] ? new Color(100, 100, 100) : UiTheme.CREAM;
            g.setColor(color);
            g.drawString(label, x + 10, rowY + 18);
            if (eliminated[i]) {
                g.drawLine(x + 10, rowY + 12, x + 10 + metrics.stringWidth(label), rowY + 12);
            }
        }
    }

    private static String ellipsize(String text, int maxWidth, FontMetrics metrics) {
        if (metrics.stringWidth(text) <= maxWidth) {
            return text;
        }
        String cut = text;
        while (cut.length() > 1 && metrics.stringWidth(cut + "...") > maxWidth) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "...";
    }
}
