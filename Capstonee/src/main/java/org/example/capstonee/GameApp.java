package org.example.capstonee;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {

    private double tpf;

    @Override
    protected void onUpdate(double tpf) {
        this.tpf = tpf;
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Game App");
        settings.setVersion("0.1");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setSceneFactory(new SceneFactory());
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    private Entity player;

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Left") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).left();
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.A, VirtualButton.LEFT);

        getInput().addAction(new UserAction("Right") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).right();
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.D, VirtualButton.RIGHT);

        getInput().addAction(new UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                player.getComponent(PlayerComponent.class).jump();
            }
        }, KeyCode.W, VirtualButton.A);

        getInput().addAction(new UserAction("Interact") {
            @Override
            protected void onActionBegin() {
                var interactionZones = getGameWorld().getEntitiesByType(EntityType.INTERACTION_ZONE);
                for (Entity zone : interactionZones) {
                    if (player.isColliding(zone)) {
                        Entity npc = zone.getComponent(InteractionZoneComponent.class).getNpc();
                        String dialog = npc.getComponent(NPCComponent.class).getDialog();
                        System.out.println("NPC says: " + dialog);
                        break;
                    }
                }
            }
        }, KeyCode.E);

    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new MapFactory());

        player = null;
        Level level = getAssetLoader().loadLevel("tmx/test.tmx", new TMXLevelLoader());
        getGameWorld().setLevel(level);

        player = spawn("player", 16, 16);
        set("player", player);

        spawn("background");

        NPCLocations.spawnNPCs();

        Viewport viewport = getGameScene().getViewport();
        viewport.setBounds(-1500, 0, 250 * 70, getAppHeight());
        viewport.bindToEntity(player, getAppWidth() / 2, getAppHeight() / 2);
        viewport.setLazy(true);

        viewport.setZoom(3.0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
