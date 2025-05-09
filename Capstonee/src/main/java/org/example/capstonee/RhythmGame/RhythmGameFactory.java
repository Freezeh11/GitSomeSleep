package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.geometry.Point2D;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.particle.ParticleEmitter;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle; // Import Rectangle for basic background option
import javafx.util.Duration;
import org.example.capstonee.RhythmGame.GlowEffectComponent; // Assuming this is your glow component
import org.example.capstonee.RhythmGame.RhythmNoteComponent; // Assuming this is your note component
import org.example.capstonee.RhythmGame.RhythmEntityType; // Assuming this is your entity type enum


import static com.almasb.fxgl.dsl.FXGL.*;

public class RhythmGameFactory implements EntityFactory {

    // --- Define constants used by RhythmGameManager ---
    public static final int NOTE_SIZE = 60;
    public static final double NOTE_SPEED = 400; // pixels per second

    public static final double NOTE_SPAWN_Y = -NOTE_SIZE; // Start notes NOTE_SIZE pixels above Y=0

    // NOTE: HIT_LINE_Y is used in RhythmGameManager but is not defined here.
    // It should likely be a constant accessible to both.
    // For now, defining it here for marker placement consistency
    public static final double HIT_LINE_Y = getAppHeight() - 150; // Example: adjust based on your UI


    private static final double LANE_AREA_WIDTH = 550;
    private static final double LANE_AREA_START_X = (getAppWidth() - LANE_AREA_WIDTH) / 2.0; // Use getAppWidth()
    private static final int NUM_LANES_INTERNAL = 4;
    private static final double LANE_WIDTH = NOTE_SIZE; // Assuming lane width is note size
    // Adjusted lane spacing calculation
    private static final double LANE_SPACING = (LANE_AREA_WIDTH - (NUM_LANES_INTERNAL * LANE_WIDTH)) / (NUM_LANES_INTERNAL > 1 ? (NUM_LANES_INTERNAL - 1) : 1);

    public static final double[] LANE_X_POSITIONS = calculateLanePositions();

    // Calculate the center X position for each lane
    private static double[] calculateLanePositions() {
        double[] positions = new double[NUM_LANES_INTERNAL];
        for (int i = 0; i < NUM_LANES_INTERNAL; i++) {
            // Positions based on the left edge of the lane block + half lane width to get center
            positions[i] = LANE_AREA_START_X + i * (LANE_WIDTH + LANE_SPACING) + (NOTE_SIZE / 2.0);
        }
        return positions;
    }

    // Helper method to get the texture file name based on lane index
    private String getNoteTextureName(int laneIndex) {
        // Ensure lane index is within bounds for consistent mapping
        switch (laneIndex % NUM_LANES_INTERNAL) {
            case 0: return "markers/moranote.png"; // Replace with your actual texture paths
            case 1: return "markers/moranote.png";
            case 2: return "markers/moranote.png";
            case 3: return "markers/moranote.png";
            default: return "markers/moranote.png";// Fallback texture
        }
    }

    // Helper method to get the texture file name for markers (can be the same or different)
    private String getMarkerTextureName(int laneIndex) {
        // Replace with your actual texture paths for hit zone markers
        switch (laneIndex % NUM_LANES_INTERNAL) {
            case 0: return "notes/hilichurlhitmarker.png";
            case 1: return "notes/hilichurlhitmarker.png";
            case 2: return "notes/hilichurlhitmarker.png";
            case 3: return "notes/hilichurlhitmarker.png";
            default: return "notes/hilichurlhitmarker.png";
        }
    }

    // --- ADDED: Spawn method for the rhythm game background ---
    @Spawns("rhythmBackground")
    public Entity newRhythmBackground(SpawnData data) {
        System.out.println("DEBUG: Spawning rhythmBackground entity.");

        // You need an actual texture file in assets/textures/background/
        String backgroundTexturePath = "background/pixelatedliyue.png"; // <-- REPLACE WITH YOUR TEXTURE PATH
        Texture backgroundTexture;

        try {
            backgroundTexture = texture(backgroundTexturePath, getAppWidth(), getAppHeight());
        } catch (Exception e) {
            System.err.println("Failed to load background texture: " + backgroundTexturePath + ". Using black rectangle as fallback.");
            e.printStackTrace();
            // Fallback to a simple black rectangle if texture loading fails
            backgroundTexture = new Texture(new Rectangle(getAppWidth(), getAppHeight(), Color.BLACK).snapshot(null, null));
        }


        return entityBuilder(data)
                .type(RhythmEntityType.BACKGROUND) // Assuming you have RhythmEntityType.BACKGROUND
                .at(0, 0) // Position at the top-left corner
                .view(backgroundTexture) // Use the loaded texture (or fallback)
                .zIndex(-1) // Put background behind all other entities
                .build();
    }
    // --- END ADDED BACKGROUND SPAWNER ---


