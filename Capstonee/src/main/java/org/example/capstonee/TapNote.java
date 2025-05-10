package org.example.capstonee;
import org.example.capstonee.Note;

import java.awt.geom.Rectangle2D;
public class TapNote extends Note {

	public TapNote(char l, double time) {
		MOVING = false;
		x = laneCharToInt(l)*WIDTH + WIDTH/2;
		y = -WIDTH * 2; // Start above screen
		LANE = l;
		endTime = time;
		startTime = endTime - (super.SPEED*super.SCONST);
		rect = new Rectangle2D.Double(x - WIDTH/2, y - WIDTH/2, WIDTH, WIDTH);
	}
	public TapNote(int l, double time) {
		MOVING = false;
		x = l*WIDTH;
		y = -50;
		LANE = laneIntToChar(l);
		endTime = time;
		startTime = endTime - (super.SPEED*super.SCONST);
		rect = new Rectangle2D.Double(this.x, this.y, WIDTH, HEIGHT);
	}
	public TapNote(double x, double y, char l, double time) {
		MOVING = false;
		this.x = x;
		this.y = y;
		LANE = l;
		endTime = time;
		startTime = endTime - (super.SPEED*super.SCONST);
		rect = new Rectangle2D.Double(this.x, this.y, WIDTH, HEIGHT);
	}

	public void setTarget(Rectangle2D.Double s, double releaseTime) {
		setTarget(s);
	}

	@Override
	public String toString() {
		return "{" + LANE + "," + endTime + "," + MOVING + "}";
	}

	// Dummy methods to make the abstract class happy

	@Override
	public double getReleaseTime() {
		return getEndTime();
	}
	@Override
	protected void lock() {}
	@Override
	protected void unlock() {}
	@Override
	protected boolean isLocked() {
		return false;
	}
	@Override
	protected void setTracked(boolean b) {}
	@Override
	protected boolean isTracked() {
		return false;
	}

}
