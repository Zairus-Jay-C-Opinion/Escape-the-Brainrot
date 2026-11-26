package systems;

import engine.Config;
import exception.SaveDataException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Persists and restores checkpoint progress using java.util.Properties.
 */
public class SaveManager {

    private final Path savePath;

    public SaveManager() {
        this(Config.getSaveFilePath());
    }

    public SaveManager(String path) {
        this.savePath = Paths.get(path);
    }

    public boolean hasSaveFile() {
        return Files.exists(savePath) && Files.isRegularFile(savePath);
    }

    public boolean saveExists() {
        return hasSaveFile();
    }

    public void saveCheckpoint(
            String mapId,
            int tileX,
            int tileY,
            BrainrotGauge gauge,
            int rebirths,
            int defeatedBosses
    ) throws SaveDataException {
        int strain = gauge != null ? (int) Math.round(gauge.getStrain()) : Config.getStartStrain();
        int rot = gauge != null ? (int) Math.round(gauge.getRot()) : 0;
        saveCheckpoint(mapId, tileX, tileY, strain, rot, rebirths, defeatedBosses);
    }

    public void saveCheckpoint(
            String mapId,
            int tileX,
            int tileY,
            int strain,
            int rot,
            int rebirths,
            int defeatedBosses
    ) throws SaveDataException {
        Properties props = new Properties();
        props.setProperty("map.id", mapId);
        props.setProperty("player.tileX", Integer.toString(tileX));
        props.setProperty("player.tileY", Integer.toString(tileY));
        props.setProperty("gauge.strain", Integer.toString(strain));
        props.setProperty("gauge.rot", Integer.toString(rot));
        props.setProperty("rebirth.count", Integer.toString(rebirths));
        props.setProperty("progress.defeatedBosses", Integer.toString(defeatedBosses));

        try {
            Path parent = savePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream out = Files.newOutputStream(savePath)) {
                props.store(out, "Escape the Brainrot Checkpoint Save");
            }
        } catch (IOException e) {
            throw new SaveDataException("Failed to write save file: " + savePath, e);
        }
    }

    public Properties loadCheckpoint() throws SaveDataException {
        if (!hasSaveFile()) {
            throw new SaveDataException("Save file not found at: " + savePath);
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(savePath)) {
            props.load(in);
        } catch (IOException e) {
            throw new SaveDataException("Failed to read save file: " + savePath, e);
        }

        validateRequiredKeys(props);
        return props;
    }

    private void validateRequiredKeys(Properties props) throws SaveDataException {
        String mapId = props.getProperty("map.id", "").trim();
        if (mapId.isEmpty()) {
            throw new SaveDataException("Missing or empty map.id in save file.");
        }

        int tileX = parseNonNegativeInt(props, "player.tileX");
        int tileY = parseNonNegativeInt(props, "player.tileY");
        int strain = parseNonNegativeInt(props, "gauge.strain");
        int rot = parseNonNegativeInt(props, "gauge.rot");
        int rebirths = parseNonNegativeInt(props, "rebirth.count");
        int defeatedBosses = parseNonNegativeInt(props, "progress.defeatedBosses");

        if (tileX < 0 || tileY < 0 || strain < 0 || rot < 0 || rebirths < 0 || defeatedBosses < 0) {
            throw new SaveDataException("Negative value detected in save data.");
        }
        if (rebirths > Config.getMaxRebirths()) {
            throw new SaveDataException("Saved rebirth count exceeds maximum (" + Config.getMaxRebirths() + ").");
        }
    }

    private int parseNonNegativeInt(Properties props, String key) throws SaveDataException {
        String raw = props.getProperty(key);
        if (raw == null || raw.trim().isEmpty()) {
            throw new SaveDataException("Missing required save key: " + key);
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new SaveDataException("Invalid integer for save key '" + key + "': " + raw, e);
        }
    }

    public boolean deleteSave() {
        try {
            return Files.deleteIfExists(savePath);
        } catch (IOException e) {
            return false;
        }
    }
}
