package org.example.capstonee.Menu;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.FXGLScene;
import com.almasb.fxgl.app.scene.MenuType;

// Import needed FXGL classes
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;

import com.almasb.fxgl.audio.Music; // Needed for Music asset type and INDEFINITE constant
// Needed for the object returned by playMusic for control
import com.almasb.fxgl.audio.AudioPlayer.*;
import com.almasb.fxgl.audio.Audio.*;
import com.almasb.fxgl.audio.Music.*;

import com.almasb.fxgl.audio.AudioPlayer; // Needed for getAudioPlayer() return type (less common direct use, but good practice)

// Import needed JavaFX classes
import javafx.geometry.Pos;
import javafx.geometry.Insets; // Needed for Insets
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.control.Button; // Needed for Button

// Import other project classes
import org.example.capstonee.GameApp; // Needed to call static startSelectedSong
import org.example.capstonee.Menu.ParallaxBackground;
import org.example.capstonee.Menu.OptionsPane;
import org.example.capstonee.Song.Song; // Needed for Song type
import org.example.capstonee.Song.SongDatabase; // Needed to get song list

import java.util.ArrayList; // Although not used in the final code, keeping if previously needed
import java.util.List;

import java.util.stream.Collectors; // Needed for stream operations

// Correct static imports from FXGL dsl
// Provides access to getUIFactoryService, getDialogService, getAppWidth, getAppHeight, texture, getAssetLoader, getAudioPlayer, getGameController, getSettings (if needed)
import static com.almasb.fxgl.dsl.FXGL.*;


public class MainMenu extends FXGLMenu {



    private ParallaxBackground parallaxBackground;
    // REMOVED: private static String pendingNewGameSaveSlot = null;

