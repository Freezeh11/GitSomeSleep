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
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.example.capstonee.RhythmGame.RhythmEntityType;

import static com.almasb.fxgl.dsl.FXGL.*;
import static org.example.capstonee.EntityType.*;

public class MapFactory implements EntityFactory {

@Spawns("rhythmBackground")
    public Entity rhythmBackground(SpawnData data) {
        return entityBuilder()
                .view(new ScrollingBackgroundView(texture("background/pixelatedliyue.png").getImage(), getAppWidth(), getAppHeight()))
                .zIndex(-1)
                .with(new IrremovableComponent())
                .build();
    }




    @Spawns("background")
    public Entity newBackground(SpawnData data) {
        return entityBuilder()
                .view(new ScrollingBackgroundView(texture("background/Black.png").getImage(), getAppWidth(), getAppHeight()))
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

    // In MapFactory.java
    @Spawns("spawnPoint")
    public Entity newSpawnPoint(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.SPAWN_POINT)
                .with(new SpawnPointComponent())
                .build();
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.addGroundSensor(new HitBox("GROUND_SENSOR", new Point2D(4, 30), BoundingShape.box(8, 2)));

        physics.setFixtureDef(new FixtureDef().friction(0.0f));

        return entityBuilder(data)
                .type(PLAYER)
                .bbox(new HitBox(new Point2D(0, 0), BoundingShape.box(16, 32)))
                .with(physics)
                .with(new CollidableComponent(true))
                .with(new IrremovableComponent())
                .with(new PlayerComponent())
                .zIndex(100)  // Higher than default tiles (which are usually 0)
                .build();
    }

    @Spawns("npc")
    public Entity newNPC(SpawnData data) {
        System.out.println("Creating NPC at: " + data.getX() + "," + data.getY()); // Debug

        boolean isMovable = data.get("isMovable");
        String dialog = data.get("dialog");
        int minX = data.get("minX");
        int maxX = data.get("maxX");

        // Create a more visible NPC (red with white border)
        Rectangle visual = new Rectangle(16, 32, Color.RED);
        visual.setStroke(Color.WHITE);
        visual.setStrokeWidth(2);

        return entityBuilder(data)
                .type(EntityType.NPC)
                .bbox(new HitBox(new Point2D(0, 0), BoundingShape.box(16, 32)))
                .viewWithBBox(visual) // Highly visible appearance
                .zIndex(100) // Above most other entities
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
    int num = 0;
    @Spawns("nextMapTrigger")
    public Entity newNextMapTrigger(SpawnData data) {
        num++;
        String nextMap = "tmx/level"+ num +".tmx";
        System.out.println("DEBUG: Forcing nextMap to: " + nextMap);

        return entityBuilder(data)
                .type(EntityType.NEXT_MAP)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .with(new NextMapComponent(nextMap))
                .build();
    }

    @Spawns("text")
    public Entity newText(SpawnData data) {
        // Default values
        String textContent = "Default Text";
        int fontSize = 16;
        Color color = Color.WHITE;

        // Try to get properties from the object (if they exist in properties section)
        if (data.hasKey("text")) {
            textContent = data.get("text");
        }
        if (data.hasKey("fontSize")) {
            fontSize = data.get("fontSize");
        }
        if (data.hasKey("color")) {
            Object colorObj = data.get("color");
            color = colorObj instanceof Color ? (Color)colorObj : Color.web(colorObj.toString());
        }

        // Create text with bounding box for better positioning
        Text text = new Text(textContent);
        text.setFont(Font.font(fontSize));
        text.setFill(color);

        return entityBuilder(data)
                .type(EntityType.TEXT)
                .viewWithBBox(text)  // Use viewWithBBox for proper positioning
                .with(new TextComponent(textContent, fontSize, color))
                .zIndex(1000)  // Higher z-index to ensure visibility
                .build();
    }
}