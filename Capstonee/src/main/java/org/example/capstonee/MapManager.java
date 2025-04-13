package org.example.capstonee;

import com.almasb.fxgl.dsl.FXGL;

public class MapManager {

    public static void loadMap(String mapFileName) {
        FXGL.setLevelFromMap(mapFileName); // Loads from assets/levels/
    }
}