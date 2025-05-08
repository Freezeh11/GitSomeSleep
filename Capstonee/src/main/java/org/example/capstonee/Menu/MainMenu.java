package org.example.capstonee.Menu;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.FXGLScene;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.example.capstonee.Menu.ParallaxBackground;

public class MainMenu extends FXGLMenu {

    private ParallaxBackground parallaxBackground;

    public MainMenu() {
        super(MenuType.MAIN_MENU);

        // 1. Create and configure Parallax Background
        parallaxBackground = new ParallaxBackground();

        try {
            // *** CORRECTED PATHS ***
            // Based on your file structure: resources/assets/textures/background/
            // Remove the 's' from 'backgrounds'
            // Remove any leading spaces from filenames

            parallaxBackground.addLayer("background/nearest4.png", 5.0);
            parallaxBackground.addLayer("background/nearest3.png", 15.0);
            parallaxBackground.addLayer("background/nearest2.png", 30.0);
            parallaxBackground.addLayer("background/ nearest1.png", 35.0); // <-- Ensure NO leading space here!

            // Add the parallax background root pane FIRST
            getContentRoot().getChildren().add(parallaxBackground.getRoot());

        } catch (Exception e) {
            // This catch block is less likely to be hit now as errors are logged in addLayer,
            // but it's good to keep for unexpected issues.
            System.err.println("An unexpected error occurred setting up parallax background: " + e.getMessage());
            var fallbackBG = FXGL.texture("ui/glitch.png", getAppWidth(), getAppHeight());
            getContentRoot().getChildren().add(fallbackBG);
        }
        // ... rest of MainMenu constructor (Title, Buttons) ...
        // 2. Title
        Text title = FXGL.getUIFactoryService().newText("Platformer with Rhythm", Color.LIGHTGOLDENRODYELLOW, 52);
        StackPane titlePane = new StackPane(title);
        titlePane.setPrefSize(getAppWidth(), 200);
        titlePane.setAlignment(Pos.CENTER);
        titlePane.setTranslateY(getAppHeight() * 0.1); // Position title

        // 3. Menu Buttons
        var btnStartGame = createMenuButton("Start New Game", this::fireNewGame);
        var btnOptions = createMenuButton("Options", () -> {
            FXGL.getDialogService().showMessageBox("Options menu is not yet implemented.");
        });
        var btnExitGame = createMenuButton("Exit Game", this::fireExit);

        VBox buttonBox = new VBox(25,
                btnStartGame,
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
        var button = FXGL.getUIFactoryService().newButton(text);
        button.setOnAction(e -> action.run());
        button.setPrefWidth(300);
        button.setPrefHeight(50);
        button.setStyle("-fx-font-size: 18px; -fx-background-color: #4A4A5C; -fx-text-fill: white;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-font-size: 18px; -fx-background-color: #6A6A7C; -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-font-size: 18px; -fx-background-color: #4A4A5C; -fx-text-fill: white;"));
        return button;
    }


    protected void onEnteredFrom(FXGLScene prevState) {
        super.onEnteredFrom(prevState);
        if (parallaxBackground != null) {
            parallaxBackground.start();
        }
        // Music...
    }


    protected void onExitingTo(FXGLScene nextState) {
        super.onExitingTo(nextState);
        if (parallaxBackground != null) {
            parallaxBackground.stop();
        }
        // Music...
    }
}