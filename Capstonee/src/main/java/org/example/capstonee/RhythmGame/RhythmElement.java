package org.example.capstonee.RhythmGame;

import java.awt.*;
import java.util.Random;

/**
 * RhythmMaster
 * Author: Peter Mitchell (2021)
 *
 * RhythmElement class:
 * Defines a single character that must be pressed to gain score.
 * This is class is used to store the visual representation and
 * input required to get it correct.
 */
public class RhythmElement extends Rectangle {
    /**
     * When true this will ignore all characters that are not in the VALID_CHARACTERS array.
     */
    private static boolean ONLY_ALLOW_VALID_CHARACTERS = true;
    /**
     * The visual size of the individual elements.
     */
    public static final int SIZE = 100;
    /**
     * All the valid characters that can be randomly shown.
     */
    private static final char[] VALID_CHARACTERS = {'W','A','S','D'};
    /**
     * Colours that pair with the VALID_CHARACTERS 1:1 on the index.
     */
    private static final Color[] COLOURS = {new Color(255,0,0,200),
            new Color(0,255,0,200),
            new Color(0,0,255,200),
            new Color(186, 152, 28,200)};
    /**
     * Font used for drawing the element.
     */
    private final Font font = new Font("Arial", Font.BOLD, SIZE);
    /**
     * Character representation for checking if the input is valid.
     */
    private char character;
    /**
     * Cached String version of the character to draw.
     */
    private String characterStr;
    /**
     * Colour to draw the character with.
     */
    private Color drawColour;

    /**
     * Randomly selects a character from the valid characters, and configures it
     * ready for drawing and interaction by the player.
     *
     * @param position Start position to move down from.
     * @param rand Reference to the Random object for random character selection.
     */
    public RhythmElement(Position position, Random rand) {
        super(position, SIZE, SIZE);
        int charChoice = rand.nextInt(VALID_CHARACTERS.length);
        character = VALID_CHARACTERS[charChoice];
        drawColour = COLOURS[charChoice];
        characterStr = String.valueOf(character);
    }

    /**
     * Updates the element by moving it down by a fixed number of pixels.
     *
     * @param fallAmount Amount to move down in pixels.
     */
    public void update(int fallAmount) {
        position.y += fallAmount;
    }

    /**
     * Tests if a possible character value is equal to the one stored in this object.
     *
     * @param character Character to test with.
     * @return True if the character matches the one represented by this object.
     */
    public boolean isCharacterCorrect(char character) {
        return this.character == character;
    }

    /**
     * Tests if the specified character is in the valid list of characters.
     * It will always return true if ONLY_ALLOW_VALID_CHARACTERS has been set to false.
     *
     * @param character A character to test.
     * @return True if either any character is allowed for entry, or otherwise if the character is in the valid list of characters.
     */
    public static boolean isCharacterValid(char character) {
        if(!ONLY_ALLOW_VALID_CHARACTERS) return true;

        for(int i = 0; i < VALID_CHARACTERS.length; i++) {
            if(character == VALID_CHARACTERS[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Draws the element to the screen at the specified position by drawing a background oval,
     * and the character over the top of it.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    public void paint(Graphics g) {
        g.setColor(drawColour);
        g.fillOval(position.x, position.y, width, height);
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString(characterStr, position.x+15, position.y+SIZE*3/4+5);
    }
}