    @Spawns("RHYTHM_NOTE")
    public Entity newRhythmNote(SpawnData data) {
        int laneIndex = data.get("laneIndex");
        long targetHitTimestamp = data.get("targetHitTimestamp");
        double spawnX = LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0;
        double spawnY = NOTE_SPAWN_Y;
        String textureName = getNoteTextureName(laneIndex);
        Texture noteTexture = texture(textureName, NOTE_SIZE, NOTE_SIZE);

        return entityBuilder(data)
                .type(RhythmEntityType.RHYTHM_NOTE)
                .at(spawnX, spawnY)
                .viewWithBBox(noteTexture)
                // Pass all necessary info to the component
                .with(new RhythmNoteComponent(NOTE_SPEED, targetHitTimestamp, laneIndex, HIT_LINE_Y)) // <-- Added HIT_LINE_Y
                .zIndex(10) // Notes Z-index
                .build();
    }

    @Spawns("glowEffect")
    public Entity newGlowEffect(SpawnData data) {
        // Get the center position from spawn data
        double centerX = data.getX();
        double centerY = data.getY();

        // *** DEFINE YOUR DESIRED SIZE FOR THE GLOW TEXTURE HERE ***
        double desiredTextureSize = 100; // Example: Make the glow texture 100x100

        // Load the texture with the specified size
        Texture glowTexture = FXGL.texture("particles/yellow_particle.png", desiredTextureSize, desiredTextureSize); // <-- Use your actual glow texture

        Entity entity = FXGL.entityBuilder(data)
                // Position the entity so its center is at the spawned coordinates (centerX, centerY)
                .at(centerX - desiredTextureSize / 2.0, centerY - desiredTextureSize / 2.0)
                .view(glowTexture)
                // Pass duration or other properties to the component if needed
                .with(new GlowEffectComponent()) // Assuming GlowEffectComponent manages fading
                .zIndex(15) // Ensure it's visible above notes/markers
                .build();

        // The GlowEffectComponent should handle the timed removal
        // getGameTimer().runOnceAfter(entity::removeFromWorld, Duration.seconds(GlowEffectComponent.LIFETIME)); // Example if component doesn't remove itself

        return entity;
    }

    @Spawns("sparkEffect")
    public Entity newSparkEffect(SpawnData data) {
        ParticleEmitter emitter = new ParticleEmitter();
        // Use your actual spark particle image
        emitter.setSourceImage(image("particles/yellow_particle.png")); // <-- Use your actual spark texture
        emitter.setNumParticles(20);
        emitter.setSize(2, 4);
        emitter.setColor(Color.YELLOW); // Adjust color
        emitter.setExpireFunction(i -> Duration.seconds(0.5));
        emitter.setVelocityFunction(i -> new Point2D(FXGLMath.random(-150, 150), FXGLMath.random(-150, 150)));
        // Adjust emission point/shape if needed
        // emitter.setSpawnPointFunction(i -> new Point2D(0, 0));

        Entity sparkEntity = entityBuilder(data)
                .at(data.getX(), data.getY()) // Spawn at the provided coordinates
                .with(new ParticleComponent(emitter))
                .zIndex(16) // Slightly above glow? Or same level?
                .build();

        // ParticleComponent automatically removes the entity when the emitter is done
        // getGameTimer().runOnceAfter(sparkEntity::removeFromWorld, Duration.seconds(0.6)); // Usually not needed with ParticleComponent
        return sparkEntity;
    }

    @Spawns("HIT_ZONE_MARKER")
    public Entity newHitZoneMarker(SpawnData data) {
        int laneIndex = data.get("laneIndex");
        double hitLineY = data.get("hitLineY"); // Get the correct hit line Y from SpawnData
        double markerX = LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0;
        // Adjust marker Y to be centered on the hit line
        double markerY = hitLineY - NOTE_SIZE / 2.0; // Center vertically on hit line

        String textureName = getMarkerTextureName(laneIndex);
        Texture markerTexture = texture(textureName, NOTE_SIZE, NOTE_SIZE);

        return entityBuilder(data)
                .type(RhythmEntityType.HIT_ZONE_MARKER) // Assuming you have RhythmEntityType.HIT_ZONE_MARKER
                .at(markerX, markerY) // Position based on lane and hit line
                .view(markerTexture)
                .zIndex(0) // Behind notes
                .build();
    }

    // Potential Inconsistency Note:
    // The RhythmGameManager uses HIT_LINE_Y for miss checking and calculating FALL_DURATION_MS.
    // Ensure the HIT_LINE_Y constant used in RhythmGameManager is the same value as defined here.
    // Making HIT_LINE_Y public static final here and referencing it from RhythmGameManager is recommended.
}