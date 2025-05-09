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
// Import the new OptionsPane class
import org.example.capstonee.Menu.OptionsPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainMenu extends FXGLMenu {

    private ParallaxBackground parallaxBackground;
    private static String pendingNewGameSaveSlot = null;

    public MainMenu() {
        super(MenuType.MAIN_MENU);

        parallaxBackground = new ParallaxBackground();

        try {
            parallaxBackground.addLayer("background/nearest4.png", 5.0);
            parallaxBackground.addLayer("background/nearest3.png", 15.0);
            parallaxBackground.addLayer("background/nearest2.png", 30.0);
            parallaxBackground.addLayer("background/ nearest1.png", 35.0);

            getContentRoot().getChildren().add(parallaxBackground.getRoot());

        } catch (Exception e) {
            System.err.println("An unexpected error occurred setting up parallax background: " + e.getMessage());
            var fallbackBG = FXGL.texture("ui/glitch.png", getAppWidth(), getAppHeight());
            getContentRoot().getChildren().add(fallbackBG);
        }

        Text title = FXGL.getUIFactoryService().newText("Rhythm Impact", Color.LIGHTGOLDENRODYELLOW, 52);
        StackPane titlePane = new StackPane(title);
        titlePane.setPrefSize(getAppWidth(), 200);
        titlePane.setAlignment(Pos.CENTER);
        titlePane.setTranslateY(getAppHeight() * 0.1);

        var btnStartGame = createMenuButton("Start Game", this::showStartOrLoadDialog);

        var btnOptions = createMenuButton("Options", this::showOptionsDialog);

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
        button.setStyle("-fx-font-size: 18px; -fx-background-color: #036661; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-font-size: 18px; -fx-background-color: #047F79; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-font-size: 18px; -fx-background-color: #036661; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5;"));
        return button;
    }

    private void showStartOrLoadDialog() {
        FXGL.getDialogService().showChoiceBox(
                "Choose an option:",
                List.of("Start New Game", "Load Game"),
                (chosenOption) -> {
                    if (chosenOption == null) {
                        return;
                    }

                    if ("Start New Game".equals(chosenOption)) {
                        showChooseNewGameSaveSlotDialog();
                    } else if ("Load Game".equals(chosenOption)) {
                        showSimulatedLoadGameDialog();
                    }
                });
    }

    private void showChooseNewGameSaveSlotDialog() {
        // nice if fxgl savefile shit pero wa ko
        List<String> simulatedSaveSlots = new ArrayList<>();
        simulatedSaveSlots.add("Save Slot 1 (Filled)");
        simulatedSaveSlots.add("Save Slot 2 (Empty)");
        simulatedSaveSlots.add("Save Slot 3 (Filled)");
        simulatedSaveSlots.add("Save Slot 4 (Empty)");

        List<String> options = new ArrayList<>(simulatedSaveSlots);
        options.add("Cancel");

        FXGL.getDialogService().showChoiceBox(
                "Choose a slot to save your new game:",
                options,
                (chosenSlotName) -> {
                    if (chosenSlotName == null || "Cancel".equals(chosenSlotName)) {
                        showStartOrLoadDialog();
                        return;
                    }

                    if (chosenSlotName.contains("(Empty)")) {
                        prepareAndFireNewGame(chosenSlotName);
                    } else {
                        FXGL.getDialogService().showConfirmationBox(
                                "Overwrite existing save in\n" + chosenSlotName + "?",
                                (confirmed) -> {
                                    if (confirmed) {
                                        prepareAndFireNewGame(chosenSlotName);
                                    } else {
                                        showChooseNewGameSaveSlotDialog();
                                    }
                                });
                    }
                });
    }

    private void prepareAndFireNewGame(String slotName) {
        MainMenu.pendingNewGameSaveSlot = slotName;
        fireNewGame();
    }

    private void showSimulatedLoadGameDialog() {

        //eme eme
        List<String> simulatedSaveSlots = new ArrayList<>();
        simulatedSaveSlots.add("Save Slot 1 (Progress 50%)");
        simulatedSaveSlots.add("Save Slot 2 (Empty)");
        simulatedSaveSlots.add("Save Slot 3 (Completed)");
        simulatedSaveSlots.add("Save Slot 4 (Empty)");

        List<String> loadableSlots = new ArrayList<>();
        for (String slot : simulatedSaveSlots) {
            if (!slot.contains("(Empty)")) {
                loadableSlots.add(slot);
            }
        }

        if (loadableSlots.isEmpty()) {
            FXGL.getDialogService().showMessageBox("No saved games found to load.", () -> {
                showStartOrLoadDialog();
            });
        } else {
            loadableSlots.add("cancel");

            FXGL.getDialogService().showChoiceBox(
                    "select a game to load:",
                    loadableSlots,
                    (chosenSlotName) -> {
                        if (chosenSlotName == null || "cancel".equals(chosenSlotName)) {
                            showStartOrLoadDialog();
                            return;
                        }

                        FXGL.getDialogService().showMessageBox(
                                "attempting to load: " + chosenSlotName + "\n(loading not implemented in this menu pa)",
                                () -> {
                                    // Replace this with your actual loading logic
                                    // fireNewGame(); // <-- This starts a new game, replace with actual loading
                                    // You need code here (or call a method elsewhere) that reads
                                    // the data for 'chosenSlotName' using your file I/O
                                    // and sets up the game state accordingly.
                                });
                    });
        }
    }

    // Method to show the Options dialog using the custom OptionsPane
    private void showOptionsDialog() {
        OptionsPane optionsPane = new OptionsPane();

        // Show the options pane inside a dialog box
        // Add a "Close" button to the dialog itself
        FXGL.getDialogService().showBox("Options", optionsPane, FXGL.getUIFactoryService().newButton("Close"));
    }

    public static String consumePendingNewGameSaveSlot() {
        String slot = pendingNewGameSaveSlot;
        pendingNewGameSaveSlot = null;
        return slot;
    }

    protected void onEnteredFrom(FXGLScene prevState) {
        super.onEnteredFrom(prevState);
        if (parallaxBackground != null) {
            parallaxBackground.start();
        }
    }

    protected void onExitingTo(FXGLScene nextState) {
        super.onExitingTo(nextState);
        if (parallaxBackground != null) {
            parallaxBackground.stop();
        }
        pendingNewGameSaveSlot = null;
    }
}