package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The code terminal: a title bar, the question, and the code with line numbers. Long lines wrap.
 * The bottom strip of the panel is left free for the answer controls. Owner: Zrs.
 */
public class CodeBox {

    private static final Font PROMPT_FONT = new Font(Font.MONOSPACED, Font.BOLD, 14);
    private static final Font CODE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 13);
    private static final int TITLE_BAR_HEIGHT = 24;
    private static final int PADDING = 12;
    private static final int GUTTER_WIDTH = 30;
    private static final int PROMPT_LINE_HEIGHT = 19;
    private static final int CODE_LINE_HEIGHT = 18;
    // Challenge files may already number their lines ("3: int x;"); those numbers are what the answer refers to.
    private static final Pattern LABELLED_LINE = Pattern.compile("^\\s*(\\d+):\\s?(.*)$");

    /** One drawn row. A wrapped line has several rows that share one lineNumber. */
    private record Row(String gutter, String text, int lineNumber, boolean isCode) { }

    private final List<Row> rows = new ArrayList<>();
    private String title = "Code Terminal";
    private int x;
    private int y;
    private int width;
    private int height;
    private int answerAreaHeight;
    private int hoverLine = -1;
    private int scrollY;
    private int wrapWidth;

    // A throwaway 1x1 image gives us a Graphics2D to measure text before the first real draw.
    private static final FontMetrics PROMPT_METRICS = measure(PROMPT_FONT);
    private static final FontMetrics CODE_METRICS = measure(CODE_FONT);

    private static FontMetrics measure(Font font) {
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics().getFontMetrics(font);
    }

    public void setBounds(int x, int y, int width, int height, int answerAreaHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.answerAreaHeight = answerAreaHeight;
        this.wrapWidth = width - 2 * PADDING - GUTTER_WIDTH;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /** Top edge of the strip reserved for answer controls. */
    public int getAnswerAreaTop() {
        return y + height - answerAreaHeight;
    }

    public int getX() {
        return x;
    }

    public int getWidth() {
        return width;
    }

    /** Highlights a code line while the mouse is over it, or pass -1 for none. */
    public void setHoverLine(int lineNumber) {
        this.hoverLine = lineNumber;
    }

    private int maxScroll(){
        int totalHeight = 0;
        for (Row row : rows){
            totalHeight += row.isCode ? CODE_LINE_HEIGHT : PROMPT_LINE_HEIGHT;
        }
        int visibleHeight = height - TITLE_BAR_HEIGHT - answerAreaHeight - 12;
        return Math.max(0, totalHeight - visibleHeight);
    }

    public void scroll(int deltaPixels){
        scrollY = Math.max(0, Math.min(maxScroll(), scrollY + deltaPixels));
    }

    public void setContent(String prompt, String code) {
        rows.clear();
        scrollY = 0;

        for (String line : prompt.split("\n")) {
            for (String piece : wrap(line, PROMPT_METRICS, width - 2 * PADDING)) {
                rows.add(new Row("", piece, 0, false));
            }
        }

        List<String> codeLines = code.isEmpty() ? List.of() : List.of(code.split("\n", -1));
        boolean labelled = !codeLines.isEmpty() && codeLines.stream().allMatch(l -> l.isBlank() || LABELLED_LINE.matcher(l).matches());
        int sequence = 0;
        for (String line : codeLines) {
            sequence++;
            int number = sequence;
            String text = line;
            if (labelled) {
                Matcher m = LABELLED_LINE.matcher(line);
                if (m.matches()) {
                    number = Integer.parseInt(m.group(1));
                    text = m.group(2);
                }
            }
            boolean first = true;
            for (String piece : wrap(text, CODE_METRICS, wrapWidth)) {
                rows.add(new Row(first ? String.valueOf(number) : "", piece, number, true));
                first = false;
            }
        }
    }

    /** The code line number under a game-pixel point, or -1. Uses the numbers the file gives its lines. */
    public int lineAt(int px, int py) {
        if (py < y + TITLE_BAR_HEIGHT || py >= getAnswerAreaTop()){
            return -1;
        }
        int rowY = firstCodeRowTop();
        for (Row row : rows) {
            if (!row.isCode) {
                continue;
            }
            if (new Rectangle(x + PADDING, rowY, width - 2 * PADDING, CODE_LINE_HEIGHT).contains(px, py)) {
                return row.lineNumber;
            }
            rowY += CODE_LINE_HEIGHT;
        }
        return -1;
    }

    public void draw(Graphics2D g) {
        UiTheme.panel(g, x, y, width, height, UiTheme.TEAL_PANEL, UiTheme.TEAL);
        g.setColor(new Color(40, 120, 124, 150));
        g.fillRect(x + 1, y + 1, width - 2, TITLE_BAR_HEIGHT);
        g.setFont(UiTheme.MONO_BOLD_SMALL);
        UiTheme.shadowText(g, title, x + PADDING, y + 17, UiTheme.CREAM);

        // Clip so a very long snippet can never spill into the answer controls.
        java.awt.Shape oldClip = g.getClip();
        g.setClip(x + 1, y + TITLE_BAR_HEIGHT, width - 2, height - TITLE_BAR_HEIGHT - answerAreaHeight);
        int rowY = y + TITLE_BAR_HEIGHT + 6 - scrollY;
        for (Row row : rows) {
            if (row.isCode) {
                drawCodeRow(g, row, rowY);
                rowY += CODE_LINE_HEIGHT;
            } else {
                g.setFont(PROMPT_FONT);
                g.setColor(UiTheme.CREAM);
                g.drawString(row.text, x + PADDING, rowY + 14);
                rowY += PROMPT_LINE_HEIGHT;
            }
        }
        g.setClip(oldClip);

        int max = maxScroll();
        if (max > 0) {
            int trackY = y + TITLE_BAR_HEIGHT + 4;
            int trackH = height - TITLE_BAR_HEIGHT - answerAreaHeight - 8;
            int thumbH = Math.max(20, trackH * trackH / (trackH + max));
            int thumbY = trackY + (trackH - thumbH) * scrollY / max;
            g.setColor(new Color(40, 100, 104, 90));
            g.fillRect(x + width - 7, trackY, 3, trackH);
            g.setColor(new Color(120, 220, 220, 180));
            g.fillRect(x + width - 7, thumbY, 3, thumbH);
        }

        g.setColor(new Color(70, 200, 200, 90));
        g.drawLine(x + 8, getAnswerAreaTop() - 2, x + width - 9, getAnswerAreaTop() - 2);
    }

    private void drawCodeRow(Graphics2D g, Row row, int rowY) {
        if (row.lineNumber == hoverLine) {
            g.setColor(new Color(90, 200, 200, 60));
            g.fillRect(x + 4, rowY, width - 8, CODE_LINE_HEIGHT);
        }
        g.setFont(CODE_FONT);
        g.setColor(new Color(110, 170, 170));
        g.drawString(row.gutter, x + PADDING, rowY + 13);
        g.setColor(new Color(190, 230, 225));
        g.drawString(row.text, x + PADDING + GUTTER_WIDTH, rowY + 13);
    }

    /** Draws the typed-answer line inside the answer strip. */
    public void drawTypedInput(Graphics2D g, String hint, String typed, boolean caretOn) {
        int top = getAnswerAreaTop();
        g.setFont(UiTheme.MONO_SMALL);
        g.setColor(UiTheme.CREAM_DIM);
        g.drawString(hint, x + PADDING, top + 16);
        UiTheme.panel(g, x + PADDING, top + 24, width - 2 * PADDING, 28, new Color(4, 14, 18), UiTheme.TEAL);
        g.setFont(UiTheme.MONO_MEDIUM);
        g.setColor(UiTheme.CREAM);
        g.drawString("> " + typed + (caretOn ? "_" : ""), x + PADDING + 8, top + 44);
    }

    private int firstCodeRowTop() {
        int rowY = y + TITLE_BAR_HEIGHT + 6 - scrollY;
        for (Row row : rows) {
            if (row.isCode) {
                return rowY;
            }
            rowY += PROMPT_LINE_HEIGHT;
        }
        return rowY;
    }

    private static List<String> wrap(String text, FontMetrics metrics, int maxWidth) {
        List<String> pieces = new ArrayList<>();
        String rest = text.replace("\t", "    ");
        while (metrics.stringWidth(rest) > maxWidth && rest.length() > 1) {
            int cut = rest.length();
            while (cut > 1 && metrics.stringWidth(rest.substring(0, cut)) > maxWidth) {
                cut--;
            }
            // Prefer breaking after a space so words stay whole, unless that wastes most of the line.
            int space = rest.lastIndexOf(' ', cut - 1);
            if (space > cut / 2) {
                cut = space + 1;
            }
            pieces.add(rest.substring(0, cut));
            rest = rest.substring(cut);
        }
        pieces.add(rest);
        return pieces;
    }
}
