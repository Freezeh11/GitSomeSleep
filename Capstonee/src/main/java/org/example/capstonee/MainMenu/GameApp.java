package org.example.capstonee.MainMenu;

// This class holds the entirety of the game

import audio.AudioPlayer;
import gamestates.GameOptions;
import gamestates.Gamestate;
import gamestates.Menu;
import gamestates.Playing;
import gamestates.About;
import inputs.MouseInputs;
import ui.AudioOptions;
import utils.LoadSave;

import java.awt.*;

public class GameApp implements Runnable {

    private GameWindow gameWindow;
    private GamePanel gamePanel;
    private Thread gameThread;
    private final int FPS_SET = 120;
    private final int UPS_SET = 200;

    private Playing playing;
    private Menu menu;
    private About about;
    private AudioPlayer audioPlayer;
    private AudioOptions audioOptions;
    private GameOptions gameOptions;

    public static final int TILES_DEFAULT_SIZE = 32;
    public static final float SCALE = 2.0f;
    public static final int TILES_IN_WIDTH = 26;
    public static final int TILES_IN_HEIGHT = 14;
    public static final int TILES_SIZE = (int) (TILES_DEFAULT_SIZE * SCALE);
    public static final int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
    public static final int GAME_HEIGHT = TILES_SIZE * TILES_IN_HEIGHT;

    private boolean initialized = false;
    private boolean isFirstRender = true;

    // Constructor
    public GameApp() {

        gamePanel = new GamePanel(this);
        gamePanel.addMouseListener(new MouseInputs(gamePanel));
        gameWindow = new GameWindow(gamePanel);
        initClasses();
        initialized = true;
        Gamestate.state = Gamestate.MENU;
        gamePanel.setFocusable(true);
        gamePanel.requestFocus();
        gamePanel.enableMouseInput();

        // start the game loop after initialization
        startGameLoop();
    }

    private void initClasses() {
        menu = new Menu(this);
        playing = new Playing(this, gamePanel);

        audioPlayer = new AudioPlayer(this);
        gameOptions = new GameOptions(this);
        audioOptions = new AudioOptions(this);
        about = new About(this);
    }

    private void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void update() {
        switch (Gamestate.state) {
            case MENU:
                menu.update();
                break;
            case PLAYING:
                playing.update();
                break;
            case OPTIONS:
                gameOptions.update();
                break;
            case ABOUT:
                about.update();
                break;
            case EXIT:
            default:
                System.exit(0);
                break;
        }
    }

    public void render(Graphics g) {
        if (isFirstRender) {
            if (menu == null || !menu.isInitialized()) {
                return;
            }
            isFirstRender = false;
            gamePanel.enableMouseInputAfterRender();
        }

        switch (Gamestate.state) {
            case MENU:
                if (menu != null) {
                    menu.draw(g);
                } else {
                    System.err.println("Error: 'menu' is null in Game.render()");
                }
                break;
            case PLAYING:
                if (playing != null) {
                    playing.draw(g);
                } else {
                    System.err.println("Error: 'playing' is null in Game.render()");
                }
                break;
            case OPTIONS:
                if (gameOptions != null) {
                    gameOptions.draw(g);
                } else {
                    System.err.println("Error: 'options' is null in Game.render()");
                }
                break;
            case ABOUT:
                if (about != null) {
                    about.draw(g);
                } else {
                    System.err.println("Error: 'about' is null in Game.render()");
                }
                break;
            default:
                System.err.println("Error: Unknown Gamestate: " + Gamestate.state);
                break;
        }
    }

    @Override
    public void run() {

        double timePerFrame = 1000000000.0 / FPS_SET;
        double timePerUpdate = 1000000000.0 / UPS_SET;
        long previousTime = System.nanoTime();

        int frames = 0;
        int updates = 0;
        long lastCheck = System.currentTimeMillis();

        double deltaU = 0;
        double deltaF = 0;

        while (true) {
            long currentTime = System.nanoTime();
            deltaU += (currentTime - previousTime) / timePerUpdate;
            deltaF += (currentTime - previousTime) / timePerFrame;
            previousTime = currentTime;

            if (deltaU >= 1) {
                update();
                updates++;
                deltaU--;
            }

            if (deltaF >= 1) {
                gamePanel.repaint();
                frames++;
                deltaF--;
            }

            if (System.currentTimeMillis() - lastCheck >= 1000) {
                lastCheck = System.currentTimeMillis();
                System.out.println("FPS: " + frames + " | UPS: " + updates);
                frames = 0;
                updates = 0;
            }
        }
    }

    public void windowFocusLost() {
        if (Gamestate.state == Gamestate.PLAYING) {
            playing.getPlayer().resetDirBooleans();
        }
    }

    public Menu getMenu() {
        return menu;
    }

    public Playing getPlaying() {
        return playing;
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public GameOptions getGameOptions() {
        return gameOptions;
    }

    public AudioOptions getAudioOptions() {
        return audioOptions;
    }

    public About getAbout() {
        return about;
    }
}