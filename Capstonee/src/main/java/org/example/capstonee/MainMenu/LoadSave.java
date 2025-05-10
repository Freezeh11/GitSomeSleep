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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class LoadSave {


    public static final String CLOUD_1 = "CloudBackground1.png";
    public static final String CLOUD_2 = "CloudBackground2.png";
    public static final String CLOUD_3 = "CloudBackground3.png";
    public static final String CLOUD_4 = "CloudBackground4.png";
    public static final String OPTIONS_MENU = "MenuBackGroundNewPathfinder.png";
    public static final String NEWMENU = "MenuBackGroundNewPathfinder.png";
    public static final String ABOUT_PAGE = "aboutPageOfficial.png";
    public static final String MENU_BUTTONS = "menu_atlas.png";
    public static final String MENU_TITLE = "pathfinder.png";
    public static final String PAUSE_BACKGROUND = "pause menu.png";
    public static final String SOUND_BUTTONS = "music atlas.png";
    public static final String URM_BUTTONS = "pause atlas.png";
    public static final String VOLUME_BUTTONS = "volume_buttons.png";
    public static final String MENU_OVERLAY = "menuoverlay.png";
    public static final String MENU_BACKGROUND_IMG = "";


    public static BufferedImage getSpriteAtlas(String fileName) {
        BufferedImage img;
        InputStream is = LoadSave.class.getResourceAsStream("/" + fileName);
        try{
            img = ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("image not found?! what the sigma???");
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                System.err.println("InputStream *is* failed to close!");
            }
        }
        return img;
    }

    public static BufferedImage[] GetAllLevels(){
        URL url = LoadSave.class.getResource("/rounds");
        File file = null;

        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        File[] files = file.listFiles();
        File[] filesSorted = new File[files.length];

        for(int i = 0; i < filesSorted.length; i++)
            for(int j = 0; j < files.length; j++){
                if(files[j].getName().equals((i + 1) + ".png"))
                    filesSorted[i] = files[j];
            }



        BufferedImage[] imgs = new BufferedImage[filesSorted.length];

        for(int i =0; i <  imgs.length; i++) {
            try {
                imgs[i] = ImageIO.read(filesSorted[i]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for(File f :files)
            System.out.println("file: " + f.getName());
        for(File f :filesSorted)
            System.out.println("file sorted: " + f.getName());

        return imgs;
    }



}