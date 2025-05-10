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

public class Constants {



    public static class UI {
        public static class Buttons {
            public static final int B_WIDTH_DEFAULT = 96;
            public static final int B_HEIGHT_DEFAULT = 32;
            public static final int B_WIDTH = (int)(B_WIDTH_DEFAULT * GameApp.SCALE);
            public static final int B_HEIGHT = (int)(B_HEIGHT_DEFAULT * GameApp.SCALE);
        }

        public static class PauseButtons {
            public static final int SOUND_SIZE_DEFAULT = 16;
            public static final int SOUND_SIZE = (int)(SOUND_SIZE_DEFAULT * GameApp.SCALE);
            public static final int SOUND_SIZE_MENU = ((int)(SOUND_SIZE_DEFAULT * GameApp.SCALE)) + 20;
        }

        public static class UrmButtons {
            public static final int URM_DEFAULT_SIZE = 16;
            public static final int URM_SIZE = (int)(URM_DEFAULT_SIZE * GameApp.SCALE);
            public static final int URM_SIZE_MENU = 70;
        }

        public static class VolumeButtons {
            public static final int VOLUME_DEFAULT_WIDTH = 28;
            public static final int VOLUME_DEFAULT_HEIGHT = 43;
            public static final int SLIDER_DEFAULT_WIDTH = 215;

            public static final int VOLUME_WIDTH = (int)(VOLUME_DEFAULT_WIDTH * GameApp.SCALE / 2);
            public static final int VOLUME_HEIGHT = (int)(VOLUME_DEFAULT_HEIGHT * GameApp.SCALE / 2);
            public static final int SLIDER_WIDTH = (int)(SLIDER_DEFAULT_WIDTH * GameApp.SCALE / 2);
            public static final int VOLUME_HEIGHT_MENU = ((int)(VOLUME_DEFAULT_HEIGHT * GameApp.SCALE / 2)) + 20;
            public static final int SLIDER_WIDTH_MENU = ((int)(SLIDER_DEFAULT_WIDTH * GameApp.SCALE / 2)) + 250;
        }
    }

}