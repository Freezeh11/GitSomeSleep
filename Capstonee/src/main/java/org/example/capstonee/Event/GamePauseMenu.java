package org.example.capstonee.Event;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.example.capstonee.Event.GameSaveEvent; // We will create this next
import org.example.capstonee.Menu.OptionsPane; // Import the OptionsPane

import java.util.ArrayList;
import java.util.List;


public class GamePauseMenu extends FXGLMenu {

    public GamePauseMenu() {
        super(MenuType.GAME_MENU);

        // Simple dark background overlay
        Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), Color.BLACK.deriveColor(0, 0, 0, 0.7));
        getContentRoot().getChildren().add(bg);

        // Title
        Text title = FXGL.getUIFactoryService().newText("Paused", Color.WHITE, 48);
        StackPane titlePane = new StackPane(title);
        titlePane.setPrefSize(getAppWidth(), 100);
        titlePane.setAlignment(Pos.CENTER);
        titlePane.setTranslateY(getAppHeight() * 0.2);

        // Buttons
        var btnResume = createMenuButton("Resume Game", this::fireResume); // Built-in resume
        var btnSaveGame = createMenuButton("Save Game", this::showChooseSaveSlotDialog); // Need to pick a slot
        var btnOptions = createMenuButton("Options", this::showOptionsDialog); // Show audio options
        var btnExitToMenu = createMenuButton("Exit to Main Menu", this::fireExitToMainMenu); // Built-in exit to main menu

        VBox buttonBox = new VBox(20,
                btnResume,
                btnSaveGame,
                btnOptions,
                btnExitToMenu
        );
        buttonBox.setAlignment(Pos.CENTER);

        StackPane buttonsPane = new StackPane(buttonBox);
        buttonsPane.setPrefSize(getAppWidth(), getAppHeight());
        buttonsPane.setAlignment(Pos.CENTER);
        buttonsPane.setTranslateY(getAppHeight() * 0.15); // Position below title

        getContentRoot().getChildren().addAll(titlePane, buttonsPane);
    }

    // Helper method to create styled buttons (can be slightly different from MainMenu)
    private Node createMenuButton(String text, Runnable action) {
        var button = FXGL.getUIFactoryService().newButton(text);
        button.setOnAction(e -> action.run());
        button.setPrefWidth(250); // Slightly narrower for pause menu? Adjust as needed
        button.setPrefHeight(45);
        String buttonStyle = "-fx-font-size: 16px; -fx-background-color: #4A4A5C; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5;";
        String buttonHoverStyle = "-fx-font-size: 16px; -fx-background-color: #6A6A7C; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5;";
        button.setStyle(buttonStyle);
        button.setOnMouseEntered(e -> button.setStyle(buttonHoverStyle));
        button.setOnMouseExited(e -> button.setStyle(buttonStyle));
        return button;
    }

    // --- Save Game Slot Selection ---
    private void showChooseSaveSlotDialog() {
        // --- Simulate save slots using a hardcoded list ---
        // In a real game, you would dynamically check if a slot file exists
        List<String> simulatedSaveSlots = new ArrayList<>();
        simulatedSaveSlots.add("Save Slot 1 (Filled)"); // Example of a filled slot
        simulatedSaveSlots.add("Save Slot 2 (Empty)"); // Example of an empty slot
        simulatedSaveSlots.add("Save Slot 3 (Filled)");
        simulatedSaveSlots.add("Save Slot 4 (Empty)");

        List<String> options = new ArrayList<>(simulatedSaveSlots);
        options.add("Cancel");

        FXGL.getDialogService().showChoiceBox(
                "Choose a slot to save to:",
                options,
                (chosenSlotName) -> {
                    if (chosenSlotName == null || "Cancel".equals(chosenSlotName)) {
                        // User cancelled, do nothing (stay on pause menu)
                        return;
                    }

                    // --- Handle Slot Selection ---
                    if (chosenSlotName.contains("(Empty)")) {
                        // User selected an empty slot, trigger save
                        triggerSave(chosenSlotName);
                    } else {
                        // User selected a filled slot, ask for confirmation to overwrite
                        FXGL.getDialogService().showConfirmationBox(
                                "Overwrite existing save in\n" + chosenSlotName + "?",
                                (confirmed) -> {
                                    if (confirmed) {
                                        // User confirmed overwrite, trigger save
                                        triggerSave(chosenSlotName);
                                    } else {
                                        // User cancelled overwrite, show slot selection again
                                        showChooseSaveSlotDialog();
                                    }
                                });
                    }
                });
    }

    // Method to fire the save event
    private void triggerSave(String slotName) {
        // Fire a custom event that GameApp listens for
        FXGL.getEventBus().fireEvent(new GameSaveEvent(slotName));

        // Optionally show a confirmation message
        FXGL.getDialogService().showMessageBox("Game state for slot " + slotName + " requested to be saved.", () -> {
            // Dialog closed, stay on pause menu or resume? Usually stay paused.
        });
    }


    // --- Show Options Dialog ---
    private void showOptionsDialog() {
        OptionsPane optionsPane = new OptionsPane();

        // Show the options pane inside a dialog box
        // Add a "Close" button to the dialog itself
        // The dialog is modal and will pause the game rendering behind it (handled by FXGL)
        FXGL.getDialogService().showBox("Options", optionsPane, FXGL.getUIFactoryService().newButton("Close"));
        // The dialog will close when the user clicks the "Close" button.
        // The OptionsPane handles the audio changes directly via its controls.
    }
}