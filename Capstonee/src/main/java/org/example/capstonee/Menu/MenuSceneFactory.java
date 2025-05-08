package org.example.capstonee.Menu;


import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;


public class MenuSceneFactory extends SceneFactory {


    @Override
    public FXGLMenu newMainMenu() {
        return new MainMenu(); // Assumes MainMenu.java is in this package or imported
    }


    // REMOVE THE newGameMenu() method override if you want the FXGL default.
    // FXGL will provide its own default game menu if this method isn't overridden.


     /*
     // If you later want to customize it, you'd add it back:
     @Override
     public FXGLMenu newGameMenu() {
         // Then figure out the correct class for your FXGL version, e.g.:
         // return new SomeOtherDefaultGameMenuClassName();
         // OR
         // return new MyCustomGamePauseMenu();
     }
     */
}

