package org.example.capstonee.RhythmGame; // Package kept as per your structure

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class RhythmGameFactory implements EntityFactory {


    public static final int NOTE_SIZE = 60;
    public static final double NOTE_SPEED = 400;

    private static final double APP_WIDTH = 1280;
    public static final double[] LANE_X_POSITIONS = {

            430 + NOTE_SIZE/2.0,
            430 + NOTE_SIZE + 60 + NOTE_SIZE/2.0,
            430 + (NOTE_SIZE + 60)*2 + NOTE_SIZE/2.0,
            430 + (NOTE_SIZE + 60)*3 + NOTE_SIZE/2.0
    };


    @Spawns("RHYTHM_NOTE")
    public Entity newRhythmNote(SpawnData data) {
        int laneIndex = data.get("laneIndex");
        long targetHitTimestamp = data.get("targetHitTimestamp");

        double spawnY = -NOTE_SIZE;

        return entityBuilder(data)
                .type(RhythmEntityType.RHYTHM_NOTE)
               .at(LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0, spawnY)
               .viewWithBBox(new Circle(NOTE_SIZE / 2.0, getNoteColor(laneIndex))) // Circle view with bounding box
                .with(new RhythmNoteComponent(NOTE_SPEED, targetHitTimestamp, laneIndex))
                .build();
    }

    @Spawns("HIT_ZONE_MARKER")
    public Entity newHitZoneMarker(SpawnData data) {
        int laneIndex = data.get("laneIndex");
        double hitLineY = data.get("hitLineY");
        return entityBuilder(data)
                .type(RhythmEntityType.HIT_ZONE_MARKER)
                .at(LANE_X_POSITIONS[laneIndex] - NOTE_SIZE / 2.0, hitLineY - NOTE_SIZE / 2.0)
                .view(new Circle(NOTE_SIZE / 2.0))
                .zIndex(0)
                .build();
    }


    private Color getNoteColor(int laneIndex) {
        switch (laneIndex) {
            case 0: return Color.web("#FF0000C8"); // red
            case 1: return Color.web("#00FF00C8"); // green
            case 2: return Color.web("#0000FFC8"); // blue
            case 3: return Color.web("#BA981CC8"); // mama mo
            default: return Color.GRAY; // fallback
        }
    }
}