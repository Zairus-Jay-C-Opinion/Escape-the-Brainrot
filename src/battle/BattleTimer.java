package battle;

/**
 * Frame-based countdown timer synced to the 60 FPS game loop.
 * Owner: Andrew.
 */
public class BattleTimer {
    private static final int TICKS_PER_SECOND = 60;

    private final int maxSeconds;
    private int remainingTicks;
    private boolean paused;

    public BattleTimer(int maxSeconds) {
        this.maxSeconds = maxSeconds;
        this.remainingTicks = maxSeconds * TICKS_PER_SECOND;
        this.paused = false;
    }

    /**
     * Advances the timer by one frame. Returns true on the exact tick time expires.
     */
    public boolean tick() {
        if (paused || remainingTicks <= 0) {
            return false;
        }
        remainingTicks--;
        return remainingTicks == 0;
    }

    public void reset() {
        this.remainingTicks = maxSeconds * TICKS_PER_SECOND;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getRemainingSeconds() {
        // Round up so 1 tick left still displays as 1s
        return (int) Math.ceil((double) remainingTicks / TICKS_PER_SECOND);
    }

    public double getTimeRatio() {
        if (maxSeconds <= 0) return 0;
        return (double) remainingTicks / (maxSeconds * TICKS_PER_SECOND);
    }
}
