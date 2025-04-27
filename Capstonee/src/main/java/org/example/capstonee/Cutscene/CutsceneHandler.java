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
        getCutsceneService().startCutscene(cutscene);
//        rhythmGameManager.start("beatmap.txt");

        FXGL.getGameTimer().runOnceAfter(()->{
            try {
                rhythmGameManager.start("assets/music/songfile/thirdbosssong.txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, Duration.seconds(0));
    }
}