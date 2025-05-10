package org.example.capstonee;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class NoteController {
	private PriorityQueue<Note> noteList;
	private LinkedList<Note> visibleNotes;
	private ArrayList<Note> activeHolds;
	private Rectangle2D.Double target;
	private FurInputHandler iH;
	private ScoreCounter sC;
	private LinkedList<Note> removeList;
	private double earliestStart;
	private double latestEnd;
	private int amtOfLanes;
	public int WINDOW_WIDTH = 1000;
	public int WINDOW_HEIGHT = 1000;
	private double lastKnownTime;

	public NoteController(FurInputHandler iH, int amtOfLanes) {
		this.lastKnownTime = 0;
		this.amtOfLanes = amtOfLanes;
		this.iH = iH;
		Note.WIDTH = (WINDOW_WIDTH / amtOfLanes);
		noteList = new PriorityQueue<Note>((n1, n2) ->
				Double.compare(n1.getStartTime(), n2.getStartTime()));
		this.lastKnownTime = 0;
		this.amtOfLanes = amtOfLanes;
		this.iH = iH;
		Note.WIDTH = (WINDOW_WIDTH / amtOfLanes);
		noteList = new PriorityQueue<Note>();
		removeList = new LinkedList<Note>();
		activeHolds = new ArrayList<Note>();
		visibleNotes = new LinkedList<Note>();
	}
	public NoteController(Rectangle2D.Double target, int amtOfLanes) {
		this.lastKnownTime = 0;
		this.amtOfLanes = amtOfLanes;
		Note.WIDTH = (WINDOW_WIDTH / amtOfLanes);
		this.target = target;
		noteList = new PriorityQueue<Note>();
		removeList = new LinkedList<Note>();
		activeHolds = new ArrayList<Note>();
		visibleNotes = new LinkedList<Note>();
	}
	public void attachScoreCounter(ScoreCounter c) {
		this.sC = c;
	}

	public void addNote(Note n) {
		if (noteList.isEmpty()) {
			earliestStart = n.getStartTime();
			latestEnd = n.getEndTime();
		} else {
			earliestStart = Math.min(n.getStartTime(), earliestStart);
			latestEnd = Math.max(n.getEndTime(), latestEnd);
		}
		noteList.add(n);
		updateNoteValue();
	}
	public void addNote(Note n, Rectangle2D.Double t) {
		n.setTarget(t);
		noteList.add(n);
		earliestStart = Math.min(n.getStartTime(), earliestStart);
		latestEnd = Math.max(n.getEndTime(), latestEnd);
		updateNoteValue();
	}
	
	public void addNote(Note n, Rectangle2D.Double t, double releaseTime) {
		earliestStart = Math.min(n.getStartTime(), earliestStart);
		latestEnd = Math.max(n.getReleaseTime(), latestEnd);
		n.setTarget(t,releaseTime);
		noteList.add(n);
		updateNoteValue();
	}
	public void addNote(Note n, double releaseTime) {
		addNote(n, this.target, releaseTime);
	}
	
	public void createTapNote(char lane, double timing) {
		addNote(new TapNote(lane, timing));
	}
	public void createTapNote(int lane, double timing) {
		addNote(new TapNote(lane, timing));
	}
	public void createTapNote(char lane, double timing, Rectangle2D.Double t) {
		addNote(new TapNote(lane, timing), t);
	}
	
	
	public void createHoldNote(char lane, double timing, double releaseTime) {
		addNote(new HoldNote(lane, timing), releaseTime);
	}
	public void createHoldNote(int lane, double timing, double releaseTime) {
		addNote(new HoldNote(lane, timing), releaseTime);
	}
	
	public void setTarget(Rectangle2D.Double t) {
		setTarget(t, false);
	}
	public void setTarget(Rectangle2D.Double t, boolean update) {
		this.target = t;
		if(update) {
			for(Note n:noteList) {
				n.setTarget(this.target);
			}
		}
	}
	public void setWindowSize(int width, int height) {
		WINDOW_WIDTH = width;
		WINDOW_HEIGHT = height;
		Note.WIDTH = WINDOW_WIDTH / amtOfLanes;
	}
	
	public void sort() {
		ArrayList<Note> tempList = new ArrayList<>(noteList);
		tempList.sort(null);
		noteList = new PriorityQueue<>(tempList);
	}



	public boolean computeInput(Note n, double time) {
		// ToDO: compute input for notes
		// compute TapNote 
		if(n instanceof TapNote) {
			int val = n.computeInput(time);
			if(val > 0) {
				sC.addScore(val);
				return true;
			}
		} else if(n instanceof HoldNote) {
			// ignore note if its already being tracked or if it's not within the timing range
			if(n.computeInput(time) > 1 && !n.isTracked()) {
				// add the untracked note to the activeHolds list
				activeHolds.add(n);
				n.setTracked(true);
			}
			if(n.isTracked() && (time - n.getReleaseTime() > 0)) {
				return true; // return true to signify its deletion time
			}
		}
		
		return false;
	}
	
	public void update(double time, double dt) {
		updateVisibleNotes(time);
		lastKnownTime = time;
		// unlock notes
		for(char c: iH.getKeys()) {
			if(!iH.getValue(c) && iH.getLock(c)) {
				iH.unlock(c);
			}
		}
		// Before processing visible notes:
		for(Note n : visibleNotes) {
			if(n == null || n.getRect() == null) {
				System.err.println("Found bad note in visibleNotes!");
				continue;
			}
			// ... rest of update logic ...
		}
		// compute score increases from held notes
		for(Note i:activeHolds) {
			if(i.isLocked()) {
				continue;
			}
			sC.addScoreRaw(0.005,0.005);
		}
		for(Note n:visibleNotes) { //TODO: pull notes from notelist and put into activenotes. pull from activenotes and put into removelist. iterate over activenotes onl
			// skip notes that aren't onscreen
			if(n.getStartTime() > time)
				continue;
			// timeout TapNote
			if(n.getEndTime() + Note.MISS < time && n instanceof TapNote) {
				sC.addScore(1);
				removeList.push(n);
			}
			// timeout HoldNotes that have been released
			if(n instanceof HoldNote && time >= n.getReleaseTime() && n.isLocked()) {
				// break combo
				sC.addScore(1);
				// KILL
				removeList.push(n);
				// remove it from activeholds if possible
				if(n.isTracked()) {
					activeHolds.remove(n);
					n.setTracked(false);
				}
			}
			// move notes that should be on screen
			if(time >= n.getStartTime() && !n.getMoving()) {
				n.setMoving(true);
			}
			// translate the note
			n.autoMove(dt);
			// TAPNOTES: update locks and compute inputs 
			if(n instanceof TapNote) {
				if(iH.getValue(n.getLane()) && !iH.getLock(n.getLane())) {
					boolean removeN = computeInput(n, time);
					if(removeN) {
						removeList.push(n);
					}
					iH.lock(n.getLane());
				}
			}
			// TODO: make this logic more robust and not bad
			// HOLDNOTES: this is way too complicated to summarize im so sorry
			if(n instanceof HoldNote) {
				// lock the key if its pressed and not already locked
				boolean removeN = false;
				if(iH.getValue(n.getLane())) {
					iH.lock(n.getLane());
				}
				if(!n.isLocked()) {
					// determine whether to yeet the note or not
					removeN = computeInput(n, time);
					
					// if the player lets go, lock the note and break combo
					if(!iH.getValue(n.getLane()) && n.isTracked() && n.getEndTime() + Note.MISS < time) {
						n.lock();
						sC.addScore(1);
					}
					// remove notes that should be removed
					if(removeN) {
						sC.round();
						activeHolds.remove(n);
						n.setTracked(false);
						removeList.push(n);
					}
				}
			}
		}

		// remove notes that should be removed
		while(!removeList.isEmpty()) {
			Note n = removeList.pop();
			noteList.remove(n);
			n.destroy();
			n = null;
		}
	}

	public PriorityQueue<Note> getList(){
		return this.noteList;
	}
	
	public double getEarliest() {
		return earliestStart;
	}
	public double getLatest() {
		return latestEnd;
	}
	
	public int getLaneCount() {
		return amtOfLanes;
	}
	public LinkedList<Note> getVisibleNotes() {
		return visibleNotes;
	}

	// In the updateVisibleNotes method:
	private void updateVisibleNotes(double currentTime) {
		visibleNotes.clear();
		for (Note note : noteList) {
			if (note == null) continue;
			// Widen the visibility window significantly (5000ms before and after)
			if (note.getStartTime() - 5000 <= currentTime &&
					note.getEndTime() + 5000 >= currentTime) {
				visibleNotes.add(note);
			}
		}
	}
	
	public String toString() {
		StringBuilder out = new StringBuilder();
		for(Note n:noteList) {
			out.append(n + "\n");
		}
		return out.toString();
	}
	public void destroy() {
		while(!noteList.isEmpty()) {
			Note n = noteList.poll();
			if(n != null) {
				n.destroy();
			}
		}
		while(!removeList.isEmpty()) {
			Note n = removeList.poll();
			if(n != null) {
				n.destroy();
			}
		}
		noteList = null;
		removeList = null;
		target = null;
		iH = null;
		sC = null;
	}

	private void updateNoteValue() {
		for(Note i:noteList) {
			i.HITVALUE = 1000000/noteList.size();
		}
	}

	// In NoteController.java
	public void setLatest(double songDuration) {
		// Convert song duration from seconds to milliseconds if needed
		this.latestEnd = songDuration * 1000; // Assuming your timing is in milliseconds

		// Alternatively, if your timing is already in seconds:
		// this.latestEnd = songDuration;

		System.out.println("Adjusted latest note time to: " + this.latestEnd);
	}
}
