package org.example.capstonee;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.ui.FXGLTextFlow;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings; // Keep Bindings for text property, but not position
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.geometry.Pos;
import org.example.capstonee.RhythmGame.RhythmEntityType;
import org.example.capstonee.RhythmGame.RhythmGameFactory;
import org.example.capstonee.RhythmGame.RhythmNoteComponent;
import org.example.capstonee.RhythmGame.RhythmAudioPlayer;


import static org.example.capstonee.RhythmGame.RhythmGameFactory.NOTE_SIZE;
import static org.example.capstonee.RhythmGame.RhythmGameFactory.NOTE_SPEED;
import static org.example.capstonee.RhythmGame.RhythmGameFactory.LANE_X_POSITIONS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.almasb.fxgl.dsl.FXGL.*;


enum RhythmGameState {
    READY, PLAYING, GAME_ENDED
}

public class GameApp extends GameApplication {


    private static final int NUM_LANES = LANE_X_POSITIONS.length;

    private static final double HIT_LINE_Y = 720 - 100;
    private static final double HIT_WINDOW_MS = 150;


    private static final double FALL_DURATION_MS = (HIT_LINE_Y / NOTE_SPEED) * 1000;


    private Entity player;
    private boolean rhythmGameActive = false;
    private RhythmGameState rhythmGameState = RhythmGameState.READY;



    private RhythmAudioPlayer rhythmAudioPlayer;
    private long rhythmGameStartTime = 0;
    private long songElapsedTimeMs = 0;

    private List<NoteInfo> beatmap;
    private int nextNoteIndexToSpawn = 0;


    private Text scoreText;
    private Text comboText;
    private List<Text> fadingTexts = new ArrayList<>();
    private VBox messageBox;
    private Text messageTopLineText;
    private Text messageBottomLineText;


    private static class NoteInfo {
        long timestampMs;
        int laneIndex;

        NoteInfo(long timestampMs, int laneIndex) {
            this.timestampMs = timestampMs;
            this.laneIndex = laneIndex;
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        if (rhythmGameActive && rhythmGameState == RhythmGameState.PLAYING) {
            songElapsedTimeMs = System.currentTimeMillis() - rhythmGameStartTime;
            set("songElapsedTimeMs", songElapsedTimeMs);

            while (nextNoteIndexToSpawn < beatmap.size()) {
                NoteInfo nextNote = beatmap.get(nextNoteIndexToSpawn);
                long spawnTimeMs = nextNote.timestampMs - (long)FALL_DURATION_MS;

                if (songElapsedTimeMs >= spawnTimeMs) {
                    System.out.println("Spawning note at song time: " + songElapsedTimeMs + " ms. Target hit: " + nextNote.timestampMs + " ms");
                    spawn(RhythmEntityType.RHYTHM_NOTE.toString(), new SpawnData(0, 0)
                            .put("laneIndex", nextNote.laneIndex)
                            .put("targetHitTimestamp", nextNote.timestampMs));
                    nextNoteIndexToSpawn++;
                } else {
                    break;
                }
            }


            List<Entity> notes = getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE);
            List<Entity> missedNotes = new ArrayList<>();

            for (Entity noteEntity : notes) {
                RhythmNoteComponent noteComponent = noteEntity.getComponent(RhythmNoteComponent.class);
                long targetHitTime = noteComponent.getTargetHitTimestamp();

                if (songElapsedTimeMs > targetHitTime + HIT_WINDOW_MS) {
                    System.out.println("Note missed! Lane: " + noteComponent.getLaneIndex() + " Target Time: " + targetHitTime + " vs Current: " + songElapsedTimeMs);
                    resetCombo();
                    rhythmAudioPlayer.playMissSound();
                    missedNotes.add(noteEntity);
                    addFadingText("MISS", LANE_X_POSITIONS[noteComponent.getLaneIndex()], HIT_LINE_Y, Color.RED);
                }
            }
            missedNotes.forEach(Entity::removeFromWorld);
        }
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Game App");
        settings.setVersion("0.1");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setSceneFactory(new SceneFactory());
        settings.setApplicationMode(ApplicationMode.DEVELOPER); // Keep this if needed for debug tools
        // Use the 'settings' object passed as an argument
        }

