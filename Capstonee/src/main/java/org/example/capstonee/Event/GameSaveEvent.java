package org.example.capstonee.Event;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Custom event fired by the GamePauseMenu when a save is requested.
 * Contains the name of the save slot to use.
 */
public class GameSaveEvent extends Event {

    // Define a unique EventType for this event
    public static final EventType<GameSaveEvent> SAVE =
            new EventType<>(Event.ANY, "GAME_SAVE");

    private final String slotName;

    public GameSaveEvent(String slotName) {
        // Call the super constructor with the event type
        super(SAVE);
        this.slotName = slotName;
    }

    public String getSlotName() {
        return slotName;
    }

    @Override
    public String toString() {
        return "GameSaveEvent{" +
                "slotName='" + slotName + '\'' +
                '}';
    }
}