package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle; // Added import

import static com.almasb.fxgl.dsl.FXGL.*; // Assuming you use FXGL methods here

public class RhythmGameFactory implements EntityFactory {

    public static final int NOTE_SIZE = 60;
    public static final double NOTE_SPEED = 400; // pixels per second

    // --- Define NOTE_SPAWN_Y here ---
    // Notes are spawned slightly above the screen top to give them time to become visible
    public static final double NOTE_SPAWN_Y = -NOTE_SIZE; // Start notes NOTE_SIZE pixels above Y=0

    private static final double LANE_AREA_WIDTH = 550;
    private static final double LANE_AREA_START_X = (1280 - LANE_AREA_WIDTH) / 2.0;
    private static final int NUM_LANES_INTERNAL = 4; // Using a clear internal name
    private static final double LANE_WIDTH = NOTE_SIZE;
    // Assuming uniform spacing between lanes:
    private static final double LANE_SPACING = (LANE_AREA_WIDTH - (NUM_LANES_INTERNAL * LANE_WIDTH)) / (NUM_LANES_INTERNAL > 1 ? (NUM_LANES_INTERNAL - 1) : 1); // Handle single lane case

    // Public constant for LANE_X_POSITIONS accessible from RhythmGameManager
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


    @Spawns("RHYTHM_NOTE")
    public Entity newRhythmNote(SpawnData data) {
        int laneIndex = data.get("laneIndex");
        long targetHitTimestamp = data.get("targetHitTimestamp");

        // Use the defined NOTE_SPAWN_Y constant
        double spawnX = LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0;
        // double spawnY = -NOTE_SIZE; // Old hardcoded value
        double spawnY = NOTE_SPAWN_Y; // <-- Use the constant

        return entityBuilder(data)
                .type(RhythmEntityType.RHYTHM_NOTE)
                .at(spawnX, spawnY)
                // Using a Circle view as per your code
                .viewWithBBox(new Circle(NOTE_SIZE / 2.0, getNoteColor(laneIndex)))
                // Pass NOTE_SPEED to the component as it's used there for movement
                .with(new RhythmNoteComponent(NOTE_SPEED, targetHitTimestamp, laneIndex))
                .zIndex(10) // Ensure notes appear above markers and background
                .build();
    }

    @Spawns("HIT_ZONE_MARKER")
    public Entity newHitZoneMarker(SpawnData data) {
        int laneIndex = data.get("laneIndex");
        double hitLineY = data.get("hitLineY"); // Y coordinate of the hit line passed from manager

        // Position the marker centered within the lane at the hit line Y
        double markerX = LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0;

        // Use the hitLineY passed from the manager
        double markerY = hitLineY - NOTE_SIZE / 2.0; // Position the *top* of the marker shape so its center is at hitLineY or adjust based on shape

        // Optional: Add lane background visuals here if needed
        /*
        entityBuilder()
            .at(LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0, 0) // Start at the top of the screen
            .view(new Rectangle(NOTE_SIZE, getAppHeight(), Color.rgb(200, 200, 200, 0.1))) // Semi-transparent rectangle covering the lane
            .zIndex(-1) // Behind everything
            .with(new com.almasb.fxgl.entity.components.IrremovableComponent()) // Prevent removal during general cleanup
            .buildAndAttach(getGameWorld()); // Build and add directly
        */


        // Circle visual for the hit zone marker
        Circle markerCircle = new Circle(NOTE_SIZE / 2.0);
        markerCircle.setStroke(getNoteColor(laneIndex).darker());
        markerCircle.setStrokeWidth(2);
        markerCircle.setFill(Color.TRANSPARENT); // Make the inside transparent

        return entityBuilder(data)
                .type(RhythmEntityType.HIT_ZONE_MARKER)
                .at(markerX, markerY)
                .view(markerCircle)
                .zIndex(0) // Between background/lanes and notes
                .build();
    }

    // Helper method for getting note colors
    private Color getNoteColor(int laneIndex) {
        // Ensure lane index is within bounds for consistent color mapping
        switch (laneIndex % NUM_LANES_INTERNAL) {
            case 0: return Color.web("#FF5733C8"); // orange-red with some transparency
            case 1: return Color.web("#33FF57C8"); // bright green with transparency
            case 2: return Color.web("#3357FFC8"); // bright blue with transparency
            case 3: return Color.web("#FF33F6C8"); // pink with transparency
            default: return Color.GRAY; // Fallback color
        }
    }
}