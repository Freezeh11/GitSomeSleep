package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.texture.Texture; // Import Texture
import javafx.scene.paint.Color; // Keep Color if using it elsewhere or for debug
import javafx.scene.shape.Circle; // Keep if you still use it (though we're removing it from spawns)
import javafx.scene.shape.Rectangle; // Keep if you still use it

import static com.almasb.fxgl.dsl.FXGL.*;

public class RhythmGameFactory implements EntityFactory {

    public static final int NOTE_SIZE = 60;
    public static final double NOTE_SPEED = 400; // pixels per second

    public static final double NOTE_SPAWN_Y = -NOTE_SIZE; // Start notes NOTE_SIZE pixels above Y=0

    private static final double LANE_AREA_WIDTH = 550;
    private static final double LANE_AREA_START_X = (getAppWidth() - LANE_AREA_WIDTH) / 2.0; // Use getAppWidth()
    private static final int NUM_LANES_INTERNAL = 4;
    private static final double LANE_WIDTH = NOTE_SIZE;
    private static final double LANE_SPACING = (LANE_AREA_WIDTH - (NUM_LANES_INTERNAL * LANE_WIDTH)) / (NUM_LANES_INTERNAL > 1 ? (NUM_LANES_INTERNAL - 1) : 1);

    public static final double[] LANE_X_POSITIONS = calculateLanePositions();

    // Calculate the center X position for each lane
    private static double[] calculateLanePositions() {
        double[] positions = new double[NUM_LANES_INTERNAL];
        for (int i = 0; i < NUM_LANES_INTERNAL; i++) {
            // Position is the start of the lane area + offset for lane number + half lane width to get center
            positions[i] = LANE_AREA_START_X + i * (LANE_WIDTH + LANE_SPACING) + (NOTE_SIZE / 2.0);
        }
        return positions;
    }

    // Helper method to get the texture file name based on lane index
    private String getNoteTextureName(int laneIndex) {
        // Ensure lane index is within bounds for consistent mapping
        switch (laneIndex % NUM_LANES_INTERNAL) {
            case 0: return "markers/moranote.png";
            case 1: return "markers/moranote.png";
            case 2: return "markers/moranote.png";
            case 3: return "markers/moranote.png";
            default: return "markers/moranote.png";// Fallback texture
        }
    }

    // Helper method to get the texture file name for markers (can be the same or different)
    private String getMarkerTextureName(int laneIndex) {
        // For simplicity, let's use the same textures as the notes for markers



         switch (laneIndex % NUM_LANES_INTERNAL) {
             case 0: return "notes/hilichurlhitmarker.png"; // Example: points to assets/textures/notes/note_orange.png
             case 1: return "notes/hilichurlhitmarker.png";  // Example: points to assets/textures/notes/note_green.png
             case 2: return "notes/hilichurlhitmarker.png";   // Example: points to assets/textures/notes/note_blue.png
             case 3: return "notes/hilichurlhitmarker.png";   // Example: points to assets/textures/notes/note_pink.png
             default: return "notes/hilichurlhitmarker.png";
         }

    }


    @Spawns("RHYTHM_NOTE")
    public Entity newRhythmNote(SpawnData data) {
        int laneIndex = data.get("laneIndex");
        long targetHitTimestamp = data.get("targetHitTimestamp");

        double spawnX = LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0;
        double spawnY = NOTE_SPAWN_Y;

        // Get the texture name based on the lane
        String textureName = getNoteTextureName(laneIndex);

        // Load the texture and size it to NOTE_SIZE x NOTE_SIZE
        Texture noteTexture = texture(textureName, NOTE_SIZE, NOTE_SIZE);

        return entityBuilder(data)
                .type(RhythmEntityType.RHYTHM_NOTE)
                .at(spawnX, spawnY)
                // Use viewWithBBox with the texture. FXGL will create a bounding box based on the texture size.
                .viewWithBBox(noteTexture)
                // Pass NOTE_SPEED to the component as it's used there for movement
                .with(new RhythmNoteComponent(NOTE_SPEED, targetHitTimestamp, laneIndex))
                .zIndex(10) // Ensure notes appear above markers and background
                .build();
    }

    @Spawns("HIT_ZONE_MARKER")
    public Entity newHitZoneMarker(SpawnData data) {
        int laneIndex = data.get("laneIndex");
        double hitLineY = data.get("hitLineY");

        // Position the marker centered within the lane at the hit line Y
        double markerX = LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0;
        // Position the *top-left* of the texture such that its *center* is at hitLineY
        double markerY = hitLineY - NOTE_SIZE / 2.0;


        // Get the texture name for the marker
        String textureName = getMarkerTextureName(laneIndex);

        // Load the texture and size it
        Texture markerTexture = texture(textureName, NOTE_SIZE, NOTE_SIZE);

        // Optional: Add some visual effect to the marker to distinguish it from notes,
        // e.g., make it slightly transparent or change its blend mode if needed,
        // but this depends on the image content itself.
        // markerTexture.setOpacity(0.5); // Example transparency


        return entityBuilder(data)
                .type(RhythmEntityType.HIT_ZONE_MARKER)
                .at(markerX, markerY)
                // Use view() as BoundingBox is likely not needed for the marker itself.
                .view(markerTexture)
                .zIndex(0) // Between background/lanes and notes
                .build();
    }

    // The getNoteColor helper is no longer used for visuals but might be needed elsewhere.
    // You can remove it if it's not called from other classes.
    // private Color getNoteColor(int laneIndex) { ... }
}