    private Music menuMusicAsset; // Field to hold the menu music asset
    public MainMenu() {
        super(MenuType.MAIN_MENU);

        // Load Menu Music Asset (do this early)
        try {
            // Replace with your actual menu music file path relative to assets/
            menuMusicAsset = getAssetLoader().loadMusic("music/menu_theme.wav"); // Example menu music path
            if (menuMusicAsset == null) {
                System.err.println("Failed to load menu music asset: music/menu_theme.wav");
            }
        } catch (Exception e) {
            System.err.println("Failed to load menu music asset: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            menuMusicAsset = null; // Ensure it's null on failure
        }

        // parallaxBackground setup
        parallaxBackground = new ParallaxBackground();
        try {
            parallaxBackground.addLayer("background/nearest4.png", 5.0);
            parallaxBackground.addLayer("background/nearest3.png", 15.0);
            parallaxBackground.addLayer("background/nearest2.png", 30.0);
            parallaxBackground.addLayer("background/ nearest1.png", 35.0); // Corrected potential typo
            getContentRoot().getChildren().add(parallaxBackground.getRoot());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred setting up parallax background: " + e.getMessage());
            e.printStackTrace(); // Print stack trace
            // Ensure fallback texture is loaded correctly
            try {
                var fallbackBG = texture("ui/glitch.png", getAppWidth(), getAppHeight()); // Example fallback texture
                getContentRoot().getChildren().add(fallbackBG);
            } catch (Exception loadError) {
                System.err.println("Failed to load fallback background texture: " + loadError.getMessage());
                loadError.printStackTrace();
            }
        }

        // Title setup
        Text title = getUIFactoryService().newText("Rhythm Impact", Color.LIGHTGOLDENRODYELLOW, 52); // getUIFactoryService() from dsl
        StackPane titlePane = new StackPane(title);
        titlePane.setPrefSize(getAppWidth(), 200); // getAppWidth() from dsl
        titlePane.setAlignment(Pos.CENTER);
        titlePane.setTranslateY(getAppHeight() * 0.1); // getAppHeight() from dsl

        // Menu Buttons
        var btnSelectSong = createMenuButton("Select Song", this::showSongSelection); // Renamed button for clarity
        var btnOptions = createMenuButton("Options", this::showOptionsDialog);
        var btnExitGame = createMenuButton("Exit Game", this::fireExit); // fireExit is inherited from FXGLMenu

        VBox buttonBox = new VBox(25,
                btnSelectSong, // Use the renamed button
                btnOptions,
                btnExitGame
        );
        buttonBox.setAlignment(Pos.CENTER);

        StackPane buttonsPane = new StackPane(buttonBox);
        buttonsPane.setPrefSize(getAppWidth(), getAppHeight());
        buttonsPane.setAlignment(Pos.CENTER);
        buttonsPane.setTranslateY(getAppHeight() * 0.15);

        getContentRoot().getChildren().addAll(titlePane, buttonsPane); // getContentRoot() inherited
    }

    // Helper to create styled buttons
    private Node createMenuButton(String text, Runnable action) {
        // getUIFactoryService() from dsl
        var button = getUIFactoryService().newButton(text);
        button.setOnAction(e -> action.run());
        button.setPrefWidth(300);
        button.setPrefHeight(50);
        String buttonStyle = "-fx-font-size: 18px; -fx-background-color: #036661; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5;";
        String buttonHoverStyle = "-fx-font-size: 18px; -fx-background-color: #047F79; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5;";
        button.setStyle(buttonStyle);
        button.setOnMouseEntered(e -> button.setStyle(buttonHoverStyle));
        button.setOnMouseExited(e -> button.setStyle(buttonStyle));
        return button;
    }

    // --- New Song Selection Methods ---

    private void showSongSelection() {
        List<Song> songs = SongDatabase.getSongs(); // Get songs from your database

        if (songs.isEmpty()) {
            getDialogService().showMessageBox("No songs available."); // getDialogService() from dsl
            return;
        }

        List<String> songOptions = songs.stream()
                .map(Song::toString) // Use the toString method (Name (Difficulty))
                .collect(Collectors.toList());

        songOptions.add("Cancel"); // Add a cancel option

        getDialogService().showChoiceBox( // getDialogService() from dsl
                "Select a Song:",
                songOptions,
                (chosenSongString) -> {
                    if (chosenSongString == null || "Cancel".equals(chosenSongString)) {
                        // User cancelled or closed the dialog
                        return;
                    }

                    Song selectedSong = SongDatabase.getSongByDisplayString(chosenSongString); // Get song by display string

                    if (selectedSong != null) {
                        showSongDetailsDialog(selectedSong); // Show details for the selected song
                    } else {
                        System.err.println("Selected song not found in database: " + chosenSongString);
                        getDialogService().showMessageBox("Error: Could not find song data."); // getDialogService() from dsl
                    }
                });
    }

    private void showSongDetailsDialog(Song song) {
        // Create a VBox to display song details
        VBox detailsBox = new VBox(10);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        detailsBox.setPadding(new Insets(15)); // Insets from javafx.geometry

        Text nameText = getUIFactoryService().newText(song.getName(), Color.LIGHTGOLDENRODYELLOW, 32); // getUIFactoryService() from dsl
        Text difficultyText = getUIFactoryService().newText("Difficulty: " + song.getDifficulty(), Color.WHITE, 20); // getUIFactoryService() from dsl
        Text descriptionText = getUIFactoryService().newText("Description:\n" + song.getDescription(), Color.WHITE, 18); // getUIFactoryService() from dsl
        descriptionText.setWrappingWidth(getAppWidth() * 0.4); // Wrap long descriptions, getAppWidth() from dsl

        detailsBox.getChildren().addAll(nameText, difficultyText, descriptionText);

        // Create buttons for the dialog
        Button btnPlay = getUIFactoryService().newButton("Play Song"); // getUIFactoryService() from dsl
        Button btnCancel = getUIFactoryService().newButton("Cancel"); // getUIFactoryService() from dsl

        // When btnPlay is clicked, FXGL automatically closes the dialog
        // after executing the action. The explicit closeBox() is not needed here.
        btnPlay.setOnAction(e -> {
            // The dialog is closing now or very soon, proceed to play song
            handlePlaySong(song); // Call the method to start the game
        });

        // showBox handles btnCancel click automatically to close the dialog
        getDialogService().showBox( // getDialogService() from dsl
                "Song Details", // Dialog Title
                detailsBox,     // Content
                btnPlay,        // Button 1
                btnCancel       // Button 2
        );
    }

    private void handlePlaySong(Song song) {
        System.out.println("Attempting to play song: " + song.getName());
        // Stop menu music before starting the game
        stopMenuMusic();
        // Call the static method in GameApp to start the rhythm game
        GameApp.startSelectedSong(song.getBeatmapPath(), song.getMusicAssetPath());
    }

    private void stopMenuMusic() {
        // Check if the active playback instance exists and is playing
        FXGL.getAudioPlayer().stopMusic(menuMusicAsset);
        // Optional: stop any other potential music playing just in case (safety)
        // getAudioPlayer().stopAllMusic(); // Can keep this if desired, but stopping the specific active music is usually sufficient
    }


    // --- Keep other MainMenu methods if they are necessary for your menu logic ---
    // (Based on the provided code, showOptionsDialog is needed)

    private void showOptionsDialog() {
        OptionsPane optionsPane = new OptionsPane();
        // getDialogService() and getUIFactoryService() from dsl
        getDialogService().showBox("Options", optionsPane, getUIFactoryService().newButton("Close"));
    }


    // Removed methods related to old start game/load game logic


    // Correctly handle music playback when entering and exiting the menu
     // Use @Override for clarity
    protected void onEnteredFrom(FXGLScene prevState) {
        super.onEnteredFrom(prevState);
        if (parallaxBackground != null) {
            parallaxBackground.start();
        }
        // Start Menu Music when the menu is entered
        if (menuMusicAsset != null) {
            System.out.println("DEBUG: Playing menu music.");
            // Play the music asset and store the active playback instance
            FXGL.getAudioPlayer().loopMusic(menuMusicAsset);

            // Optional: set volume, other properties on activeMenuMusic
                // activeMenuMusic.setVolume(0.5); // Example

        } else {
            System.err.println("Menu music asset is null, cannot play.");
        }
    }

     // Use @Override for clarity
    protected void onExitingTo(FXGLScene nextState) {
        super.onExitingTo(nextState);
        if (parallaxBackground != null) {
            parallaxBackground.stop();
        }
        // Menu music is stopped by handlePlaySong before exiting to the game scene.
        // If you needed to stop music on ANY exit (e.g. exiting game), you could uncomment stopMenuMusic() here.
        // stopMenuMusic(); // This would stop music if exiting to options or any other scene too
    }
}