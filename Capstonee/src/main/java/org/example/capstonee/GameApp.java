package org.example.capstonee;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

import com.almasb.fxgl.entity.Entity;
import javafx.scene.text.Text;

import java.io.InputStream;
import java.util.Map;

public class GameApp extends GameApplication {

    private Entity player;

    @Override
    protected void initSettings(GameSettings settings) {
        AppConfig.configure(settings);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        GameVars.init(vars);
    }

    @Override
    protected void initGame() {
        InputStream is = getClass().getResourceAsStream("/assets/maps/test.tmx");
        System.out.println(is != null ? "Found!" : "Not found!");
        // Load the map first
        MapManager.loadMap("test.tmx");
        player = PlayerFactory.createPlayer(300, 300);

    }

    @Override
    protected void initInput() {
        InputManager.setupPlayerMovement(() -> player);
    }

    @Override
    protected void initUI() {
        Text ui = UIManager.createPixelCounterText();
        UIManager.addToScene(ui);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
