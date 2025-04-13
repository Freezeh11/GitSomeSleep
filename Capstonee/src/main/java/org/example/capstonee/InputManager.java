package org.example.capstonee;

import com.almasb.fxgl.entity.Entity;
import javafx.scene.input.KeyCode;

import java.util.function.Supplier;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

public class InputManager {

    public static void setupPlayerMovement(Supplier<Entity> getPlayer) {
        onKey(KeyCode.D, () -> {
            Entity player = getPlayer.get();
            if (player != null)
                player.translateX(5);
            inc("pixelsMoved", +5);
            return null;
        });

        onKey(KeyCode.A, () -> {
            Entity player = getPlayer.get();
            if (player != null)
                player.translateX(-5);
            inc("pixelsMoved", -5);
            return null;
        });

        onKey(KeyCode.W, () -> {
            Entity player = getPlayer.get();
            if (player != null)
                player.translateY(-5);
            inc("pixelsMoved", +5);
            return null;
        });

        onKey(KeyCode.S, () -> {
            Entity player = getPlayer.get();
            if (player != null)
                player.translateY(5);
            inc("pixelsMoved", +5);
            return null;
        });
    }
}
