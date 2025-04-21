package org.example.capstonee;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;


public class MovableBlockLocations {

    public static void spawnBlocks() {
        FXGL.spawn("movableBlock", new SpawnData(64, 64));

        FXGL.spawn("movableBlock", new SpawnData(128, 128));

    }
}