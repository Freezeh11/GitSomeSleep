package org.example.capstonee.Song;

import org.example.capstonee.Song.Song;

import java.util.ArrayList;
import java.util.List;

// Static class to hold and provide song data
public class SongDatabase {

    private static final List<Song> SONGS = new ArrayList<>();

    static {
        // Add your songs here
        // Make sure the paths are correct relative to your assets folder (e.g., assets/music/songfile/beatmap.txt)
        SONGS.add(new Song(
                "Zhongli Theme",           // Name
                "Normal",                  // Difficulty
                "A calm, yet powerful theme.", // Description
                "music/songfile/thirdbosssong.txt", // Path to the beatmap file
                "music/songs/zhongli.wav"      // Path to the music file
        ));

        // Add more songs like this:
        // SONGS.add(new Song(
        //         "Another Song",
        //         "Hard",
        //         "This one is tricky!",
        //         "music/songfile/another_beatmap.txt",
        //         "music/songs/another_song.wav"
        // ));
    }

    public static List<Song> getSongs() {
        return new ArrayList<>(SONGS); // Return a copy to prevent external modification
    }

    // Helper method to find a song by its display string (Name (Difficulty))
    public static Song getSongByDisplayString(String displayString) {
        for (Song song : SONGS) {
            if (song.toString().equals(displayString)) {
                return song;
            }
        }
        return null; // Not found
    }
}