    @Override
    protected void initInput() {

        getInput().addAction(new UserAction("Left") {
            @Override protected void onAction() { if (!rhythmGameActive) player.getComponent(PlayerComponent.class).left(); }
            @Override protected void onActionEnd() { if (!rhythmGameActive) player.getComponent(PlayerComponent.class).stop(); }
        }, KeyCode.A, VirtualButton.LEFT);
        getInput().addAction(new UserAction("Right") {
            @Override protected void onAction() { if (!rhythmGameActive) player.getComponent(PlayerComponent.class).right(); }
            @Override protected void onActionEnd() { if (!rhythmGameActive) player.getComponent(PlayerComponent.class).stop(); }
        }, KeyCode.D, VirtualButton.RIGHT);
        getInput().addAction(new UserAction("Jump") {
            @Override protected void onActionBegin() { if (!rhythmGameActive) player.getComponent(PlayerComponent.class).jump(); }
        }, KeyCode.W, VirtualButton.A);

         getInput().addAction(new UserAction("Interact") {
            @Override
            protected void onActionBegin() {
                if (!rhythmGameActive) {
                    var interactionZones = getGameWorld().getEntitiesByType(EntityType.INTERACTION_ZONE);
                    for (Entity zone : interactionZones) {
                        if (player.isColliding(zone)) {
                            Entity npc = zone.getComponent(InteractionZoneComponent.class).getNpc();
                            String dialog = npc.getComponent(NPCComponent.class).getDialog();
                            System.out.println("NPC says: " + dialog);
                           startRhythmGame();
                            break;
                        }
                    }
                } else if (rhythmGameActive && rhythmGameState == RhythmGameState.GAME_ENDED) {
                    endRhythmGame(false);
                }
            }
        }, KeyCode.E);


        getInput().addAction(new UserAction("RhythmLane0") {
            @Override protected void onActionBegin() { if (rhythmGameActive && rhythmGameState == RhythmGameState.PLAYING) handleRhythmInput(0); }
        }, KeyCode.F);
        getInput().addAction(new UserAction("RhythmLane1") {
            @Override protected void onActionBegin() { if (rhythmGameActive && rhythmGameState == RhythmGameState.PLAYING) handleRhythmInput(1); }
        }, KeyCode.G);
        getInput().addAction(new UserAction("RhythmLane2") {
            @Override protected void onActionBegin() { if (rhythmGameActive && rhythmGameState == RhythmGameState.PLAYING) handleRhythmInput(2); }
        }, KeyCode.H);
        getInput().addAction(new UserAction("RhythmLane3") {
            @Override protected void onActionBegin() { if (rhythmGameActive && rhythmGameState == RhythmGameState.PLAYING) handleRhythmInput(3); }
        }, KeyCode.J);

        getInput().addAction(new UserAction("RhythmStart") {
            @Override
            protected void onActionBegin() {
                if (rhythmGameActive && rhythmGameState == RhythmGameState.READY) {
                    startPlayingRhythmGame();
                }
            }
        }, KeyCode.SPACE);
    }


    private void startRhythmGame() {
        System.out.println("Starting integrated rhythm game setup...");
        rhythmGameActive = true;
        rhythmGameState = RhythmGameState.READY;

        getGameWorld().getEntitiesCopy().forEach(entity -> {
            if (entity.getType() != EntityType.PLATFORM) {
                entity.setVisible(false);
            }
        });
        if (player != null) player.setVisible(false);
        List<Entity> entitiesToRemove = getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE, RhythmEntityType.HIT_ZONE_MARKER);
        getGameWorld().removeEntities(entitiesToRemove);


        setupRhythmGameUI();


        loadBeatmap("beatmaps/sample_song.beatmap");


        rhythmAudioPlayer = new RhythmAudioPlayer();
        rhythmAudioPlayer.loadMusic();

        nextNoteIndexToSpawn = 0;


