package org.example.capstonee;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.util.Duration;

import java.util.ArrayList;

public class NPCLocations {
    public static void spawnNPCs(String levelName) {
        System.out.println("Attempting to spawn NPCs for level: " + levelName); // Debug

        FXGL.runOnce(() -> {
            // Clear existing NPCs
            var existingNPCs = new ArrayList<>(FXGL.getGameWorld().getEntitiesByType(EntityType.NPC));
            var existingZones = new ArrayList<>(FXGL.getGameWorld().getEntitiesByType(EntityType.INTERACTION_ZONE));

            System.out.println("Clearing " + existingNPCs.size() + " existing NPCs"); // Debug

            existingNPCs.forEach(Entity::removeFromWorld);
            existingZones.forEach(Entity::removeFromWorld);

            // Spawn new ones with exact string matching
            switch (levelName.toLowerCase()) { // Case-insensitive check
                case "tutorial":
                    System.out.println("Spawning tutorial NPCs");
                    spawnTutorialNPCs();
                    break;
                case "level1":
                    System.out.println("Spawning level1 NPCs");
                    spawnLevel1NPCs();
                    break;
                default:
                    System.out.println("Unknown level: " + levelName);
            }
        }, Duration.seconds(0.05));
    }

    private static void spawnTutorialNPCs() {
        Entity npc = FXGL.spawn("npc", new SpawnData(64, 112)
                .put("isMovable", false)
                .put("dialog", "YAWAAAAAA")
                .put("minX", 0)
                .put("maxX", 0));
        FXGL.spawn("interactionZone", new SpawnData(64, 112).put("npc", npc));
    }

    private static void spawnLevel1NPCs() {
        Entity npc = FXGL.spawn("npc", new SpawnData(145, 704)
                .put("isMovable", false)
                .put("dialog", "Welcome to Level 1!")
                .put("minX", 0)
                .put("maxX", 0));
        FXGL.spawn("interactionZone", new SpawnData(145, 704).put("npc", npc));
    }
}