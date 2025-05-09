package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.app.scene.GameScene;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

// Added imports for Dialog and Button
import javafx.scene.control.Button;
import javafx.geometry.Insets;


import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;
import static org.example.capstonee.RhythmGame.RhythmGameFactory.NOTE_SIZE;

public class RhythmGameUI {

    private Text scoreText;
    private Text comboText;

    // Correctly declare these as fields so they are accessible by other methods
    private VBox messageBox;
    private Text messageTopLineText;
    private Text messageBottomLineText;

    private List<Text> fadingTexts = new ArrayList<>();

    private final GameScene gameScene;
    // Added field for RhythmGameManager reference
    private RhythmGameManager gameManager;


    public RhythmGameUI(GameScene gameScene) {
        this.gameScene = gameScene;
        // gameManager is null initially, set via setter later
    }

    // Added setter for RhythmGameManager
    public void setGameManager(RhythmGameManager gameManager) {
        this.gameManager = gameManager;
    }


    // This is the *single* setup method
    public void setup() {
        System.out.println("Setting up Rhythm Game UI...");
        // Always clean up previous UI elements
        cleanup();

        // Initialize UI elements that persist during gameplay
        scoreText = getUIFactoryService().newText("", Color.BLACK, 48);
        scoreText.setX(getAppWidth() / 2.0 - scoreText.getLayoutBounds().getWidth() / 2.0);
        scoreText.setY(50);
        scoreText.textProperty().bind(Bindings.convert(getip("score")));

        scoreText.textProperty().addListener((obs, oldV, newV) -> {
            // Recalculate position when text changes length
            scoreText.setX(getAppWidth() / 2.0 - scoreText.getLayoutBounds().getWidth() / 2.0);
        });
        gameScene.addUINode(scoreText);
        scoreText.setVisible(false); // Initially hidden


        comboText = getUIFactoryService().newText("", Color.BLACK, 24);
        comboText.setX(getAppWidth() / 2.0 - comboText.getLayoutBounds().getWidth() / 2.0);
        comboText.setY(80);
        comboText.textProperty().bind(Bindings.concat("Combo: ", getip("combo").asString()));

        comboText.textProperty().addListener((obs, oldV, newV) -> {
            // Recalculate position when text changes length
            comboText.setX(getAppWidth() / 2.0 - comboText.getLayoutBounds().getWidth() / 2.0);
        });
        gameScene.addUINode(comboText);
        comboText.setVisible(false); // Initially hidden

        // Initialize the message box for READY state using the fields
        messageBox = new VBox(10);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPrefSize(getAppWidth() * 0.6, 150);
        messageBox.setLayoutX(getAppWidth() / 2.0 - messageBox.getPrefWidth() / 2.0);
        messageBox.setLayoutY(getAppHeight() / 2.0 - messageBox.getPrefHeight() / 2.0);
        messageBox.setBackground(new Background(new BackgroundFill(Color.web("#762323C8"), null, null))); // Add background back
        messageTopLineText = getUIFactoryService().newText("", Color.WHITE, 30);
        messageBottomLineText = getUIFactoryService().newText("", Color.WHITE, 24);
        messageBox.getChildren().addAll(messageTopLineText, messageBottomLineText);

        // Add the messageBox to the scene and hide it initially
        gameScene.addUINode(messageBox);
        messageBox.setVisible(false); // Initially hidden

    } // End of the single setup method


    public void showReadyScreen() {
        // These methods now correctly access the fields initialized in setup
        if (scoreText != null) scoreText.setVisible(false);
        if (comboText != null) comboText.setVisible(false);
        if (messageBox != null) { // Always check if nodes are initialized
            messageBox.setVisible(true); // Show the message box
            if (messageTopLineText != null) messageTopLineText.setText("Press SPACE to Start");
            if (messageBottomLineText != null) messageBottomLineText.setText("Match the falling notes!");
            if (messageTopLineText != null) messageTopLineText.setVisible(true);
            if (messageBottomLineText != null) messageBottomLineText.setVisible(true);
        }
    }

    public void showPlayingUI() {

        if (scoreText != null) scoreText.setVisible(true);
        if (comboText != null) comboText.setVisible(true);
        if (messageBox != null) messageBox.setVisible(false);
    }


    public void showEndScreen(boolean songFinished, int finalScore) {
        System.out.println("Showing end screen dialog. Score: " + finalScore);


        cleanup();


        // Create content for the dialog box
        VBox dialogContent = new VBox(15); // Increased spacing
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(25)); // Add some padding

        Text messageText = getUIFactoryService().newText(songFinished ? "Song Complete!" : "Game Over!", Color.LIGHTGOLDENRODYELLOW, 36); // Larger title
        Text scoreDisplay = getUIFactoryService().newText("Final Score: " + finalScore, Color.WHITE, 28); // Score text

        dialogContent.getChildren().addAll(messageText, scoreDisplay);

        // Create the button that will trigger the return
        Button backToMenuButton = getUIFactoryService().newButton("Back to Main Menu");


        if (gameManager != null) {
            backToMenuButton.setOnAction(e -> {

                gameManager.finalizeAndReturn();
            });
        } else {
            System.err.println("RhythmGameManager not set in RhythmGameUI! Cannot finalize game properly.");

            backToMenuButton.setOnAction(e -> getGameController().gotoMainMenu());
        }


        getDialogService().showBox(
                "Game Ended", // Dialog Title (can be anything)
                dialogContent, // Content VBox
                backToMenuButton // The button(s)
        );

        System.out.println("End game dialog displayed.");
    }

    public void addFadingText(String text, double x, double y, Color color) {
        Text fadingText = getUIFactoryService().newText(text, color, 20);
        double textWidth = fadingText.getLayoutBounds().getWidth();

        fadingText.setTranslateX(x - textWidth / 2);
        fadingText.setTranslateY(y - NOTE_SIZE / 2.0);

        gameScene.addUINode(fadingText);
        fadingTexts.add(fadingText);

        Duration animationDuration = Duration.seconds(1.0);

        FadeTransition ft = new FadeTransition(animationDuration, fadingText);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);

        TranslateTransition tt = new TranslateTransition(animationDuration, fadingText);
        tt.setByY(-50);

        ft.setOnFinished(event -> {
            gameScene.removeUINode(fadingText);
            fadingTexts.remove(fadingText);
        });

        ft.play();
        tt.play();
    }

    public void cleanup() {
        System.out.println("Cleaning up Rhythm Game UI elements...");
        // Remove score, combo, and the READY message box
        if (scoreText != null) {
            gameScene.removeUINode(scoreText);
            scoreText = null; // Dereference
        }
        if (comboText != null) {
            gameScene.removeUINode(comboText);
            comboText = null; // Dereference
        }
        if (messageBox != null) { // Remove the READY message box
            gameScene.removeUINode(messageBox);
            messageBox = null; // Nullify the reference
            messageTopLineText = null; // Nullify text references
            messageBottomLineText = null;
        }



        new ArrayList<>(fadingTexts).forEach(t -> {
            gameScene.removeUINode(t);

        });
        fadingTexts.clear();


        System.out.println("UI cleanup complete.");
    }

    public void hideAll() {

        if (scoreText != null) scoreText.setVisible(false);
        if (comboText != null) comboText.setVisible(false);
        if (messageBox != null) messageBox.setVisible(false);

    }
}