package org.example.capstonee;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PlayerFactory {

    public static Entity createPlayer(double x, double y) {

        return FXGL.entityBuilder()
                .type(EntityType.PLAYER)
                .at(x, y)
                .view(new Rectangle(25, 25, Color.BLUE))
                .with(new PlayerComponent())
                .buildAndAttach();
    }
}