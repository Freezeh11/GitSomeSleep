package org.example.capstonee.RhythmGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * RhythmMaster
 * Author: Peter Mitchell (2021)
 *
 * GamePanel class:
 * Creates and maintains the game state managing the update loop.
 */
public class GamePanel extends JPanel implements ActionListener {
    /**
     * The number of spawns before the game ends.
     */
    private static final int TOTAL_SPAWNS = 100;
    /**
     * Time in milliseconds between updates.
     */
    private static final int TIMER_DELAY = 30;
    /**
     * Width of the panel in pixels.
     */
    private static final int GAME_WIDTH = 600;
    /**
     * Height of the panel in pixels.
     */
    private static final int GAME_HEIGHT = 800;
    /**
     * A reference to the region where a perfect score is achieved.
     */
    private static final int PERFECT_LINE_HEIGHT = GAME_HEIGHT-RhythmElement.SIZE-50;

    /**
     * Colour used for drawing the "NICE!" text.
     */
    private final Color FADING_NICE_COLOUR = new Color(0,62,49);
    /**
     * Colour used for drawing the "INCORRECT!" text.
     */
    private final Color FADING_INCORRECT_COLOUR = new Color(128,0,0);
    /**
     * Colour used for drawing the "PERFECT!" text.
     */
    private final Color FADING_PERFECT_COLOUR = new Color(255, 207, 61);
    /**
     * Colour used for drawing the "TOO SOON!" text.
     */
    private final Color FADING_TOOSOON_COLOUR = new Color(38, 216, 239);

    /**
     * Defines the possible GameStates.
     * Ready: Initial state waiting to start the game. Goes to Playing when Space is pressed.
     * Playing: Primary state with falling Rhythm Elements ends when all elements have passed.
     * GameEnded: End state that can return to Ready by pressing Space.
     */
    public enum GameState { Ready, Playing, GameEnded };

    /**
     * Current GameState.
     */
    private GameState gameState;
    /**
     * The timer used for keeping consistent updates and redrawing.
     */
    private Timer gameTimer;
    /**
     * List of all active Rhythm Elements.
     */
    private List<RhythmElement> rhythmElementList;
    /**
     * List of all active FadingEventTexts.
     */
    private List<FadingEventText> fadingEventTexts;
    /**
     * Reference to the shared Random object for randomising game elements.
     */
    private Random rand;
    /**
     * Timer used to manage spawning of additional Rhythm Elements.
     */
    private int spawnTimer = 0;
    /**
     * Time to use for period between the spawning of Rhythm Elements.
     */
    private int timeBetweenSpawns = 500;
    /**
     * Current score. Increased by correctly playing the game.
     */
    private int score;
    /**
     * Number of correct inputs in a row increases the score.
     */
    private int comboStreak;
    /**
     * Number of spawns remaining until the game ends.
     */
    private int spawnsRemaining;
    /**
     * Message panel overlay for showing the ready and game over states.
     */
    private MessagePanel messagePanel;
    /**
     * Modifies the speed at which elements fall.
     */
    private int speedFactor = 5;

    /**
     * Creates the initial game state and configures the panel ready for game to start.
     */
    public GamePanel() {
        setPreferredSize(new Dimension(GAME_WIDTH,GAME_HEIGHT));
        setBackground(Color.lightGray);
        gameTimer = new Timer(TIMER_DELAY,this);
        rhythmElementList = new ArrayList<>();
        fadingEventTexts = new ArrayList<>();
        rand = new Random();
        spawnsRemaining = TOTAL_SPAWNS;
        messagePanel = new MessagePanel(new Position(0,GAME_HEIGHT/2-50), GAME_WIDTH,70);
        messagePanel.showStartMessage();
        comboStreak = 0;
        gameState = GameState.Ready;
    }

    /**
     * Handles the inputs from the keyboard. Pressing Escape at any time will quit.
     * Pressing Space when in the Ready or GameEnded states will move to the next state.
     * Pressing any key during the Playing state will handle the input to test if it is
     * valid based on the next character falling.
     *
     * @param keyCode The key that was pressed.
     */
    public void handleInput(int keyCode) {
        if(keyCode == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        } else if(gameState == GameState.Playing && RhythmElement.isCharacterValid((char)keyCode)) {
            if(rhythmElementList.size() > 0) {
                handleRhythmInteraction((char)keyCode);
            }
        } else if(gameState == GameState.Ready && keyCode == KeyEvent.VK_SPACE) {
            gameState = GameState.Playing;
            gameTimer.start();
        } else if(gameState == GameState.GameEnded && keyCode == KeyEvent.VK_SPACE) {
            gameState = GameState.Ready;
            spawnsRemaining = TOTAL_SPAWNS;
            messagePanel.showStartMessage();
            comboStreak = 0;
            score = 0;
            repaint();
        }
    }

    /**
     * Updates during the Playing state to spawn any additional Rhythm elements on a timer,
     * update any existing Rhythm Elements, update any existing FadingEventTexts, and
     * then tests the game state to check if the game has ended. Finally forces a repaint() to
     * draw any changes that have occurred.
     */
    public void update() {
        if(gameState != GameState.Playing) return;

        updateRhythmSpawnTimer();
        updateRhythmElements();
        updateFadingText();

        if(spawnsRemaining == 0 && rhythmElementList.size() == 0) {
            gameState = GameState.GameEnded;
            fadingEventTexts.clear();
            messagePanel.showGameOver(score);
            gameTimer.stop();
        }
        repaint();
    }

