package org.example.capstonee.Menu;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.core.Inject;

import com.almasb.fxgl.app.scene.MenuType;
import org.example.capstonee.Event.GamePauseMenu;

public class MenuSceneFactory extends SceneFactory {

    @Override
    public FXGLMenu newMainMenu() {
        return new MainMenu();
    }

    @Override
    public FXGLMenu newGameMenu() {
        return new GamePauseMenu();
    }
}