package org.example.capstonee;

import com.almasb.fxgl.dsl.views.ScrollingBackgroundView;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.*;
import static org.example.capstonee.EntityType.*;

public class MapFactory implements EntityFactory {

    @Spawns("background")
    public Entity newBackground(SpawnData data) {
        return entityBuilder()
                .view(new ScrollingBackgroundView(texture("background/bruh.jpg").getImage(), getAppWidth(), getAppHeight()))
                .zIndex(-1)
                .with(new IrremovableComponent())
                .build();
    }

    @Spawns("platform")
    public Entity newPlatform(SpawnData data) {
        return entityBuilder(data)
                .type(PLATFORM)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new PhysicsComponent())
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.addGroundSensor(new HitBox("GROUND_SENSOR", new Point2D(4, 30), BoundingShape.box(8, 2))); // Adjusted for 16x16 size

        // this avoids player sticking to walls
        physics.setFixtureDef(new FixtureDef().friction(0.0f));

        return entityBuilder(data)
                .type(PLAYER)
                .bbox(new HitBox(new Point2D(0, 0), BoundingShape.box(16, 32))) //Box HitBox
                .with(physics)
                .with(new CollidableComponent(true))
                .with(new IrremovableComponent())
                .with(new PlayerComponent())
                .build();
    }

    @Spawns("npc")
    public Entity newNPC(SpawnData data) {
        boolean isMovable = data.get("isMovable");
        String dialog = data.get("dialog");
        int minX = data.get("minX");
        int maxX = data.get("maxX");
        return entityBuilder(data)
                .type(EntityType.NPC)
                .bbox(new HitBox(new Point2D(0, 0), BoundingShape.box(16, 32)))
                .viewWithBBox(new Rectangle(16, 32, Color.BLUE))
                .zIndex(-1)
                .with(new NPCComponent(isMovable, dialog, minX, maxX))
                .build();
    }

    @Spawns("interactionZone")
    public Entity newInteractionZone(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.INTERACTION_ZONE)
                .viewWithBBox(new Rectangle(16, 32, Color.RED)) // Red rectangle for debugging
                .with(new InteractionZoneComponent(data.get("npc")))
                .zIndex(-1)
                .build();
    }
}