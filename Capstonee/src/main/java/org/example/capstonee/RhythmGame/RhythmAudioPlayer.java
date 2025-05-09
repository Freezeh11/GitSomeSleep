package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.audio.AudioPlayer;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.audio.Sound;
import com.almasb.fxgl.dsl.FXGL;

import static com.almasb.fxgl.dsl.FXGL.getAudioPlayer;
import static com.almasb.fxgl.dsl.FXGL.getAssetLoader;


public class RhythmAudioPlayer {

    private Music currentMusic;
    private boolean isMusicPlaying = false;
    private final AudioPlayer audioPlayer;

    private Sound hitSound;
    private Sound missSound;

    private static final String HIT_SOUND_PATH = "sounds/hit.wav";
    private static final String MISS_SOUND_PATH = "sounds/miss.wav";
    // REMOVED: private static final String DEFAULT_MUSIC_ASSET_PATH = "songs/zhongli.wav";


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
            hitSound = null;
            missSound = null;
        }
    }


    // *** Modified loadMusic to always take a path and remove default ***
    public void loadMusic(String assetPath) {
        // Stop previous music if any is playing *using this player*
        if (currentMusic != null && isMusicPlaying) {
            audioPlayer.stopMusic(currentMusic);
            isMusicPlaying = false;
        }
        currentMusic = null; // Always release the reference to the old music

        try {
            System.out.println("Attempting to load music asset: " + assetPath);
            // Assuming assetPath is correct relative to assets/
            currentMusic = getAssetLoader().loadMusic(assetPath); // Use the provided path

            if (currentMusic != null) {
                System.out.println("Successfully loaded music: " + assetPath);
                System.out.println("Music asset loaded successfully, ready for playback.");
            } else {
                System.err.println("Failed to load music asset (getAssetLoader returned null): " + assetPath);
            }
        } catch (Exception e) {
            System.err.println("Exception loading music asset: " + assetPath);
            e.printStackTrace();
            currentMusic = null;
        }
    }

    // REMOVED: Overload for default music (loadMusic())


    public void playMusic() {
        System.out.println("Checking music state before playing...");
        if (currentMusic != null) {
            if (!isMusicPlaying) {
                System.out.println("Attempting to play music.");
                audioPlayer.playMusic(currentMusic);
                isMusicPlaying = true;
                System.out.println("Music playback initiated.");
            } else {
                System.out.println("Music is already marked as playing. Skipping play call.");
            }
        } else {
            System.err.println("Error: Cannot play music. No music loaded or load failed.");
        }
    }

    public void stopMusic() {
        System.out.println("DEBUG: Received stopMusic call.");
        if (currentMusic != null && isMusicPlaying) {
            audioPlayer.stopMusic(currentMusic);
            isMusicPlaying = false;
            System.out.println("Music stopped via stopMusic.");
        } else if (currentMusic == null) {
            System.out.println("DEBUG: stopMusic called, but currentMusic is null.");
        } else if (!isMusicPlaying) {
            System.out.println("DEBUG: stopMusic called, but isMusicPlaying is false.");
        }
    }


    public boolean isMusicPlaying() {
        return isMusicPlaying;
    }


    public void playHitSound() {
        if (hitSound != null) {
            audioPlayer.playSound(hitSound);
        } else {
            System.err.println("Attempted to play hit sound, but it was not loaded.");
        }
    }


    public void playMissSound() {
        if (missSound != null) {
            audioPlayer.playSound(missSound);
        } else {
            System.err.println("Attempted to play miss sound, but it was not loaded.");
        }
    }

    public void stopAll() {
        System.out.println("DEBUG: Stopping all audio via stopAll().");

        if (currentMusic != null && isMusicPlaying) {
            System.out.println("DEBUG: Stopping current music track: " + currentMusic.getAudio());
            audioPlayer.stopMusic(currentMusic);
        }
        isMusicPlaying = false;

        // Explicitly stop all Music and Sound objects managed by FXGL's AudioPlayer
        FXGL.getAudioPlayer().stopAllMusic();
        FXGL.getAudioPlayer().stopAllSounds();

        currentMusic = null; // Release reference

        System.out.println("DEBUG: Audio stop sequence completed.");
    }
}