package org.example.capstonee.Song;


public class Song {
    private final String name;
    private final String difficulty; // Could use an enum later if needed
    private final String description;
    private final String beatmapPath;   // Path to the .txt beatmap file
    private final String musicAssetPath; // Path to the actual audio file (e.g., .wav, .mp3)

    public Song(String name, String difficulty, String description, String beatmapPath, String musicAssetPath) {
        this.name = name;
        this.difficulty = difficulty;
        this.description = description;
        this.beatmapPath = beatmapPath;
        this.musicAssetPath = musicAssetPath;
    }

    public String getName() {
        return name;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getDescription() {
        return description;
    }

    public String getBeatmapPath() {
        return beatmapPath;
    }

    public String getMusicAssetPath() {
        return musicAssetPath;
    }

    // Useful for displaying in a list/choice box
    @Override
    public String toString() {
        return name + " (" + difficulty + ")";
    }
}