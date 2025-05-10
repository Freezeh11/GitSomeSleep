package org.example.capstonee;
public class ScoreCounter {
	private double totalScore;
	private double combo;

	public ScoreCounter() {
		this(0, 0);
	}
	public ScoreCounter(double score, double combo) {
		totalScore = score;
		this.combo = combo;
	}
	
	public void addScore(int i) {
		switch(i) {
			case 1:
				totalScore += 0;
				combo = 0;
				break;
			case 2:
				totalScore += 100;
				combo++;
				break;
			case 3:
				totalScore += 200;
				combo++;
				break;
			default:
				break;
		}
	}
	public void addScoreRaw(double i, double c) {
		totalScore += i;
		combo += c;
	}
	
	public double getScore() {
		return (int)totalScore;
	}
	public int getCombo() {
		return (int)combo;
	}
	
	public void round() {
		totalScore = Math.round(totalScore);
		combo = Math.round(combo);
	}
}
