package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.particle.ParticleComponent;
import javafx.geometry.Point2D; // Needed for Point2D
import javafx.scene.paint.Color; // Needed for Color
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList; // Needed for ArrayList
import java.util.List; // Needed for List
import java.util.stream.Collectors; // Needed for Collectors

// Correct static imports from FXGL dsl
import static com.almasb.fxgl.dsl.FXGL.*;

// Correct static imports from your RhythmGameFactory
import static org.example.capstonee.RhythmGame.RhythmGameFactory.*;
import static org.example.capstonee.RhythmGame.RhythmGameState.PLAYING; // Keep this import

// Assuming you have this enum defined elsewhere
// import org.example.capstonee.EntityType; // If you use EntityType, but based on new plan maybe not

public class RhythmGameManager {

    // ... (Keep existing fields and constants) ...
    private static final double HIT_LINE_Y = 720 - 100;
    private static final double HIT_WINDOW_MS = 150;
    private static final double PERFECT_WINDOW_MS = HIT_WINDOW_MS / 3.0;
    private static final double FALL_DURATION_MS = (HIT_LINE_Y / RhythmGameFactory.NOTE_SPEED) * 1000; // Make sure NOTE_SPEED is static in RhythmGameFactory
    private static final int NUM_LANES = RhythmGameFactory.LANE_X_POSITIONS.length; // Make sure LANE_X_POSITIONS is static in RhythmGameFactory
    private static final double NOTE_SIZE = RhythmGameFactory.NOTE_SIZE; // Make sure NOTE_SIZE is static in RhythmGameFactory


    private boolean isActive = false;
    private RhythmGameState state = RhythmGameState.READY;
    private long gameStartTime = 0;
    private long songElapsedTimeMs = 0;
    private int nextNoteIndexToSpawn = 0;
    private boolean songFinishedNaturally = false;

    private final RhythmAudioPlayer audioPlayer;
    private final RhythmGameUI gameUI;
    private final GameScene gameScene;
    private List<NoteInfo> beatmap;
    private Runnable onGameEndCallback;

    public RhythmGameManager(GameScene gameScene, RhythmGameUI gameUI, RhythmAudioPlayer audioPlayer) {
        this.gameScene = gameScene;
        this.gameUI = gameUI;
        this.audioPlayer = audioPlayer;
    }

    // Keep these methods as they were
    public boolean isActive() { return isActive; }
    public RhythmGameState getState() { return state; }
    public void setOnGameEndCallback(Runnable onGameEndCallback) { this.onGameEndCallback = onGameEndCallback; }


    // Start method, adjusted to accept beatmap path and load the beatmap
    public void start(String beatmapPath) throws IOException {
        if (isActive) return;
        System.out.println("Starting Rhythm Game setup for beatmap: " + beatmapPath);
        isActive = true;
        state = RhythmGameState.READY; // Start in READY state, wait for SPACE
        songFinishedNaturally = false;

        // --- Viewport configuration moved to GameApp.startRhythmGameInstance (gotoGame handles it) ---
        // Viewport viewport = getGameScene().getViewport();
        // viewport.setZoom(1.0);
        // viewport.setX(0);
        // viewport.setY(0);
        // viewport.unbind();
        // --- End Viewport configuration ---

        cleanupEntities(); // Clean up entities from previous game or menu

        // Spawn the rhythm background
        spawn("rhythmBackground");

        // Load the beatmap
        beatmap = BeatmapLoader.loadBeatmapFile("assets/" + beatmapPath); // Assuming path is relative to assets/
        if (beatmap == null || beatmap.isEmpty()) {
            System.err.println("Error: Beatmap is empty or failed to load from " + beatmapPath + ". Aborting rhythm game start.");
            isActive = false;
            state = RhythmGameState.GAME_ENDED; // Indicate failure state
            gameUI.cleanup(); // Clean UI if setup fails
            if (onGameEndCallback != null) {
                getGameTimer().runOnceAfter(onGameEndCallback, Duration.seconds(0.1)); // Return to menu after brief delay
            }
            return;
        }

        nextNoteIndexToSpawn = 0;
        // Music is loaded in GameApp.startSelectedSong before this method is called

        set("score", 0);
        set("combo", 0);
        set("songElapsedTimeMs", 0L);

        gameUI.setup();
        gameUI.showReadyScreen(); // Show "Ready" UI
        spawnHitZoneMarkers(); // <-- This is the method call that was reported as unresolved

        System.out.println("Rhythm Game Ready. Press SPACE to start.");
    }