    /**
     * Draws elements based on game state. When Playing the Rhythm elements and fading text
     * are draw, otherwise the message panel is shown ot indicate messages to the player.
     * At all times the score, and bottom circles indicating where to get perfect scores are shown.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    public void paint(Graphics g) {
        super.paint(g);
        if(gameState == GameState.Playing) {
            for (RhythmElement rhythmElement : rhythmElementList) {
                rhythmElement.paint(g);
            }
            for(FadingEventText text : fadingEventTexts) {
                text.paint(g);
            }
        } else {
            messagePanel.paint(g);
        }

        g.setColor(Color.BLACK);
        for(int x = RhythmElement.SIZE; x < GAME_WIDTH-RhythmElement.SIZE; x+=RhythmElement.SIZE) {
            g.drawOval(x,PERFECT_LINE_HEIGHT,RhythmElement.SIZE,RhythmElement.SIZE);
        }
        drawScore(g);
    }

    /**
     * Triggered by the game timer, forcing an update() call.
     *
     * @param e Information about the event that occurred.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        update();
    }

    /**
     * Adds a new RhythmElement if there are still more to spawn.
     */
    private void addNewRhythmElement() {
        if(spawnsRemaining == 0) return;
        spawnsRemaining--;

        int xPosition = rand.nextInt((getWidth()-2*RhythmElement.SIZE)/RhythmElement.SIZE)*RhythmElement.SIZE+RhythmElement.SIZE;
        rhythmElementList.add(new RhythmElement(new Position(xPosition,-RhythmElement.SIZE),rand));
    }

    /**
     * Updates the timer to spawn additional Rhythm Elements.
     * When it reaches 0 it will spawn a new one and reset the timer.
     */
    private void updateRhythmSpawnTimer() {
        spawnTimer += TIMER_DELAY;
        if(spawnTimer >= timeBetweenSpawns) {
            addNewRhythmElement();
            spawnTimer = 0;
        }
    }

    /**
     * Updates all currently active RhythmElements by making them move down based
     * on the speedFactor. Any expired ones that have gone off screen are removed,
     * and due to the miss are counted as losing the combo streak.
     */
    private void updateRhythmElements() {
        for(int i = 0; i < rhythmElementList.size(); i++) {
            rhythmElementList.get(i).update(TIMER_DELAY/speedFactor);
            if(rhythmElementList.get(i).position.y > getHeight()) {
                rhythmElementList.remove(i);
                i--;
                comboStreak = 0;
            }
        }
    }

    /**
     * Updates any FadingEventTexts by forcing them to update. Then checks
     * each element after updating to determine if they are no longer visible.
     * Any that are not visible are removed.
     */
    private void updateFadingText() {
        for(int i = 0; i < fadingEventTexts.size(); i++) {
            fadingEventTexts.get(i).update(TIMER_DELAY);
            if(fadingEventTexts.get(i).isExpired()) {
                fadingEventTexts.remove(i);
                i--;
            }
        }
    }

    /**
     * Finds the next element that should be pressed. Then gives score and displays a
     * FadingEventText based on where the element was when it was checked. If the element
     * was more than halfway up the screen it is deemed "TOO SOON". If the input was not
     * correct it will show "INCORRECT". If the key was correct it will either show
     * "NICE" or "PERFECT" based on where the element was when the key was pressed.
     * Prefect awards double points.
     * "TOO SOON" and "INCORRECT" reset the combo streak. The others increase it by 1.
     * Score is calculated as (10+comboStreak) and double for "PERFECT".
     * After all calculations the element used is removed.
     *
     * @param key The character representing the key that was pressed.
     */
    private void handleRhythmInteraction(char key) {

        int rhythmIndex;
        for(rhythmIndex = 0; rhythmIndex < rhythmElementList.size(); rhythmIndex++) {
            if(rhythmElementList.get(rhythmIndex).position.y < GAME_HEIGHT-50) {
                break;
            }
        }
        if(rhythmIndex == rhythmElementList.size()) return;

        if(rhythmElementList.get(rhythmIndex).position.y < GAME_HEIGHT / 2) {
            addFadingText("TOO SOON!", rhythmIndex, FADING_TOOSOON_COLOUR);
            comboStreak = 0;
        } else if(rhythmElementList.get(rhythmIndex).isCharacterCorrect(key)) {
            if(rhythmElementList.get(rhythmIndex).position.y > PERFECT_LINE_HEIGHT - RhythmElement.SIZE/2) {
                score += (10 + comboStreak)*2;
                addFadingText("PERFECT! +" + (10 + comboStreak)*2, rhythmIndex, FADING_PERFECT_COLOUR);
                comboStreak++;
                System.out.println(score);
            } else {
                score += 10 + comboStreak;
                addFadingText("NICE! +" + (10 + comboStreak), rhythmIndex, FADING_NICE_COLOUR);
                comboStreak++;
                System.out.println(score);
            }
        } else {
            addFadingText("INCORRECT!", rhythmIndex, FADING_INCORRECT_COLOUR);
            comboStreak = 0;
        }
        rhythmElementList.remove(rhythmIndex);
    }

    /**
     * Creates a FadingEventText to display the specified text.
     *
     * @param message Text to show on the FadingEventText.
     * @param elementIndexToSpawnOff Index of the RhythmElementList to spawn from.
     * @param colour The Colour to display the text with.
     */
    private void addFadingText(String message, int elementIndexToSpawnOff, Color colour) {
        fadingEventTexts.add(new FadingEventText(message,
                new Position(rhythmElementList.get(elementIndexToSpawnOff).position), colour));
    }

    /**
     * Draws the score centered at the top of the screen.
     *
     * @param g Reference to the Graphics object for rendering.
     */
    private void drawScore(Graphics g) {
        String scoreText = String.valueOf(score);
        g.setFont(new Font("Arial", Font.BOLD, 35));
        int width = g.getFontMetrics().stringWidth(scoreText);
        g.setColor(Color.BLACK);
        g.drawString(scoreText, GAME_WIDTH/2 - width/2, 40);
    }
}

