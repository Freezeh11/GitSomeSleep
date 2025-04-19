package org.example.capstonee.RhythmGame;

import java.awt.*;

/**
 * Rhythm Master
 * Author: Peter Mitchell (2021)
 *
 * FadingEventText class:
 * Draws text that will slowly move upward and fade out.
 */
public class FadingEventText {
    /**
     * The base colour to fade out from.
     */
    private Color colour;
    /**
     * Font to draw text with.
     */
    private Font font = new Font("Arial", Font.BOLD, 20);
    /**
     * Current colour to draw with from most recent update.
     */
    private Color drawColour;
    /**
     * Current position to draw at.
     */
    private Position position;
    /**
     * The message to display.
     */
    private String text;
    /**
     * Fades from 255 down to 0 representing the alpha value of the drawn text.
     */
    private int fadeValue;

    /**
     * Sets up the text ready to be draw/updated starting with full alpha.
     *
     * @param text The message to display.
     * @param position The position to start the text at.
     * @param startColour The colour to use for text that will be faded out.
     */
    public FadingEventText(String text, Position position, Color startColour) {
        this.colour = startColour;
        fadeValue = 255;
        drawColour = new Color(colour.getRed(),colour.getGreen(),colour.getBlue(),fadeValue);
        this.position = position;
        this.text = text;
    }

    /**
     * Updates the alpha colour channel, and position based on the delta time.
     *
     * @param deltaTime Amount of time since last update.
     */
    public void update(int deltaTime) {
        int changeAmount = deltaTime / 6;
        fadeValue = Math.max(0,fadeValue - changeAmount);
        position.y -= changeAmount / 2;
        drawColour = new Color(colour.getRed(),colour.getGreen(),colour.getBlue(),fadeValue);
    }

    /**
     * Tests if the text is no longer visible.
     *
     * @return True if alpha has reached 0.
     */
    public boolean isExpired() {
        return fadeValue == 0;
    }

    /**
     * Draws the text with stored properties of the class.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    public void paint(Graphics g) {
        g.setColor(drawColour);
        g.setFont(font);
        g.drawString(text,position.x, position.y);
    }
}

