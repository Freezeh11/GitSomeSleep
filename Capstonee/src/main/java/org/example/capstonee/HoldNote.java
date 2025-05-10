package org.example.capstonee;

import java.awt.geom.Rectangle2D;

public class HoldNote extends Note{
	
	private double releaseTime;
	private double localHeight;
	private boolean locked, tracked;
	
	public HoldNote(char l, double time){
		MOVING = false;
		locked = false;
		tracked = false;
		x = laneCharToInt(l)*WIDTH;
		y = -50;
		LANE = l;
		endTime = time;
		startTime = endTime - (super.SPEED*super.SCONST);
		rect = new Rectangle2D.Double(this.x, this.y, super.WIDTH, super.HEIGHT);
	}
	
	public HoldNote(int l, double time) {
		MOVING = false;
		locked = false;
		tracked = false;
		x = l*WIDTH;
		y = -50;
		LANE = laneIntToChar(l);
		endTime = time;
		startTime = endTime - (super.SPEED*super.SCONST);
		rect = new Rectangle2D.Double(this.x, this.y, super.WIDTH, super.HEIGHT);
	}
	
	@Override
	public void setTarget(Rectangle2D.Double s, double releaseTime) {
		dist = Math.abs(s.getCenterY() - getCenterY());
		spd = dist/(endTime - startTime);
		
		this.releaseTime = releaseTime;
		localHeight = spd*(releaseTime - getEndTime());
		rect.setRect(rect.getX(), rect.getY() - this.localHeight, rect.getWidth(), this.localHeight+super.HEIGHT);
		this.y = rect.getY();
		this.x = rect.getX();
	}
	
	public Rectangle2D.Double getRect() {
		return rect;
	}

	public double getReleaseTime() {
		return this.releaseTime;
	}
	
	public boolean isLocked() {
		return locked;
	}
	public void lock() {
		locked = true;
	}
	public void unlock() {
		locked = false;
	}
	public void setTracked(boolean b) {
		tracked = b;
	}
	public boolean isTracked() {
		return tracked;
	}

}
