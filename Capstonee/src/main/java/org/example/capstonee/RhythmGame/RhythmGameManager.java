package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.app.scene.GameScene;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.almasb.fxgl.dsl.FXGL.*;
import static org.example.capstonee.RhythmGame.RhythmGameFactory.*;

public class RhythmGameManager {


    private static final double HIT_LINE_Y = 720 - 100;
    private static final double HIT_WINDOW_MS = 150;
    private static final double PERFECT_WINDOW_MS = HIT_WINDOW_MS / 3.0;
    private static final double FALL_DURATION_MS = (HIT_LINE_Y / NOTE_SPEED) * 1000;
    private static final int NUM_LANES = LANE_X_POSITIONS.length;


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

    public boolean isActive() {
        return isActive;
    }

    public RhythmGameState getState() {
        return state;
    }

    public void setOnGameEndCallback(Runnable onGameEndCallback) {
        this.onGameEndCallback = onGameEndCallback;
    }


    public void start() {
        if (isActive) return;

        System.out.println("Starting Rhythm Game...");
        isActive = true;
        state = RhythmGameState.READY;
        songFinishedNaturally = false;



        Viewport viewport = gameScene.getViewport();
        viewport.setZoom(1.0);
        viewport.setX(0);
        viewport.setY(0);
        viewport.unbind();


        Entity player = geto("player");
        if (player != null) player.setVisible(false);
        getGameWorld().getEntitiesCopy().stream()
                .filter(e -> e != player && e.getType() != RhythmEntityType.HIT_ZONE_MARKER && e.getType() != RhythmEntityType.RHYTHM_NOTE) // Keep rhythm entities if any exist
                .forEach(e -> e.setVisible(false));



        cleanupEntities();


        // TODO: replace ang "beatmaps/sample_song.beatmap" with em accurate one
        beatmap = BeatmapLoader.loadBeatmap("beatmaps/sample_song.beatmap");
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

        audioPlayer.loadMusic();

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
        System.out.println("DEBUG: gameStartTime captured as: " + gameStartTime);
    }

    public void update(double tpf) {
        if (!isActive || state != RhythmGameState.PLAYING) return;

        songElapsedTimeMs = System.currentTimeMillis() - gameStartTime;
        set("songElapsedTimeMs", songElapsedTimeMs);


        spawnNotes();


        checkMisses();

        if (nextNoteIndexToSpawn >= beatmap.size() && getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE).isEmpty()) {

            System.out.println("Song finished naturally.");
            songFinishedNaturally = true;
            end();
        }
    }


    public void handleInput(int laneIndex) {
        if (!isActive || state != RhythmGameState.PLAYING) return;


        List<Entity> notesInLane = getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE)
                .stream()
                .filter(note -> note.getComponent(RhythmNoteComponent.class).getLaneIndex() == laneIndex)
                .collect(Collectors.toList());

        Entity bestMatch = null;
        long closestTimeDiff = Long.MAX_VALUE;

        for (Entity note : notesInLane) {
            RhythmNoteComponent noteComponent = note.getComponent(RhythmNoteComponent.class);
            long targetHitTime = noteComponent.getTargetHitTimestamp();
            long timeDiff = songElapsedTimeMs - targetHitTime; // positive = late, negative = early

             if (Math.abs(timeDiff) <= HIT_WINDOW_MS && note.getY() >= HIT_LINE_Y - NOTE_SIZE * 1.5) {
                if (Math.abs(timeDiff) < closestTimeDiff) {
                    closestTimeDiff = Math.abs(timeDiff);
                    bestMatch = note;
                }
            }
        }

        if (bestMatch != null) {
            RhythmNoteComponent hitNoteComponent = bestMatch.getComponent(RhythmNoteComponent.class);
            long timeDiff = Math.abs(songElapsedTimeMs - hitNoteComponent.getTargetHitTimestamp());

            String feedbackText;
            Color feedbackColor;
            int points;

            if (timeDiff <= PERFECT_WINDOW_MS) {
                feedbackText = "PERFECT!";
                feedbackColor = Color.YELLOW;
                points = (10 + geti("combo")) * 2;
                inc("score", points);
                inc("combo", 1);
                audioPlayer.playHitSound();
            } else {
                feedbackText = "NICE!";
                feedbackColor = Color.LIMEGREEN;
                points = 5 + geti("combo");
                inc("score", points);
                inc("combo", 1);
                audioPlayer.playHitSound();
            }


            Point2D noteCenter = bestMatch.getCenter();
            gameUI.addFadingText(feedbackText + " +" + points, noteCenter.getX(), noteCenter.getY(), feedbackColor);
            bestMatch.removeFromWorld();

        } else {
            resetCombo();
            audioPlayer.playMissSound();
            gameUI.addFadingText("MISS", LANE_X_POSITIONS[laneIndex], HIT_LINE_Y + NOTE_SIZE / 2.0, Color.GRAY);
        }
    }


    public void end() {
        if (!isActive) return;

        System.out.println("Ending Rhythm Game Session...");
        state = RhythmGameState.GAME_ENDED;

        audioPlayer.stopMusic();
        gameUI.showEndScreen(songFinishedNaturally, geti("score"));
    }

    public void finalizeAndReturn() {
        if (!isActive || state != RhythmGameState.GAME_ENDED) return;

        System.out.println("Finalizing rhythm game and preparing return...");

        cleanupEntities();
        gameUI.cleanup();

        isActive = false;
        state = RhythmGameState.READY;
        songFinishedNaturally = false;

        if (onGameEndCallback != null) {
            onGameEndCallback.run();
        } else {
            System.err.println("Warning: onGameEndCallback is null in RhythmGameManager. Cannot trigger return sequence.");
        }
    }



    private void spawnNotes() {
        while (nextNoteIndexToSpawn < beatmap.size()) {
            NoteInfo nextNote = beatmap.get(nextNoteIndexToSpawn);
            long spawnTimeMs = nextNote.getTimestampMs() - (long) FALL_DURATION_MS;

            if (songElapsedTimeMs >= spawnTimeMs) {
                spawn(RhythmEntityType.RHYTHM_NOTE.toString(), new SpawnData(0, 0) // Position is set by factory/component
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
                System.out.println("Note missed! Lane: " + noteComponent.getLaneIndex() + " Target Time: " + targetHitTime + " Current Time: " + songElapsedTimeMs);
                resetCombo();
                audioPlayer.playMissSound();
                missedNotes.add(noteEntity);
                gameUI.addFadingText("MISS", LANE_X_POSITIONS[noteComponent.getLaneIndex()], HIT_LINE_Y + NOTE_SIZE/2.0, Color.RED);
            }
        }
        missedNotes.forEach(Entity::removeFromWorld);
    }

    private void resetCombo() {
        if (geti("combo") > 0) {
            System.out.println("Combo Reset!");
        }
        set("combo", 0);
    }

    private void spawnHitZoneMarkers() {
        getGameWorld().getEntitiesByType(RhythmEntityType.HIT_ZONE_MARKER).forEach(Entity::removeFromWorld);

        for (int i = 0; i < NUM_LANES; i++) {
            spawn(RhythmEntityType.HIT_ZONE_MARKER.toString(), new SpawnData(0, 0) // Position set in factory
                    .put("laneIndex", i)
                    .put("hitLineY", HIT_LINE_Y));
        }
    }

    private void cleanupEntities() {
        getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByType(RhythmEntityType.HIT_ZONE_MARKER).forEach(Entity::removeFromWorld);
    }
}