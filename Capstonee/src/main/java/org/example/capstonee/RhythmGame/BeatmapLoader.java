package org.example.capstonee.RhythmGame;

import java.util.ArrayList;
import java.util.List;

import static org.example.capstonee.RhythmGame.RhythmGameFactory.LANE_X_POSITIONS;


public class BeatmapLoader {

    private static final int NUM_LANES = LANE_X_POSITIONS.length;

    public static List<NoteInfo> loadBeatmap(String filename) {
        List<NoteInfo> beatmap = new ArrayList<>();
        System.out.println("Loading beatmap from: " + filename + " (using placeholder logic)");

        long currentTime = 1000;
        long timeBetweenNotes = 300;

        for (int i = 0; i < 100; i++) {
            int lane = i % NUM_LANES;
            beatmap.add(new NoteInfo(currentTime, lane));
            currentTime += timeBetweenNotes;
        }

        // wala pa ni
        beatmap.sort((n1, n2) -> Long.compare(n1.getTimestampMs(), n2.getTimestampMs()));

        System.out.println("Loaded " + beatmap.size() + " notes into beatmap.");
        return beatmap;
    }
}