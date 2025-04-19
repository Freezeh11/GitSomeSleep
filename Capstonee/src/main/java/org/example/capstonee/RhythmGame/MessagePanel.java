package org.example.capstonee.RhythmGame;

import java.awt.*;

/**
 * Rhythm Master
 * Author: Peter Mitchell (2021)
 *
 * MessagePanel class:
 * Represents a simple pair of text lines drawn with a background.
 */
public class MessagePanel extends Rectangle {
    /**
     * The font to use for drawing both of the messages.
     */
    private final Font font = new Font("Arial", Font.BOLD, 30);

    /**
     * The current message to display on the top line.
     */
    private String topLine;
    /**
     * The current message to display on the bottom line.
     */
    private String bottomLine;

    /**
     * Configures the status panel to be ready for drawing a background,
     * and initial default text.
     *
     * @param position Top left corner of the panel.
     * @param width Width of the area to draw within.
     * @param height Height of the area to draw within.
     */
    public MessagePanel(Position position, int width, int height) {
        super(position, width, height);
    }

    /**
     * Updates the message to display the game over screen.
     *
     * @param finalScore The score achieved during the game.
     */
    public void showGameOver(int finalScore) {
        topLine = "You scored: " + finalScore;
        bottomLine = "Press SPACE to start a new game!";
    }

    /**
     * Updates the message to display the ready to start message.
     */
    public void showStartMessage() {
        topLine = "Press SPACE to start!";
        bottomLine = "Press the right keys to get score!";
    }

    /**
     * Sets the message to display on the top line of output to any specified String.
     *
     * @param message Message to display on the top line.
     */
    public void setTopLine(String message) {
        topLine = message;
    }

    /**
     * Sets the message to display on the bottom line of output to any specified String.
     *
     * @param message Message to display on the bottom line.
     */
    public void setBottomLine(String message) {
        bottomLine = message;
    }

    /**
     * Draws a maroon background with black text centred over two lines using
     * the top line and bottom line messages.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    public void paint(Graphics g) {
        g.setColor(new Color(118, 35, 35, 200));
        g.fillRect(position.x, position.y, width, height);
        g.setColor(Color.BLACK);
        g.setFont(font);
        int strWidth = g.getFontMetrics().stringWidth(topLine);
        g.drawString(topLine, position.x+width/2-strWidth/2, position.y+30);
        strWidth = g.getFontMetrics().stringWidth(bottomLine);
        g.drawString(bottomLine, position.x+width/2-strWidth/2, position.y+60);
    }
}

