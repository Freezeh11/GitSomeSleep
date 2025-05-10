package org.example.capstonee.MainMenu;

import org.example.capstonee.GameApp;
import org.example.capstonee.MainMenu.AudioPlayer;
import org.example.capstonee.MainMenu.AudioOptions;
import org.example.capstonee.MainMenu.Constants;
import org.example.capstonee.MainMenu.GameOptions;
import org.example.capstonee.MainMenu.Gamestate;
import org.example.capstonee.MainMenu.LoadSave;
import org.example.capstonee.MainMenu.Menu;
import org.example.capstonee.MainMenu.MenuButton;
import org.example.capstonee.MainMenu.MouseInputs;
import org.example.capstonee.MainMenu.PauseButton;
import org.example.capstonee.MainMenu.PauseOverlay;
import org.example.capstonee.MainMenu.SoundButton;
import org.example.capstonee.MainMenu.State;
import org.example.capstonee.MainMenu.Statemethods;
import org.example.capstonee.MainMenu.UrmButton;
import org.example.capstonee.MainMenu.VolumeButton;


import java.awt.event.MouseEvent;

public abstract class State {

    protected GameApp game;

    public State(GameApp game) {
        this.game = game;
    }

    public boolean isIn(MouseEvent e, MenuButton mb){
        return mb.getBounds().contains(e.getX(), e.getY());
    }

    public GameApp getGame(){
        return game;
    }

    public abstract void mouseDragged(MouseEvent e);
}