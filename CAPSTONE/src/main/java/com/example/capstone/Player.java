package com.example.capstone;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import java.util.Set;

public class Player extends ImageView {
    // Physics constants
    private static final double GRAVITY = 0.5;
    private static final double TERMINAL_VELOCITY = 15;
    private static final double JUMP_FORCE = -12;
    private static final double MAX_SPEED = 6;
    private static final double ACCELERATION = 0.5;
    private static final double FRICTION = 0.85;

    // Timing constants
    private static final double COYOTE_TIME_MAX = 0.15; // 150ms coyote time
    private static final double JUMP_BUFFER_MAX = 0.1;  // 100ms jump buffer
    private static final double TIME_STEP = 1/60.0;

    // State variables
    double velocityX = 0;
    double velocityY = 0;
    private boolean onGround = false;
    private boolean canJump = true;
    private double coyoteTime = 0;
    private double jumpBufferTime = 0;
    private boolean wallCollision = false;

    public Player() {
        try {
            // Load the player image from resources
            Image playerImage = new Image(getClass().getResourceAsStream("/assets/testplayer.png"));
            setImage(playerImage);
            setFitWidth(40);
            setFitHeight(40);
            setTranslateX(100);
            setTranslateY(100);
        } catch (Exception e) {
            System.err.println("Error loading player image: " + e.getMessage());

            // Fallback to a rectangle if image fails to load
            setImage(null);
            setFitWidth(40);
            setFitHeight(40);
            setTranslateX(100);
            setTranslateY(100);
            setStyle("-fx-fill: blue;");
        }
    }

    public void update(Set<KeyCode> pressedKeys) {
        // max fall speed
        if (!onGround) {
            velocityY = Math.min(velocityY + GRAVITY, TERMINAL_VELOCITY);
        }

        // Handle horizontal movement with acceleration/deceleration
        if (pressedKeys.contains(KeyCode.LEFT)) {
            velocityX = Math.max(velocityX - ACCELERATION, -MAX_SPEED);
        } else if (pressedKeys.contains(KeyCode.RIGHT)) {
            velocityX = Math.min(velocityX + ACCELERATION, MAX_SPEED);
        } else {

            // Apply friction when no keys are pressed
            velocityX *= FRICTION;
            if (Math.abs(velocityX) < 0.1) velocityX = 0;
        }

        // Jump handling with coyote time and jump buffering
        if (pressedKeys.contains(KeyCode.SPACE)) {
            if ((onGround || coyoteTime > 0) && canJump) {
                velocityY = JUMP_FORCE;
                onGround = false;
                canJump = false;
                coyoteTime = 0;
            }
            // Set jump buffer when in air
            if (!onGround) {
                jumpBufferTime = JUMP_BUFFER_MAX;
            }
        } else {
            // Reset jump ability when space is released
            canJump = true;
        }

        // Update coyote time (allows jumping shortly after leaving ground)
        if (onGround) {
            coyoteTime = COYOTE_TIME_MAX;
        } else {
            coyoteTime = Math.max(0, coyoteTime - TIME_STEP);
        }

        // Check jump buffer for delayed jumps
        if (jumpBufferTime > 0 && onGround) {
            velocityY = JUMP_FORCE;
            onGround = false;
            jumpBufferTime = 0;
        } else {
            jumpBufferTime = Math.max(0, jumpBufferTime - TIME_STEP);
        }

        // Apply velocity to position
        setTranslateX(getTranslateX() + velocityX);
        setTranslateY(getTranslateY() + velocityY);
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public void setWallCollision(boolean wallCollision) {
        this.wallCollision = wallCollision;
    }

    public boolean hasWallCollision() {
        return wallCollision;
    }

}