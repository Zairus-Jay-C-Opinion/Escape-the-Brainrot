package engine;

import exception.AssetLoadException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One WAV file that can be played or looped, plus shared SFX/BGM helpers.
 * A missing or unplayable file makes every method do nothing.
 */
public class Sound {

    // Shared SFX cache and active BGM slot so screens can switch music without leaking audio lines.
    private static final float BGM_GAIN_DB = -9.0f;
    private static final Map<String, Sound> SFX_CACHE = new ConcurrentHashMap<>();
    private static Sound currentBgm;
    private static String currentBgmPath;
    private static boolean muted;

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

    /** Adjusts playback gain in decibels if the audio line supports MASTER_GAIN. */
    public void setVolume(float decibels) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float clamped = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibels));
        gain.setValue(clamped);
    }

    public void play() {
        if (clip != null && !muted) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void loop() {
        if (clip != null && !muted) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }

    /** Stops playback and frees the audio line. The sound cannot be played again afterwards. */
    public void close() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    // ---------------------------------------------------------------- shared SFX / BGM manager

    /** Plays a cached one-shot sound effect (e.g. "/sounds/click.wav"). */
    public static void playSfx(String path) {
        if (muted || path == null) {
            return;
        }
        Sound sfx = SFX_CACHE.computeIfAbsent(path, Sound::new);
        sfx.play();
    }

    /** Switches the looping background music track unless the same track is already active. */
    public static synchronized void playBgm(String path) {
        if (path == null) {
            stopBgm();
            return;
        }
        if (path.equals(currentBgmPath) && currentBgm != null) {
            return;
        }
        stopBgm();
        Sound next = new Sound(path);
        next.setVolume(BGM_GAIN_DB);
        currentBgm = next;
        currentBgmPath = path;
        if (!muted) {
            currentBgm.loop();
        }
    }

    /** Stops and releases the active background music track. */
    public static synchronized void stopBgm() {
        if (currentBgm != null) {
            currentBgm.close();
            currentBgm = null;
        }
        currentBgmPath = null;
    }

    /** Toggles global audio mute for BGM and SFX. Returns true if now muted. */
    public static synchronized boolean toggleMute() {
        muted = !muted;
        if (currentBgm != null) {
            if (muted) {
                currentBgm.stop();
            } else {
                currentBgm.loop();
            }
        }
        return muted;
    }

    public static boolean isMuted() {
        return muted;
    }
}
