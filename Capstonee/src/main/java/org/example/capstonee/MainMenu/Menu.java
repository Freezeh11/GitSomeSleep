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


import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import org.example.capstonee.GameApp.*;

public class Menu extends State implements Statemethods{

    private MenuButton[] buttons = new MenuButton[4];
    private BufferedImage backgroundImg, backgroundTitle, backgroundCity,cloudBackground1,cloudBackground2,cloudBackground3,cloudBackground4, overlayImg;
    private int menuX, menuY, menuWidth, menuHeight;
    private double SKYscrollOffset;
    private BufferedImage[] CatPixelated = new BufferedImage[4];
    private double CITYscrollOffset;
    private double CLOUD4scrollOffset, CLOUD3scrollOffset;
    private int currentTitleFrame = 0;
    private long lastFrameTime = 0;
    private final long FRAME_INTERVAL = 400;

    public boolean isInitialized() {
        return isInitialized;
    }

    private boolean isInitialized = false;

    public Menu(GameApp game) {
        super(game);
        System.out.println("Menu constructor called.");
        loadCONTENT();
        isInitialized = true;
    }


    private void loadCONTENT(){

        backgroundImg = LoadSave.getSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
        menuWidth = (int) (backgroundImg.getWidth() * GameApp.SCALE);
        menuHeight = (int) (backgroundImg.getHeight() * GameApp.SCALE);
        menuX = GameApp.GAME_WIDTH / 2 - menuWidth / 2;
        menuY = (int) (45 * GameApp.SCALE);

        cloudBackground1 = LoadSave.getSpriteAtlas(LoadSave.CLOUD_1);
        menuWidth = (int) (cloudBackground1.getWidth() * GameApp.SCALE);
        menuHeight = (int) (cloudBackground1.getHeight() * GameApp.SCALE);
        menuX = GameApp.GAME_WIDTH / 2 - menuWidth / 2;
        menuY = (int) (45 * GameApp.SCALE);

        cloudBackground2 = LoadSave.getSpriteAtlas(LoadSave.CLOUD_2);
        menuWidth = (int) (cloudBackground2.getWidth() * GameApp.SCALE);
        menuHeight = (int) (cloudBackground2.getHeight() * GameApp.SCALE);
        menuX = GameApp.GAME_WIDTH / 2 - menuWidth / 2;
        menuY = (int) (45 * GameApp.SCALE);

        cloudBackground3 = LoadSave.getSpriteAtlas(LoadSave.CLOUD_3);
        menuWidth = (int) (cloudBackground3.getWidth() * GameApp.SCALE);
        menuHeight = (int) (cloudBackground3.getHeight() * GameApp.SCALE);
        menuX = GameApp.GAME_WIDTH / 2 - menuWidth / 2;
        menuY = (int) (45 * GameApp.SCALE);

        cloudBackground4 = LoadSave.getSpriteAtlas(LoadSave.CLOUD_4);
        menuWidth = (int) (cloudBackground4.getWidth() * GameApp.SCALE);
        menuHeight = (int) (cloudBackground4.getHeight() * GameApp.SCALE);
        menuX = GameApp.GAME_WIDTH / 2 - menuWidth / 2;
        menuY = (int) (45 * GameApp.SCALE);

        backgroundTitle = LoadSave.getSpriteAtlas(LoadSave.MENU_TITLE);
        menuWidth = (int)(backgroundTitle.getWidth() * GameApp.SCALE);
        menuHeight = (int)(backgroundTitle.getHeight() * GameApp.SCALE);
        menuX = GAME_WIDTH / 2 - menuWidth / 2;
        menuY = (int) (-121 * SCALE);

        overlayImg = LoadSave.getSpriteAtlas(LoadSave.MENU_OVERLAY);
        menuWidth = (int)(backgroundTitle.getWidth() * GameApp.SCALE);
        menuHeight = (int)(backgroundTitle.getHeight() * GameApp.SCALE);
        menuX = GAME_WIDTH / 2 - menuWidth / 2;
        menuY = (int) (-121 * SCALE);

        buttons[0] = new MenuButton(GameApp.GAME_WIDTH/2,(int)(190*GameApp.SCALE),0,Gamestate.PLAYING);
        buttons[1] = new MenuButton(GameApp.GAME_WIDTH/2,(int)(230*GameApp.SCALE),1,Gamestate.OPTIONS);
        buttons[2] = new MenuButton(GameApp.GAME_WIDTH/2,(int)(270*GameApp.SCALE),2,Gamestate.ABOUT);
        buttons[3] = new MenuButton(GameApp.GAME_WIDTH/2,(int)(310*GameApp.SCALE),3,Gamestate.EXIT);

        BufferedImage spriteSheet = LoadSave.getSpriteAtlas(LoadSave.CAT_PIXEL); // Load the sprite sheet
        int spriteWidth = spriteSheet.getWidth() / 8; // Divide the width by 8 (columns)
        int spriteHeight = spriteSheet.getHeight() / 10; // Divide the height by 10 (rows)

        // Directly reference the sprite positions in the sprite sheet
        CatPixelated[0] = spriteSheet.getSubimage(2 * spriteWidth, 0 * spriteHeight, spriteWidth, spriteHeight); // Row 2, Column 0
        CatPixelated[1] = spriteSheet.getSubimage(2 * spriteWidth, 1 * spriteHeight, spriteWidth, spriteHeight); // Row 2, Column 1
        CatPixelated[2] = spriteSheet.getSubimage(2 * spriteWidth, 2 * spriteHeight, spriteWidth, spriteHeight); // Row 2, Column 2
        CatPixelated[3] = spriteSheet.getSubimage(2 * spriteWidth, 3 * spriteHeight, spriteWidth, spriteHeight); // Row 2, Column 3

    }

