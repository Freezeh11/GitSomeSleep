package org.example.capstonee.RhythmGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BeatmapLoader {

    private static final int NUM_LANES = 4; // assuming 4 lanes
    private static Random random = new Random();

    public static List<NoteInfo> loadBeatmap(String filename) {
        List<NoteInfo> beatmap = new ArrayList<>();
        System.out.println("Loading beatmap from: " + filename + " (using relative delays structure)");

        long initialDelayMs = 2000;
        int firstNoteLane = 1;
        long[][] noteSequence = {



                { 500, random.nextInt(4)},
                { 300, random.nextInt(4)},
                { 500, random.nextInt(4)},
                { 300, random.nextInt(4)},
                { 500, random.nextInt(4) },
                { 500, random.nextInt(4)},
                { 400, random.nextInt(4)},
                { 500, random.nextInt(4)},
                { 300, random.nextInt(4)},
                { 500, random.nextInt(4)},
                { 500, random.nextInt(4)},
                { 400, random.nextInt(4)},
                { 500, random.nextInt(4)},
                { 300, random.nextInt(4)},
                { 500, random.nextInt(4) },
                { 500, random.nextInt(4)},
                { 400, random.nextInt(4)},
                { 500, random.nextInt(4)},
                { 300, random.nextInt(4)},



//                { 500, 1 }, // 1 second AFTER the first hit
//                { 500, 1}, // 1 second AFTER the second hit
//                { 300, 1},  // 0.5 seconds AFTER the third hit
//                { 500, 1},  // etc.
//                { 300, 1},
//                { 500, 1 }, // 1 second AFTER the first hit
//                { 500, 1}, // 1 second AFTER the second hit
//                { 400, 1},  // 0.5 seconds AFTER the third hit
//                { 500, 1},  // etc.
//                { 300, 1},
//                { 500, 1 }, // 1 second AFTER the first hit
//                { 500, 1}, // 1 second AFTER the second hit
//                { 400, 1},  // 0.5 seconds AFTER the third hit
//                { 500, 1},  // etc.
//                { 300, 1},
//                { 500, 1 }, // 1 second AFTER the first hit
//                { 500, 1}, // 1 second AFTER the second hit
//                { 400, 1},  // 0.5 seconds AFTER the third hit
//                { 500, 1},  // etc.
//                { 300, 1},
        };


        long currentTimeMs = initialDelayMs;
        beatmap.add(new NoteInfo(currentTimeMs, firstNoteLane));
        // Log the CORRECT first timestamp
        System.out.println("DEBUG: BeatmapLoader - First Note Timestamp: " + currentTimeMs + "ms");



        for (long[] noteData : noteSequence) {
            long delay = noteData[0];
            int lane = (int) noteData[1];

            currentTimeMs += delay;
            beatmap.add(new NoteInfo(currentTimeMs, lane));

            System.out.println("DEBUG: BeatmapLoader - Added Note: Time=" + currentTimeMs + "ms, Lane=" + lane + " (Delay: " + delay + "ms)");
        }

        beatmap.sort((n1, n2) -> Long.compare(n1.getTimestampMs(), n2.getTimestampMs()));

        System.out.println("Loaded " + beatmap.size() + " notes from relative sequence.");
        return beatmap;
    }
}