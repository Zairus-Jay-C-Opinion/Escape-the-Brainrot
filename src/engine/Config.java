package engine;

import exception.AssetLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Reads balance numbers from res/config.properties. Owner: Zrs. */
public final class Config {

    private static final Properties properties = new Properties();

    private Config() { }

    /** Call once at startup, before anything calls getInt/getDouble. */
    public static void load() throws AssetLoadException {
        try (InputStream stream = AssetManager.open("/config.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            throw new AssetLoadException("Could not read config.properties", e);
        }
    }

    public static int getInt(String key) {
        return Integer.parseInt(requireValue(key));
    }

    public static double getDouble(String key) {
        return Double.parseDouble(requireValue(key));
    }

    // A missing key is a programming/config mistake, so fail loudly instead of returning a silent 0.
    private static String requireValue(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing key in config.properties: " + key);
        }
        return value.trim();
    }
}
