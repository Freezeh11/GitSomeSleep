package org.example.capstonee;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
// Import necessary classes for audio (using JLayer for MP3)
// Ensure you have the jlayer-1.0.1.jar dependency in your project (e.g., Maven/Gradle).
import javazoom.jl.player.Player;
// If you were using Clip for WAV/other formats, you might keep javax.sound.sampled.*
// import javax.sound.sampled.*;


public class Main extends JPanel {
	static NoteController noteList;
	static NoteCatcherController catcher;
	static FurInputHandler k;
	static ScoreCounter scorecounter;
	static Font scoreFont, comboFont;
	static Rectangle2D.Double blocc;
	static boolean quit;
	static final boolean hidden = false;
	// static Clip audioClip; // Use Player for MP3

	// Define the path to your custom beatmap file
	private static final String CUSTOM_BEATMAP_PATH = "src/main/resources/assets/music/songfile/firstbosssong.txt";
	// Define the path to your audio file (PLACEHOLDER - REPLACE WITH YOUR ACTUAL AUDIO FILE PATH)
	private static final String AUDIO_FILE_PATH = "src/main/resources/beatmaps/2266557 nyankobrq & yaca - twinkle night (feat somunia)/audio.mp3"; // <-- !!! REPLACE THIS !!!


	public static void main(String[] args) throws InterruptedException {
		quit = false;

		k = new FurInputHandler(4); // Initialize with 4 lanes as per your beatmap data implies
		scorecounter = new ScoreCounter();
		catcher = new NoteCatcherController(k);

		scoreFont = new Font("Arial", Font.PLAIN, 50);
		comboFont = new Font("Arial", Font.PLAIN, 64);

		JFrame j = new JFrame();
		j.setTitle("Rhythm Game"); // Changed title
		j.setSize(new Dimension(900,900)); // Set size explicitly
		j.setPreferredSize(new Dimension(900,900)); // Set preferred size
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add key listener
		j.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				k.press(e.getKeyChar());
			}
			public void keyReleased(KeyEvent e) {
				k.release(e.getKeyChar());
			}
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == 'q') {
					quit = true;
				}
			}
		});

		// Create and add the Main panel
		Main gamePanel = new Main();
		j.add(gamePanel);
		j.setBackground(new Color(0,0,0)); // Set background color
		j.setResizable(false); // Prevent resizing
		j.setVisible(true); // Make frame visible
		j.requestFocus(); // Request focus for key events


		catcher.setWindowSize(j.getWidth(), j.getHeight());

		// Initialize NoteController with the number of lanes (derived from FurInputHandler or set explicitly)
		int numberOfLanes = k.getSize(); // Use size from FurInputHandler (set to 4 above)
		noteList = new NoteController(k, numberOfLanes);
		noteList.setTarget(catcher.getBindingBox());
		noteList.attachScoreCounter(scorecounter);
		noteList.setWindowSize(j.getWidth(), j.getHeight());

		// Calculate the area below the catcher
		blocc = new Rectangle2D.Double(
				0,
				catcher.getBindingBox().getY()+catcher.getBindingBox().getHeight(),
				j.getWidth(),
				j.getHeight() - (catcher.getBindingBox().getY()+catcher.getBindingBox().getHeight()) // Correct height calculation
		);


		// --- Beatmap Loading ---
		// Use the SimpleBeatmapParser instead of OsuParser
		SimpleBeatmapParser beatmapParser = new SimpleBeatmapParser(CUSTOM_BEATMAP_PATH);
		beatmapParser.parse(noteList);

		// Sort notes by start time after loading
		noteList.sort();

		// --- Audio Loading ---
		System.out.println("Loading audio from: " + AUDIO_FILE_PATH);
		// Use JLayer Player for MP3 files
		// This runs in a separate thread so it doesn't block the game loop
		new Thread(() -> {
			try {
				FileInputStream fis = new FileInputStream(AUDIO_FILE_PATH);
				Player player = new Player(fis);
				player.play();
				// No easy way to get playback position or signal end from Player
				// The game loop will end based on the last note's time.
			} catch (FileNotFoundException e) {
				System.err.println("Audio file not found: " + AUDIO_FILE_PATH);
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println("Audio playback error:");
				e.printStackTrace();
			}
		}).start();


		// --- Game Loop ---
		// Determine game duration based on the last note's end time plus a buffer
		// NoteController.getLatest() returns the latest *end* or *release* time in ms
		double gameDurationMillis = noteList.getLatest() + 5000; // Last note time + 5 second buffer

		// Calculate start offset (how much time before the first note the game starts)
		// A negative offset means the game starts before time=0, allowing notes to scroll down.
		double startOffset = -5000; // Start 5 seconds before time=0 to see notes approach

		double startTime = System.currentTimeMillis(); // Wall clock time when the game loop starts
		double lastFrameTime = System.currentTimeMillis(); // Wall clock time at the start of the previous frame

		System.out.println("Starting game loop...");
		System.out.println("Earliest note time: " + noteList.getEarliest() + " ms");
		System.out.println("Latest note time: " + noteList.getLatest() + " ms");
		System.out.println("Game will run until approximately: " + gameDurationMillis + " ms (game time)");


		while (!quit) {
			long loopStartTime = System.currentTimeMillis(); // Time at the very start of this loop iteration

			// Calculate current game time (accounts for offset)
			double currentTimeMillis = loopStartTime - startTime + startOffset;

			// Calculate delta time (time elapsed since the start of the previous loop iteration)
			double deltaTimeMillis = loopStartTime - lastFrameTime;
			lastFrameTime = loopStartTime; // Update for the next iteration

			// Check exit condition: game time exceeds total duration AND all notes have been processed/removed
			// Checking if noteList is empty is important for short beatmaps or if loading failed
			if (currentTimeMillis > gameDurationMillis && noteList.getList().isEmpty()) {
				System.out.println("Game duration reached and no notes left.");
				quit = true; // Set quit flag to break loop
			}

			// --- Update Game State ---
			catcher.update(); // Update catcher state based on input handler
			noteList.update(currentTimeMillis, deltaTimeMillis); // Update notes (movement, input checking, scoring)

			// --- Rendering ---
			j.repaint(); // Request a repaint call (triggers paintComponent)

			// --- FPS Control ---
			long loopEndTime = System.currentTimeMillis(); // Time at the end of this loop iteration
			long frameDuration = loopEndTime - loopStartTime; // How long this frame took

			long sleepTime = 10 - frameDuration; // Aim for ~100 FPS (1000/10 = 10ms frame time)

			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// Handle interruption if needed
					Thread.currentThread().interrupt(); // Restore interrupt status
					quit = true; // Also signal quit on interruption
				}
			}
		}

		System.out.println("Game loop ended.");

		// --- Cleanup ---
		// Ensure paintComponent finishes before disposing if possible (Swing handles this usually)
		// Give Swing a moment to process any pending events, although repaint should be quick.
		try {
			Thread.sleep(100); // A small delay just in case, though usually not strictly necessary
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}


		// Dispose window first
		if (j != null) {
			j.dispose();
			System.out.println("JFrame disposed.");
		}

		// Clean up other components
		if (noteList != null) {
			noteList.destroy();
			System.out.println("NoteController destroyed.");
		}
		// Catcher and InputHandler might not need explicit destroy if they don't hold significant native resources,
		// but including destroy methods is good practice if they did.
		if (catcher != null) {
			catcher.destroy(); // Make sure NoteCatcherController has a destroy method
			System.out.println("NoteCatcherController destroyed.");
		}
		if (k != null) {
			k.destroy(); // Make sure FurInputHandler has a destroy method
			System.out.println("FurInputHandler destroyed.");
		}
		// ScoreCounter likely doesn't need destroy

		// Audio cleanup happens implicitly when the Player thread finishes or the program exits.
		// If using Clip, audioClip.stop() and audioClip.close() would be called here.

		System.out.println("Application finished.");
	}

	// Override paintComponent for custom drawing
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g); // Call super for proper Swing rendering (clears background)

		Graphics2D g2 = (Graphics2D)g;
		// System.out.println("=== PAINT CALL ==="); // Debug line - can be noisy

		// Add anti-aliasing for smoother drawing
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


		// --- Drawing Notes ---
		if (noteList != null) {
			// Visible notes list is updated by NoteController.update()
			for(Note i : noteList.getVisibleNotes()) {
				if(i == null || i.getRect() == null) {
					// System.err.println("Skipping null note/rect in visibleNotes during paint."); // Less noisy
					continue;
				}
				try {
					// Draw the note rectangle
					g2.setColor(new Color(255, 50, 255)); // Bright pink fill
					g2.fill(i.getRect());
					g2.setColor(Color.YELLOW); // Yellow outline
					g2.draw(i.getRect());

					// Optional: Draw something specific for HoldNotes if needed
					if (i instanceof HoldNote) {
						// Add unique rendering for hold notes if desired
					}

				} catch(Exception e) { // Catch any drawing issues for a single note
					System.err.println("Rendering error for a note: " + e.getMessage());
					// e.printStackTrace(); // Be cautious with printing full stack traces in paint
				}
			}
		} else {
			// Optional: print a message if noteList is null during paint
			// System.out.println("NoteList is null during paint, skipping note drawing."); // Less noisy
		}

		// --- Drawing Hidden Area Gradient ---
		// Check catcher and hidden before using
		if(hidden && catcher != null && catcher.getBindingBox() != null) {
			try {
				// Ensure dimensions are sensible before creating gradient/rectangle
				double catcherY = catcher.getBindingBox().getY();
				double gradientHeight = 500; // How far above the catcher the gradient extends
				double gradientStartY = catcherY;
				double gradientEndY = catcherY - gradientHeight;

				if (gradientEndY < 0) gradientEndY = 0; // Don't draw beyond top of window
				if (gradientStartY > 0 && gradientEndY < gradientStartY) { // Only draw if gradient has positive height
					GradientPaint gradient = new GradientPaint(
							0, (int) gradientStartY, new Color(0, 0, 0, 255), // Opaque Black at catcher line
							0, (int) gradientEndY, new Color(0, 0, 0, 0));   // Transparent Black upwards
					g2.setPaint(gradient);

					Rectangle2D.Double fadeRect = new Rectangle2D.Double(0, gradientEndY, getWidth(), gradientStartY - gradientEndY);
					if (fadeRect != null) {
						g2.fill(new Area(fadeRect));
					}
				}
			} catch (Exception e) { // Catching broader Exception for safety here
				System.err.println("Rendering error for hidden gradient: " + e.getMessage());
				// e.printStackTrace();
			}
		}

		// --- Drawing Catcher Fill (Pressed Lanes) ---
		// This draws the colored rectangles when a key is pressed for a lane
		g2.setColor(new Color(255,0,0, 150)); // Semi-transparent Red color
		// Check catcher before calling getFill()
		if (catcher != null) {
			Rectangle2D.Double[] fillRects = catcher.getFill();
			if (fillRects != null) { // Check the returned array
				for(Rectangle2D.Double i : fillRects) {
					if (i != null) { // Check individual rectangles in the array
						g2.fill(i);
					} else {
						// System.err.println("Skipping null fill rectangle from catcher."); // Less noisy
					}
				}
			}
		} else {
			// System.out.println("Catcher is null during paint, skipping fill drawing."); // Less noisy
		}


		// --- Drawing Score and Combo ---
		// Check fonts and scorecounter before using
		if (scoreFont != null && scorecounter != null) {
			g2.setColor(Color.WHITE); // Set color for text
			g2.setFont(scoreFont);
			// Position the score (adjust x, y as needed)
			g2.drawString("Score: " + (int)scorecounter.getScore(), 20, 40);
		} else {
			// System.out.println("Score font or counter is null during paint, skipping score drawing."); // Less noisy
		}

		if (comboFont != null && scorecounter != null) {
			g2.setColor(Color.CYAN); // Set color for combo text
			g2.setFont(comboFont);
			// Position the combo (adjust x, y as needed, maybe center it)
			// This positioning (100, 500) seems fixed, might need adjustment
			String comboText = "" + (int)scorecounter.getCombo();
			FontMetrics fm = g2.getFontMetrics();
			int textWidth = fm.stringWidth(comboText);
			int panelWidth = getWidth();
			int comboX = (panelWidth - textWidth) / 2; // Center the combo text horizontally
			int comboY = (int) (catcher.getBindingBox().getY() - 50); // Position above catcher? Or fixed?
			if(comboY < 100) comboY = 100; // Prevent drawing off top

			g2.drawString(comboText, comboX, comboY);
		} else {
			// System.out.println("Combo font or counter is null during paint, skipping combo drawing."); // Less noisy
		}


		// --- Drawing Bottom Block ---
		g2.setColor(new Color(50,50,50)); // Dark Gray
		// Check blocc before using
		if (blocc != null) {
			g2.fill(blocc);
		} else {
			// System.out.println("Blocc is null during paint, skipping blocc drawing."); // Less noisy
		}

		// Optional: Draw the catcher outlines
		// g2.setColor(Color.BLUE);
		// if (catcher != null) {
		//     Rectangle2D.Double[] outlines = catcher.getDraw(); // Assuming this method exists and returns rects
		//     if (outlines != null) {
		//         for (Rectangle2D.Double outline : outlines) {
		//             if (outline != null) {
		//                 g2.draw(outline);
		//             }
		//         }
		//     }
		// }
	}

	// Ensure NoteCatcherController has a basic destroy method
	// Add to NoteCatcherController.java:
	// public void destroy() {
	//     // Clear any lists or nullify references if needed
	//     // Example: if it held references to graphics objects or threads
	// }
}