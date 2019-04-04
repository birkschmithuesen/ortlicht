//Records every single frame as single txt file. This method is not fast enaugh, to capture 30fps
 
import java.io.*;
//import java.io.DataOutputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;
import processing.core.PApplet;
import oscP5.*;

/**
 *
 * @author birk
 */
public class TrainingsRecorder {

    PApplet papplet;
    private int id = 0;

    TrainingsRecorder() {
        papplet = new PApplet();
    }

    public void saveTrainingsSet(OscMessage fftAnalyse, LedColor[] ledColors) {
        //String outData;
        try {
            File file = new File("data/training/input_" + id + ".txt");
            PrintWriter writer = new PrintWriter(file);

            for (int i = 0; i < 20; i++) {
                if (fftAnalyse.getTypetagAsBytes()[i] == 'f') {
                    float theValue = fftAnalyse.get(i).floatValue();
                    //outData = Float.toString(theValue) + "\t";
                    //saveToFile(outStream, outData);

                    if (i < 19) {
                        writer.print(theValue + "\t"); //numpy has a problem with a tab-stop at the end
                    } else {
                        writer.print(theValue);
                    }
                }
            }
            writer.close();

            file = new File("data/training/output_" + id + ".txt");
            writer = new PrintWriter(file);

            for (int i = 0; i < ledColors.length; i++) {
                ledColors[i].clamp();
                writer.println(ledColors[i].x + "\t" + ledColors[i].y + "\t" + ledColors[i].z);

            }
            writer.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not open file" + e);
        }
        //System.out.println(id);
        id++;

    }

    public void saveTrainingsSet_sw(OscMessage fftAnalyse, LedColor[] ledColors) {
        //String outData;
        try {
            File file = new File("data/training/input_sw_" + id + ".txt");
            PrintWriter writer = new PrintWriter(file);

            for (int i = 0; i < 20; i++) {
                if (fftAnalyse.getTypetagAsBytes()[i] == 'f') {
                    float theValue = fftAnalyse.get(i).floatValue();
                    //outData = Float.toString(theValue) + "\t";
                    //saveToFile(outStream, outData);

                    if (i < 19) {
                        writer.print(theValue + "\t"); //numpy has a problem with a tab-stop at the end
                    } else {
                        writer.print(theValue);
                    }
                }
            }
            writer.close();

            file = new File("data/training/output_sw_" + id + ".txt");
            writer = new PrintWriter(file);

            for (int i = 0; i < ledColors.length; i++) {
                ledColors[i].clamp();
                writer.println(ledColors[i].x);

            }
            writer.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not open file" + e);
        }
        //System.out.println(id);
        id++;

    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

}
