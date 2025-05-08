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
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import javafx.scene.input.KeyCode;
import org.example.capstonee.Cutscene.CutsceneHandler;
import org.example.capstonee.Menu.MenuSceneFactory;
import org.example.capstonee.RhythmGame.*;
import org.example.capstonee.Menu.MenuSceneFactory; // <--- IMPORT YOUR NEW FACTORY


import java.util.Map;


import static com.almasb.fxgl.dsl.FXGL.*;


public class GameApp extends GameApplication {


    private Entity player;
    private RhythmAudioPlayer rhythmAudioPlayer;
    private RhythmGameUI rhythmGameUI;
    private RhythmGameManager rhythmGameManager;


    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Platformer with Rhythm");
        settings.setVersion("0.2");
        settings.setWidth(1280);
        settings.setHeight(720);
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




    @Override
    protected void initGame() {




        rhythmAudioPlayer = new RhythmAudioPlayer();
        rhythmGameUI = new RhythmGameUI(getGameScene());
        rhythmGameManager = new RhythmGameManager(getGameScene(), rhythmGameUI, rhythmAudioPlayer);
        rhythmGameManager.setOnGameEndCallback(this::returnToPlatformerMode);


        getGameWorld().addEntityFactory(new MapFactory());
        getGameWorld().addEntityFactory(new RhythmGameFactory());


        Level level = getAssetLoader().loadLevel("tmx/test.tmx", new TMXLevelLoader());
        getGameWorld().setLevel(level);


        player = spawn("player", 16, 16);
        set("player", player);
        spawn("background") ;


        NPCLocations.spawnNPCs();


        configurePlatformerViewport();


        System.out.println("Game Initialized - Platformer Mode Active.");
        System.out.println("DEBUG: Global Music Volume: " + getSettings().getGlobalMusicVolume());
        System.out.println("DEBUG: Global Sound Volume: " + getSettings().getGlobalSoundVolume());
    }


    @Override
    protected void initGameVars(Map<String, Object> vars) {


        vars.put("score", 0);
        vars.put("combo", 0);
        vars.put("songElapsedTimeMs", 0L);
    }


    @Override
    protected void onUpdate(double tpf) {
        if (rhythmGameManager.isActive()) {
            rhythmGameManager.update(tpf);
        }
    }




    private void configurePlatformerViewport() {
        Viewport viewport = getGameScene().getViewport();
        viewport.setBounds(-1500, 0, 250 * 70, getAppHeight());
        viewport.bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);
        viewport.setLazy(true);
        viewport.setZoom(3.0);
    }


    private void returnToPlatformerMode() {
        System.out.println("Transitioning back to Platformer Mode...");


        configurePlatformerViewport();
        getGameWorld().getEntitiesCopy().forEach(e -> e.setVisible(true));
        if (player != null) {
            player.setVisible(true);
            player.getComponent(PlayerComponent.class).stop();
        }


        rhythmGameUI.hideAll();


        System.out.println("Platformer Mode Restored.");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
