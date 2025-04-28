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
import javafx.util.Duration;
import org.example.capstonee.RhythmGame.GlowEffectComponent;

import static com.almasb.fxgl.dsl.FXGL.*;

public class RhythmGameFactory implements EntityFactory {

    // ... (Keep existing constants and methods like NOTE_SIZE, LANE_X_POSITIONS, etc.) ...
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
            case 0:
                return "markers/moranote.png";
            case 1:
                return "markers/moranote.png";
            case 2:
                return "markers/moranote.png";
            case 3:
                return "markers/moranote.png";
            default:
                return "markers/moranote.png";// Fallback texture
        }
    }

    // Helper method to get the texture file name for markers (can be the same or different)
    private String getMarkerTextureName(int laneIndex) {
        // For simplicity, let's use the same textures as the notes for markers
        switch (laneIndex % NUM_LANES_INTERNAL) {
            case 0:
                return "notes/hilichurlhitmarker.png"; // Example: points to assets/textures/notes/note_orange.png
            case 1:
                return "notes/hilichurlhitmarker.png";  // Example: points to assets/textures/notes/note_green.png
            case 2:
                return "notes/hilichurlhitmarker.png";   // Example: points to assets/textures/notes/note_blue.png
            case 3:
                return "notes/hilichurlhitmarker.png";   // Example: points to assets/textures/notes/note_pink.png
            default:
                return "notes/hilichurlhitmarker.png";
        }
    }


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
                .with(new RhythmNoteComponent(NOTE_SPEED, targetHitTimestamp, laneIndex))
                .zIndex(10) // Notes Z-index
                .build();
    }

    @Spawns("glowEffect")
    public Entity newGlowEffect(SpawnData data) {
        Entity entity = FXGL.entityBuilder(data)
                .at(data.getX(), data.getY())
                // *** Use the intended particle texture ***
                .view("particles/yellow_particle.png")
                .with(new GlowEffectComponent())
                // *** Adjust Z-Index: Above notes (10) and markers (0), below potential high UI ***
                .zIndex(15)
                .build();
        return entity;
    }

    @Spawns("sparkEffect")
    public Entity newSparkEffect(SpawnData data) {
        ParticleEmitter emitter = new ParticleEmitter();
        emitter.setSourceImage(image("particles/yellow_particle.png"));
        emitter.setNumParticles(20);
        emitter.setSize(2, 4);
        emitter.setColor(Color.YELLOW);
        emitter.setExpireFunction(i -> Duration.seconds(0.5));
        emitter.setVelocityFunction(i -> new Point2D(FXGLMath.random(-150, 150), FXGLMath.random(-150, 150)));

        Entity sparkEntity = entityBuilder(data)
                .at(data.getX(), data.getY())
                .with(new ParticleComponent(emitter))
                // Make sure sparks are visible too
                .zIndex(16) // Slightly above glow? Or same level?
                .build();

        getGameTimer().runOnceAfter(sparkEntity::removeFromWorld, Duration.seconds(0.6));
        return sparkEntity;
    }

    @Spawns("HIT_ZONE_MARKER")
    public Entity newHitZoneMarker(SpawnData data) {
        int laneIndex = data.get("laneIndex");
        double hitLineY = data.get("hitLineY");
        double markerX = LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0;
        // Adjust marker Y to be centered on the hit line
        double markerY = hitLineY - NOTE_SIZE / 2.0; // Center vertically on hit line

        String textureName = getMarkerTextureName(laneIndex);
        Texture markerTexture = texture(textureName, NOTE_SIZE, NOTE_SIZE);

        return entityBuilder(data)
                .type(RhythmEntityType.HIT_ZONE_MARKER)
                .at(markerX, markerY) // Position based on lane and hit line
                .view(markerTexture)
                .zIndex(0) // Behind notes
                .build();
    }

    // Ensure you have a spawner for the rhythm background
}