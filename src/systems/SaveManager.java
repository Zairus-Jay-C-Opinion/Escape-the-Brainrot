package systems;

import exception.SaveDataException;

// TODO (Aeron)
/** Saves and loads progress using java.util.Properties. */
public class SaveManager {

    private String saveFilePath; // TODO (Aeron)

    public SaveManager(String saveFilePath) {
        // TODO (Aeron)
    }

    /** Writes the gauge and stats to the save file. */
    public void save(BrainrotGauge gauge, PlayerStats stats) throws SaveDataException {
        // TODO (Aeron)
    }

    /** Reads the save file back into the given gauge and stats. */
    public void load(BrainrotGauge gauge, PlayerStats stats) throws SaveDataException {
        // TODO (Aeron)
    }

    public boolean saveExists() {
        // TODO (Aeron)
        return false;
    }
}
