package org.example.capstonee;

import java.awt.geom.Rectangle2D;

public class NoteCatcher {
	private Rectangle2D.Double R;
	private char LETTER;
	private boolean pressed;
	public NoteCatcher(Rectangle2D.Double r, char l) {
		this.R = r;
		this.LETTER = l;
		this.pressed = false;
	}
	public void press() {
		pressed = true;
	}
	public void release() {
		pressed = false;
	}
	public boolean isPressed() {
		return pressed;
	}
	public char getLane() {
		return LETTER;
	}
	public Rectangle2D.Double getRect(){
		return R;
	}
	
	public void destroy() {
		R = null;
	}
	
}
