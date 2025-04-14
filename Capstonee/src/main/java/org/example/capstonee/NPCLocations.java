package org.example.capstonee;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;


public class NPCLocations {

    public static void spawnNPCs() {
        Entity npc = FXGL.spawn("npc", new SpawnData(288.00, 239)
                .put("isMovable", true)
                .put("dialog", "YAWAAAAAA")
                .put("minX", 270)
                .put("maxX", 305));
        FXGL.spawn("interactionZone", new SpawnData(288.00, 239).put("npc", npc));

        Entity npc2 = FXGL.spawn("npc", new SpawnData(192, 192)
                .put("isMovable", false)
                .put("dialog", "HOLY SHET")
                .put("minX", 0)
                .put("maxX", 0));
        FXGL.spawn("interactionZone", new SpawnData(192, 192).put("npc", npc2));
    }
}