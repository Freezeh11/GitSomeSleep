package com.example.capstone;

import javafx.scene.input.KeyCode;
import java.util.HashSet;
import java.util.Set;

public class InputHandler {
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    public void handleKeyPress(KeyCode code) {
        pressedKeys.add(code);
    }

    public void handleKeyRelease(KeyCode code) {
        pressedKeys.remove(code);
    }

    public Set<KeyCode> getPressedKeys() {
        return pressedKeys;
    }
}