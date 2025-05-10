package org.example.capstonee.MainMenu;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

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

import static org.example.capstonee.MainMenu.Constants.UI.UrmButtons.URM_SIZE_MENU;


public class GameOptions extends State implements Statemethods {

    private AudioOptions audioOptions;
    private BufferedImage backgroundImg, optionsBackgroundImg;
    private BufferedImage cloudBackground1, cloudBackground2, cloudBackground3, cloudBackground4;
    private int bgX, bgY, bgW, bgH;

    private double SKYscrollOffset, CITYscrollOffset, CLOUD4scrollOffset, CLOUD3scrollOffset;
    private UrmButton menuB;

    public GameOptions(GameApp game) {
        super(game);
        loadImgs();
        loadButton();
        audioOptions = new AudioOptions(game, 1);
        System.out.println("AudioOptions initialized: " + (audioOptions != null));
    }

    private void loadButton() {
        int menuX = (int) (403 * GameApp.SCALE);
        int menuY = (int) (300 * GameApp.SCALE);
        menuB = new UrmButton(menuX, menuY, URM_SIZE_MENU + 100, URM_SIZE_MENU + 100, 2, 6);
        System.out.println("Button Position: (" + menuB.getX() + ", " + menuB.getY() + ")");
    }

    private void loadImgs() {
        optionsBackgroundImg = LoadSave.getSpriteAtlas(LoadSave.OPTIONS_MENU);

        backgroundImg = LoadSave.getSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
        cloudBackground1 = LoadSave.getSpriteAtlas(LoadSave.CLOUD_1);
        cloudBackground2 = LoadSave.getSpriteAtlas(LoadSave.CLOUD_2);
        cloudBackground3 = LoadSave.getSpriteAtlas(LoadSave.CLOUD_3);
        cloudBackground4 = LoadSave.getSpriteAtlas(LoadSave.CLOUD_4);

        bgW = 800;
        bgH = 500;
        bgX = (GameApp.GAME_WIDTH / 2 - bgH / 2) - 150;
        bgY = (int) (80 * GameApp.SCALE);
    }

    @Override
    public void update() {
        audioOptions.updateMenu();
        menuB.update();

        //parallax Scrolling
        SKYscrollOffset -= 1.0;
        if (SKYscrollOffset <= -GameApp.GAME_WIDTH) {
            SKYscrollOffset = 0;
        }

        CITYscrollOffset -= 0.5;
        if (CITYscrollOffset <= -GameApp.GAME_WIDTH) {
            CITYscrollOffset = 0;
        }

        CLOUD4scrollOffset -= 0.2;
        if (CLOUD4scrollOffset <= -GameApp.GAME_WIDTH) {
            CLOUD4scrollOffset = 0;
        }

        CLOUD3scrollOffset -= 0.3;
        if (CLOUD3scrollOffset <= -GameApp.GAME_WIDTH) {
            CLOUD3scrollOffset = 0;
        }
    }

    @Override
    public void draw(Graphics g) {
        // parallax Layers
        g.drawImage(cloudBackground1, 0, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);
        g.drawImage(cloudBackground2, 0, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);

        g.drawImage(cloudBackground3, (int) CLOUD3scrollOffset, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);
        g.drawImage(cloudBackground3, (int) CLOUD3scrollOffset + GameApp.GAME_WIDTH, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);

        g.drawImage(cloudBackground4, (int) CLOUD4scrollOffset, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);
        g.drawImage(cloudBackground4, (int) CLOUD4scrollOffset + GameApp.GAME_WIDTH, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);

        g.drawImage(backgroundImg, (int) CITYscrollOffset, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);
        g.drawImage(backgroundImg, (int) CITYscrollOffset + GameApp.GAME_WIDTH, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);

        g.drawImage(optionsBackgroundImg, bgX, bgY, bgW, bgH, null);

        // UI Elements
        audioOptions.drawMenu(g);
        menuB.drawMenu(g);
    }

    public void mouseDragged(MouseEvent e) {
        audioOptions.mouseDragged(e);
        if (audioOptions.getvolumeButtonMenu().isMousePressed()) {
            audioOptions.getvolumeButtonMenu().changeX(e.getX());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("Mouse pressed at: " + e.getX() + ", " + e.getY());
        if (isIn(e, menuB)) {
            menuB.setMousePressed(true);
            System.out.println("UrmButton pressed");
        } else {
            audioOptions.mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isIn(e, menuB)) {
            if (menuB.isMousePressed()) {
                Gamestate.state = Gamestate.MENU;
            }
        } else {
            audioOptions.mouseReleased(e);
        }
        menuB.resetBools();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        menuB.setMouseOver(false);
        if (isIn(e, menuB)) {
            menuB.setMouseOver(true);
        } else {
            audioOptions.mouseMoved(e);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            Gamestate.state = Gamestate.MENU;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    private boolean isIn(MouseEvent e, UrmButton b) {
        boolean inside = b.getBounds().contains(e.getX(), e.getY());
        System.out.println("Mouse is inside button: " + inside);
        return inside;
    }
}
