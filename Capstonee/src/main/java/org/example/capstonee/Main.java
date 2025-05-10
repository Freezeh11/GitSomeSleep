package org.example.capstonee;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;
import javax.sound.sampled.*;
import javazoom.jl.player.Player;

public class Main extends JPanel {
	static NoteController noteList;
	static NoteCatcherController catcher;
	static FurInputHandler k;
	static ScoreCounter scorecounter;
	static Font scoreFont, comboFont;
	static Rectangle2D.Double blocc;
	static boolean quit;
	static final boolean hidden = false;
	static Clip audioClip;

	public static void main(String[] args) {
		quit = false;

		int numberOfLanes = 4;
		k = new FurInputHandler(numberOfLanes);
		scorecounter = new ScoreCounter();
		catcher = new NoteCatcherController(k);

		scoreFont = new Font("Arial", Font.PLAIN, 50);
		comboFont = new Font("Arial", Font.PLAIN, 64);

		JFrame j = new JFrame();
		j.setTitle("hehe");
		j.setSize(new Dimension(900, 900));
		j.setPreferredSize(new Dimension(900, 900));
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		j.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				k.press(e.getKeyChar());
			}

			@Override
			public void keyReleased(KeyEvent e) {
				k.release(e.getKeyChar());
				k.unlock(e.getKeyChar());
			}

			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == 'q') {
					quit = true;
				}
			}
		});
		j.add(new Main());
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		j.setBackground(new Color(0, 0, 0));
		j.setResizable(false);
		j.setVisible(true);
		j.requestFocus();

		catcher.setWindowSize(j.getWidth(), j.getHeight());

		noteList = new NoteController(k, numberOfLanes);
		noteList.setTarget(catcher.getBindingBox());
		noteList.attachScoreCounter(scorecounter);
		noteList.setWindowSize(j.getWidth(), j.getHeight());

		if (catcher != null && catcher.getBindingBox() != null) {
			blocc = new Rectangle2D.Double(
					0,
					catcher.getBindingBox().getY() + catcher.getBindingBox().getHeight(),
					j.getWidth(),
					j.getHeight() - (catcher.getBindingBox().getY() + catcher.getBindingBox().getHeight())
			);
		} else {
			// Handle case where catcher or binding box is null
			blocc = new Rectangle2D.Double(0, j.getHeight() - 100, j.getWidth(), 100); // Default bottom block
		}


		String folderPath = "src/main/resources/beatmaps/2266557 nyankobrq & yaca - twinkle night (feat somunia)/";

		OsuParser osuparse = new OsuParser(folderPath);

		String[] possibleDiffs = osuparse.getDifficulties();
		int selectedDiff = 0;
		if (possibleDiffs != null && possibleDiffs.length > 0) {
			System.out.println("Available difficulties:");
			for (int i = 0; i < possibleDiffs.length; i++) {
				System.out.println((i + 1) + ": " + possibleDiffs[i]);
			}
			Scanner scanner = new Scanner(System.in);
			int choice = 0;
			while (choice <= 0 || choice > possibleDiffs.length) {
				System.out.print("Select difficulty (1-" + possibleDiffs.length + "): ");
				if (scanner.hasNextInt()) {
					choice = scanner.nextInt();
				} else {
					System.out.println("Invalid input. Please enter a number.");
					scanner.next(); // consume the invalid input
				}
			}
			selectedDiff = choice - 1;
			scanner.close(); // Close the scanner
		} else {
			System.err.println("No .osu files found in the specified directory.");
			// Optionally quit or load a default beatmap
			quit = true; // Exit if no beatmaps are found
		}


		if (!quit) { // Only proceed if a difficulty was selected
			osuparse.setDifficulty(possibleDiffs[selectedDiff]);
			osuparse.parse(noteList);

			noteList.sort(); // Sort notes by start time


			double songDuration = 180 * 1000; // Default duration in milliseconds
			String audioPath = osuparse.getPathToAudio();
			Player mp3Player = null;
			audioClip = null; // Explicitly nullify Clip if using MP3

			if (audioPath != null) {
				System.out.println("Loading audio from: " + audioPath);
				try {
					if (audioPath.toLowerCase().endsWith(".mp3")) {
						FileInputStream fis = new FileInputStream(audioPath);
						mp3Player = new Player(fis);
						// JLayer Player does not easily provide duration or time position.
						// We'll rely on the beatmap's latest note time for game duration.
						System.out.println("Loaded MP3 audio.");
					} else {
						// Assume other formats are loadable by Clip
						File audioFile = new File(audioPath);
						if (audioFile.exists()) {
							AudioInputStream inputStream = AudioSystem.getAudioInputStream(audioFile);
							audioClip = AudioSystem.getClip();
							audioClip.open(inputStream);
							FloatControl gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
							gainControl.setValue(-10.0f); // Reduce volume
							songDuration = audioClip.getMicrosecondLength() / 1000.0; // Get actual duration in milliseconds
							System.out.println("Loaded WAV/other audio. Duration: " + songDuration + " ms");
						} else {
							System.err.println("Audio file not found: " + audioPath);
						}
					}
				} catch (Exception e) {
					System.err.println("Audio loading error:");
					e.printStackTrace();
					// Continue without audio if loading fails
				}
			} else {
				System.err.println("Audio path not found in beatmap.");
			}

			// Set game duration based on the latest note time plus a buffer
			// The parser updates noteList.latestEnd (which is in ms)
			double gameDurationMillis = noteList.getLatest() + 5000; // Last event time + 5 second buffer


			// Calculate start offset (how much time before time=0 the game actually starts)
			// Notes will start moving when game time reaches their start time.
			// We want the game to start *before* the first note's start time
			// so the player can see it coming.
			double startOffset = noteList.getEarliest() - 5000; // Start 5 seconds before the first note's start time

			// If earliest note time is very early or negative (e.g., Osu),
			// ensure the offset doesn't make the game start excessively early.
			// Maybe a minimum offset is better.
			startOffset = Math.min(startOffset, -5000.0); // Start at most 5 seconds before time=0


			// --- Game Loop ---
			double startTimeMillis = System.currentTimeMillis(); // Wall clock time when the game loop starts
			double lastFrameTimeMillis = System.currentTimeMillis(); // Wall clock time at the start of the previous frame

			System.out.println("Starting game loop...");
			System.out.println("Earliest note time: " + noteList.getEarliest() + " ms");
			System.out.println("Latest note time: " + noteList.getLatest() + " ms");
			System.out.println("Estimated game end time (ms from beatmap start): " + gameDurationMillis);
			System.out.println("Game loop start offset: " + startOffset + " ms");


			// Start audio playback slightly before the first note appears, if Clip is used
			// For MP3 Player, it was already started in its thread or will be started manually below.
			if (audioClip != null) {
				// Start playback when game time reaches the beginning of the relevant period
				// We can't perfectly sync System.currentTimeMillis with audio clip position easily.
				// A simple approach is to start the clip when game time hits 0 or starts (if offset is negative).
				// A more complex approach would involve getting the clip's microsecond position and syncing game time to it.
				// Let's just start it relative to the game loop's clock reaching zero.
				// The game loop's currentTimeMillis will be negative initially due to startOffset.
				// Start audio when currentTimeMillis is near 0 or starts.
				if (startOffset < 0) { // If we have a pre-roll time
					// Start audio when wall clock time minus game loop start time equals the absolute value of the offset
					// This means audio starts 'startOffset' milliseconds after the game loop begins.
					long audioStartTimeOffset = (long)Math.abs(startOffset);
					try {
						Thread.sleep(audioStartTimeOffset);
						if (audioClip != null && !audioClip.isRunning()) {
							audioClip.start();
							System.out.println("Clip audio started after offset.");
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						quit = true;
					}
				} else { // If startOffset is 0 or positive, start audio immediately or when game time reaches 0
					if (audioClip != null && !audioClip.isRunning()) {
						audioClip.start();
						System.out.println("Clip audio started immediately.");
					}
				}
			} else if (mp3Player != null) {
				// If using JLayer Player, it's probably best to start its thread
				// slightly before the game loop starts its time tracking from 0.
				// This requires managing the audio thread carefully.
				// Given the previous code started the MP3 thread immediately after loading,
				// we'll assume that's the intended behaviour for Player.
				// There is no easy way to *stop* or *reposition* a JLayer Player.
				System.out.println("MP3 audio started by its loading thread.");
			}


			while (!quit) {
				long loopStartTimeMillis = System.currentTimeMillis();

				// Calculate current game time (absolute time in milliseconds relative to beatmap start = 0)
				double currentTimeMillis = loopStartTimeMillis - startTimeMillis + startOffset;

				// Calculate delta time (time elapsed since the start of the previous frame in milliseconds)
				double deltaTimeMillis = loopStartTimeMillis - lastFrameTimeMillis;
				lastFrameTimeMillis = loopStartTimeMillis;

				// Update game state
				// Synchronize access to noteList as it's modified by update and read by paint
				synchronized (noteList) {
					noteList.update(currentTimeMillis, deltaTimeMillis);
				}

				// Rendering
				j.repaint(); // Request a repaint (call paintComponent)

				// Check Exit Condition
				// Exit when game time is past the latest event AND there are no active/pending notes left
				boolean notesFinished = noteList.getActiveNotes().isEmpty() && noteList.getList().isEmpty();
				if (currentTimeMillis > gameDurationMillis && notesFinished) {
					System.out.println("Game duration reached and notes finished. Quitting.");
					quit = true;
				}

				// FPS Control
				long loopEndTimeMillis = System.currentTimeMillis();
				long frameDurationMillis = loopEndTimeMillis - loopStartTimeMillis;

				long targetFrameDuration = 1000 / 60; // Aim for 60 FPS
				long sleepTimeMillis = targetFrameDuration - frameDurationMillis;

				if (sleepTimeMillis > 0) {
					try {
						Thread.sleep(sleepTimeMillis);
					} catch (InterruptedException e) {
						System.err.println("Game loop interrupted.");
						Thread.currentThread().interrupt();
						quit = true;
					}
				}
			} // End of game loop
		} // End of if (!quit) block after difficulty selection


		System.out.println("Game loop ended or beatmap not loaded.");

		// --- Cleanup ---
		try {
			Thread.sleep(100); // Give Swing a moment for final repaint
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Stop and close audio
		if (audioClip != null) {
			audioClip.stop();
			audioClip.close();
			System.out.println("Clip audio stopped and closed.");
		}
		// No easy way to stop JLayer Player thread gracefully here. It finishes when done.


		if (j != null) {
			j.dispose();
			System.out.println("JFrame disposed.");
		}

		if (noteList != null) {
			noteList.destroy();
		}
		if (catcher != null) {
			catcher.destroy();
		}
		if (k != null) {
			k.destroy();
		}

		System.out.println("Application finished.");
	}

	@Override
	public void paint(Graphics g) {
		//super.paintComponent(g); // Call super for proper Swing rendering (clears background) - Use this if inheriting from JPanel correctly
		// If not inheriting from JPanel or overriding paintComponent, manual background clear is needed.
		// Since Main extends JPanel and overrides paint, calling super.paint is usually correct for JPanels.
		// Let's use super.paint which also handles double buffering.
		super.paint(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Draw background (can also be done by super.paint if JPanel's default is set or in paintComponent)
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());


		// Draw judgment line
		g2.setColor(Color.WHITE);
		int judgmentLineY = (catcher != null && catcher.getBindingBox() != null) ? (int) catcher.getBindingBox().getY() : getHeight() - 100;
		g2.drawLine(0, judgmentLineY, getWidth(), judgmentLineY);

		// Draw notes as circles
		try {
			// Synchronize access to noteList.getVisibleNotes() as it's modified by update and read by paint
			LinkedList<Note> visibleNotesToDraw = null;
			if (noteList != null) {
				synchronized (noteList) {
					visibleNotesToDraw = noteList.getVisibleNotes();
				}
			}

			if (visibleNotesToDraw != null) {
				for (Note note : visibleNotesToDraw) {
					// Check if the note and its rectangle are valid before drawing
					if (note == null || note.getRect() == null) {
						// System.err.println("Skipping null note/rect during paint loop.");
						continue;
					}

					try {
						// Draw note body (always circle)
						if (note instanceof HoldNote) {
							g2.setColor(new Color(100, 200, 255)); // Blue for hold notes
						} else {
							g2.setColor(new Color(255, 100, 100)); // Red for tap notes
						}

						Rectangle2D.Double rect = note.getRect();

						// Ensure rectangle has valid dimensions before drawing
						if (rect.getWidth() > 0 && rect.getHeight() > 0) {
							// For circles, width and height should ideally be equal for the note head visual
							// If Note.WIDTH and Note.HEIGHT are different, fillOval draws an ellipse.
							// Let's assume Note.WIDTH and Note.HEIGHT are suitable for the head diameter/size.
							g2.fillOval(
									(int) rect.getX(),
									(int) rect.getY(),
									(int) rect.getWidth(),
									(int) rect.getHeight()
							);

							// Draw outline
							g2.setColor(Color.WHITE);
							g2.drawOval(
									(int) rect.getX(),
									(int) rect.getY(),
									(int) rect.getWidth(),
									(int) rect.getHeight()
							);

							// Optional: Draw hold note tail if it's a HoldNote
							if (note instanceof HoldNote) {
								HoldNote hn = (HoldNote) note;
								// The HoldNote's rect should encompass the head and tail.
								// The fillOval above only draws the head if rect.getHeight() is the total height.
								// To draw the tail, we need to calculate its rectangle.
								// Assuming the head is at the bottom of the HoldNote's total rect:
								double tailHeight = hn.getRect().getHeight() - Note.HEIGHT;
								if (tailHeight > 0) {
									Rectangle2D.Double tailRect = new Rectangle2D.Double(
											rect.getX(),
											rect.getY(), // Tail starts at the top of the rect
											rect.getWidth(),
											tailHeight
									);
									g2.setColor(new Color(100, 200, 255, 150)); // Slightly transparent blue for tail
									g2.fillRect(
											(int) tailRect.getX(),
											(int) tailRect.getY(),
											(int) tailRect.getWidth(),
											(int) tailRect.getHeight()
									);
									// Outline for the tail (optional)
									// g2.setColor(Color.WHITE);
									// g2.drawRect((int) tailRect.getX(), (int) tailRect.getY(), (int) tailRect.getWidth(), (int) tailRect.getHeight());
								}
							}
						} else {
							// System.err.println("Skipping note with invalid dimensions: " + rect);
						}


					} catch (Exception e) {
						System.err.println("Error drawing a specific note: " + e.getMessage());
						// e.printStackTrace(); // Be careful with stack traces in paint
					}
				}
			}
		} catch (Exception e) {
			// Catch any exception during the iteration or list access
			System.err.println("General error during note drawing loop: " + e.getMessage());
			// e.printStackTrace();
		}


		// Draw catcher fill (pressed lanes)
		g2.setColor(new Color(255, 0, 0, 150)); // Semi-transparent Red
		if (catcher != null) {
			Rectangle2D.Double[] fillRects = catcher.getFill();
			if (fillRects != null) {
				for (Rectangle2D.Double fillRect : fillRects) {
					if (fillRect != null) {
						g2.fill(fillRect);
					}
				}
			}
		}

		// Draw catcher outlines
		g2.setColor(Color.BLUE); // Blue outlines
		if (catcher != null) {
			Rectangle2D.Double[] outlines = catcher.getDraw(); // Assuming NoteCatcherController has getDraw()
			if (outlines != null) {
				for (Rectangle2D.Double outline : outlines) {
					if (outline != null) {
						g2.draw(outline);
					}
				}
			}
		}


		// Draw score and combo
		if (scorecounter != null) {
			if (scoreFont != null) {
				g2.setFont(scoreFont);
				g2.setColor(Color.WHITE);
				g2.drawString("Score: " + (int) scorecounter.getScore(), 20, 50);
			}

			if (comboFont != null) {
				g2.setFont(comboFont);
				if (scorecounter.getCombo() > 0) {
					String comboText = "" + (int) scorecounter.getCombo();
					FontMetrics fm = g2.getFontMetrics();
					int textWidth = fm.stringWidth(comboText);
					int panelWidth = getWidth();
					int comboX = (panelWidth - textWidth) / 2;
					int comboY = 100; // Fixed Y position for combo

					g2.setColor(Color.CYAN); // Combo color
					g2.drawString(comboText, comboX, comboY);
				}
			}
		}


		// Draw bottom block
		g2.setColor(new Color(50, 50, 50)); // Dark Gray
		if (blocc != null) {
			g2.fill(blocc);
		}
	}
}