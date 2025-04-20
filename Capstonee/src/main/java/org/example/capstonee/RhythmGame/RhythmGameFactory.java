package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class RhythmGameFactory implements EntityFactory {

    public static final int NOTE_SIZE = 60;
    public static final double NOTE_SPEED = 400; // pixels per second

   private static final double LANE_AREA_WIDTH = 550;
    private static final double LANE_AREA_START_X = (1280 - LANE_AREA_WIDTH) / 2.0;
    private static final int NUM_LANES_INTERNAL = 4;
    private static final double LANE_WIDTH = NOTE_SIZE;
    private static final double LANE_SPACING = (LANE_AREA_WIDTH - (NUM_LANES_INTERNAL * LANE_WIDTH)) / (NUM_LANES_INTERNAL -1);
    public static final double[] LANE_X_POSITIONS = calculateLanePositions();

    private static double[] calculateLanePositions() {
        double[] positions = new double[NUM_LANES_INTERNAL];
        for (int i = 0; i < NUM_LANES_INTERNAL; i++) {
          positions[i] = LANE_AREA_START_X + i * (LANE_WIDTH + LANE_SPACING) + (NOTE_SIZE / 2.0);
        }
        return positions;
    }


    @Spawns("RHYTHM_NOTE")
    public Entity newRhythmNote(SpawnData data) {
        int laneIndex = data.get("laneIndex");
        long targetHitTimestamp = data.get("targetHitTimestamp");

        double spawnX = LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0;
        double spawnY = -NOTE_SIZE;

        return entityBuilder(data)
                .type(RhythmEntityType.RHYTHM_NOTE)
                .at(spawnX, spawnY)
                .viewWithBBox(new Circle(NOTE_SIZE / 2.0, getNoteColor(laneIndex)))
                .with(new RhythmNoteComponent(NOTE_SPEED, targetHitTimestamp, laneIndex))
                .zIndex(10)
                .build();
    }

    @Spawns("HIT_ZONE_MARKER")
    public Entity newHitZoneMarker(SpawnData data) {
        int laneIndex = data.get("laneIndex");
        double hitLineY = data.get("hitLineY");

        double markerX = LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0;

        double markerY = hitLineY - NOTE_SIZE / 2.0;

        // optional but here we can add em background rectz for the lane linesz!
        /*
        entityBuilder()
            .at(markerX, 0)
            .view(new Rectangle(NOTE_SIZE, getAppHeight(), Color.rgb(200, 200, 200, 0.1)))
            .zIndex(-1)
            .buildAndAttach();
        */


        Circle markerCircle = new Circle(NOTE_SIZE / 2.0);
        markerCircle.setStroke(getNoteColor(laneIndex).darker());
        markerCircle.setStrokeWidth(2);
        markerCircle.setFill(Color.TRANSPARENT);

        return entityBuilder(data)
                .type(RhythmEntityType.HIT_ZONE_MARKER)
                .at(markerX, markerY)
                .view(markerCircle)
                .zIndex(0)
                .build();
    }


    private Color getNoteColor(int laneIndex) {

        switch (laneIndex % NUM_LANES_INTERNAL) {
            case 0: return Color.web("#FF5733C8"); // orange-red
            case 1: return Color.web("#33FF57C8"); // bright green?
            case 2: return Color.web("#3357FFC8"); // bright blue
            case 3: return Color.web("#FF33F6C8"); // pink
            default: return Color.GRAY; // gtfo
        }
    }
}