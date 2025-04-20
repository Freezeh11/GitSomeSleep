package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.audio.AudioPlayer;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.audio.Sound;

import static com.almasb.fxgl.dsl.FXGL.getAudioPlayer;
import static com.almasb.fxgl.dsl.FXGL.getAssetLoader;


public class RhythmAudioPlayer {

    private Music currentMusic;
    private AudioPlayer audioPlayer;

    private Sound hitSound;
    private Sound missSound;


    public RhythmAudioPlayer() {
        this.audioPlayer = getAudioPlayer();
        loadSoundEffects();
    }


    private void loadSoundEffects() {
        try {

            String hitSoundPath = "sounds/hit.wav";
            String missSoundPath = "sounds/miss.wav";

            hitSound = getAssetLoader().loadSound(hitSoundPath);
            missSound = getAssetLoader().loadSound(missSoundPath);

            if (hitSound == null) {
                System.err.println("Failed to load hit sound: " + hitSoundPath);
            } else {
                System.out.println("Successfully loaded hit sound: " + hitSoundPath);
            }
            if (missSound == null) {
                System.err.println("Failed to load miss sound: " + missSoundPath);
            } else {
                System.out.println("Successfully loaded miss sound: " + missSoundPath);
            }

        } catch (Exception e) {
            System.err.println("Exception during sound effect loading.");
            e.printStackTrace();
            hitSound = null;
            missSound = null;
        }
    }



    public void loadMusic() {
        // YAWA
        if (currentMusic != null) {
            audioPlayer.stopMusic(currentMusic);
        }


        String assetPath = "songs/desperato.wav";
        // -----------------------------------------------------------------


        try {
            System.out.println("Attempting to load music asset with path: " + assetPath);
            currentMusic = getAssetLoader().loadMusic(assetPath);

            if (currentMusic != null) {
                System.out.println("Successfully loaded music asset: " + assetPath);
            } else {
                System.err.println("Failed to load music asset: " + assetPath + " (getAssetLoader returned null). Check file path and format.");
            }
        } catch (Exception e) { // This catches exceptions during load
            System.err.println("Exception loading music asset: " + assetPath);
            e.printStackTrace();
            currentMusic = null;
        }
    }


    public void playMusic() {
        System.out.println("Checking music state before playing...");
        if (currentMusic != null) {
            System.out.println("Attempting to play music (currentMusic is not null).");
            audioPlayer.playMusic(currentMusic);
            System.out.println("Music playback initiated.");
        } else {
            System.err.println("Error: Cannot play music. No music loaded or load failed.");
        }
    }

    // stop music
    public void stopMusic() {
        if (currentMusic != null) {
            audioPlayer.stopMusic(currentMusic);
            System.out.println("Music stopped.");
        }
    }

    // hit sounds
    public void playHitSound() {
        if (hitSound != null) {
            audioPlayer.playSound(hitSound);
        }
    }

    // miss sounds
    public void playMissSound() {
        if (missSound != null) {
            audioPlayer.playSound(missSound);
        }
    }
}