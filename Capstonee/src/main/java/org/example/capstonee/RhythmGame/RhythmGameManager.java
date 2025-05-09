package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.particle.ParticleComponent;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.almasb.fxgl.dsl.FXGL.*;

import static org.example.capstonee.RhythmGame.RhythmGameFactory.*;
import static org.example.capstonee.RhythmGame.RhythmGameState.PLAYING;

public class RhythmGameManager {

    private static final double HIT_LINE_Y = 720 - 100;
    private static final double HIT_WINDOW_MS = 150;
    private static final double PERFECT_WINDOW_MS = HIT_WINDOW_MS / 3.0;
    private static final double FALL_DURATION_MS = (HIT_LINE_Y / RhythmGameFactory.NOTE_SPEED) * 1000;
    private static final int NUM_LANES = RhythmGameFactory.LANE_X_POSITIONS.length;
    private static final double NOTE_SIZE = RhythmGameFactory.NOTE_SIZE;


    private boolean isActive = false;
    private RhythmGameState state = RhythmGameState.READY;
    private long gameStartTime = 0;
    private long songElapsedTimeMs = 0;
    private int nextNoteIndexToSpawn = 0;
    private boolean songFinishedNaturally = false;

    private final RhythmAudioPlayer audioPlayer;
    private final RhythmGameUI gameUI; // Needs a method to call finalizeAndReturn? No, UI *calls* finalizeAndReturn
    private final GameScene gameScene;
    private List<NoteInfo> beatmap;
    private Runnable onGameEndCallback; // Called by finalizeAndReturn

    public RhythmGameManager(GameScene gameScene, RhythmGameUI gameUI, RhythmAudioPlayer audioPlayer) {
        this.gameScene = gameScene;
        this.gameUI = gameUI;
        this.audioPlayer = audioPlayer;
    }

    public boolean isActive() { return isActive; }
    public RhythmGameState getState() { return state; }
    public void setOnGameEndCallback(Runnable onGameEndCallback) { this.onGameEndCallback = onGameEndCallback; }


    public void start(String beatmapPath) throws IOException {
        if (isActive) return;
        System.out.println("Starting Rhythm Game setup for beatmap: " + beatmapPath);
        isActive = true;
        state = RhythmGameState.READY;
        songFinishedNaturally = false;

        cleanupEntities();

        spawn("rhythmBackground");

        beatmap = BeatmapLoader.loadBeatmapFile("assets/" + beatmapPath);
        if (beatmap == null || beatmap.isEmpty()) {
            System.err.println("Error: Beatmap is empty or failed to load from " + beatmapPath + ". Aborting rhythm game start.");
            isActive = false;
            state = RhythmGameState.GAME_ENDED;
            gameUI.cleanup();
            if (onGameEndCallback != null) {
                getGameTimer().runOnceAfter(onGameEndCallback, Duration.seconds(0.1));
            }
            return;
        }

        nextNoteIndexToSpawn = 0;

        set("score", 0);
        set("combo", 0);
        set("songElapsedTimeMs", 0L);

        gameUI.setup(); // UI setup happens here
        gameUI.showReadyScreen();
        spawnHitZoneMarkers();

        System.out.println("Rhythm Game Ready. Press SPACE to start.");
    }

    public void startPlaying() {
        if (!isActive || state != RhythmGameState.READY) return;
        System.out.println("Starting Rhythm Game Play...");
        state = PLAYING;
        gameUI.showPlayingUI();

        audioPlayer.playMusic();

        gameStartTime = System.currentTimeMillis();
        songElapsedTimeMs = 0;
        System.out.println("DEBUG: gameStartTime captured as: " + gameStartTime);
    }

