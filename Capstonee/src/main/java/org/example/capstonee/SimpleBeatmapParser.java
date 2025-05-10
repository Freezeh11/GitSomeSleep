package org.example.capstonee;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class SimpleBeatmapParser {

    private String filePath;
    private int numObjects;

    public SimpleBeatmapParser(String filePath) {
        this.filePath = filePath;
        this.numObjects = 0;
    }

    /**
     * Parses the simple beatmap file and adds notes to the NoteController.
     * The file format is expected to be lines like:
     * { delay_in_ms, lane_index },
     * where lane_index is 0-based. delay_in_ms is the time AFTER the previous note.
     *
     * @param noteList The NoteController to add notes to.
     */
    public void parse(NoteController noteList) {
        File file = new File(this.filePath);
        System.out.println("Trying to load simple beatmap from: " + file.getAbsolutePath());

        if (!file.exists()) {
            System.err.println("Error: Beatmap file not found at " + file.getAbsolutePath());
            return;
        }

        double currentAbsoluteTime = 0.0; // Time in milliseconds from the start

        try (Scanner scan = new Scanner(file, "UTF-8")) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine().trim();

                // Skip empty lines and lines not starting/ending with expected format
                if (line.isEmpty() || !line.startsWith("{") || !line.endsWith("},")) {
                    // Optional: Log skipped lines if needed for debugging the file format
                    // System.out.println("Skipping line (format mismatch): " + line);
                    continue;
                }

                try {
                    // Extract content between { and },
                    String content = line.substring(1, line.length() - 2).trim(); // Remove { and }, and trim

                    String[] parts = content.split(",");

                    if (parts.length == 2) {
                        // Parse delay and lane index
                        double delay = Double.parseDouble(parts[0].trim());
                        int lane = Integer.parseInt(parts[1].trim());

                        // Calculate absolute time for this note
                        currentAbsoluteTime += delay;

                        // Clamp lane value to ensure it's within the valid range for the NoteController
                        lane = Math.max(0, Math.min(lane, noteList.getLaneCount() - 1));

                        // System.out.printf("Parsed note - Lane: %d, Delay: %.2f ms, Absolute Time: %.2f ms%n", lane, delay, currentAbsoluteTime);

                        // Create a Tap Note at the calculated absolute time
                        // Your custom format only has tap notes based on the example data
                        noteList.createTapNote(lane, currentAbsoluteTime);
                        numObjects++;

                    } else {
                        System.err.println("Skipping malformed line (incorrect number of parts): " + line);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing numbers in line: " + line + " - " + e.getMessage());
                    // Continue to the next line after a parsing error
                } catch (StringIndexOutOfBoundsException e) {
                    System.err.println("Error processing line format: " + line + " - " + e.getMessage());
                    // Continue to the next line after a format error
                } catch (Exception e) {
                    System.err.println("Unexpected error processing line: " + line + " - " + e.getMessage());
                    e.printStackTrace();
                    // Continue to the next line after an unexpected error
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("Error reading file (FileNotFoundException): " + file.getAbsolutePath());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An error occurred while reading the beatmap file: " + file.getAbsolutePath());
            e.printStackTrace();
        }

        System.out.println("Finished parsing. Total notes loaded: " + numObjects);

        // Set the latest time in NoteController based on the last note's time
        // This is used for the game loop exit condition.
        // We add a small buffer after the last note time.
        if (numObjects > 0) {
            // NoteController already updates latestEnd in addNote methods
            // We just need to ensure the game loop uses getLatest() correctly.
        } else {
            System.out.println("Warning: No notes were loaded from the beatmap file.");
            // Set a minimal latest time or handle empty beatmap scenario
            noteList.setLatest(5000); // Set a short duration if no notes
        }
    }

    // You might want getters if needed elsewhere, but not required for Main's use
    public int getNumObjects() {
        return numObjects;
    }
}