package org.example.capstonee.Menu;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.FXGLScene;
import com.almasb.fxgl.app.scene.MenuType;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;

import com.almasb.fxgl.audio.Music;

import com.almasb.fxgl.audio.AudioPlayer;

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.control.Button;

import org.example.capstonee.GameApp;
import org.example.capstonee.Menu.ParallaxBackground;
import org.example.capstonee.Menu.OptionsPane;
import org.example.capstonee.Song.Song;
import org.example.capstonee.Song.SongDatabase;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

import static com.almasb.fxgl.dsl.FXGL.*;


public class MainMenu extends FXGLMenu {



    private ParallaxBackground parallaxBackground;

    private Music menuMusicAsset;
    public MainMenu() {
        super(MenuType.MAIN_MENU);

        try {
            menuMusicAsset = getAssetLoader().loadMusic("music/menu_theme.wav");
            if (menuMusicAsset == null) {
                System.err.println("Failed to load menu music asset: music/menu_theme.wav");
            }
        } catch (Exception e) {
            System.err.println("Failed to load menu music asset: " + e.getMessage());
            e.printStackTrace();
            menuMusicAsset = null;
        }

        parallaxBackground = new ParallaxBackground();
        try {
            parallaxBackground.addLayer("background/nearest4.png", 5.0);
            parallaxBackground.addLayer("background/nearest3.png", 15.0);
            parallaxBackground.addLayer("background/nearest2.png", 30.0);
            parallaxBackground.addLayer("background/ nearest1.png", 35.0);
            getContentRoot().getChildren().add(parallaxBackground.getRoot());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred setting up parallax background: " + e.getMessage());
            e.printStackTrace();
            try {
                var fallbackBG = texture("ui/glitch.png", getAppWidth(), getAppHeight());
                getContentRoot().getChildren().add(fallbackBG);
            } catch (Exception loadError) {
                System.err.println("Failed to load fallback background texture: " + loadError.getMessage());
                loadError.printStackTrace();
            }
        }

        Text title = getUIFactoryService().newText("Rhythm Impact", Color.LIGHTGOLDENRODYELLOW, 52);
        StackPane titlePane = new StackPane(title);
        titlePane.setPrefSize(getAppWidth(), 200);
        titlePane.setAlignment(Pos.CENTER);
        titlePane.setTranslateY(getAppHeight() * 0.1);

        var btnSelectSong = createMenuButton("Select Song", this::showSongSelection);
        var btnOptions = createMenuButton("Options", this::showOptionsDialog);
        var btnExitGame = createMenuButton("Exit Game", this::fireExit);

        VBox buttonBox = new VBox(25,
                btnSelectSong,
                btnOptions,
                btnExitGame
        );
        buttonBox.setAlignment(Pos.CENTER);

        StackPane buttonsPane = new StackPane(buttonBox);
        buttonsPane.setPrefSize(getAppWidth(), getAppHeight());
        buttonsPane.setAlignment(Pos.CENTER);
        buttonsPane.setTranslateY(getAppHeight() * 0.15);

        getContentRoot().getChildren().addAll(titlePane, buttonsPane);
    }

    private Node createMenuButton(String text, Runnable action) {
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

    private void showSongSelection() {
        List<Song> songs = SongDatabase.getSongs();

        if (songs.isEmpty()) {
            getDialogService().showMessageBox("No songs available.");
            return;
        }

        List<String> songOptions = songs.stream()
                .map(Song::toString)
                .collect(Collectors.toList());

        songOptions.add("Cancel");

        getDialogService().showChoiceBox(
                "Select a Song:",
                songOptions,
                (chosenSongString) -> {
                    if (chosenSongString == null || "Cancel".equals(chosenSongString)) {
                        return;
                    }

                    Song selectedSong = SongDatabase.getSongByDisplayString(chosenSongString);

                    if (selectedSong != null) {
                        showSongDetailsDialog(selectedSong);
                    } else {
                        System.err.println("Selected song not found in database: " + chosenSongString);
                        getDialogService().showMessageBox("Error: Could not find song data.");
                    }
                });
    }

    private void showSongDetailsDialog(Song song) {
        VBox detailsBox = new VBox(10);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        detailsBox.setPadding(new Insets(15));

        Text nameText = getUIFactoryService().newText(song.getName(), Color.LIGHTGOLDENRODYELLOW, 32);
        Text difficultyText = getUIFactoryService().newText("Difficulty: " + song.getDifficulty(), Color.WHITE, 20);
        Text descriptionText = getUIFactoryService().newText("Description:\n" + song.getDescription(), Color.WHITE, 18);
        descriptionText.setWrappingWidth(getAppWidth() * 0.4);

        detailsBox.getChildren().addAll(nameText, difficultyText, descriptionText);

        Button btnPlay = getUIFactoryService().newButton("Play Song");
        Button btnCancel = getUIFactoryService().newButton("Cancel");

        btnPlay.setOnAction(e -> {
            handlePlaySong(song);
        });

        getDialogService().showBox(
                "Song Details",
                detailsBox,
                btnPlay,
                btnCancel
        );
    }

    private void handlePlaySong(Song song) {
        System.out.println("Attempting to play song: " + song.getName());
        stopMenuMusic();
        GameApp.startSelectedSong(song.getBeatmapPath(), song.getMusicAssetPath());
    }

    private void stopMenuMusic() {
        FXGL.getAudioPlayer().stopMusic(menuMusicAsset);
    }


    private void showOptionsDialog() {
        OptionsPane optionsPane = new OptionsPane();
        getDialogService().showBox("Options", optionsPane, getUIFactoryService().newButton("Close"));
    }


    protected void onEnteredFrom(FXGLScene prevState) {
        super.onEnteredFrom(prevState);
        if (parallaxBackground != null) {
            parallaxBackground.start();
        }
        if (menuMusicAsset != null) {
            System.out.println("DEBUG: Playing menu music.");
            FXGL.getAudioPlayer().loopMusic(menuMusicAsset);
        } else {
            System.err.println("Menu music asset is null, cannot play.");
        }
    }

    protected void onExitingTo(FXGLScene nextState) {
        super.onExitingTo(nextState);
        if (parallaxBackground != null) {
            parallaxBackground.stop();
        }
    }
}