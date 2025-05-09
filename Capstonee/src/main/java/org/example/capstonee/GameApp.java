package org.example.capstonee;


import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
// Remove the old SceneFactory import if it was just the default one.
// import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
import com.almasb.fxgl.event.EventBus;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.physics.CollisionHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import org.example.capstonee.Cutscene.CutsceneHandler;
import org.example.capstonee.Menu.MenuSceneFactory;
import org.example.capstonee.RhythmGame.*;
import org.example.capstonee.Menu.MenuSceneFactory; // <--- IMPORT YOUR NEW FACTORY


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import static com.almasb.fxgl.dsl.FXGL.*;


public class GameApp extends GameApplication {


    private Entity player;
    private String nextLevelToLoad = null;
    private boolean isTransitioning = false;
    private String currentLevel = "tutorial";
    private RhythmAudioPlayer rhythmAudioPlayer;
    private RhythmGameUI rhythmGameUI;
    private RhythmGameManager rhythmGameManager;


    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Platformer with Rhythm");
        settings.setVersion("0.2");
        settings.setWidth(1280);
        settings.setHeight(720);;
        // settings.setSceneFactory(new SceneFactory()); // <--- REMOVE THIS LINE (or the default one)
        settings.setSceneFactory(new MenuSceneFactory()); // <--- ADD THIS LINE
        settings.setMainMenuEnabled(true);
        // settings.setGameMenuEnabled(true); // This is usually enabled by default if MainMenu is enabled
        settings.setApplicationMode(ApplicationMode.DEVELOPER); // Or ApplicationMode.RELEASE
    }

    @Override
    protected void initInput() {


        getInput().addAction(new UserAction("Left") {
            @Override
            protected void onAction() {
                if (!rhythmGameManager.isActive()) player.getComponent(PlayerComponent.class).left();
            }


            @Override
            protected void onActionEnd() {
                if (!rhythmGameManager.isActive()) player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.A, VirtualButton.LEFT);


        getInput().addAction(new UserAction("Right") {
            @Override
            protected void onAction() {
                if (!rhythmGameManager.isActive()) player.getComponent(PlayerComponent.class).right();
            }


            @Override
            protected void onActionEnd() {
                if (!rhythmGameManager.isActive()) player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.D, VirtualButton.RIGHT);


        getInput().addAction(new UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                if (!rhythmGameManager.isActive()) player.getComponent(PlayerComponent.class).jump();
            }
        }, KeyCode.W, VirtualButton.A);




        getInput().addAction(new UserAction("Interact / Confirm") {
            @Override
            protected void onActionBegin() {
                if (!rhythmGameManager.isActive()) {
                    var interactionZones = getGameWorld().getEntitiesByType(EntityType.INTERACTION_ZONE);
                    for (Entity zone : interactionZones) {
                        if (player.isColliding(zone)) {
                            Entity npc = zone.getComponent(InteractionZoneComponent.class).getNpc();
                            String dialog = npc.getComponent(NPCComponent.class).getDialog();
                            System.out.println("NPC says: " + dialog + " - Starting Rhythm Game!");
                            CutsceneHandler.playCutscene("gameCutscene_sample1.txt", npc, rhythmGameManager);
                            break;
                        }
                    }


                } else if (rhythmGameManager.getState() == RhythmGameState.GAME_ENDED) {
                    rhythmGameManager.finalizeAndReturn();
                }


            }
        }, KeyCode.E);




        getInput().addAction(new UserAction("RhythmLane0") {
            @Override
            protected void onActionBegin() {
                rhythmGameManager.handleInput(0);
            }
        }, KeyCode.H);


        getInput().addAction(new UserAction("RhythmLane1") {
            @Override
            protected void onActionBegin() {
                rhythmGameManager.handleInput(1);
            }
        }, KeyCode.J);


        getInput().addAction(new UserAction("RhythmLane2") {
            @Override
            protected void onActionBegin() {
                rhythmGameManager.handleInput(2);
            }
        }, KeyCode.K);


        getInput().addAction(new UserAction("RhythmLane3") {
            @Override
            protected void onActionBegin() {
                rhythmGameManager.handleInput(3);
            }
        }, KeyCode.L);




        getInput().addAction(new UserAction("RhythmStart") {
            @Override
            protected void onActionBegin() {
                if (rhythmGameManager.isActive() && rhythmGameManager.getState() == RhythmGameState.READY) {
                    rhythmGameManager.startPlaying();
                }
            }
        }, KeyCode.SPACE);
    }

    private int levelWidth;
    private int levelHeight;
    private void loadLevel(String levelFile) {
        try {
            System.out.println("Attempting to load: " + levelFile);

            if (rhythmAudioPlayer != null) {
                rhythmAudioPlayer.stopAll();
            }

            getGameController().pauseEngine();
            getGameWorld().getEntitiesCopy()
                    .stream()
                    .filter(e -> e.getType() != EntityType.PLAYER)
                    .forEach(Entity::removeFromWorld);

            // Load level
            Level level = getAssetLoader().loadLevel(levelFile, new TMXLevelLoader());
            getGameWorld().setLevel(level);

            this.currentLevel = levelFile.replace("tmx/", "").replace(".tmx", "");
            levelWidth = level.getWidth() * 16;
            levelHeight = level.getHeight() * 16;

            // In your loadLevel() method:
            Point2D spawnPosition = getGameWorld().getEntitiesByType(EntityType.SPAWN_POINT)
                    .stream()
                    .findFirst()
                    .map(e -> {
                        System.out.println("Found spawn point at: " + e.getPosition()); // Debug log
                        return e.getPosition();
                    })
                    .orElseThrow(() -> new RuntimeException("No spawn point found in level: " + levelFile));

            // Add debug output to verify player position
            System.out.println("Setting player position to: " + spawnPosition);
            player.setPosition(spawnPosition);
            System.out.println("Actual player position after set: " + player.getPosition());


            configurePlatformerViewport();
            spawn("background");
            NPCLocations.spawnNPCs(currentLevel);

            getGameController().resumeEngine();
            System.out.println("Successfully loaded: " + levelFile);

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR loading " + levelFile);
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    protected void initGame() {
        rhythmAudioPlayer = new RhythmAudioPlayer();
        rhythmGameUI = new RhythmGameUI(getGameScene());
        rhythmGameManager = new RhythmGameManager(getGameScene(), rhythmGameUI, rhythmAudioPlayer);
        rhythmGameManager.setOnGameEndCallback(this::returnToPlatformerMode);

        getGameWorld().addEntityFactory(new MapFactory());
        getGameWorld().addEntityFactory(new RhythmGameFactory());

        // Load level first to get dimensions
        Level level = getAssetLoader().loadLevel("tmx/tutorial.tmx", new TMXLevelLoader());
        getGameWorld().setLevel(level);

        // Set level dimensions
        levelWidth = level.getWidth() * 16;
        levelHeight = level.getHeight() * 16;

        // Find spawn point
        Point2D spawnPosition = getGameWorld().getEntitiesByType(EntityType.SPAWN_POINT)
                .stream()
                .findFirst()
                .map(Entity::getPosition)
                .orElseThrow(() -> new RuntimeException("No spawn point found in initial level"));

        // Now create player at spawn point
        player = spawn("player", spawnPosition.getX(), spawnPosition.getY());
        set("player", player);
        spawn("background");
        NPCLocations.spawnNPCs(currentLevel);

        // Configure viewport after level and player are set up
        configurePlatformerViewport();

        System.out.println("Game Initialized - Platformer Mode Active.");

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.NEXT_MAP) {
            @Override
            protected void onCollisionBegin(Entity player, Entity trigger) {
                System.out.println("DEBUG: Collision detected with nextMapTrigger!");

                if (rhythmGameManager.isActive()) {
                    rhythmGameManager.finalizeAndReturn();
                }

                if (rhythmAudioPlayer.isMusicPlaying()) {
                    rhythmAudioPlayer.stopAll();
                }

                String nextMap = trigger.getComponent(NextMapComponent.class).getNextMap();
                System.out.println("Player hit trigger! Loading: " + nextMap);
                loadLevel(nextMap);
            }
        });
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {


        vars.put("score", 0);
        vars.put("combo", 0);
        vars.put("songElapsedTimeMs", 0L);
    }


    @Override
    protected void onUpdate(double tpf) {
        if (isTransitioning) return;

        // Safety check - if we're not in rhythm game but audio is playing, stop it
        if (!rhythmGameManager.isActive() && rhythmAudioPlayer.isMusicPlaying()) {
            rhythmAudioPlayer.stopAll();
        }

        if (rhythmGameManager.isActive()) {
            rhythmGameManager.update(tpf);
        }
    }




    private void configurePlatformerViewport() {
        Viewport viewport = getGameScene().getViewport();
        viewport.setBounds(0, 0, levelWidth, levelHeight);
        viewport.bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);
        viewport.setLazy(true);
        viewport.setZoom(3.0); // Comment out this line temporarily
    }


    private void returnToPlatformerMode() {
        FXGL.runOnce(() -> {
            configurePlatformerViewport();
            getGameWorld().getEntitiesCopy().forEach(e -> {
                if (e.getType() != RhythmEntityType.RHYTHM_NOTE &&
                        e.getType() != RhythmEntityType.HIT_ZONE_MARKER &&
                        e.getType() != RhythmEntityType.BACKGROUND) {
                    e.setVisible(true);
                }
            });

            if (player != null) {
                player.setVisible(true);
                player.getComponent(PlayerComponent.class).stop();
            }

            rhythmGameUI.hideAll();
            spawn("background");
            NPCLocations.spawnNPCs(currentLevel);
        }, Duration.seconds(0)); // Added Duration parameter
    }


    public static void main(String[] args) {
        launch(args);
    }
}
