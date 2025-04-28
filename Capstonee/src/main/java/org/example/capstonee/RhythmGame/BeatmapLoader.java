package org.example.capstonee.RhythmGame;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BeatmapLoader {

    private static final int NUM_LANES = 4; // assuming 4 lanes
    private static Random random = new Random();

    private static final long DEFAULT_INITIAL_DELAY_MS = 5000;
    private static final int DEFAULT_FIRST_NOTE_LANE = 1;


    public static List<NoteInfo> loadBeatmapFile(String resourcePath) throws IOException {
        List<NoteInfo> beatmap = new ArrayList<>();
        System.out.println("Attempting to load beatmap resource: " + resourcePath);
        InputStream is = BeatmapLoader.class.getClassLoader().getResourceAsStream(resourcePath);

        if (is == null) {
            String errorMessage = "Error: Beatmap resource not found: " + resourcePath;
            System.err.println(errorMessage);
            throw new IOException(errorMessage);
        }
        long currentTimeMs = DEFAULT_INITIAL_DELAY_MS;
        int firstNoteLane = DEFAULT_FIRST_NOTE_LANE;
        beatmap.add(new NoteInfo(currentTimeMs, firstNoteLane));
        System.out.println("DEBUG: BeatmapLoader - Added First Note (Initial): Time=" + currentTimeMs + "ms, Lane=" + firstNoteLane);


        int lineNumber = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            System.out.println("DEBUG: Starting to read lines from resource stream.");
            while ((line = br.readLine()) != null) {
                lineNumber++;

                String originalLine = line;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) {
                    continue;
                }

                if (line.endsWith(",")) {
                    line = line.substring(0, line.length() - 1).trim();
                }
                if (!line.startsWith("{") || !line.endsWith("}")) {
                    System.err.println("Warning: Line " + lineNumber + ": Skipping malformed line (missing/incorrect braces or format): " + originalLine); // Updated warning message
                    continue;
                }
                String content;
                try {
                    content = line.substring(1, line.length() - 1).trim();
                } catch (IndexOutOfBoundsException e) {
                    System.err.println("Warning: Line " + lineNumber + ": Skipping malformed line (content extraction error): " + originalLine);
                    continue;
                }
                String[] parts = content.split(",");
                if (parts.length != 2) {
                    System.err.println("Warning: Line " + lineNumber + ": Skipping malformed line (incorrect comma count " + parts.length + "): " + originalLine);
                    continue;
                }
                try {
                    long delay = Long.parseLong(parts[0].trim());
                    int lane = Integer.parseInt(parts[1].trim());
                    if (lane < 0 || lane >= NUM_LANES) {
                        System.err.println("Warning: Line " + lineNumber + ": Skipping malformed line (invalid lane " + lane + ", must be 0-" + (NUM_LANES - 1) + "): " + originalLine);
                        continue;
                    }
                    currentTimeMs += delay;
                    beatmap.add(new NoteInfo(currentTimeMs, lane));
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Line " + lineNumber + ": Skipping malformed line (parsing error '" + e.getMessage() + "'): " + originalLine);
                }
            }
            System.out.println("DEBUG: Finished reading lines from resource stream."); // Add debug print here
        } catch (IOException e) {
            String errorMessage = "FATAL ERROR: Failed to read beatmap stream for resource: " + resourcePath + " - " + e.getMessage();
            System.err.println(errorMessage);
            throw new IOException(errorMessage, e);
        }
        beatmap.sort((n1, n2) -> Long.compare(n1.getTimestampMs(), n2.getTimestampMs()));
        System.out.println("DEBUG: Successfully loaded " + beatmap.size() + " notes from resource: " + resourcePath);
        return beatmap;
    }

}