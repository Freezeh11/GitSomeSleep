package org.example.capstonee.Cutscene;

import com.almasb.fxgl.cutscene.Cutscene;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;

public class CutsceneHandler {



    public static void playCutscene(String cutsceneFileName) {
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

    }
}