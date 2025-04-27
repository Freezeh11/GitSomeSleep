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

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;
import static org.example.capstonee.RhythmGame.RhythmGameFactory.NOTE_SIZE;

public class RhythmGameUI {

    private Text scoreText;
    private Text comboText;
    private VBox messageBox;
    private Text messageTopLineText;
    private Text messageBottomLineText;
    private List<Text> fadingTexts = new ArrayList<>();

    private final GameScene gameScene;

    public RhythmGameUI(GameScene gameScene) {
        this.gameScene = gameScene;
    }

    public void setup() {
        System.out.println("Setting up Rhythm Game UI...");
        cleanup();

        scoreText = getUIFactoryService().newText("", Color.BLACK, 48);
        scoreText.setX(getAppWidth() / 2.0 - scoreText.getLayoutBounds().getWidth() / 2.0);
        scoreText.setY(50);
        scoreText.textProperty().bind(Bindings.convert(getip("score")));

        scoreText.textProperty().addListener((obs, oldV, newV) -> {
            scoreText.setX(getAppWidth() / 2.0 - scoreText.getLayoutBounds().getWidth() / 2.0);
        });
        gameScene.addUINode(scoreText);
        scoreText.setVisible(false);


        comboText = getUIFactoryService().newText("", Color.BLACK, 24);
        comboText.setX(getAppWidth() / 2.0 - comboText.getLayoutBounds().getWidth() / 2.0); // Initial positioning
        comboText.setY(80);
        comboText.textProperty().bind(Bindings.concat("Combo: ", getip("combo").asString()));

        comboText.textProperty().addListener((obs, oldV, newV) -> {
            comboText.setX(getAppWidth() / 2.0 - comboText.getLayoutBounds().getWidth() / 2.0);
        });
        gameScene.addUINode(comboText);
        comboText.setVisible(false);

        messageBox = new VBox(10);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPrefSize(getAppWidth() * 0.6, 150);
        messageBox.setLayoutX(getAppWidth() / 2.0 - messageBox.getPrefWidth() / 2.0);
        messageBox.setLayoutY(getAppHeight() / 2.0 - messageBox.getPrefHeight() / 2.0);
        messageBox.setBackground(new Background(new BackgroundFill(Color.web("#762323C8"), null, null)));
        messageTopLineText = getUIFactoryService().newText("", Color.WHITE, 30);
        messageBottomLineText = getUIFactoryService().newText("", Color.WHITE, 24);
        messageBox.getChildren().addAll(messageTopLineText, messageBottomLineText);

        gameScene.addUINode(messageBox);
        messageBox.setVisible(false);
    }

    public void showReadyScreen() {

        scoreText.setVisible(false);
        comboText.setVisible(false);
        messageBox.setVisible(true);
        messageTopLineText.setText("Press SPACE to Start");
        messageBottomLineText.setText("Match the falling notes!");
        messageTopLineText.setVisible(true);
        messageBottomLineText.setVisible(true);
    }

    public void showPlayingUI() {
        scoreText.setVisible(true);
        comboText.setVisible(true);
        messageBox.setVisible(false);
    }

    public void showEndScreen(boolean songFinished, int finalScore) {
        scoreText.setVisible(false);
        comboText.setVisible(false);
        messageBox.setVisible(true);
        messageTopLineText.setText(songFinished ? "Song Finished!" : "Game Over!");
        messageBottomLineText.setText("Final Score: " + finalScore + "\nPress E to Return");
        messageTopLineText.setVisible(true);
        messageBottomLineText.setVisible(true);
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
        if (scoreText != null) gameScene.removeUINode(scoreText);
        if (comboText != null) gameScene.removeUINode(comboText);
        if (messageBox != null) gameScene.removeUINode(messageBox);
        fadingTexts.forEach(gameScene::removeUINode);
        fadingTexts.clear();

        scoreText = null;
        comboText = null;
        messageBox = null;
        messageTopLineText = null;
        messageBottomLineText = null;
    }

    public void hideAll() {
        if (scoreText != null) scoreText.setVisible(false);
        if (comboText != null) comboText.setVisible(false);
        if (messageBox != null) messageBox.setVisible(false);
        fadingTexts.forEach(t -> t.setVisible(false));
    }
}