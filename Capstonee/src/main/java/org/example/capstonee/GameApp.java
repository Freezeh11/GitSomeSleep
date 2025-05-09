package org.example.capstonee;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import org.example.capstonee.RhythmGame.RhythmAudioPlayer;
import org.example.capstonee.RhythmGame.RhythmGameManager;
import org.example.capstonee.RhythmGame.RhythmGameUI;
import org.example.capstonee.RhythmGame.RhythmGameState;
import org.example.capstonee.RhythmGame.RhythmGameFactory;
import org.example.capstonee.Menu.MenuSceneFactory;
import org.example.capstonee.Song.Song;
import org.example.capstonee.Menu.OptionsPane;
import org.example.capstonee.Song.SongDatabase;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {

    private RhythmAudioPlayer rhythmAudioPlayer;
    private RhythmGameUI rhythmGameUI;
    private RhythmGameManager rhythmGameManager;

    private static String pendingBeatmapPath = null;
    private static String pendingMusicAssetPath = null;

    private final BooleanProperty isGamePaused = new SimpleBooleanProperty(false);

    private String currentBeatmapPath = null;
    private String currentMusicAssetPath = null;


    private static GameApp instance;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Rhythm Impact");
        settings.setVersion("1.0");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setSceneFactory(new MenuSceneFactory());
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }


    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("RhythmLane0") {
            @Override
            protected void onActionBegin() {
                if (rhythmGameManager != null && rhythmGameManager.isActive() && rhythmGameManager.getState() == RhythmGameState.PLAYING) {
                    rhythmGameManager.handleInput(0);
                }
            }
        }, KeyCode.H);

        getInput().addAction(new UserAction("RhythmLane1") {
            @Override
            protected void onActionBegin() {
                if (rhythmGameManager != null && rhythmGameManager.isActive() && rhythmGameManager.getState() == RhythmGameState.PLAYING) {
                    rhythmGameManager.handleInput(1);
                }
            }
        }, KeyCode.J);

        getInput().addAction(new UserAction("RhythmLane2") {
            @Override
            protected void onActionBegin() {
                if (rhythmGameManager != null && rhythmGameManager.isActive() && rhythmGameManager.getState() == RhythmGameState.PLAYING) {
                    rhythmGameManager.handleInput(2);
                }
            }
        }, KeyCode.K);

        getInput().addAction(new UserAction("RhythmLane3") {
            @Override
            protected void onActionBegin() {
                if (rhythmGameManager != null && rhythmGameManager.isActive() && rhythmGameManager.getState() == RhythmGameState.PLAYING) {
                    rhythmGameManager.handleInput(3);
                }
            }
        }, KeyCode.L);

        getInput().addAction(new UserAction("RhythmStart") {
            @Override
            protected void onActionBegin() {
                if (rhythmGameManager != null && rhythmGameManager.isActive() && rhythmGameManager.getState() == RhythmGameState.READY) {
                    rhythmGameManager.startPlaying();
                }
            }
        }, KeyCode.SPACE);
    }



    @Override
    protected void initGame() {
        System.out.println("GameApp: initGame started.");

        rhythmAudioPlayer = new RhythmAudioPlayer();
        rhythmGameUI = new RhythmGameUI(getGameScene());
        rhythmGameManager = new RhythmGameManager(getGameScene(), rhythmGameUI, rhythmAudioPlayer);
        rhythmGameUI.setGameManager(rhythmGameManager);

        rhythmGameManager.setOnGameEndCallback(this::returnToMainMenu);

        getGameWorld().addEntityFactory(new RhythmGameFactory());

        instance = this;
        System.out.println("GameApp: initGame finished. Instance set.");

        try {
            getSettings().globalMusicVolumeProperty().bind(
                    Bindings.when(OptionsPane.globalMuteProperty())
                            .then(0.0)
                            .otherwise(OptionsPane.globalVolumeProperty())
            );

            getSettings().globalSoundVolumeProperty().bind(
                    Bindings.when(OptionsPane.globalMuteProperty())
                            .then(0.0)
                            .otherwise(OptionsPane.globalVolumeProperty())
            );

            System.out.println("DEBUG: Audio settings bound conditionally to OptionsPane properties.");
        } catch (Exception e) {
            System.err.println("Error binding audio settings to OptionsPane properties. Ensure OptionsPane methods are static and return properties.");
            e.printStackTrace();
        }


        isGamePaused.addListener((obs, wasPaused, isNowPaused) -> {
            if (isNowPaused) {
                System.out.println("GameApp: Game Paused (Custom Listener). Pausing music.");
                pauseGameMusic();
            } else {
                System.out.println("GameApp: Game Resumed (Custom Listener). Resuming music.");
                resumeGameMusic();
            }
        });


        if (pendingBeatmapPath != null && pendingMusicAssetPath != null) {
            System.out.println("GameApp (initGame): Found pending song data. Starting rhythm game instance.");
            try {
                currentBeatmapPath = pendingBeatmapPath;
                currentMusicAssetPath = pendingMusicAssetPath;

                if (rhythmAudioPlayer != null) {
                    rhythmAudioPlayer.stopAll();
                    System.out.println("DEBUG (initGame): Stopped all audio before starting song.");
                }

                rhythmAudioPlayer.loadMusic(currentMusicAssetPath);

                rhythmGameManager.start(currentBeatmapPath);

                pendingBeatmapPath = null;
                pendingMusicAssetPath = null;

            } catch (Exception e) {
                System.err.println("Failed to start rhythm game song during initGame: " + e.getMessage());
                e.printStackTrace();
                getGameTimer().runOnceAfter(() -> {
                    getDialogService().showMessageBox("Failed to start song.\n" + e.getMessage(), this::returnToMainMenu);
                }, Duration.seconds(0.1));
            }

        } else {
            System.err.println("GameApp (initGame): No pending song data found. Cannot start game session.");
            getGameTimer().runOnceAfter(() -> {
                getDialogService().showMessageBox("Failed to start game.\nNo song selected.", this::returnToMainMenu);
            }, Duration.seconds(0.1));
        }
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("combo", 0);
        vars.put("songElapsedTimeMs", 0L);
    }

    @Override
    protected void onUpdate(double tpf) {
        if (rhythmGameManager != null && rhythmGameManager.isActive()) {
            rhythmGameManager.update(tpf);
        }
    }



    protected void onEnterGameMenu() {
        System.out.println("GameApp: Entering Game Menu. Setting custom pause state to true.");
        isGamePaused.set(true);
    }


    protected void onExitGameMenu() {
        System.out.println("GameApp: Exiting Game Menu. Setting custom pause state to false.");
        isGamePaused.set(false);
    }


    public static void pauseGame() {
        if (instance != null) {
            instance.isGamePaused.set(true);
        }
    }

    public static void resumeGame() {
        if (instance != null) {
            instance.isGamePaused.set(false);
        }
    }

    public static boolean isPaused() {
        return instance != null && instance.isGamePaused.get();
    }


    public static void startSelectedSong(String beatmapPath, String musicAssetPath) {
        System.out.println("GameApp (Static): Preparing to start song: " + beatmapPath);

        pendingBeatmapPath = beatmapPath;
        pendingMusicAssetPath = musicAssetPath;

        getGameController().startNewGame();
        System.out.println("GameApp (Static): Called startNewGame(). initGame will handle song loading.");
    }

    public static void restartCurrentSong() {
        System.out.println("GameApp (Static): Restarting current song...");
        if (instance != null && instance.currentBeatmapPath != null && instance.currentMusicAssetPath != null) {
            pendingBeatmapPath = instance.currentBeatmapPath;
            pendingMusicAssetPath = instance.currentMusicAssetPath;

            instance.currentBeatmapPath = null;
            instance.currentMusicAssetPath = null;

            if (instance.rhythmAudioPlayer != null) {
                instance.rhythmAudioPlayer.stopAll();
                System.out.println("DEBUG (restartCurrentSong): Stopped audio.");
            }

            getGameController().startNewGame();
            System.out.println("GameApp (Static): Called startNewGame() for restart.");

        } else {
            System.err.println("GameApp instance or current song data is not initialized! Cannot restart.");
            getGameTimer().runOnceAfter(() -> {
                getDialogService().showMessageBox("Failed to restart song.\nNo song data available.", instance::returnToMainMenu);
            }, Duration.seconds(0.1));
        }
    }


    public static void pauseGameMusic() {
        System.out.println("DEBUG (GameApp Static): Attempting to pause music.");
        if (instance != null && instance.rhythmAudioPlayer != null) {
            instance.rhythmAudioPlayer.pauseMusic();
            System.out.println("DEBUG (GameApp Static): Music paused.");
        } else {
            System.err.println("DEBUG (GameApp Static): Cannot pause music, instance or audioPlayer is null.");
        }
    }

    public static void resumeGameMusic() {
        System.out.println("DEBUG (GameApp Static): Attempting to resume music.");
        if (instance != null && instance.rhythmAudioPlayer != null) {
            instance.rhythmAudioPlayer.resumeMusic();
            System.out.println("DEBUG (GameApp Static): Music resumed.");
        } else {
            System.err.println("DEBUG (GameApp Static): Cannot resume music, instance or audioPlayer is null.");
        }
    }


    private void returnToMainMenu() {
        System.out.println("GameApp (Instance): Returning to Main Menu.");

        currentBeatmapPath = null;
        currentMusicAssetPath = null;

        if (rhythmAudioPlayer != null) {
            rhythmAudioPlayer.stopAll();
            System.out.println("DEBUG (returnToMainMenu): Stopped all audio.");
        } else {
            System.err.println("DEBUG (returnToMainMenu): rhythmAudioPlayer is null when trying to stop audio.");
        }

        getGameController().gotoMainMenu();
        System.out.println("GameApp (Instance): Transitioned to Main Menu scene.");
    }


    public static void main(String[] args) {
        launch(args);
    }
}