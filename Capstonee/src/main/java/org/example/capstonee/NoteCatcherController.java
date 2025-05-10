package org.example.capstonee;

import java.awt.geom.Rectangle2D;

public class NoteCatcherController {
	private FurInputHandler inputhandler;
	private NoteCatcher[] catchers;
	public int WINDOW_WIDTH = 1000;
	public int WINDOW_HEIGHT = 1000;
	private Rectangle2D.Double BINDING_BOX;
	
	public NoteCatcherController(FurInputHandler inputhandler) {
		BINDING_BOX = new Rectangle2D.Double(0, 600, 1000, 10);
		this.inputhandler = inputhandler;
		catchers = new NoteCatcher[inputhandler.getSize()];
		createCatchers();
	}
	private void createCatchers() {
		char[] tempChars = inputhandler.getKeys();
		
		for(int i = 0; i < tempChars.length; i++) {
			catchers[i] = new NoteCatcher(
					new Rectangle2D.Double(
							i*((double)WINDOW_WIDTH/(double)catchers.length),
							BINDING_BOX.getMinY() - BINDING_BOX.getHeight()/2,
							((double)WINDOW_WIDTH/(double)catchers.length),
							25)
					, tempChars[i]);
		}
	}
	public void update() {
		for(NoteCatcher i:catchers) {
			if(inputhandler.getValue(i.getLane())) {
				i.press();
			}
			else {
				i.release();
			}
		}
	}



	public Rectangle2D.Double[] getList(){
		Rectangle2D.Double[] temp = new Rectangle2D.Double[catchers.length];
		for(int i = 0; i < catchers.length; i++) {
			temp[i] = catchers[i].getRect();
		}
		return temp;
	}
	public Rectangle2D.Double[] getFill(){
		int c = 0;
		for(NoteCatcher i:catchers) {
			if(i.isPressed()) {
				c++;
			}
		}
		Rectangle2D.Double[] temp = new Rectangle2D.Double[c];
		int index = 0;
		for(int i = 0; i < catchers.length; i++) {
			if(catchers[i].isPressed()) {
				temp[index] = catchers[i].getRect();
				index++;
			}
		}
		return temp;
	}
	public Rectangle2D.Double[] getDraw(){
		int c = 0;
		for(NoteCatcher i:catchers) {
			if(!i.isPressed()) {
				c++;
			}
		}
		Rectangle2D.Double[] temp = new Rectangle2D.Double[c];
		int index = 0;
		for(int i = 0; i < catchers.length; i++) {
			if(!catchers[i].isPressed()) {
				temp[index] = catchers[i].getRect();
				index++;
			}
		}
		return temp;
	}
	public Rectangle2D.Double getBindingBox(){
		return BINDING_BOX;
	}
	
	public void setWindowSize(int width, int height) {
		WINDOW_WIDTH = width;
		WINDOW_HEIGHT = height;
		BINDING_BOX.setRect(0, 3*height/4, width, 10);
		createCatchers();
	}
	
	public void destroy() {
		inputhandler = null;
		for(int i = 0; i < catchers.length; i++) {
			catchers[i].destroy();
			catchers[i] = null;
		}
		BINDING_BOX = null;
	}
}
