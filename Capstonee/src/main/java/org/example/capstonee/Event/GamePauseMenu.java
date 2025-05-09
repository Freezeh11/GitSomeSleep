package org.example.capstonee.Event;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.FXGLScene;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.example.capstonee.Menu.OptionsPane;
import org.example.capstonee.GameApp;


public class GamePauseMenu extends FXGLMenu {

    public GamePauseMenu() {
        super(MenuType.GAME_MENU);

        Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), Color.BLACK.deriveColor(0, 0, 0, 0.7));
        getContentRoot().getChildren().add(bg);

        Text title = FXGL.getUIFactoryService().newText("Paused", Color.WHITE, 48);
        StackPane titlePane = new StackPane(title);
        titlePane.setPrefSize(getAppWidth(), 100);
        titlePane.setAlignment(Pos.CENTER);
        titlePane.setTranslateY(getAppHeight() * 0.2);

        var btnResume = createMenuButton("Resume Game", this::fireResume);

        var btnRestartSong = createMenuButton("Restart Song", this::restartSong);

        var btnOptions = createMenuButton("Options", this::showOptionsDialog);
        var btnExitToMenu = createMenuButton("Exit to Main Menu", this::exitToMainMenu);

        VBox buttonBox = new VBox(20,
                btnResume,
                btnRestartSong,
                btnOptions,
                btnExitToMenu
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
        button.setPrefWidth(250);
        button.setPrefHeight(45);
        String buttonStyle = "-fx-font-size: 16px; -fx-background-color: #4A4A5C; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5;";
        String buttonHoverStyle = "-fx-font-size: 16px; -fx-background-color: #6A6A7C; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5;";
        button.setStyle(buttonStyle);
        button.setOnMouseEntered(e -> button.setStyle(buttonHoverStyle));
        button.setOnMouseExited(e -> button.setStyle(buttonStyle));
        return button;
    }

    private void exitToMainMenu() {
        GameApp.pauseGameMusic();
        fireExitToMainMenu();
    }

    private void restartSong() {
        GameApp.pauseGameMusic();
        GameApp.restartCurrentSong();
    }


    private void showOptionsDialog() {
        OptionsPane optionsPane = new OptionsPane();
        FXGL.getDialogService().showBox("Options", optionsPane, FXGL.getUIFactoryService().newButton("Close"));
    }


    protected void onEnteredFrom(FXGLScene prevState) {
        super.onEnteredFrom(prevState);
        System.out.println("DEBUG (PauseMenu): Entered from " + prevState.getClass().getSimpleName());
        if (prevState == FXGL.getGameScene()) {
            GameApp.pauseGameMusic();
        }
    }


    protected void onExitingTo(FXGLScene nextState) {
        super.onExitingTo(nextState);
        System.out.println("DEBUG (PauseMenu): Exiting to " + nextState.getClass().getSimpleName());
        if (nextState == FXGL.getGameScene()) {
            GameApp.resumeGameMusic();
        }
    }
}
