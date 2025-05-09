package org.example.capstonee;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

// Import necessary classes for Rhythm Game and Menu
import org.example.capstonee.RhythmGame.RhythmAudioPlayer;
import org.example.capstonee.RhythmGame.RhythmGameManager;
import org.example.capstonee.RhythmGame.RhythmGameUI;
import org.example.capstonee.RhythmGame.RhythmGameState;
import org.example.capstonee.RhythmGame.RhythmGameFactory;
import org.example.capstonee.Menu.MenuSceneFactory;
import org.example.capstonee.Song.Song; // Needed for startSelectedSong
import org.example.capstonee.Menu.OptionsPane; // Needed if binding options

import java.util.Map;

// Correct static imports from FXGL dsl
// Provides access to getGameController, getDialogService, getGameWorld, getAppWidth, getAppHeight, getAssetLoader, getAudioPlayer, getInput, geti, set, geto, spawn, getGameTimer, getSettings, getSceneService
import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {

    private RhythmAudioPlayer rhythmAudioPlayer;
    private RhythmGameUI rhythmGameUI;
    private RhythmGameManager rhythmGameManager;

    private static String pendingBeatmapPath = null;
    private static String pendingMusicAssetPath = null;

    // Keep a static reference for MainMenu to call
    private static GameApp instance;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Rhythm Impact");
        settings.setVersion("1.0");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setSceneFactory(new MenuSceneFactory());
        settings.setMainMenuEnabled(true); // Start at Main Menu
        settings.setGameMenuEnabled(false); // Disable game menu

        // --- REMOVED AUDIO BINDING FROM HERE ---
        // It will be moved to initGame()

        settings.setApplicationMode(ApplicationMode.DEVELOPER); // Or RELEASE
    }

    @Override
    protected void initInput() {
        // ... (initInput method remains the same) ...
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

        // Initialize rhythm game components (ensure they are created here every time initGame runs)
        rhythmAudioPlayer = new RhythmAudioPlayer(); // Fresh instance for each game session
        rhythmGameUI = new RhythmGameUI(getGameScene()); // Fresh instance for each game session
        rhythmGameManager = new RhythmGameManager(getGameScene(), rhythmGameUI, rhythmAudioPlayer); // Fresh instance

        // Set the callback for when a song finishes - goes back to menu
        rhythmGameManager.setOnGameEndCallback(this::returnToMainMenu);

        // Add entity factories needed for rhythm game visuals
        getGameWorld().addEntityFactory(new RhythmGameFactory());

        // Initialize static instance reference
        instance = this; // <-- Set the instance reference here!
        System.out.println("GameApp: initGame finished. Instance set.");

        // --- ADD AUDIO BINDING HERE (already done in previous fix) ---
        try {
            getSettings().setGlobalMusicVolume(OptionsPane.globalVolumeProperty().get());
            getSettings().setGlobalSoundVolume(OptionsPane.globalVolumeProperty().get());
            getSettings().globalMusicVolumeProperty().bind(OptionsPane.globalVolumeProperty());
            getSettings().globalSoundVolumeProperty().bind(OptionsPane.globalVolumeProperty());
            OptionsPane.globalMuteProperty().addListener((obs, oldVal, isMuted) -> {
                if (isMuted) {
                    getSettings().setGlobalMusicVolume(0.0);
                    getSettings().setGlobalSoundVolume(0.0);
                } else {
                    getSettings().setGlobalMusicVolume(OptionsPane.globalVolumeProperty().get());
                    getSettings().setGlobalSoundVolume(OptionsPane.globalVolumeProperty().get());
                }
            });
            System.out.println("DEBUG: Audio settings bound to OptionsPane properties.");
        } catch (Exception e) {
            System.err.println("Error binding audio settings to OptionsPane properties...");
            e.printStackTrace();
        }
        // --- End Audio Binding ---


        // --- Use the pending song data to start the game manager ---
        if (pendingBeatmapPath != null && pendingMusicAssetPath != null) {
            System.out.println("GameApp (initGame): Found pending song data. Starting rhythm game instance.");
            try {
                // Stop any lingering audio from the menu or previous game (though startNewGame usually cleans up)
                if (rhythmAudioPlayer != null) {
                    rhythmAudioPlayer.stopAll();
                    System.out.println("DEBUG (initGame): Stopped all audio before starting song.");
                }

                // Load the specific music for this song
                rhythmAudioPlayer.loadMusic(pendingMusicAssetPath);

                // Start the rhythm game setup (UI, notes loading, etc.)
                rhythmGameManager.start(pendingBeatmapPath);

                // Clear the pending data now that it's used
                pendingBeatmapPath = null;
                pendingMusicAssetPath = null;

            } catch (Exception e) {
                System.err.println("Failed to start rhythm game song during initGame: " + e.getMessage());
                e.printStackTrace();
                // Handle error - show message box and return to menu
                // Need to use runOnceAfter because dialogs shouldn't be shown directly during initGame
                getGameTimer().runOnceAfter(() -> {
                    getDialogService().showMessageBox("Failed to start song:\n" + e.getMessage(), this::returnToMainMenu);
                }, Duration.seconds(0.1)); // Small delay to ensure scene transition is stable
            }

        } else {
            // This case might happen if startNewGame() was called without going through the menu flow
            // Or if there was an error setting the pending paths.
            // In a rhythm game, starting without a song doesn't make sense for the main game mode.
            System.err.println("GameApp (initGame): No pending song data found. Cannot start game session.");
            getGameTimer().runOnceAfter(() -> {
                getDialogService().showMessageBox("Failed to start game.\nNo song selected.", this::returnToMainMenu);
            }, Duration.seconds(0.1));
        }
        // --- End pending song data usage ---
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        // ... (initGameVars method remains the same) ...
        vars.put("score", 0);
        vars.put("combo", 0);
        vars.put("songElapsedTimeMs", 0L);
        // Add variables needed by RhythmGameUI, like combo break text, etc.
    }

    @Override
    protected void onUpdate(double tpf) {
        // ... (onUpdate method remains the same) ...
        if (rhythmGameManager != null && rhythmGameManager.isActive()) {
            rhythmGameManager.update(tpf);
        }
    }

    // --- Static Method to Start a Song from the Menu ---
    public static void startSelectedSong(String beatmapPath, String musicAssetPath) {
        System.out.println("GameApp (Static): Preparing to start song: " + beatmapPath);

        // Store the song data in static fields
        pendingBeatmapPath = beatmapPath;
        pendingMusicAssetPath = musicAssetPath;

        // Trigger the transition to the game scene, which will call initGame()
        getGameController().startNewGame();
        System.out.println("GameApp (Static): Called startNewGame(). initGame will handle song loading.");
    }

    // --- Instance Method to Start a Song ---
    private void startRhythmGameInstance(String beatmapPath, String musicAssetPath) {
        // ... (startRhythmGameInstance method remains the same) ...
        System.out.println("GameApp (Instance): startRhythmGameInstance starting song: " + beatmapPath);

        if (rhythmGameManager == null || rhythmAudioPlayer == null || rhythmGameUI == null) {
            System.err.println("Rhythm game components not initialized. Re-initializing.");
            // Call initGame() only if components are null, otherwise assume they are already created by the initial initGame() call
            if (rhythmGameManager == null || rhythmAudioPlayer == null || rhythmGameUI == null) {
                initGame(); // Attempt to re-initialize - though this is less ideal, could indicate a flow issue if not null after initial initGame
            }
            if (rhythmGameManager == null) { // Check again
                System.err.println("Failed to initialize rhythm game components. Cannot start song.");
                getDialogService().showMessageBox("Failed to start song.\nInitialization error.", this::returnToMainMenu);
                return;
            }
        }

        try {
            if (rhythmAudioPlayer != null) {
                rhythmAudioPlayer.stopAll();
                System.out.println("DEBUG (startRhythmGameInstance): Stopped all audio before starting song.");
            } else {
                System.err.println("DEBUG (startRhythmGameInstance): rhythmAudioPlayer is null when trying to stop audio.");
            }

            // Load the specific music for this song
            rhythmAudioPlayer.loadMusic(musicAssetPath);

            // Start the rhythm game setup
            rhythmGameManager.start(beatmapPath);

            // Transition from Menu Scene to Game Scene
            System.out.println("GameApp (Instance): Transitioning to Game Scene.");
            getGameController().startNewGame();

        } catch (Exception e) {
            System.err.println("Failed to start rhythm game song: " + e.getMessage());
            e.printStackTrace();
            getDialogService().showMessageBox("Failed to start song:\n" + e.getMessage(), this::returnToMainMenu);
        }
    }


    // --- Method to Return to Main Menu ---
    private void returnToMainMenu() {
        // ... (returnToMainMenu method remains the same) ...
        System.out.println("GameApp (Instance): Returning to Main Menu.");
        if (rhythmAudioPlayer != null) {
            rhythmAudioPlayer.stopAll();
            System.out.println("DEBUG (returnToMainMenu): Stopped all audio before going to menu.");
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