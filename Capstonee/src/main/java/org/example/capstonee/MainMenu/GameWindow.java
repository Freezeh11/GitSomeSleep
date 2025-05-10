package org.example.capstonee.MainMenu;

import javax.swing.JFrame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.Component;

// this is the window itself (or the frame itself) of the game

public class GameWindow {
    private JFrame jframe;

    public GameWindow(GamePanel gamePanel) {
        jframe = new JFrame();

        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);          // allows us to terminate program when we "close" using exit button
        jframe.add(gamePanel);                                          // by invoking this, we are adding the rectangle we added
        jframe.pack();                                                  // sets the dimensions following gamepanel's
        jframe.setLocationRelativeTo(null);                             // puts our frame at the center of the screen
        jframe.setResizable(false);                                     // will not allow the user to change the dimensions of the window by dragging the corners
        jframe.setVisible(true);                                        // makes frame visible on computer screen
        jframe.addWindowFocusListener(new WindowFocusListener() {       // this checks if the user is currently on the game's tab

            @Override
            public void windowLostFocus(WindowEvent e) {                  // if the player is out of the game's tab, we invoke a function. in this case, we want player to stop moving
                System.out.println("user has tabbed out of game");
                gamePanel.getGame().windowFocusLost();
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {

            }
        });
    }
}
 