    // startPlaying() method - called when user presses SPACE in READY state
    public void startPlaying() {
        if (!isActive || state != RhythmGameState.READY) return;
        System.out.println("Starting Rhythm Game Play...");
        state = PLAYING; // Change state to PLAYING
        gameUI.showPlayingUI(); // Show playing UI (e.g., score, combo)

        // Play the music (loaded in GameApp.startSelectedSong)
        audioPlayer.playMusic();

        gameStartTime = System.currentTimeMillis();
        songElapsedTimeMs = 0;
        System.out.println("DEBUG: gameStartTime captured as: " + gameStartTime);
    }

    // update() method - called by GameApp's onUpdate loop when isActive is true
    public void update(double tpf) {
        if (!isActive || state != PLAYING) return;

        // Update song time based on system time for sync
        songElapsedTimeMs = System.currentTimeMillis() - gameStartTime;
        set("songElapsedTimeMs", songElapsedTimeMs);

        spawnNotes();
        checkMisses();

        // Check if song is finished *and* all notes are gone
        if (nextNoteIndexToSpawn >= beatmap.size() && getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE).isEmpty()) {
            if (!songFinishedNaturally) {
                songFinishedNaturally = true;
                System.out.println("Song finished naturally. Ending game soon.");
                getGameTimer().runOnceAfter(this::end, Duration.seconds(0.5)); // Small delay before showing score
            }
        }
    }

    // handleInput() method - called by GameApp's input actions
    public void handleInput(int laneIndex) {
        if (!isActive || state != PLAYING) return; // Only handle input if game is PLAYING

        // ... (rest of your handleInput logic remains the same) ...
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
        // ... (rest of handleInput) ...
    }


    // end() method - called when the song finishes naturally or game ends
    public void end() {
        if (!isActive || state == RhythmGameState.GAME_ENDED) return;
        System.out.println("Ending Rhythm Game Session...");
        state = RhythmGameState.GAME_ENDED; // Set state to ENDED
        audioPlayer.stopMusic(); // Stop the music
        // cleanupEntities(); // Cleanup handled in finalizeAndReturn
        gameUI.showEndScreen(songFinishedNaturally, geti("score")); // Show end screen UI
        System.out.println("Rhythm Game Ended. Waiting for finalization.");
    }

    // finalizeAndReturn() method - called when user confirms on the end screen
    public void finalizeAndReturn() {
        // Check state before finalizing - must be in ENDED state
        if (state != RhythmGameState.GAME_ENDED) {
            System.err.println("Cannot finalize, game not in GAME_ENDED state. Current state: " + state);
            return;
        }
        System.out.println("Finalizing rhythm game and preparing return...");

        // Ensure audio is completely stopped
        if (audioPlayer != null) {
            audioPlayer.stopAll();
            System.out.println("DEBUG: Stopped all audio in finalizeAndReturn");
        }

        cleanupEntities(); // Clean up visual entities
        gameUI.cleanup(); // Clean up UI

        isActive = false; // Reset active state
        state = RhythmGameState.READY; // Reset state
        songFinishedNaturally = false;
        nextNoteIndexToSpawn = 0;
        beatmap = null; // Release beatmap

        // Call the callback (which returns to menu in GameApp)
        if (onGameEndCallback != null) {
            System.out.println("DEBUG: Calling onGameEndCallback to return to menu.");
            // Add a short delay before returning to menu so the end screen is visible
            getGameTimer().runOnceAfter(onGameEndCallback, Duration.seconds(2.0)); // Increased delay slightly
        } else {
            System.err.println("onGameEndCallback is null!");
        }
    }


    // spawnNotes() method - spawns notes based on elapsed time
    private void spawnNotes() {
        if (beatmap == null) return;
        while (nextNoteIndexToSpawn < beatmap.size()) {
            NoteInfo nextNote = beatmap.get(nextNoteIndexToSpawn);
            long targetHitTime = nextNote.getTimestampMs();
            long spawnTimeMs = targetHitTime - (long) FALL_DURATION_MS;

            if (songElapsedTimeMs >= spawnTimeMs) {
                // Position is set by factory based on lane, pass target time
                spawn(RhythmEntityType.RHYTHM_NOTE.toString(), new SpawnData()
                        .put("laneIndex", nextNote.getLaneIndex())
                        .put("targetHitTimestamp", nextNote.getTimestampMs()));
                nextNoteIndexToSpawn++;
            } else {
                break; // Notes are sorted by time
            }
        }
    }

    // checkMisses() method - removes notes that pass the hit line without being hit
    private void checkMisses() {
        List<Entity> notes = getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE);
        List<Entity> missedNotes = new ArrayList<>();

        for (Entity noteEntity : notes) {
            RhythmNoteComponent noteComponent = noteEntity.getComponent(RhythmNoteComponent.class);
            long targetHitTime = noteComponent.getTargetHitTimestamp();

            // Check if current time is past hit time plus window
            if (songElapsedTimeMs > targetHitTime + HIT_WINDOW_MS) {
                triggerMissFeedback(noteComponent.getLaneIndex());
                missedNotes.add(noteEntity);
            }
            // Optional: Remove notes that are far off screen
            if (noteEntity.getY() > getAppHeight() + NOTE_SIZE) {
                // triggerMissFeedback(noteComponent.getLaneIndex()); // Or count off-screen as miss
                // missedNotes.add(noteEntity); // Mark for removal
            }
        }
        missedNotes.stream().distinct().forEach(Entity::removeFromWorld);
    }

    // triggerMissFeedback() method - handles combo reset and miss sound/text
    private void triggerMissFeedback(int laneIndex) {
        resetCombo();
        audioPlayer.playMissSound();
        gameUI.addFadingText("MISS", LANE_X_POSITIONS[laneIndex], HIT_LINE_Y + NOTE_SIZE / 2.0, Color.GRAY);
    }

    // resetCombo() method
    private void resetCombo() {
        if (geti("combo") > 0) {
            System.out.println("Combo Reset!");
        }
        set("combo", 0);
        // gameUI.showComboBreakText(); // Example if you have UI for this
    }

    // spawnHitZoneMarkers() method - spawns the visual markers for hit zones
    // THIS METHOD IS CALLED IN start() AND SHOULD BE DEFINED IN THIS CLASS
    private void spawnHitZoneMarkers() {
        System.out.println("DEBUG: Spawning Hit Zone Markers.");
        // Clear existing markers before spawning new ones
        getGameWorld().getEntitiesByType(RhythmEntityType.HIT_ZONE_MARKER).forEach(Entity::removeFromWorld);

        for (int i = 0; i < NUM_LANES; i++) {
            // Use spawn data to pass info to factory
            spawn(RhythmEntityType.HIT_ZONE_MARKER.toString(), new SpawnData()
                    .put("laneIndex", i)
                    .put("hitLineY", HIT_LINE_Y));
        }
        System.out.println("DEBUG: Hit Zone Markers spawned.");
    }

    // cleanupEntities() method - removes rhythm game specific entities
    private void cleanupEntities() {
        System.out.println("DEBUG: Cleaning up rhythm game entities.");
        getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByType(RhythmEntityType.HIT_ZONE_MARKER).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByComponent(GlowEffectComponent.class).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByComponent(ParticleComponent.class).forEach(Entity::removeFromWorld);
        // Ensure background is also removed
        if (RhythmEntityType.BACKGROUND != null) { // Check enum exists
            getGameWorld().getEntitiesByType(RhythmEntityType.BACKGROUND).forEach(Entity::removeFromWorld);
        } else {
            System.err.println("Cleanup warning: RhythmEntityType.BACKGROUND not defined.");
        }
        System.out.println("DEBUG: Rhythm game entities removed.");
    }
}