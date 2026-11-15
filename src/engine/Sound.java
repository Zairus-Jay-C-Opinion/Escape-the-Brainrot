package engine;

import exception.AssetLoadException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/** One WAV file that can be played or looped. A missing or unplayable file makes every method do nothing. */
public class Sound {

    // null when the file is missing or no audio device is available.
    private final Clip clip;

    public Sound(String path) {
        clip = open(path);
    }

    private static Clip open(String path) {
        // BufferedInputStream is needed because AudioSystem must be able to mark and reset the stream.
        try (InputStream raw = AssetManager.open(path);
             AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(raw))) {
            Clip loaded = AudioSystem.getClip();
            loaded.open(audio);
            return loaded;
        } catch (AssetLoadException | UnsupportedAudioFileException | IOException
                 | LineUnavailableException | IllegalArgumentException e) {
            return null;
        }
    }

    public void play() {
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void loop() {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /** Stops playback and frees the audio line. The sound cannot be played again afterwards. */
    public void close() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