        set("score", 0);
        set("combo", 0);

        if (messageTopLineText != null) {
            messageTopLineText.setText("Press SPACE to Start");
            messageTopLineText.setVisible(true);
        }
        if (messageBottomLineText != null) {
            messageBottomLineText.setText("Press the right keys to get score!");
            messageBottomLineText.setVisible(true);
        }
        if (messageBox != null) messageBox.setVisible(true);


        spawnHitZoneMarkers();
    }

    private void startPlayingRhythmGame() {
        System.out.println("Starting rhythm game playing state...");
        rhythmGameState = RhythmGameState.PLAYING;

        rhythmAudioPlayer.playMusic(); // <--- REMOVE this duplicate call
        rhythmGameStartTime = System.currentTimeMillis();
        songElapsedTimeMs = 0;




        if (messageBox != null) messageBox.setVisible(false);
        if (messageTopLineText != null) messageTopLineText.setVisible(false);
        if (messageBottomLineText != null) messageBottomLineText.setVisible(false);



        rhythmGameStartTime = System.currentTimeMillis();
        songElapsedTimeMs = 0;


        if (scoreText != null) scoreText.setVisible(true);
        if (comboText != null) comboText.setVisible(true);
    }

    private void endRhythmGame(boolean songFinished) {
        System.out.println("Ending integrated rhythm game...");


        if (rhythmAudioPlayer != null) {
            rhythmAudioPlayer.stopMusic();
        }


        List<Entity> entitiesToRemove = getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE, RhythmEntityType.HIT_ZONE_MARKER);
        getGameWorld().removeEntities(entitiesToRemove);


        fadingTexts.forEach(getGameScene().getUINodes()::remove);
        fadingTexts.clear();


        if (scoreText != null) scoreText.setVisible(false);
        if (comboText != null) comboText.setVisible(false);


        rhythmGameState = RhythmGameState.GAME_ENDED;
        if (messageBox != null) messageBox.setVisible(true);
        if (messageTopLineText != null) {
            messageTopLineText.setText(songFinished ? "Song Finished!" : "Game Over!");
            messageTopLineText.setVisible(true);
        }
        if (messageBottomLineText != null) {
            messageBottomLineText.setText("Final Score: " + geti("score") + "\nPress E to Return");
            messageBottomLineText.setVisible(true);
        }


        runOnce(() -> {

            if (messageBox != null) messageBox.setVisible(false);
            if (messageTopLineText != null) messageTopLineText.setVisible(false);
            if (messageBottomLineText != null) messageBottomLineText.setVisible(false);



            getGameWorld().getEntitiesCopy().forEach(entity -> {
                if (entity.getType() != EntityType.PLATFORM) {
                    entity.setVisible(true);
                }
            });
            if (player != null) player.setVisible(true);


            rhythmGameActive = false;
            rhythmGameState = RhythmGameState.READY;


            if (player != null) {
                player.getComponent(PlayerComponent.class).stop();
            }

        }, Duration.seconds(3));
    }

    private void handleRhythmInput(int laneIndex) {
        System.out.println("Rhythm button pressed for lane: " + laneIndex + " at song time: " + songElapsedTimeMs);

        List<Entity> notesInLane = getGameWorld().getEntitiesByType(RhythmEntityType.RHYTHM_NOTE)
                .stream()
                .filter(note -> note.getComponent(RhythmNoteComponent.class).getLaneIndex() == laneIndex)
                .collect(Collectors.toList());

        Entity bestMatch = null;
        long closestTimeDiff = Long.MAX_VALUE;

        for (Entity note : notesInLane) {
            RhythmNoteComponent noteComponent = note.getComponent(RhythmNoteComponent.class);
            long targetHitTime = noteComponent.getTargetHitTimestamp();
            long timeDiff = Math.abs(songElapsedTimeMs - targetHitTime);

            double noteY = note.getY();
            if (timeDiff <= HIT_WINDOW_MS && noteY >= HIT_LINE_Y - NOTE_SIZE * 1.5) {
                if (timeDiff < closestTimeDiff) {
                    closestTimeDiff = timeDiff;
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

            if (timeDiff <= HIT_WINDOW_MS / 3) {
                feedbackText = "PERFECT!";
                feedbackColor = Color.YELLOW;
                points = (10 + geti("combo")) * 2;
                inc("score", points);
                inc("combo", 1);
                rhythmAudioPlayer.playHitSound();
            } else if (timeDiff <= HIT_WINDOW_MS) {
                feedbackText = "NICE!";
                feedbackColor = Color.LIMEGREEN;
                points = 10 + geti("combo");
                inc("score", points);
                inc("combo", 1);
                rhythmAudioPlayer.playHitSound();
            } else {
                feedbackText = "HIT?";
                feedbackColor = Color.ORANGE;
                points = 0;
                resetCombo();
                rhythmAudioPlayer.playMissSound();
            }

            addFadingText(feedbackText + " +" + points, bestMatch.getCenter().getX(), bestMatch.getCenter().getY(), feedbackColor);
            bestMatch.removeFromWorld();

        } else {
            System.out.println("No hittable note found in lane " + laneIndex + " at time " + songElapsedTimeMs);
            resetCombo();
            rhythmAudioPlayer.playMissSound();

            Entity tooSoonNote = notesInLane.stream()
                    .filter(note -> note.getComponent(RhythmNoteComponent.class).getTargetHitTimestamp() > songElapsedTimeMs + HIT_WINDOW_MS)
                    .findFirst()
                    .orElse(null);

            if (tooSoonNote != null) {
                addFadingText("TOO SOON!", tooSoonNote.getCenter().getX(), tooSoonNote.getCenter().getY(), Color.CYAN);
            }
        }
    }

    private void resetCombo() {
        set("combo", 0);
    }


    /**
     * Sets up the UI nodes for the rhythm game: score, combo, and message panel.
     * These nodes are added to the scene but their visibility is managed externally.
     * Uses getAppWidth() and getAppHeight() for positioning, which is static
     * based on initial window size and does not auto-update on window resize.
     */
    private void setupRhythmGameUI() {
        System.out.println("Setting up Rhythm Game UI...");
        // Remove previous UI elements if they exist (important for restarts)
        if (scoreText != null) getGameScene().removeUINode(scoreText);
        if (comboText != null) getGameScene().removeUINode(comboText);
        if (messageBox != null) getGameScene().removeUINode(messageBox);
        if (messageTopLineText != null) getGameScene().removeUINode(messageTopLineText);
        if (messageBottomLineText != null) getGameScene().removeUINode(messageBottomLineText);

        scoreText = getUIFactoryService().newText("", Color.BLACK, 48);

         scoreText.setText(String.valueOf(geti("score")));
        scoreText.setX(getAppWidth() / 2.0 - scoreText.getLayoutBounds().getWidth() / 2.0);
        scoreText.setY(50);
        scoreText.textProperty().bind(Bindings.convert(getip("score")));
        getGameScene().addUINode(scoreText);
        scoreText.setVisible(false);


        comboText = getUIFactoryService().newText("", Color.BLACK, 24);

        comboText.setText("Combo: " + geti("combo"));
        comboText.setX(getAppWidth() / 2.0 - comboText.getLayoutBounds().getWidth() / 2.0);
        comboText.setY(80);
        comboText.textProperty().bind(Bindings.concat("Combo: ", getip("combo").asString()));
        getGameScene().addUINode(comboText);
        comboText.setVisible(false);


        messageBox = new VBox(10);
        messageBox.setAlignment(Pos.CENTER);

        messageBox.setPrefSize(getAppWidth() * 0.6, 150);


        messageBox.setLayoutX(getAppWidth() / 2.0 - messageBox.getPrefWidth() / 2.0);
        messageBox.setLayoutY(getAppHeight() / 2.0 - messageBox.getPrefHeight() / 2.0);


        messageBox.setBackground(new Background(new BackgroundFill(Color.web("#762323C8"), null, null))); // Maroon with alpha


        messageTopLineText = getUIFactoryService().newText("", Color.BLACK, 30);
        messageBottomLineText = getUIFactoryService().newText("", Color.BLACK, 30);
        messageBox.getChildren().addAll(messageTopLineText, messageBottomLineText);

        getGameScene().addUINode(messageBox);
        messageBox.setVisible(false);
        messageTopLineText.setVisible(false);
        messageBottomLineText.setVisible(false);
    }

    private void spawnHitZoneMarkers() {

        List<Entity> markersToRemove = getGameWorld().getEntitiesByType(RhythmEntityType.HIT_ZONE_MARKER);
        getGameWorld().removeEntities(markersToRemove);

        for(int i = 0; i < NUM_LANES; i++) {

            spawn(RhythmEntityType.HIT_ZONE_MARKER.toString(), new SpawnData(0,0)
                    .put("laneIndex", i)
                    .put("hitLineY", HIT_LINE_Y)
            );
        }
    }


    private void addFadingText(String text, double x, double y, Color color) {
        Text fadingText = getUIFactoryService().newText(text, color, 20);


        fadingText.setText(text);
        double textWidth = fadingText.getLayoutBounds().getWidth();

        fadingText.setTranslateX(x - textWidth / 2);
        fadingText.setTranslateY(y - NOTE_SIZE / 2.0);

        getGameScene().addUINode(fadingText);
        fadingTexts.add(fadingText);

        Duration animationDuration = Duration.seconds(1.0);

        FadeTransition ft = new FadeTransition(animationDuration, fadingText);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);

        TranslateTransition tt = new TranslateTransition(animationDuration, fadingText);
        tt.setByY(-50);

        ft.play();
        tt.play();

        ft.setOnFinished(event -> {
            getGameScene().removeUINode(fadingText);
            fadingTexts.remove(fadingText);
        });
    }


    private void loadBeatmap(String filename) {
        beatmap = new ArrayList<>();
        System.out.println("Loading beatmap (placeholder)...");


        long currentTime = 1000;
        long timeBetweenNotes = 300;

        for (int i = 0; i < 100; i++) {
            int lane = i % NUM_LANES;
            beatmap.add(new NoteInfo(currentTime, lane));
            currentTime += timeBetweenNotes;
        }


        System.out.println("Loaded " + beatmap.size() + " notes into beatmap.");
        beatmap.sort((n1, n2) -> Long.compare(n1.timestampMs, n2.timestampMs));
    }


    @Override
    protected void initGame() {

        getSettings().setGlobalMusicVolume(1.0); // Use the static accessor FXGL.getSettings()
        getSettings().setGlobalSoundVolume(1.0); // Use the static accessor FXGL.getSettings()
        System.out.println("DEBUG: Global Music Volume set to: " + getSettings().getGlobalMusicVolume());
        System.out.println("DEBUG: Global Sound Volume set to: " + getSettings().getGlobalSoundVolume());



        set("score", 0);
        set("combo", 0);
        set("songElapsedTimeMs", 0L);

        // Add entity factories
        getGameWorld().addEntityFactory(new MapFactory());
        getGameWorld().addEntityFactory(new RhythmGameFactory());

        // Load platformer level and spawn player/background
        Level level = getAssetLoader().loadLevel("tmx/test.tmx", new TMXLevelLoader());
        getGameWorld().setLevel(level);
        player = spawn("player", 16, 16);
        set("player", player);
        spawn("background");
        NPCLocations.spawnNPCs();
        Viewport viewport = getGameScene().getViewport();
        viewport.setBounds(-1500, 0, 250 * 70, getAppHeight());
        viewport.bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);
        viewport.setLazy(true);
        viewport.setZoom(3.0);


        rhythmGameActive = false;
        rhythmGameState = RhythmGameState.READY;


        getGameWorld().getEntitiesCopy().forEach(entity -> entity.setVisible(true));


        setupRhythmGameUI(); // call the method here
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("combo", 0);
        vars.put("songElapsedTimeMs", 0L);
    }


    public static void main(String[] args) {
        launch(args);
    }


}