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

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import static org.example.capstonee.MainMenu.Constants.UI.PauseButtons.SOUND_SIZE;
import static org.example.capstonee.MainMenu.Constants.UI.VolumeButtons.*;


public class AudioOptions {

    private VolumeButton volumeButton, volumeButtonMenu;
    private SoundButton musicButton, sfxButton;
    private GameApp game;
    private AudioPlayer audioPlayer = new AudioPlayer(game, 1);

    public AudioOptions(GameApp game) {
        this.game = game;
        System.out.println("Initializing AudioOptions...");
        createSoundButtons();
        createVolumeButtonMenu();
        createSoundButtonsMenu();
        audioPlayer.loadClick();
        createVolumeButton();
        System.out.println("AudioOptions initialized successfully.");
    }

    public AudioOptions(GameApp game, int hey) {
        this.game = game;
        System.out.println("Initializing AudioOptions...");
        createSoundButtons();
        createVolumeButtonMenu();
        audioPlayer.loadClick();
        createSoundButtonsMenu();
        createVolumeButton();
        System.out.println("AudioOptions initialized successfully.");
    }

    private void createVolumeButton() {
        int vX = (int) (309 * GameApp.SCALE);
        int vY = (int) (278 * GameApp.SCALE);
        volumeButton = new VolumeButton(vX, vY, SLIDER_WIDTH, VOLUME_HEIGHT);
    }

    private void createVolumeButtonMenu() {
        int vX = ((int) (309 * GameApp.SCALE)) - 10;
        int vY = 508;
        volumeButtonMenu = new VolumeButton(vX, vY, SLIDER_WIDTH_MENU, VOLUME_HEIGHT_MENU, 1);
    }

    private void createSoundButtons() {
        int soundX = (int) (450 * GameApp.SCALE);
        int musicY = (int) (140 * GameApp.SCALE);
        int sfxY = (int) (186 * GameApp.SCALE);
        musicButton = new SoundButton(soundX, musicY, SOUND_SIZE, SOUND_SIZE);
        sfxButton = new SoundButton(soundX, sfxY, SOUND_SIZE, SOUND_SIZE);
    }

    private void createSoundButtonsMenu() {
        int soundX1 = (int) (390 * GameApp.SCALE);
        int soundX2 = (int) (510 * GameApp.SCALE);
        int musicY = (int) (160 * GameApp.SCALE);
        int sfxY = (int) (160 * GameApp.SCALE);
        musicButton = new SoundButton(soundX1, musicY, SOUND_SIZE + 20, SOUND_SIZE + 20);
        sfxButton = new SoundButton(soundX2, sfxY, SOUND_SIZE + 20, SOUND_SIZE + 20);
    }

    public void update() {
        musicButton.update();
        sfxButton.update();
        volumeButton.update();
        volumeButtonMenu.update();
    }

    public void updateMenu() {
        musicButton.update();
        sfxButton.update();
        volumeButtonMenu.update();
    }

    public void draw(Graphics g) {
        // Sound buttons
        musicButton.draw(g);
        sfxButton.draw(g);

        // Volume Button
        volumeButton.draw(g);
        volumeButtonMenu.draw(g);
    }

    public void drawMenu(Graphics g) {
        // Sound buttons
        musicButton.draw(g);
        sfxButton.draw(g);

        // Volume Button
        volumeButtonMenu.draw(g);
    }

    public void mouseDragged(MouseEvent e) {
        if (volumeButton.isMousePressed()) {
            float valueBefore = volumeButton.getFloatValue();
            volumeButton.changeX(e.getX());
            float valueAfter = volumeButton.getFloatValue();
            if (valueBefore != valueAfter) {
                game.getAudioPlayer().setVolume(valueAfter);
                return;
            }
        }
        if (volumeButtonMenu.isMousePressed()) {
            float valueBefore = volumeButtonMenu.getFloatValue();
            volumeButtonMenu.changeX(e.getX());
            float valueAfter = volumeButtonMenu.getFloatValue();
            if (valueBefore != valueAfter) {
                game.getAudioPlayer().setVolume(valueAfter);
                return;
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (isIn(e, musicButton)) {
            musicButton.setMousePressed(true);
            audioPlayer.playClick();
        } else if (isIn(e, sfxButton)) {
            sfxButton.setMousePressed(true);
            audioPlayer.playClick();
        } else if (isIn(e, volumeButton)) {
            volumeButton.setMousePressed(true);
            audioPlayer.playClick();
        } else if (isIn(e, volumeButtonMenu)) {
            volumeButtonMenu.setMousePressed(true);
            audioPlayer.playClick();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (isIn(e, musicButton)) {
            if (musicButton.isMousePressed())
                musicButton.setMuted(!musicButton.isMuted());
            game.getAudioPlayer().toggleSongMute();
        } else if (isIn(e, sfxButton)) {
            if (sfxButton.isMousePressed())
                sfxButton.setMuted(!sfxButton.isMuted());
            game.getAudioPlayer().toggleEffectMute();
        }

        musicButton.resetBools();
        sfxButton.resetBools();
        volumeButton.resetBools();
        volumeButtonMenu.resetBools();
    }

    public void mouseMoved(MouseEvent e) {
        musicButton.setMouseOver(false);
        sfxButton.setMouseOver(false);
        volumeButton.setMouseOver(false);
        volumeButtonMenu.setMouseOver(false);

        if (isIn(e, musicButton))
            musicButton.setMouseOver(true);
        else if (isIn(e, sfxButton))
            sfxButton.setMouseOver(true);
        else if (isIn(e, volumeButton))
            volumeButton.setMouseOver(true);
        else if (isIn(e, volumeButtonMenu))
            volumeButtonMenu.setMouseOver(true);
    }

    private boolean isIn(MouseEvent e, PauseButton b) {
        return b.getBounds().contains(e.getX(), e.getY());
    }

    public VolumeButton getvolumeButtonMenu() {
        return volumeButtonMenu;
    }
}