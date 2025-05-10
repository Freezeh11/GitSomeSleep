package org.example.capstonee;

import java.awt.geom.Rectangle2D;

import static org.example.capstonee.Main.catcher;


public abstract class Note implements Comparable<Note>{

	// Update these constants at the top of Note.java
	public static double WIDTH = 60;  // Reduced from 80
	public static double HEIGHT = 60; // Reduced from 80
	public static final double BASE_SPEED = 0.5;
	public static int HITVALUE = 100;
	public final double SPEED = 1.2;
	public final double SCONST = 1000;
	public static final double GREAT = 60;
	public static final double GOOD = 120;
	public static final double MISS = 130;

	// stuff for lower classes
	protected double x,y;
	protected double targetY;
	protected double velocity;
	protected char LANE;
	protected boolean MOVING;
	protected double endTime;
	protected double startTime;
	protected double dist;
	protected double spd;
	protected Rectangle2D.Double rect;

	// move functions
	public void autoMove(double currentTime) {
		if (MOVING) {
			// Calculate progress (0 at startTime, 1 at endTime)
			double progress = (currentTime - startTime) / (endTime - startTime);
			progress = Math.max(0, Math.min(1, progress));

			// Calculate Y position (from above screen to judgment line)
			double startY = -HEIGHT * 2; // Start further above screen
			double targetY = catcher.getBindingBox().getCenterY();
			y = startY + (targetY - startY) * progress;

			// Update circle position (centered)
			rect.setRect(x - WIDTH/2, y - WIDTH/2, WIDTH, WIDTH);
		}
	}

	public void move(double x, double y) {
		this.x += x;
		this.y += y;
		rect.setRect(this.x, this.y, rect.getWidth(), rect.getHeight());
	}
	public void moveTo(double x, double y) {
		this.x = x;
		this.y = y;
		rect.setRect(this.x, this.y, rect.getWidth(), rect.getHeight());
	}


	// getters
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public double getCenterX() {
		return rect.getCenterX();
	}
	public double getCenterY() {
		return rect.getCenterY();
	}
	public char getLane() {
		return LANE;
	}
	public int getLaneInt() {
		return laneCharToInt(getLane());
	}

	public boolean getMoving() {
		return MOVING;
	}
	public double getStartTime() {
		return startTime;
	}
	public double getEndTime() {
		return endTime;
	}
	public abstract double getReleaseTime();
	public Rectangle2D.Double getRect(){
		return rect;
	}

	// setters
	public void setMoving(boolean m) {
		MOVING = m;
	}
	public void setTarget(Rectangle2D.Double s) {
		this.targetY = s.getCenterY();
		double distance = targetY - y;
		double timeAvailable = (endTime - startTime) / 1000.0; // Convert to seconds
		this.velocity = distance / timeAvailable;
		System.out.printf("Set target - StartY: %.1f TargetY: %.1f Time: %.2fs Velocity: %.1f px/s%n",
				y, targetY, timeAvailable, velocity);
	}
	public abstract void setTarget(Rectangle2D.Double s, double releaseTime);
	// char to lane
	protected int laneCharToInt(char lane) {
		switch(lane) {
			case 'd':
				return 0;
			case 'f':
				return 1;
			case 'j':
				return 2;
			case 'k':
				return 3;
			case ' ':
				return 4;
			case 's':
				return 5;
			case 'l':
				return 6;
		}
		return -1;
	}

	protected char laneIntToChar(int lane){
		switch(lane) {
			case 0:
				return 'd';
			case 1:
				return 'f';
			case 2:
				return 'j';
			case 3:
				return 'k';
			case 4:
				return ' ';
			case 5:
				return 's';
			case 6:
				return 'l';
		}
		return '|';
	}

	public int computeInput(double currentTiming) {
		/*
		 * 0 = not within timing
		 * 1 = miss
		 * 2 = good
		 * 3 = perfect
		 */
		double distance = Math.abs(getEndTime() - currentTiming);
		if(distance < GREAT) {
			return 3;
		}
		if(distance < GOOD) {
			return 2;
		}
		/*if(distance < 90) {
			return 1;
		}*/
		return 0;
	}

	public String toString() {
		return "";
	}

	public int compareTo(Note n) {
		double diff = getEndTime() - n.getStartTime();
		if(diff == 0) {
			return 0;
		}
		return diff > 0 ? 1: -1;
	}

	public void destroy() {
		moveTo(-100,-100);  // Move offscreen instead
		// Remove: rect = null;  // Keep the rectangle alive
		MOVING = false;
	}
	protected abstract void lock();
	protected abstract void unlock();
	protected abstract boolean isLocked();
	protected abstract void setTracked(boolean b);
	protected abstract boolean isTracked();

}
