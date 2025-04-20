package org.example.capstonee.RhythmGame;

public class NoteInfo {
    long timestampMs;
    int laneIndex;

    public NoteInfo(long timestampMs, int laneIndex) {
        this.timestampMs = timestampMs;
        this.laneIndex = laneIndex;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public int getLaneIndex() {
        return laneIndex;
    }
}