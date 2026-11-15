package battle;

/** Counts a question's elapsed time upward and reports each time a Strain interval completes. Synced to the 60 updates per second loop. */
public class BattleTimer {
    private static final int TICKS_PER_SECOND = 60;

    private int intervalTicks;
    private int elapsedTicks;
    private int ticksIntoInterval;
    private boolean paused;

    public BattleTimer(int intervalSeconds) {
        setIntervalSeconds(intervalSeconds);
    }

    /** Changes the interval without losing the time already elapsed on this question. */
    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalTicks = Math.max(1, intervalSeconds) * TICKS_PER_SECOND;
        this.ticksIntoInterval = Math.min(ticksIntoInterval, intervalTicks - 1);
    }

    /** Advances one update. Returns true on the update an interval completes. */
    public boolean tick() {
        if (paused) {
            return false;
        }
        elapsedTicks++;
        ticksIntoInterval++;
        if (ticksIntoInterval >= intervalTicks) {
            ticksIntoInterval = 0;
            return true;
        }
        return false;
    }

    /** Starts over at 0:00 for the next question. */
    public void reset() {
        elapsedTicks = 0;
        ticksIntoInterval = 0;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getElapsedSeconds() {
        return elapsedTicks / TICKS_PER_SECOND;
    }

    /** How close the next Strain tick is, from 0 to 1. */
    public double getIntervalProgress() {
        return (double) ticksIntoInterval / intervalTicks;
    }
}
