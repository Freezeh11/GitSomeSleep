package org.example.capstonee;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.LinkedList;

public class OsuParser{
	private String path;
	private String diff;
	private int numObjects;
	public OsuParser() {
		this("");
	}
	public OsuParser(String path) {
		this(path, "");
	}
	public OsuParser(String path, String diff) {
		this.path = path;
		this.diff = diff;
		numObjects = 0;
	}
	
	public void setDirectory(String path) {
		this.path = path;
	}
	
	public void setDifficulty(String diff) {
		this.diff = diff;
	}
	
	void parse(NoteController noteList) {
		parse(path + diff, noteList);
	}

	// In OsuParser.java, modify the parse method:
	void parse(String filename, NoteController noteList) {
		File file = new File(filename);
		System.out.println("Trying to load: " + file.getAbsolutePath());
		if (!file.exists()) {
			System.out.println("File not found: " + file.getAbsolutePath());
			return;
		}

		try (Scanner scan = new Scanner(file, "UTF-8")) {
			String read;
			while (scan.hasNextLine()) {
				read = scan.nextLine().trim();
				System.out.println("Line: " + read);
				if (read.equalsIgnoreCase("[HitObjects]")) {
					System.out.println("Found hitobjects section!");
					break;
				}
			}

			while (scan.hasNextLine()) {
				read = scan.nextLine().trim();
				if (read.isEmpty()) continue;

				String[] stringData = read.split(",");
				if (stringData.length < 6) continue;

				try {
					int type = Integer.parseInt(stringData[3]);
					double timing = Double.parseDouble(stringData[2]);
					int lane = (int)(Double.parseDouble(stringData[0]) / (512.0 / noteList.getLaneCount()));
					lane = Math.max(0, Math.min(lane, noteList.getLaneCount() - 1)); // Clamp lane value

					System.out.printf("Parsed note - Lane: %d, Time: %.2f, Type: %d%n", lane, timing, type);

					// Type 1 = hit circle, Type 5 = slider (treat as hold)
					if (type == 1) { // Hit circle
						noteList.createTapNote(lane, timing);
						numObjects++;
					} else if (type == 5) { // Slider (treated as hold)
						double holdDuration = Double.parseDouble(stringData[5].split(":")[0]) / 1000.0;
						noteList.createHoldNote(lane, timing, timing + holdDuration);
						numObjects++;
					}
				} catch (NumberFormatException e) {
					System.err.println("Error parsing line: " + read);
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			System.err.println("Error reading file: " + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("Total notes parsed: " + numObjects);
	}
	String getPathToAudio() {
		return getPathToAudio(this.path, this.diff);
	}
	
	String getPathToAudio(String folder, String map) {
		Scanner s = null;
		try {
			s = new Scanner(new File(folder + map), "UTF-8");
		} catch(FileNotFoundException e) {
			System.out.println("File not found.");
		}
		String r = s.nextLine().toLowerCase();
		while(s.hasNextLine() && !r.equals("[general]")) {
			r = s.nextLine().toLowerCase();
		}
		while(s.hasNextLine() && !r.split(":")[0].equals("audiofilename")) {
			r = s.nextLine().toLowerCase();
		}
		String out = r.split(":")[1];
		if(out.substring(0,1).equals(" ")) {
			out = out.substring(1);
		}
		return folder + out;
	}
	String[] getDifficulties() {
		return getDifficulties(this.path);
	}
	String[] getDifficulties(String p) {
		LinkedList<String> s = new LinkedList<>();
		File[] dir = new File(p).listFiles();
		for(File i:dir) {
			if(i.isFile() && i.getName().endsWith(".osu")) {
				s.add(i.getName());
			}
		}
		String[] output = new String[s.size()];
		for(int i = 0; i < output.length; i++) {
			output[i] = s.remove();
		}
		return output;
	}
}
