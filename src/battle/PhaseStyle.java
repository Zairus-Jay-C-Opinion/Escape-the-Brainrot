package battle;

import java.awt.Color;

/** How the battle screen looks during a phase. A boss supplies one per phase so each fight gets its own mood. */
public record PhaseStyle(Color tint, Color accent) {

    /** Cold teal used for Phase 1 unless a boss says otherwise. */
    public static final PhaseStyle CALM = new PhaseStyle(new Color(10, 40, 52), new Color(90, 220, 220));
    /** Red used for Phase 2 unless a boss says otherwise. */
    public static final PhaseStyle ENRAGED = new PhaseStyle(new Color(70, 8, 18), new Color(255, 70, 70));
}
