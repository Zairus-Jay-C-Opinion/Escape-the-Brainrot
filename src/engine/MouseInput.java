package engine;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Mouse position in game pixels plus a one-update "clicked" flag, the mouse twin of InputHandler.
 * Screens never see window coordinates, so they keep working at any window size.
 */
public class MouseInput extends MouseAdapter {

    private volatile ViewTransform view = ViewTransform.fit(
            GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
    private int x = -1; // -1 until the mouse has been over the window
    private int y = -1;
    private boolean clicked;

    /** GamePanel calls this every frame so conversion always matches how the picture was just drawn. */
    public void setView(ViewTransform view) {
        this.view = view;
    }

    public synchronized int getX() {
        return x;
    }

    public synchronized int getY() {
        return y;
    }

    /** True once per left click. */
    public synchronized boolean wasClicked() {
        return clicked;
    }

    public synchronized void endFrame() {
        clicked = false;
    }

    private synchronized void track(MouseEvent e) {
        x = view.toGameX(e.getX());
        y = view.toGameY(e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        track(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        track(e);
    }

    @Override
    public synchronized void mousePressed(MouseEvent e) {
        track(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            clicked = true;
        }
    }
}
