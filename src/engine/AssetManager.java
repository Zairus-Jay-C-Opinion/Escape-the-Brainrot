package engine;

import exception.AssetLoadException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** Loads resources from the classpath (the res/ folder) and caches images. Owner: Zrs. */
public final class AssetManager {

    // Cache so each image file is only read from disk once.
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();

    private AssetManager() { }

    /** Opens any resource, e.g. "/maps/test-map.csv". Callers must close the stream. */
    public static InputStream open(String path) throws AssetLoadException {
        InputStream stream = AssetManager.class.getResourceAsStream(path);
        if (stream == null) {
            throw new AssetLoadException("Resource not found: " + path);
        }
        return stream;
    }

    /** Loads an image, e.g. "/sprites/player.png". Later calls return the cached copy. */
    public static synchronized BufferedImage getImage(String path) throws AssetLoadException {
        BufferedImage cached = imageCache.get(path);
        if (cached != null) {
            return cached;
        }
        try (InputStream stream = open(path)) {
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                throw new AssetLoadException("Not a readable image: " + path);
            }
            imageCache.put(path, image);
            return image;
        } catch (IOException e) {
            throw new AssetLoadException("Could not read image: " + path, e);
        }
    }
}