    /*
        private void loadTitle() {
            backgroundImg = LoadSave.getSpriteAtlas(LoadSave.MENU_BACKGROUND);

            backgroundTitle = LoadSave.getSpriteAtlas(LoadSave.MENU_TITLE);

            menuWidth = (int)(backgroundTitle.getWidth() * Game.SCALE);
            menuHeight = (int)(backgroundTitle.getHeight() * Game.SCALE);
            menuX = GAME_WIDTH / 2 - menuWidth / 2;
            menuY = (int) (-120 * SCALE);

        }

        private void loadButtons() {
            buttons[0] = new MenuButton(Game.GAME_WIDTH/2,(int)(190*Game.SCALE),0,Gamestate.PLAYING);
            buttons[1] = new MenuButton(Game.GAME_WIDTH/2,(int)(230*Game.SCALE),1,Gamestate.OPTIONS);
            buttons[2] = new MenuButton(Game.GAME_WIDTH/2,(int)(270*Game.SCALE),2,Gamestate.ABOUT);
            buttons[3] = new MenuButton(Game.GAME_WIDTH/2,(int)(310*Game.SCALE),3,Gamestate.EXIT);
        }
    */
    @Override
    public void update() {
        if (isInitialized) {
            for (MenuButton mb : buttons)
                mb.update();
        }

        SKYscrollOffset -= 1.0;
        if (SKYscrollOffset <= -GameApp.GAME_WIDTH) {
            SKYscrollOffset = 0l
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
        if (isInitialized) {
            //   g.drawImage(backgroundImg, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
            g.drawImage(cloudBackground1, 0, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);
            g.drawImage(cloudBackground2, 0, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);

            g.drawImage(cloudBackground3, (int) CLOUD3scrollOffset, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);
            g.drawImage(cloudBackground3, (int) CLOUD3scrollOffset + GameApp.GAME_WIDTH, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);

            g.drawImage(cloudBackground4, (int) CLOUD4scrollOffset, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);
            g.drawImage(cloudBackground4, (int) CLOUD4scrollOffset + GameApp.GAME_WIDTH, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);

            g.drawImage(backgroundImg, (int) CITYscrollOffset, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);
            g.drawImage(backgroundImg, (int) CITYscrollOffset + GameApp.GAME_WIDTH, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);

            g.drawImage(backgroundTitle, 0, menuY, GAME_WIDTH, GAME_HEIGHT, null);

            Graphics2D g2d = (Graphics2D) g;
            Composite originalComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.drawImage(overlayImg, 0, 0, GameApp.GAME_WIDTH, GameApp.GAME_HEIGHT, null);
            g2d.setComposite(originalComposite);

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFrameTime >= FRAME_INTERVAL) {
                currentTitleFrame = (currentTitleFrame + 1) % CatPixelated.length;
                lastFrameTime = currentTime;
            }
            g.drawImage(CatPixelated[currentTitleFrame],  (GameApp.GAME_WIDTH/2)+240, (GameApp.GAME_HEIGHT/2)-330, 150, 150, null);

            for (MenuButton mb : buttons)
                mb.draw(g);
        } else {
            System.err.println("Menu is not fully initialized yet.");
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        for (MenuButton mb : buttons) {
            if (isIn(e, mb)) {
                mb.setMousePressed(true);
                game.getAudioPlayer().playClick();
                break;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for(MenuButton mb : buttons) {
            if(isIn(e,mb)){
                if(mb.isMousePressed()) {
                    if (mb.getState() == Gamestate.PLAYING) {
                        game.getAudioPlayer().stopSong();
                        game.getAudioPlayer().setLevelSong(1);
                    }
                    mb.applyGameState();
                }
                break;
            }
        }
        resetButtons();
    }

    private void resetButtons() {
        for(MenuButton mb : buttons)
            mb.resetBools();

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        for(MenuButton mb : buttons) {
            mb.setMouseOver(false);
        }

        for(MenuButton mb : buttons) {
            if (isIn(e, mb)) {
                mb.setMouseOver(true);
                break;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER)
            Gamestate.state = Gamestate.PLAYING;

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void mouseDragged(MouseEvent e) {

    }
}