    public void update(double tpf) {
        if (!isActive || state != PLAYING) return;

        songElapsedTimeMs = System.currentTimeMillis() - gameStartTime;
        set("songElapsedTimeMs", songElapsedTimeMs);

        spawnNotes();
        checkMisses();

        if (nextNoteIndexToSpawn >= beatmap.size() && getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE).isEmpty()) {
            if (!songFinishedNaturally) {
                songFinishedNaturally = true;
                System.out.println("Song finished naturally. Ending game soon.");
                getGameTimer().runOnceAfter(this::end, Duration.seconds(0.5));
            }
        }
    }


    public void handleInput(int laneIndex) {
        if (!isActive || state != PLAYING) return;

        List<Entity> hittableNotes = getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE)
                .stream()
                .filter(note -> {
                    RhythmNoteComponent noteComp = note.getComponent(RhythmNoteComponent.class);
                    if (noteComp.getLaneIndex() != laneIndex) return false;
                    long timeDiff = songElapsedTimeMs - noteComp.getTargetHitTimestamp();
                    if (Math.abs(timeDiff) > HIT_WINDOW_MS) return false;
                    return true;
                })
                .sorted((n1, n2) -> {
                    long diff1 = Math.abs(songElapsedTimeMs - n1.getComponent(RhythmNoteComponent.class).getTargetHitTimestamp());
                    long diff2 = Math.abs(songElapsedTimeMs - n2.getComponent(RhythmNoteComponent.class).getTargetHitTimestamp());
                    return Long.compare(diff1, diff2);
                })
                .collect(Collectors.toList());

        if (hittableNotes.isEmpty()) {
            triggerMissFeedback(laneIndex);
            return;
        }

        Entity bestMatch = hittableNotes.get(0);
        RhythmNoteComponent hitNoteComponent = bestMatch.getComponent(RhythmNoteComponent.class);
        long actualTimeDiff = Math.abs(songElapsedTimeMs - hitNoteComponent.getTargetHitTimestamp());

        String feedbackText;
        Color feedbackColor;
        int points;

        if (actualTimeDiff <= PERFECT_WINDOW_MS) {
            feedbackText = "PERFECT!";
            feedbackColor = Color.YELLOW;
            points = (10 + geti("combo")) * 2;
            inc("score", points);
            inc("combo", 1);
            audioPlayer.playHitSound();
            double baseX = LANE_X_POSITIONS[laneIndex];
            double markerCenterX = baseX - 95;
            double markerCenterY = HIT_LINE_Y - 100;
            FXGL.spawn("glowEffect", new SpawnData(markerCenterX, markerCenterY));

        } else {
            feedbackText = "NICE!";
            feedbackColor = Color.LIMEGREEN;
            points = 5 + geti("combo");
            inc("score", points);
            inc("combo", 1);
            audioPlayer.playHitSound();
        }

        Point2D noteCenterForText = bestMatch.getCenter();
        gameUI.addFadingText(feedbackText + " +" + points, noteCenterForText.getX(), noteCenterForText.getY(), feedbackColor);
        bestMatch.removeFromWorld();
    }


    public void end() {
        if (!isActive || state == RhythmGameState.GAME_ENDED) return;
        System.out.println("Ending Rhythm Game Session...");
        state = RhythmGameState.GAME_ENDED;
        audioPlayer.stopMusic();
        gameUI.showEndScreen(songFinishedNaturally, geti("score")); // Show end screen UI
        System.out.println("Rhythm Game Ended. Waiting for dialog confirmation.");
    }

    // This method is called by the UI when the user confirms the end screen
    public void finalizeAndReturn() {
        // Check state before finalizing - must be in ENDED state or transitioning from it
        if (state != RhythmGameState.GAME_ENDED) {
            System.err.println("Cannot finalize, game not in GAME_ENDED state. Current state: " + state);
            // Potentially force state or add error handling
            // return; // Or proceed anyway if cleanup is robust
        }
        System.out.println("Finalizing rhythm game and preparing return...");

        // Ensure audio is completely stopped
        if (audioPlayer != null) {
            audioPlayer.stopAll();
            System.out.println("DEBUG: Stopped all audio in finalizeAndReturn");
        }

        cleanupEntities(); // Clean up visual entities
        // gameUI.cleanup(); // UI cleanup is now handled *before* showing the dialog in the UI itself

        isActive = false; // Reset active state
        state = RhythmGameState.READY; // Reset state for the next game session
        songFinishedNaturally = false;
        nextNoteIndexToSpawn = 0;
        beatmap = null; // Release beatmap

        // Call the callback (which returns to menu in GameApp)
        if (onGameEndCallback != null) {
            System.out.println("DEBUG: Calling onGameEndCallback to return to menu.");
            // The dialog service showing the box pauses the game loop.
            // Calling the callback directly will handle the scene transition after the dialog is closed.
            // No need for a timer here because finalizeAndReturn is called by the dialog button action
            onGameEndCallback.run();
        } else {
            System.err.println("onGameEndCallback is null!");
            // Fallback: Directly go to main menu if callback is missing
            getGameController().gotoMainMenu();
        }
    }


    private void spawnNotes() {
        if (beatmap == null) return;
        while (nextNoteIndexToSpawn < beatmap.size()) {
            NoteInfo nextNote = beatmap.get(nextNoteIndexToSpawn);
            long targetHitTime = nextNote.getTimestampMs();
            long spawnTimeMs = targetHitTime - (long) FALL_DURATION_MS;

            if (songElapsedTimeMs >= spawnTimeMs) {
                spawn(RhythmEntityType.RHYTHM_NOTE.toString(), new SpawnData()
                        .put("laneIndex", nextNote.getLaneIndex())
                        .put("targetHitTimestamp", nextNote.getTimestampMs()));
                nextNoteIndexToSpawn++;
            } else {
                break;
            }
        }
    }

    private void checkMisses() {
        List<Entity> notes = getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE);
        List<Entity> missedNotes = new ArrayList<>();

        for (Entity noteEntity : notes) {
            RhythmNoteComponent noteComponent = noteEntity.getComponent(RhythmNoteComponent.class);
            long targetHitTime = noteComponent.getTargetHitTimestamp();

            if (songElapsedTimeMs > targetHitTime + HIT_WINDOW_MS) {
                triggerMissFeedback(noteComponent.getLaneIndex());
                missedNotes.add(noteEntity);
            }
        }
        missedNotes.stream().distinct().forEach(Entity::removeFromWorld);
    }

    private void triggerMissFeedback(int laneIndex) {
        resetCombo();
        audioPlayer.playMissSound();
        gameUI.addFadingText("MISS", LANE_X_POSITIONS[laneIndex], HIT_LINE_Y + NOTE_SIZE / 2.0, Color.GRAY);
    }

    private void resetCombo() {
        if (geti("combo") > 0) {
            System.out.println("Combo Reset!");
        }
        set("combo", 0);
    }

    private void spawnHitZoneMarkers() {
        System.out.println("DEBUG: Spawning Hit Zone Markers.");
        getGameWorld().getEntitiesByType(RhythmEntityType.HIT_ZONE_MARKER).forEach(Entity::removeFromWorld);

        for (int i = 0; i < NUM_LANES; i++) {
            spawn(RhythmEntityType.HIT_ZONE_MARKER.toString(), new SpawnData()
                    .put("laneIndex", i)
                    .put("hitLineY", HIT_LINE_Y));
        }
        System.out.println("DEBUG: Hit Zone Markers spawned.");
    }

    private void cleanupEntities() {
        System.out.println("DEBUG: Cleaning up rhythm game entities.");
        getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByType(RhythmEntityType.HIT_ZONE_MARKER).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByComponent(GlowEffectComponent.class).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByComponent(ParticleComponent.class).forEach(Entity::removeFromWorld);
        if (RhythmEntityType.BACKGROUND != null) {
            getGameWorld().getEntitiesByType(RhythmEntityType.BACKGROUND).forEach(Entity::removeFromWorld);
        } else {
            System.err.println("Cleanup warning: RhythmEntityType.BACKGROUND not defined.");
        }
        System.out.println("DEBUG: Rhythm game entities removed.");
    }
}