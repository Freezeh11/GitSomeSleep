package org.example.capstonee.RhythmGame;

// ... other imports ...
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

public class RhythmGameManager {

    // ... (Keep existing fields and methods like HIT_LINE_Y, PERFECT_WINDOW_MS, etc.) ...
    private static final double HIT_LINE_Y = 720 - 100;
    private static final double HIT_WINDOW_MS = 150;
    private static final double PERFECT_WINDOW_MS = HIT_WINDOW_MS / 3.0;
    private static final double FALL_DURATION_MS = (HIT_LINE_Y / RhythmGameFactory.NOTE_SPEED) * 1000;
    private static final int NUM_LANES = RhythmGameFactory.LANE_X_POSITIONS.length;

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

    // ... methods like isActive, getState, setOnGameEndCallback, start, startPlaying, update ...
    public boolean isActive() {
        return isActive;
    }

    public RhythmGameState getState() {
        return state;
    }

    public void setOnGameEndCallback(Runnable onGameEndCallback) {
        this.onGameEndCallback = onGameEndCallback;
    }


    public void start(String beat) throws IOException {
        if (isActive) return;
        System.out.println("Starting Rhythm Game...");
        isActive = true;
        state = RhythmGameState.READY;
        songFinishedNaturally = false;
        Viewport viewport = getGameScene().getViewport(); // Use getter if scene is needed
        viewport.setZoom(1.0);
        viewport.setX(0);
        viewport.setY(0);
        viewport.unbind();

        Entity player = geto("player");
        if (player != null) player.setVisible(false);
        getGameWorld().getEntitiesCopy().stream()
                .filter(e -> e != player && e.getType() != RhythmEntityType.HIT_ZONE_MARKER && e.getType() != RhythmEntityType.RHYTHM_NOTE) // Keep rhythm entities if any exist
                .forEach(e -> e.setVisible(false));

        cleanupEntities(); // Clean up any leftover rhythm entities first

        // *** Spawn the background using the factory ***
        spawn("rhythmBackground"); // Make sure this spawner exists in RhythmGameFactory with low Z-index

        // Use the 'beat' parameter if intended, otherwise keep hardcoded path
        // beatmap = BeatmapLoader.loadBeatmapFile(beat); // Example if 'beat' is the path
        beatmap = BeatmapLoader.loadBeatmapFile("assets/music/songfile/thirdbosssong.txt"); // Or keep this
        if (beatmap == null || beatmap.isEmpty()) {
            System.err.println("Error: Beatmap is empty or failed to load. Aborting rhythm game start.");
            gameUI.cleanup();
            isActive = false;
            if (onGameEndCallback != null) {
                onGameEndCallback.run();
            }
            return;
        }
        nextNoteIndexToSpawn = 0;
        audioPlayer.loadMusic(); // Consider loading based on beatmap info
        set("score", 0);
        set("combo", 0);
        set("songElapsedTimeMs", 0L);
        gameUI.setup();
        gameUI.showReadyScreen();
        spawnHitZoneMarkers();
        System.out.println("Rhythm Game Ready.");
    }

    public void startPlaying() {
        if (!isActive || state != RhythmGameState.READY) return;
        System.out.println("Starting Rhythm Game Play...");
        state = RhythmGameState.PLAYING;
        gameUI.showPlayingUI();
        audioPlayer.playMusic();
        gameStartTime = System.currentTimeMillis();
        songElapsedTimeMs = 0;
        // System.out.println("DEBUG: gameStartTime captured as: " + gameStartTime);
    }

