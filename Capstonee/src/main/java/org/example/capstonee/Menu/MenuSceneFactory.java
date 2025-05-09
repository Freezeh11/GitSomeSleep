package org.example.capstonee.Menu;


import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;


public class MenuSceneFactory extends SceneFactory {


    @Override
    public FXGLMenu newMainMenu() {
        return new MainMenu();
    }



}