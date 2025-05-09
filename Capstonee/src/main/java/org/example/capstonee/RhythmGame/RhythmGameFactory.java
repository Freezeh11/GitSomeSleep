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
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.capstonee.RhythmGame.GlowEffectComponent;
import org.example.capstonee.RhythmGame.RhythmNoteComponent;
import org.example.capstonee.RhythmGame.RhythmEntityType;


import static com.almasb.fxgl.dsl.FXGL.*;

public class RhythmGameFactory implements EntityFactory {

    public static final int NOTE_SIZE = 60;
    public static final double NOTE_SPEED = 400;

    public static final double NOTE_SPAWN_Y = -NOTE_SIZE;

    public static final double HIT_LINE_Y = getAppHeight() - 150;


    private static final double LANE_AREA_WIDTH = 550;
    private static final double LANE_AREA_START_X = (getAppWidth() - LANE_AREA_WIDTH) / 2.0;
    private static final int NUM_LANES_INTERNAL = 4;
    private static final double LANE_WIDTH = NOTE_SIZE;
    private static final double LANE_SPACING = (LANE_AREA_WIDTH - (NUM_LANES_INTERNAL * LANE_WIDTH)) / (NUM_LANES_INTERNAL > 1 ? (NUM_LANES_INTERNAL - 1) : 1);

    public static final double[] LANE_X_POSITIONS = calculateLanePositions();

    private static double[] calculateLanePositions() {
        double[] positions = new double[NUM_LANES_INTERNAL];
        for (int i = 0; i < NUM_LANES_INTERNAL; i++) {
            positions[i] = LANE_AREA_START_X + i * (LANE_WIDTH + LANE_SPACING) + (NOTE_SIZE / 2.0);
        }
        return positions;
    }

    private String getNoteTextureName(int laneIndex) {
        switch (laneIndex % NUM_LANES_INTERNAL) {
            case 0: return "markers/moranote.png";
            case 1: return "markers/moranote.png";
            case 2: return "markers/moranote.png";
            case 3: return "markers/moranote.png";
            default: return "markers/moranote.png";
        }
    }

    private String getMarkerTextureName(int laneIndex) {
        switch (laneIndex % NUM_LANES_INTERNAL) {
            case 0: return "notes/hilichurlhitmarker.png";
            case 1: return "notes/hilichurlhitmarker.png";
            case 2: return "notes/hilichurlhitmarker.png";
            case 3: return "notes/hilichurlhitmarker.png";
            default: return "notes/hilichurlhitmarker.png";
        }
    }

    @Spawns("rhythmBackground")
    public Entity newRhythmBackground(SpawnData data) {
        System.out.println("DEBUG: Spawning rhythmBackground entity.");

        String backgroundTexturePath = "background/pixelatedliyue.png";
        Texture backgroundTexture;

        try {
            backgroundTexture = texture(backgroundTexturePath, getAppWidth(), getAppHeight());
        } catch (Exception e) {
            System.err.println("Failed to load background texture: " + backgroundTexturePath + ". Using black rectangle as fallback.");
            e.printStackTrace();
            backgroundTexture = new Texture(new Rectangle(getAppWidth(), getAppHeight(), Color.BLACK).snapshot(null, null));
        }


        return entityBuilder(data)
                .type(RhythmEntityType.BACKGROUND)
                .at(0, 0)
                .view(backgroundTexture)
                .zIndex(-1)
                .build();
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
                .with(new RhythmNoteComponent(NOTE_SPEED, targetHitTimestamp, laneIndex, HIT_LINE_Y))
                .zIndex(10)
                .build();
    }

    @Spawns("glowEffect")
    public Entity newGlowEffect(SpawnData data) {
        double centerX = data.getX();
        double centerY = data.getY();

        double desiredTextureSize = 100;

        Texture glowTexture = FXGL.texture("particles/yellow_particle.png", desiredTextureSize, desiredTextureSize);

        Entity entity = FXGL.entityBuilder(data)
                .at(centerX - desiredTextureSize / 2.0, centerY - desiredTextureSize / 2.0)
                .view(glowTexture)
                .with(new GlowEffectComponent())
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
                .zIndex(16)
                .build();

        return sparkEntity;
    }

    @Spawns("HIT_ZONE_MARKER")
    public Entity newHitZoneMarker(SpawnData data) {
        int laneIndex = data.get("laneIndex");
        double hitLineY = data.get("hitLineY");
        double markerX = LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0;
        double markerY = hitLineY - NOTE_SIZE / 2.0;

        String textureName = getMarkerTextureName(laneIndex);
        Texture markerTexture = texture(textureName, NOTE_SIZE, NOTE_SIZE);

        return entityBuilder(data)
                .type(RhythmEntityType.HIT_ZONE_MARKER)
                .at(markerX, markerY)
                .view(markerTexture)
                .zIndex(0)
                .build();
    }
}