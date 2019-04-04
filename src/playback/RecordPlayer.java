

import java.io.*;
import java.util.*;
import processing.core.PApplet;

/**
 *
 * @author birk
 */
public class RecordPlayer {

    LedColor[] ledColors;
    int frame;

    public RecordPlayer(int ledColorsLength) {
        ledColors = LedColor.createColorArray(ledColorsLength);
        frame = -1;
    }

     public void loadNextFrame_sw(String filename) {
        frame++;
        readFromFile_sw(filename + frame + ".txt");
        PApplet.println("load File: " + filename + frame);
    }

    public void loadLastFrame_sw(String filename) {
        if (frame > 0) {
            frame--;
        }
        readFromFile_sw(filename + frame + ".txt");
        PApplet.println("load File: " + filename + frame);
    }
    
    public void loadNextFrame(String filename) {
        frame++;
        readFromFile(filename + frame + ".txt");
        PApplet.println("load File: " + filename + frame);
    }

    public void loadLastFrame(String filename) {
        if (frame > 0) {
            frame--;
        }
        readFromFile(filename + frame + ".txt");
        PApplet.println("load File: " + filename + frame);
    }
    
    public void playVideo_sw(String filename, int numFrame){
        if(frame>=numFrame-1)frame=0;
        loadNextFrame_sw(filename);
    }

    public LedColor[] get() {
        return ledColors;
    }

    private void readFromFile(String filename) {
        try {
            File file = new File(filename);
            Scanner s = new Scanner(file);
            for (int i = 0; i < ledColors.length; i++) {
                String line = s.nextLine();
                String[] pieces = line.split("\t");
                ledColors[i].x = Float.parseFloat(pieces[0]);
                ledColors[i].y = Float.parseFloat(pieces[1]);
                ledColors[i].z = Float.parseFloat(pieces[2]);
            }
            PApplet.println(ledColors[0].x + "\t" + ledColors[0].y + "\t" + ledColors[0].z + "\t");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void readFromFile_sw(String filename) {
        try {
            File file = new File(filename);
            Scanner s = new Scanner(file);
            for (int i = 0; i < ledColors.length; i++) {
                String line = s.nextLine();
                String[] pieces = line.split("\t");
                ledColors[i].x = Float.parseFloat(pieces[0]);
                ledColors[i].y = Float.parseFloat(pieces[0]);
                ledColors[i].z = Float.parseFloat(pieces[0]);
            }
            PApplet.println(ledColors[0].x + "\t" + ledColors[0].y + "\t" + ledColors[0].z + "\t");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
