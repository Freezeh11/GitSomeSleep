package org.example.capstonee.Cutscene;

import com.almasb.fxgl.cutscene.*;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.util.Duration;
import org.example.capstonee.RhythmGame.RhythmGameManager;

import java.io.IOException;
import java.util.List;


import static com.almasb.fxgl.dsl.FXGL.*;

public class CutsceneHandler {

    public static void playCutscene(String cutsceneFileName, Entity npc, RhythmGameManager rhythmGameManager) {
        if (getCutsceneService() == null) {
            System.err.println("Error: CutsceneService is not available.");
            return;
        }
        System.out.println("Attempting to load cutscene: " + cutsceneFileName);

        List<String> lines = getAssetLoader().loadText(cutsceneFileName);

        if (lines == null || lines.isEmpty()) {
            System.err.println("Error: Could not load or found empty cutscene file: " + cutsceneFileName);

            if (getDialogService() != null) {
                getDialogService().showMessageBox("Error loading cutscene file: " + cutsceneFileName);
            }
            return;
        }

        var cutscene = new Cutscene(lines);
        System.out.println("Starting cutscene: " + cutsceneFileName);

        // --- Modification Start ---
        // Define what should happen AFTER the cutscene finishes
        Runnable onCutsceneFinished = () -> {
            System.out.println("Cutscene finished. Starting Rhythm Game setup...");
            try {
                // Call the start method here, NOW that the cutscene is gone
                rhythmGameManager.start("assets/music/songfile/thirdbosssong.txt");
                // The rhythm game will now be in the READY state, waiting for SPACE
            } catch (IOException e) {
                System.err.println("Failed to start rhythm game after cutscene: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for debugging
                // Handle the error - maybe show an error dialog?
                getDialogService().showMessageBox("Error starting rhythm game: " + e.getMessage());
                // Ensure game returns to normal state if rhythm game fails to start
                // rhythmGameManager.finalizeAndReturn(); // Or a dedicated error state cleanup
            }
        };

        // Start the cutscene and provide the runnable to execute when it's done
        getCutsceneService().startCutscene(cutscene, onCutsceneFinished);

        // REMOVE the old runOnceAfter call, as it starts the rhythm game too early
        // FXGL.getGameTimer().runOnceAfter(()->{
        //     try {
        //         rhythmGameManager.start("assets/music/songfile/thirdbosssong.txt");
        //     } catch (IOException e) {
        //         throw new RuntimeException(e);
        //     }
        // }, Duration.seconds(0));
        // --- Modification End ---
    }
}