    public void update(double tpf) {
        if (!isActive || state != RhythmGameState.PLAYING) return;
        // It's generally better practice to increment time using tpf for frame-rate independence,
        // but System.currentTimeMillis() is often used in rhythm games for audio sync. Be consistent.
        songElapsedTimeMs = System.currentTimeMillis() - gameStartTime;
        set("songElapsedTimeMs", songElapsedTimeMs);

        spawnNotes();
        checkMisses();

        // Check if song is finished *and* all notes are gone
        if (nextNoteIndexToSpawn >= beatmap.size() && getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE).isEmpty()) {
            // Add a small delay before ending to let the last hit sound/effect play out?
            if (!songFinishedNaturally) { // Prevent multiple calls if already flagged
                // System.out.println("Song finished naturally.");
                songFinishedNaturally = true;
                // Optional: Add a short delay before triggering end screen
                getGameTimer().runOnceAfter(this::end, Duration.seconds(0.5)); // Small delay
                // end(); // Or call end directly
            }
        }
        // Optional: Check if audio player has finished playing if that's the definitive end trigger
        // if (audioPlayer.isMusicFinished() && !songFinishedNaturally) { ... }
    }



    public void handleInput(int laneIndex) {
        if (!isActive || state != RhythmGameState.PLAYING) return;

        List<Entity> hittableNotes = getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE)
                .stream()
                .filter(note -> {
                    RhythmNoteComponent noteComp = note.getComponent(RhythmNoteComponent.class);
                    if (noteComp.getLaneIndex() != laneIndex) return false;
                    long timeDiff = songElapsedTimeMs - noteComp.getTargetHitTimestamp();
                    // Check timing first - must be within the hittable window
                    if (Math.abs(timeDiff) > HIT_WINDOW_MS) return false;
                    // Optional stricter position check (note visually near line) - adjust as needed
                    // double noteBottomY = note.getBottomY();
                    // double noteTopY = note.getY();
                    // return (noteBottomY > HIT_LINE_Y - NOTE_SIZE * 1.0) && (noteTopY < HIT_LINE_Y + NOTE_SIZE * 0.5);
                    return true; // Rely primarily on timing window
                })
                // Sort by proximity to the exact hit time (absolute difference)
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

        // The best match is the first one in the sorted list
        Entity bestMatch = hittableNotes.get(0);
        RhythmNoteComponent hitNoteComponent = bestMatch.getComponent(RhythmNoteComponent.class);
        long actualTimeDiff = Math.abs(songElapsedTimeMs - hitNoteComponent.getTargetHitTimestamp());

        // Successful Hit
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

            // *** Spawn the glow effect at the HIT ZONE MARKER position ***
            double baseX = LANE_X_POSITIONS[laneIndex];
            double markerCenterX = baseX - 95; // Center X of the lane
            double markerCenterY = HIT_LINE_Y - 100; // Center Y of the hit line itself
            // SpawnData now uses the marker's center coordinates
            FXGL.spawn("glowEffect", new SpawnData(markerCenterX, markerCenterY));

        } else { // Within HIT_WINDOW_MS but outside PERFECT_WINDOW_MS
            feedbackText = "NICE!";
            feedbackColor = Color.LIMEGREEN;
            points = 5 + geti("combo");
            inc("score", points);
            inc("combo", 1);
            audioPlayer.playHitSound();
            // No glow effect for NICE hits in this version
        }

        // Use the hit note's center for the floating text position
        Point2D noteCenterForText = bestMatch.getCenter();
        gameUI.addFadingText(feedbackText + " +" + points, noteCenterForText.getX(), noteCenterForText.getY(), feedbackColor);
        bestMatch.removeFromWorld(); // Remove the successfully hit note

    }

    // ... (triggerMissFeedback, end, finalizeAndReturn, spawnNotes, checkMisses, resetCombo, spawnHitZoneMarkers) ...
    private void triggerMissFeedback(int laneIndex) {
        resetCombo();
        audioPlayer.playMissSound();
        // Position miss text near the hit line for the specific lane
        gameUI.addFadingText("MISS", LANE_X_POSITIONS[laneIndex], HIT_LINE_Y + NOTE_SIZE / 2.0, Color.GRAY);
    }


    // REMOVED spawnPerfectSparks method

    // REMOVED the manual spawnGlowEffect method


    public void end() {
        if (!isActive || state == RhythmGameState.GAME_ENDED) return; // Prevent multiple calls
        System.out.println("Ending Rhythm Game Session...");
        state = RhythmGameState.GAME_ENDED;
        audioPlayer.stopMusic();
        // Clear any remaining notes immediately when ending
        cleanupEntities();
        gameUI.showEndScreen(songFinishedNaturally, geti("score"));
    }

    public void finalizeAndReturn() {
        // Only allow finalizing from the ended state
        if (state != RhythmGameState.GAME_ENDED) {
            System.out.println("Cannot finalize, game not in GAME_ENDED state.");
            return;
        }
        System.out.println("Finalizing rhythm game and preparing return...");

        // Ensure cleanup happens before setting isActive to false
        cleanupEntities(); // Double check cleanup
        gameUI.cleanup();
        //audioPlayer.cleanup(); // Add cleanup for audio player if needed

        isActive = false;
        state = RhythmGameState.READY; // Reset state for potential restart
        songFinishedNaturally = false;
        nextNoteIndexToSpawn = 0; // Reset beatmap progress
        beatmap = null; // Clear beatmap data

        // Restore non-rhythm game elements visibility
        Entity player = geto("player");
        if (player != null) player.setVisible(true);
        getGameWorld().getEntitiesCopy().stream()
                .filter(e -> e.getType() != RhythmEntityType.RHYTHM_NOTE
                                && e.getType() != RhythmEntityType.HIT_ZONE_MARKER
                                && e.getType() != RhythmEntityType.BACKGROUND // Also don't show rhythm background again
                        // Add other rhythm-specific types if any
                )
                .forEach(e -> e.setVisible(true)); // Make others visible again

        // Reset viewport if necessary (depends on what the callback does)
        // Viewport viewport = getGameScene().getViewport();
        // viewport.bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0); // Example re-binding


        if (onGameEndCallback != null) {
            System.out.println("Executing onGameEndCallback...");
            onGameEndCallback.run();
        } else {
            System.err.println("Warning: onGameEndCallback is null in RhythmGameManager. Cannot trigger return sequence.");
        }
    }


    private void spawnNotes() {
        if (beatmap == null) return; // Safety check
        while (nextNoteIndexToSpawn < beatmap.size()) {
            NoteInfo nextNote = beatmap.get(nextNoteIndexToSpawn);
            long targetHitTime = nextNote.getTimestampMs();
            // Calculate the time relative to the song start when this note *should* enter the screen
            long spawnTimeMs = targetHitTime - (long) FALL_DURATION_MS;

            // Check if the current song time has reached or passed the calculated spawn time
            if (songElapsedTimeMs >= spawnTimeMs) {
                // System.out.println("DEBUG: Spawning Note #" + nextNoteIndexToSpawn + " (Target: " + targetHitTime + "ms, Lane: " + nextNote.getLaneIndex() + ") at song time " + songElapsedTimeMs + "ms");
                spawn(RhythmEntityType.RHYTHM_NOTE.toString(), new SpawnData() // Position is set by factory based on lane
                        .put("laneIndex", nextNote.getLaneIndex())
                        .put("targetHitTimestamp", nextNote.getTimestampMs())); // Pass target time
                nextNoteIndexToSpawn++;
            } else {
                // Notes in the beatmap are usually sorted by time.
                // If the current note isn't ready to be spawned, subsequent notes won't be either.
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

            // A note is considered missed if the current time is significantly past its hit time + allowed window
            if (songElapsedTimeMs > targetHitTime + HIT_WINDOW_MS) {
                // System.out.println("Note missed! Lane: " + noteComponent.getLaneIndex() + " Target Time: " + targetHitTime + " Current Time: " + songElapsedTimeMs);
                triggerMissFeedback(noteComponent.getLaneIndex()); // Use the helper for feedback
                missedNotes.add(noteEntity); // Mark for removal
            }
            // Optional: Also remove notes that have fallen way off screen, even if before the miss time calculation,
            if (noteEntity.getY() > getAppHeight() + NOTE_SIZE) { // If note is fully below screen
                // Silently remove without penalty? Or trigger miss? Decide game logic.
                // triggerMissFeedback(noteComponent.getLaneIndex()); // Uncomment if off-screen counts as miss
                missedNotes.add(noteEntity);
            }
        }

        // Remove all missed notes from the game world
        // Use distinct to avoid trying to remove the same note twice if multiple conditions met
        missedNotes.stream().distinct().forEach(Entity::removeFromWorld);
    }



    private void resetCombo() {
        if (geti("combo") > 0) {
            // System.out.println("Combo Reset!"); // Optional debug/gameplay message
        }
        set("combo", 0);
    }

    private void spawnHitZoneMarkers() {
        // Clear existing markers before spawning new ones (important if restarting)
        getGameWorld().getEntitiesByType(RhythmEntityType.HIT_ZONE_MARKER).forEach(Entity::removeFromWorld);

        for (int i = 0; i < NUM_LANES; i++) {
            spawn(RhythmEntityType.HIT_ZONE_MARKER.toString(), new SpawnData() // Position set in factory
                    .put("laneIndex", i)
                    .put("hitLineY", HIT_LINE_Y)); // Pass the Y coord for positioning
        }
    }


    private void cleanupEntities() {
        // Remove all active rhythm game specific entities
        getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByType(RhythmEntityType.HIT_ZONE_MARKER).forEach(Entity::removeFromWorld);
        // Also remove any active effects like glow or sparks
        getGameWorld().getEntitiesByComponent(GlowEffectComponent.class).forEach(Entity::removeFromWorld);
        // Remove sparks if they use ParticleComponent and maybe a specific name/type
        getGameWorld().getEntitiesByComponent(ParticleComponent.class).forEach(Entity::removeFromWorld); // Removes all particle entities
        // Remove background
        // *** Make sure BACKGROUND type is defined in RhythmEntityType enum ***
        if (RhythmEntityType.BACKGROUND != null) {
            getGameWorld().getEntitiesByType(RhythmEntityType.BACKGROUND).forEach(Entity::removeFromWorld);
        } else {
            System.err.println("Cleanup warning: RhythmEntityType.BACKGROUND not defined.");
        }

        // *** FIX: Remove the duplicate line ***
        // getGameWorld().getEntitiesByComponent(GlowEffectComponent.class).forEach(Entity::removeFromWorld); // DELETE THIS LINE
    }
}