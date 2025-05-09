package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.audio.AudioPlayer;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.audio.Sound;
import com.almasb.fxgl.dsl.FXGL;

import static com.almasb.fxgl.dsl.FXGL.getAudioPlayer;
import static com.almasb.fxgl.dsl.FXGL.getAssetLoader;


public class RhythmAudioPlayer {

    private Music currentMusic;
    private final AudioPlayer audioPlayer;

    private Sound hitSound;
    private Sound missSound;

    private static final String HIT_SOUND_PATH = "sounds/hit.wav";
    private static final String MISS_SOUND_PATH = "sounds/miss.wav";

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

    public void loadMusic(String assetPath) {
        stopMusic();
        currentMusic = null;

        try {
            System.out.println("Attempting to load music asset: " + assetPath);
            currentMusic = getAssetLoader().loadMusic(assetPath);

            if (currentMusic != null) {
                System.out.println("Successfully loaded music: " + assetPath);
                System.out.println("Music asset loaded successfully, ready for playback.");
            } else {
                System.err.println("Failed to load music asset (getAssetLoader returned null or asset not found): " + assetPath);
            }
        } catch (Exception e) {
            System.err.println("Exception loading music asset: " + assetPath);
            e.printStackTrace();
            currentMusic = null;
        }
    }

    public void playMusic() {
        System.out.println("Attempting to play music.");
        if (currentMusic != null) {
            audioPlayer.loopMusic(currentMusic);
            System.out.println("Music playback initiated (looping).");
        } else {
            System.err.println("Error: Cannot play music. No music loaded or load failed.");
        }
    }

    public void stopMusic() {
        System.out.println("DEBUG: Received stopMusic call.");
        if (currentMusic != null) {
            audioPlayer.stopMusic(currentMusic);
            System.out.println("Music stopped via stopMusic.");
            currentMusic = null;
        } else {
            System.out.println("DEBUG: stopMusic called, but currentMusic is null.");
        }
    }

    public void pauseMusic() {
        System.out.println("DEBUG (RhythmAudioPlayer): Pausing music.");
        if (currentMusic != null) {
            audioPlayer.pauseMusic(currentMusic);
            System.out.println("DEBUG (RhythmAudioPlayer): Music pause requested.");
        } else {
            System.out.println("DEBUG (RhythmAudioPlayer): No music reference to pause.");
        }
    }

    public void resumeMusic() {
        System.out.println("DEBUG (RhythmAudioPlayer): Resuming music.");
        if (currentMusic != null) {
            audioPlayer.resumeMusic(currentMusic);
            System.out.println("DEBUG (RhythmAudioPlayer): Music resume requested.");
        } else {
            System.out.println("DEBUG (RhythmAudioPlayer): No music reference to resume.");
        }
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
        System.out.println("DEBUG: Stopping all audio via RhythmAudioPlayer stopAll().");

        stopMusic();

        FXGL.getAudioPlayer().stopAllMusic();
        FXGL.getAudioPlayer().stopAllSounds();

        System.out.println("DEBUG: Audio stop sequence completed.");
    }
}