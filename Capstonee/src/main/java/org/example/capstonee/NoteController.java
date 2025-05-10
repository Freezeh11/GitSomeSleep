package org.example.capstonee;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class NoteController {
	// Use startTime comparator for the main queue so we process notes in order of when they become relevant
	private PriorityQueue<Note> noteList; // Notes not yet visible or active
	private LinkedList<Note> visibleNotes; // Notes currently on screen or in the hit window
	private ArrayList<Note> activeHolds; // Hold notes currently being held by the player
	private Rectangle2D.Double target;
	private FurInputHandler iH;
	private ScoreCounter sC;
	private LinkedList<Note> removeList; // Notes to be removed at the end of the update cycle
	private double earliestStart; // Time of the earliest note's start
	private double latestEnd; // Time of the latest note's end/release

	public int WINDOW_WIDTH = 1000; // Default value, updated by setWindowSize
	public int WINDOW_HEIGHT = 1000; // Default value, updated by setWindowSize
	private int amtOfLanes; // Added missing field

	// Keep track of notes that have started but haven't finished
	private LinkedList<Note> activeNotes; // Notes that have started moving but haven't timed out/been hit


	public NoteController(FurInputHandler iH, int amtOfLanes) {
		this.amtOfLanes = amtOfLanes;
		this.iH = iH;
		Note.WIDTH = (WINDOW_WIDTH / amtOfLanes); // Set note width based on window size and lanes
		// Use a comparator to sort by startTime for PriorityQueue
		noteList = new PriorityQueue<Note>((n1, n2) ->
				Double.compare(n1.getStartTime(), n2.getStartTime()));

		removeList = new LinkedList<Note>();
		activeHolds = new ArrayList<Note>(); // Still useful for tracking *held* state
		visibleNotes = new LinkedList<Note>(); // Still useful for rendering
		activeNotes = new LinkedList<>(); // New list to manage notes that are 'in play'

		// Initialize earliest/latest to sensible defaults
		earliestStart = Double.MAX_VALUE;
		latestEnd = Double.MIN_VALUE;
	}

	// Overloaded constructor (might not be needed if only using the one above in Main)
	public NoteController(Rectangle2D.Double target, int amtOfLanes) {
		this(null); // Call the main constructor
		this.setTarget(target); // Set the target afterwards
	}

	public void attachScoreCounter(ScoreCounter c) {
		this.sC = c;
	}

	public void addNote(Note n) {
		// Add note to the main list (sorted by startTime)
		noteList.add(n);
		// Update earliest/latest times
		earliestStart = Math.min(n.getStartTime(), earliestStart);
		latestEnd = Math.max(n.getEndTime(), latestEnd); // Use endTime here initially
		if (n instanceof HoldNote) {
			latestEnd = Math.max(n.getReleaseTime(), latestEnd); // Update if it's a hold note with later release
		}
	}

	public void addNote(Note n, Rectangle2D.Double t) {
		// Set target first before calculating start/end times based on it (if applicable)
		n.setTarget(t); // setTarget calculates spd based on target and times
		addNote(n); // Add to the list and update earliest/latest
	}

	public void addNote(Note n, Rectangle2D.Double t, double releaseTime) {
		// Set target and release time first
		n.setTarget(t, releaseTime); // setTarget calculates spd based on target and times
		addNote(n); // Add to the list and update earliest/latest (latestEnd updated in addNote based on releaseTime)
	}

	// Convenience methods calling the main addNote methods with the controller's target
	public void addNote(Note n, double releaseTime) {
		addNote(n, this.target, releaseTime);
	}

	public void createTapNote(char lane, double timing) {
		// Create note and add it using the controller's target
		addNote(new TapNote(lane, timing), this.target);
	}
	public void createTapNote(int lane, double timing) {
		addNote(new TapNote(lane, timing), this.target);
	}
	public void createTapNote(char lane, double timing, Rectangle2D.Double t) {
		TapNote note = new TapNote(lane, timing);
		addNote(note, t);
	}


	public void createHoldNote(char lane, double timing, double releaseTime) {
		addNote(new HoldNote(lane, timing), this.target, releaseTime);
	}
	public void createHoldNote(int lane, double timing, double releaseTime) {
		addNote(new HoldNote(lane, timing), this.target, releaseTime);
	}

	public void setTarget(Rectangle2D.Double t) {
		setTarget(t, false); // Default is not to update existing notes
	}

	public void setTarget(Rectangle2D.Double t, boolean updateExistingNotes) {
		this.target = t;
		if(updateExistingNotes) {
			System.out.println("Warning: Updating target for existing notes can be inefficient.");
			// Update notes in the main queue
			for(Note n : noteList) {
				if(n != null) n.setTarget(this.target);
			}
			// Update notes in the active/visible lists if they exist
			for(Note n : activeNotes) { // Or visibleNotes, depending on which list needs updating
				if(n != null) n.setTarget(this.target);
			}
			for(Note n : activeHolds) {
				if(n != null) n.setTarget(this.target);
			}
		}
	}

	public void setWindowSize(int width, int height) {
		WINDOW_WIDTH = width;
		WINDOW_HEIGHT = height;
		Note.WIDTH = (double)WINDOW_WIDTH / amtOfLanes; // Use double division
	}

	public void sort() {
		// PriorityQueue maintains sorted order (by startTime due to compareTo/comparator)
		// Rebuilding the queue ensures order if needed.
		ArrayList<Note> tempList = new ArrayList<>(noteList);
		noteList.clear(); // Clear the old queue
		noteList.addAll(tempList); // Add all notes back, PQ will re-sort
		System.out.println("Notes sorted by start time.");
	}


	// Checks input for a single note at a given time.
	// Returns true if the note should be removed.
	private boolean checkNoteInput(Note n, double currentTime) {
		char lane = n.getLane();

		if (n instanceof TapNote) {
			// Tap note is hit if key is pressed and within timing window
			if (iH.getValue(lane) && !iH.getLock(lane)) {
				int hitResult = n.computeInput(currentTime);
				if (hitResult > 0) { // 2 = Good, 3 = Perfect
					sC.addScore(hitResult); // Add score for Good/Perfect
					iH.lock(lane); // Lock the key until released by Main
					// System.out.printf("Tap hit on lane %c at time %.0f! Result: %d%n", lane, currentTime, hitResult);
					return true; // Remove the note
				}
			}
			// Tap note times out (miss) if it passes the hit window
			if (currentTime > n.getEndTime() + Note.MISS) {
				sC.addScore(1); // Add score for Miss (breaks combo)
				// System.out.printf("Tap missed on lane %c at time %.0f%n", lane, currentTime);
				return true; // Remove the note
			}

		} else if (n instanceof HoldNote) {
			HoldNote holdNote = (HoldNote) n;

			// HOLD NOTE START: Check if player is holding the key when the note reaches the target
			// If the key is pressed AND the note is within the "start hold" window AND it's not already tracked
			if (iH.getValue(lane) && !holdNote.isTracked()) {
				// Check timing relative to the note's *endTime* (where the head hits the line)
				double timeDelta = Math.abs(holdNote.getEndTime() - currentTime);
				if (timeDelta <= Note.GOOD) { // Use GOOD window as the start timing tolerance
					activeHolds.add(holdNote); // Start tracking the hold
					holdNote.setTracked(true); // Mark as tracked
					// We don't need to lock the key *in the IH* here for game logic,
					// the IH's getValue() and Main's lock/unlock handle the key state representation.
					// But locking here might have been intended to prevent hitting multiple notes
					// in the same lane with one press - which is handled by the !iH.getLock(lane) check.
					// Let's keep the IH lock logic in Main and the catcher update.

					// System.out.printf("Hold started on lane %c at time %.0f%n", lane, currentTime);
				}
			}

			// HOLD NOTE ACTIVE: If the note is being tracked
			if (holdNote.isTracked()) {
				// Player must continue holding the key
				if (iH.getValue(lane)) {
					// Still holding - grant score over time
					sC.addScoreRaw(0.005, 0.005); // Example: Add small score/combo per frame

					// Check for release timing (if the note head has passed endTime)
					// If current time passes releaseTime, the note should end (successfully held)
					if (currentTime >= holdNote.getReleaseTime()) {
						// Successfully held the entire note
						// System.out.printf("Hold finished successfully on lane %c at time %.0f%n", lane, currentTime);
						activeHolds.remove(holdNote); // Stop tracking the hold
						holdNote.setTracked(false);
						return true; // Remove the note
					}

				} else {
					// Player released the key while holding *before* releaseTime
					// This is a broken hold
					if (currentTime < holdNote.getReleaseTime()) {
						// System.out.printf("Hold broken on lane %c at time %.0f (released early)%n", lane, currentTime);
						sC.addScore(1); // Break combo (like a miss)
						activeHolds.remove(holdNote); // Stop tracking
						holdNote.setTracked(false);
						return true; // Remove the broken note
					}
					// If released at or after releaseTime, it was a successful hold (handled above)
				}
			}

			// HOLD NOTE TIMEOUT: If the note was never tracked and passes its hit window for starting
			if (!holdNote.isTracked() && currentTime > holdNote.getEndTime() + Note.GOOD) { // Use GOOD window for hold start miss
				sC.addScore(1); // Missed the hold start
				// System.out.printf("Hold start missed on lane %c at time %.0f%n", lane, currentTime);
				return true; // Remove the note
			}
			// A tracked hold note that isn't completed by release time might scroll off screen
			// Add a timeout *after* its release time if it's still active but wasn't successfully removed.
			// This can happen if the player held it successfully but the removal logic had an issue,
			// or if they held it past the release time and then released much later.
			if (holdNote.isTracked() && currentTime > holdNote.getReleaseTime() + Note.MISS) {
				// This is a cleanup case for tracked notes that weren't removed
				// System.out.printf("Tracked hold cleanup timeout on lane %c at time %.0f%n", lane, currentTime);
				activeHolds.remove(holdNote); // Ensure it's off the active holds list
				holdNote.setTracked(false);
				return true; // Remove the note
			}
		}

		return false; // Note should not be removed yet
	}


	// --- Main Update Method ---
	// currentTime: absolute game time in milliseconds
	// deltaTimeMillis: time elapsed since last frame in milliseconds
	public void update(double currentTime, double deltaTimeMillis) {
		// First, manage the list of notes that are 'in play' (visible or active)
		addNotesToActiveList(currentTime); // Move notes from noteList to activeNotes if their start time is reached

		// Process notes that are currently in play
		// Iterate over a copy to allow removal from activeNotes within the loop
		for (Note n : new LinkedList<>(activeNotes)) {
			if (n == null) {
				System.err.println("Null note found in activeNotes during update!");
				activeNotes.remove(n); // Attempt to remove nulls
				continue;
			}

			// Move the note if it should be moving
			if (currentTime >= n.getStartTime()) { // Check if the note's start time has passed
				if (!n.getMoving()) {
					n.setMoving(true); // Start the note moving
					// Ensure target is set (should be done on add, but safety check if target was null initially)
					if (this.target != null) {
						// Calling setTarget again might recalculate speed, which is fine if target changed.
						// If target is static, this check isn't strictly needed if set on add.
						// n.setTarget(this.target); // Moved to addNote methods
					} else {
						System.err.println("Warning: Note is active but target is null!");
					}
				}
				// Only move if MOVING flag is true (set above when start time is reached)
				n.autoMove(deltaTimeMillis);
			}

			// Check for player input and note timeout
			// checkNoteInput returns true if the note should be removed
			if (checkNoteInput(n, currentTime)) {
				removeList.add(n); // Add note to the list for removal
			}
		}

		// Remove notes marked for removal
		while (!removeList.isEmpty()) {
			Note n = removeList.removeFirst(); // Use removeFirst for LinkedList
			if (activeNotes.remove(n)) { // Remove from the active list
				// Successfully removed from activeNotes
			} else {
				System.err.println("Tried to remove note not found in active list: " + n);
			}

			// Also remove from visibleNotes if it's there (less efficient, but ensures consistency)
			visibleNotes.remove(n);

			// Clean up the note object
			n.destroy(); // Call note's destroy method (e.g., move offscreen)
		}
		removeList.clear(); // Ensure removeList is empty for the next frame


		// Update the list of notes that should be *rendered* this frame.
		// This is separate from 'activeNotes' which are notes in play for logic.
		// visibleNotes are notes within the screen bounds + buffer for drawing.
		updateVisibleNotes(currentTime); // Populate the visibleNotes list for paintComponent

	}

	// Pulls notes from the main priority queue (`noteList`) into `activeNotes`
	// once their start time has passed (considering a buffer).
	private void addNotesToActiveList(double currentTime) {
		// Check notes at the head of the priority queue
		while (!noteList.isEmpty() && noteList.peek() != null) {
			Note nextNote = noteList.peek();

			// Decide when a note becomes "active" (enters the playfield)
			// Let's use startTime minus the same buffer used for visibility in paint
			// or a fixed pre-roll time. 5000ms (5s) before start time seems reasonable.
			double activeThreshold = nextNote.getStartTime() - 5000;

			if (currentTime >= activeThreshold) {
				Note noteToAdd = noteList.poll(); // Remove from priority queue
				if (noteToAdd != null) {
					activeNotes.add(noteToAdd); // Add to the active list
					// System.out.println("Moved note to active list: " + noteToAdd);
				}
			} else {
				// The next note in the queue is not yet active, so neither are subsequent notes (due to sorting)
				break;
			}
		}
	}


	// Populates the visibleNotes list for drawing by filtering active notes
	private void updateVisibleNotes(double currentTime) {
		visibleNotes.clear(); // Clear the list from the previous frame
		// Iterate through active notes (notes currently in the playfield)
		for (Note note : activeNotes) {
			if (note == null) continue;

			// Notes in the activeNotes list are considered visible for drawing
			// as they are within the game playfield timeline (-5s before start to when removed).
			// A simpler approach might be to just add all active notes to visibleNotes.
			// If performance is an issue, add logic here to check the note's Y position
			// relative to the window boundaries.
			// For now, let's assume all active notes are visible.
			// The time-based check below is an alternative way to filter.
			// if (note.getEndTime() + Note.MISS + 5000 >= currentTime) { // Keep notes visible for a bit after their timeout
			visibleNotes.add(note);
			// }
		}
	}


	public PriorityQueue<Note> getList(){
		// Return the main queue (notes not yet active)
		return this.noteList;
	}

	/**
	 * Returns the list of notes that are currently active in the gameplay area.
	 * These notes have passed their pre-roll start time and have not yet
	 * been hit, missed, or timed out.
	 * This list is used by the update loop and potentially for rendering.
	 * @return The LinkedList of active Notes.
	 */
	public LinkedList<Note> getActiveNotes() {
		return this.activeNotes;
	}


	public double getEarliest() {
		// Return EarliestStart if valid, otherwise check lists
		if (earliestStart != Double.MAX_VALUE) return earliestStart;
		// Fallback: find earliest start time among all notes if earliestStart wasn't set
		double minStart = Double.MAX_VALUE;
		for (Note n : noteList) {
			if (n != null) minStart = Math.min(minStart, n.getStartTime());
		}
		for (Note n : activeNotes) {
			if (n != null) minStart = Math.min(minStart, n.getStartTime());
		}
		return minStart == Double.MAX_VALUE ? 0 : minStart; // Return 0 if no notes
	}

	public double getLatest() {
		// Return LatestEnd if valid, otherwise find latest end time among all notes
		if (latestEnd != Double.MIN_VALUE) return latestEnd;
		// Fallback: find latest end time/release time
		double maxEnd = Double.MIN_VALUE;
		for (Note n : noteList) {
			if (n != null) {
				maxEnd = Math.max(maxEnd, n.getEndTime());
				if (n instanceof HoldNote) maxEnd = Math.max(maxEnd, n.getReleaseTime());
			}
		}
		for (Note n : activeNotes) {
			if (n != null) {
				maxEnd = Math.max(maxEnd, n.getEndTime());
				if (n instanceof HoldNote) maxEnd = Math.max(maxEnd, n.getReleaseTime());
			}
		}
		return maxEnd == Double.MIN_VALUE ? 0 : maxEnd; // Return 0 if no notes
	}

	public int getLaneCount() {
		return amtOfLanes;
	}

	public LinkedList<Note> getVisibleNotes() {
		// Return the list populated by updateVisibleNotes
		return visibleNotes;
	}


	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("--- NoteController State ---\n");
		out.append("NoteList (Queue, pending): ").append(noteList != null ? noteList.size() : "null").append(" notes\n");
		out.append("ActiveNotes (In Play): ").append(activeNotes != null ? activeNotes.size() : "null").append(" notes\n");
		out.append("VisibleNotes (Rendering): ").append(visibleNotes != null ? visibleNotes.size() : "null").append(" notes\n");
		out.append("ActiveHolds: ").append(activeHolds != null ? activeHolds.size() : "null").append(" notes\n");
		out.append("RemoveList: ").append(removeList != null ? removeList.size() : "null").append(" notes\n");
		out.append("Earliest Note Start: ").append(earliestStart).append(" ms\n");
		out.append("Latest Note End/Release: ").append(latestEnd).append(" ms\n");
		out.append("--------------------------\n");
		return out.toString();
	}

	public void destroy() {
		System.out.println("NoteController destroying...");
		// Destroy notes in all relevant lists that hold unique note objects
		// PriorityQueue
		if (noteList != null) {
			while(!noteList.isEmpty()) {
				Note n = noteList.poll();
				if (n != null) n.destroy();
			}
			noteList = null;
		}

		// Active Notes
		if (activeNotes != null) {
			for (Note n : activeNotes) { if (n != null) n.destroy(); }
			activeNotes.clear(); activeNotes = null;
		}

		// visibleNotes and activeHolds are views/subsets, notes are destroyed via activeNotes/noteList
		if (visibleNotes != null) {
			visibleNotes.clear(); visibleNotes = null;
		}
		if (activeHolds != null) {
			activeHolds.clear(); activeHolds = null;
		}
		if (removeList != null) { // Should be empty but clean up
			for (Note n : removeList) { if (n != null) n.destroy(); } // Defensive, notes likely destroyed already
			removeList.clear(); removeList = null;
		}


		target = null;
		iH = null;
		sC = null;
		System.out.println("NoteController destroyed.");
	}

	// This method seems like it's trying to scale score based on note count?
	// It's commented out in the previous response, let's keep it commented out.
	/*
	private void updateNoteValue() {
		 // Implementation from previous code (commented out)
	}
	*/


	public void setLatest(double latestTimeMillis) {
		this.latestEnd = latestTimeMillis;
		System.out.println("NoteController's latest event time set to: " + this.latestEnd + " ms");
	}
}