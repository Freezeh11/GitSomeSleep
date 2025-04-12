package com.example.capstone;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Game extends Application {
    private Pane root = new Pane();
    private Player player;
    private MapLoader mapLoader;
    private InputHandler inputHandler = new InputHandler();

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(root, 800, 600);

        // Create player with image
        player = new Player();
        root.getChildren().add(player);

        // Load map
        mapLoader = new MapLoader("src/main/resources/map/testmap.tmx", root);

        // Setup input handling
        scene.setOnKeyPressed(e -> inputHandler.handleKeyPress(e.getCode()));
        scene.setOnKeyReleased(e -> inputHandler.handleKeyRelease(e.getCode()));

        // Game loop
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
        timer.start();

        stage.setScene(scene);
        stage.setTitle("JavaFX 2D Platformer");
        stage.show();
    }

    private void update() {
        player.update(inputHandler.getPressedKeys());
        mapLoader.handleCollisions(player);
    }

    public static void main(String[] args) {
        launch();
    }
}