package org.example.capstonee;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
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

	public static void main(String[] args) throws InterruptedException {
		quit = false;

		k = new FurInputHandler(4);
		scorecounter = new ScoreCounter();
		catcher = new NoteCatcherController(k);

		scoreFont = new Font("Arial", Font.PLAIN, 50);
		comboFont = new Font("Arial", Font.PLAIN, 64);

		JFrame j = new JFrame();
		j.setTitle("hehe");
		j.setSize(new Dimension(900,900));
		j.setPreferredSize(new Dimension(900,900));
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
		j.add(new Main());
		j.setBackground(new Color(0,0,0));
		j.setResizable(false);
		j.setVisible(true);
		j.requestFocus();

		catcher.setWindowSize(j.getWidth(), j.getHeight());

		noteList = new NoteController(k, 4);
		noteList.setTarget(catcher.getBindingBox());
		noteList.attachScoreCounter(scorecounter);
		noteList.setWindowSize(j.getWidth(), j.getHeight());

		blocc = new Rectangle2D.Double(
				0,
				catcher.getBindingBox().getY()+catcher.getBindingBox().getHeight(),
				j.getWidth(),
				j.getHeight() - catcher.getBindingBox().getMaxY()
		);

		String folderPath = "src/main/resources/beatmaps/2027343 DECO_27 - Salamander feat Hatsune Miku/";

		OsuParser osuparse = new OsuParser(folderPath);
		String[] possibleDiffs = osuparse.getDifficulties();
		for(int i = 0; i < possibleDiffs.length; i++) {
			System.out.println((i+1) + ": " + possibleDiffs[i]);
		}
		Scanner scanner = new Scanner(System.in);
		osuparse.setDifficulty(possibleDiffs[scanner.nextInt()-1]);
		osuparse.parse(noteList);

		double songDuration = 180; // Set this to your actual song duration in seconds
		if (noteList.getLatest() < songDuration) {
			noteList.setLatest(songDuration);
		}

		try {
			String audioPath = osuparse.getPathToAudio();
			System.out.println("Loading audio from: " + audioPath);

			// In Main.java, modify the audio handling:
			if (audioPath.toLowerCase().endsWith(".mp3")) {
				// For MP3s, we can't easily track position, so just let it play
				new Thread(() -> {
					try {
						FileInputStream fis = new FileInputStream(audioPath);
						Player player = new Player(fis);
						player.play();
						// After playback completes
						quit = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start();
			} else {
				// For other formats, use Clip which lets us track position
				audioClip = AudioSystem.getClip();
				AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(audioPath));
				audioClip.open(inputStream);
				FloatControl gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
				gainControl.setValue(-10.0f);
				songDuration = audioClip.getMicrosecondLength() / 1000000.0; // Get actual duration
			}
		} catch (Exception e) {
			System.err.println("Audio error:");
			e.printStackTrace();
		}

		noteList.sort();
		double startOffset = Math.min(noteList.getEarliest(), 0) - 5000;
		double timeElapsed = 0 + startOffset;
		double startTime = System.currentTimeMillis();
		while(timeElapsed < songDuration * 1000 + 5000 && !quit) {
			double currentTime = System.currentTimeMillis() - startTime + startOffset;

			// Update game state
			catcher.update();
			noteList.update(currentTime, 16); // Using fixed 16ms delta time for simplicity
			j.repaint();

			// Sleep to prevent CPU overuse
			try {
				Thread.sleep(16); // ~60fps
			} catch (InterruptedException e) {}
		}
		long timeStamp_before = System.nanoTime();
		long timeStamp_after = timeStamp_before;
		double dT = 0;

// Debug prints
		System.out.println("Total notes: " + noteList.getList().size());
		System.out.println("Earliest note: " + noteList.getEarliest());
		System.out.println("Latest note: " + noteList.getLatest());


// Then modify your game loop condition:
		while(timeElapsed < songDuration * 1000 + 5000 && !quit) {
			if(timeElapsed >= 0 && audioClip != null && !audioClip.isRunning()) {
				audioClip.start();
			}

			timeStamp_before = System.nanoTime();
			dT += timeStamp_before - timeStamp_after;

			catcher.update();
			noteList.update(timeElapsed, dT);
			j.repaint();

			timeStamp_after = System.nanoTime();
			dT = timeStamp_after - timeStamp_before;
			timeElapsed = System.currentTimeMillis() - startTime + startOffset;

			// Add small delay to prevent CPU overuse
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}

		System.out.println("Game loop completed");

		j.repaint();

		// Before disposing the window
		if(audioClip != null) {
			audioClip.stop();
			audioClip.close();
		}

		j.dispose(); // Dispose window first

// Then clean up other components
		noteList.destroy();
		catcher.destroy();
		k.destroy();

// No need to explicitly set to null - let garbage collection handle it
		System.out.println("done");
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		System.out.println("=== PAINT CALL ==="); // Debug line

		// Add this debug block before drawing notes
		if (noteList.getVisibleNotes() != null) {
			for(Note i : noteList.getVisibleNotes()) {
				if(i == null || i.getRect() == null) {
					System.err.println("Skipping null note/rect");
					continue;
				}
				try {
					g2.setColor(new Color(255, 50, 255)); // Bright pink
					g2.fill(i.getRect());
					g2.setColor(Color.YELLOW);
					g2.draw(i.getRect());
				} catch(NullPointerException e) {
					System.err.println("Rendering error: " + e.getMessage());
				}
			}
		}

		// Rest of your paint method remains the same...
		if(hidden) {
			GradientPaint gradient = new GradientPaint(
					0,
					(int)catcher.getBindingBox().getY(),
					new Color(0xFF000000, true),
					0,
					(int)catcher.getBindingBox().getY()-500,
					new Color(0x00FFFFFF, true));
			g2.setPaint(gradient);
			g2.fill(new Area(new Rectangle2D.Double(0,catcher.getBindingBox().getY()-500,400,500)));
		}

		g2.setColor(new Color(255,0,0));
		if (catcher.getFill() != null) {
			for(Rectangle2D.Double i : catcher.getFill()) {
				if (i != null) {
					g2.fill(i);
				}
			}
		}

		g2.setFont(scoreFont);
		g2.drawString(""+(int)scorecounter.getScore(), 0, 40);
		g2.setFont(comboFont);
		g2.drawString(""+(int)scorecounter.getCombo(), 100, 500);
		g2.setColor(new Color(50,50,50));
		if (blocc != null) {
			g2.fill(blocc);
		}
	}
}