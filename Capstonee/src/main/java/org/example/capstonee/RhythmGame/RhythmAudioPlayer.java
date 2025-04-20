package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.audio.AudioPlayer;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.audio.Sound;

import static com.almasb.fxgl.dsl.FXGL.getAudioPlayer;
import static com.almasb.fxgl.dsl.FXGL.getAssetLoader;


public class RhythmAudioPlayer {

    private Music currentMusic;
    private final AudioPlayer audioPlayer; // Make final if initialized in constructor

    private Sound hitSound;
    private Sound missSound;

    // Consider making sound paths constants
    private static final String HIT_SOUND_PATH = "sounds/hit.wav";
    private static final String MISS_SOUND_PATH = "sounds/miss.wav";
    // Default music path, can be overridden or passed to loadMusic
    private static final String DEFAULT_MUSIC_PATH = "songs/desperato.wav";


    public RhythmAudioPlayer() {
        this.audioPlayer = getAudioPlayer();
        loadSoundEffects();
    }


    private void loadSoundEffects() {
        try {
            hitSound = getAssetLoader().loadSound(HIT_SOUND_PATH);
            missSound = getAssetLoader().loadSound(MISS_SOUND_PATH);

            if (hitSound == null) {
                System.err.println("Failed to load hit sound: " + HIT_SOUND_PATH);
            } else {
                System.out.println("Successfully loaded hit sound: " + HIT_SOUND_PATH);
            }
            if (missSound == null) {
                System.err.println("Failed to load miss sound: " + MISS_SOUND_PATH);
            } else {
                System.out.println("Successfully loaded miss sound: " + MISS_SOUND_PATH);
            }

        } catch (Exception e) {
            System.err.println("Exception during sound effect loading.");
            e.printStackTrace();
            // Set to null explicitly on failure
            hitSound = null;
            missSound = null;
        }
    }


    // Allow loading specific music files
    public void loadMusic(String assetPath) {
        // Stop previous music if any is playing
        if (currentMusic != null) {
            audioPlayer.stopMusic(currentMusic);
            currentMusic = null; // Release reference
        }

        try {
            System.out.println("Attempting to load music asset: " + assetPath);
            currentMusic = getAssetLoader().loadMusic(assetPath);

            if (currentMusic != null) {
                System.out.println("Successfully loaded music: " + assetPath);
                // Optional: Set looping or volume defaults here if needed
                // currentMusic.setCycleCount(Music.INDEFINITE); // For looping
            } else {
                System.err.println("Failed to load music asset (getAssetLoader returned null): " + assetPath);
            }
        } catch (Exception e) {
            System.err.println("Exception loading music asset: " + assetPath);
            e.printStackTrace();
            currentMusic = null;
        }
    }

    // Overload for default music if needed
    public void loadMusic() {
        loadMusic(DEFAULT_MUSIC_PATH);
    }


    public void playMusic() {
        System.out.println("Checking music state before playing...");
        if (currentMusic != null) {
            System.out.println("Attempting to play music.");
            // Ensure volume is set correctly before playing
            // audioPlayer.setGlobalMusicVolume(getSettings().getGlobalMusicVolume()); // Ensure sync if needed
            audioPlayer.playMusic(currentMusic);
            System.out.println("Music playback initiated.");
        } else {
            System.err.println("Error: Cannot play music. No music loaded or load failed.");
        }
    }


    public void stopMusic() {
        if (currentMusic != null) {
            audioPlayer.stopMusic(currentMusic);
            System.out.println("Music stopped.");
            // Optional: Release music resource if not needed again soon?
            // currentMusic = null; // Depends on whether you reload it often
        }
    }


    public void playHitSound() {
        if (hitSound != null) {
            // Ensure volume is set correctly
            // audioPlayer.setGlobalSoundVolume(getSettings().getGlobalSoundVolume()); // Ensure sync if needed
            audioPlayer.playSound(hitSound);
        } else {
            System.err.println("Attempted to play hit sound, but it was not loaded.");
        }
    }


    public void playMissSound() {
        if (missSound != null) {
            // audioPlayer.setGlobalSoundVolume(getSettings().getGlobalSoundVolume()); // Ensure sync if needed
            audioPlayer.playSound(missSound);
        } else {
            System.err.println("Attempted to play miss sound, but it was not loaded.");
        }
    }
}