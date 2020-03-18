
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import oscP5.OscMessage;
import processing.core.PApplet;
import processing.core.PVector;

//this class records trainingsframes in a stream. At the end it will be converted and saved as delimited txt file
//the data is stored in the format:
//[[FFT, XYZ[1], RGB[1], ..., XYZ[nLeds],RGB[nLeds]],
//[[FFT, XYZ[1], RGB[1], ..., XYZ[nLeds],RGB[nLeds]],

/**
 *
 * @author birk
 */
public class TrainingsVideoRecorderPosBased {

    PApplet parent;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    DataOutputStream dos = null;
    RemoteControlledIntParameter remoteRecorderStart;
    RemoteControlledFloatParameter inputFFT;
    String filepath;
    String filename_temp = "temp.vid";
    int counter=0;
    int nLeds=0;
    int numBins =128;

    public TrainingsVideoRecorderPosBased(String filepath) {
        this.filepath = filepath;
        remoteRecorderStart = new RemoteControlledIntParameter("/Training/record", 0, 0, 1);
        inputFFT = new RemoteControlledFloatParameter("/fft", 0, 0, 256);
        try {
            // create file output stream
            fos = new FileOutputStream(filepath + filename_temp);
            //set the buffer size to 500MB
            int bufferSize = 16 * 1024;
            bos = new BufferedOutputStream(fos, bufferSize);
            // create data output stream
            dos = new DataOutputStream(bos);
        } catch (Exception e) {

            // if any I/O error occurs
            e.printStackTrace();
        }
    }

    public void run(LedBoundingBox boundingBox, PVector[] ledPositions, LedColor[] ledColors, OscMessage fft) {
        //save the frame, when new a new fft mesage arrived
        //System.out.println("receive fft"); 
        if (remoteRecorderStart.getValue() > 0) {
            parent.println("start recording");
            nLeds=ledColors.length; //this should just be done one time!
            // create new Array form: Brightness XYZ FFT
            writeFrameToStream(boundingBox, ledPositions, ledColors, fft);
            System.out.println("recording datapoint"+counter); 
            counter++;
        } //when the record from Ableton is finished
        else if (remoteRecorderStart.getChangedSinceReset()) {
            saveTrainingsData_sw();
            remoteRecorderStart.resetChanged();
        }
    }

    private void saveTrainingsData_sw() {
        try {
            //Close the binaryTemp Buffer
            dos.flush();
            bos.flush();
            if (fos != null) {
                fos.close();
            }
            System.out.println("file temp.vid closed");
            System.out.println("start to convert to csv file...");

            // Open the temp file
            InputStream fis = new FileInputStream(filepath + filename_temp);
            int bufferSize = 16 * 1024;
            BufferedInputStream bis = new BufferedInputStream(fis, bufferSize);
            DataInputStream dis = new DataInputStream(bis);

            //create the txt file
            File file = new File(filepath + "traingsdata.txt");
            PrintWriter writer = new PrintWriter(file);
            while (dis.available() > 0) {
                //write the fft data to the txt file
                for (int i = 0; i < numBins; i++) {
                    writer.print(dis.readFloat()+"\t");
                }
                for (int n = 0; n < nLeds; n++){
                    // write the XYZ position data of one LED to the text file.
                    for (int i = 0; i < 3; i++){ //read three floats for xyz
                        writer.print(dis.readFloat()+"\t");
                    }
                    //write the brightness of one one LED to the txt file. The Data in the stream is RGB, we just take the R 
                    writer.print(dis.readFloat()+"\t");
                    //skip the G and B value from the stream
                    dis.readFloat();
                    dis.readFloat();
                }
                
                
                //leds are seperated in new lines
                writer.println();
            }
            writer.close();
            System.out.println("TXT File saved to disk!");
             try {
            // create file output stream
            fos = new FileOutputStream(filepath + filename_temp);
            //set the buffer size to 500MB
            bufferSize = 16 * 1024;
            bos = new BufferedOutputStream(fos, bufferSize);
            // create data output stream
            dos = new DataOutputStream(bos);
            counter=0;
        } catch (Exception e) {

            // if any I/O error occurs
            e.printStackTrace();
        }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeFrameToStream(LedBoundingBox boundingBox, PVector[] ledPositions, LedColor[] ledColors, OscMessage fftAnalyse) {
        float[] theFrame = buildFrame(ledColors);
        float[] thePositions = buildPositions(ledPositions);
        try {
            for (int j = 0; j < numBins; j++) {
                if (fftAnalyse.getTypetagAsBytes()[j] == 'f') {
                    dos.writeFloat(fftAnalyse.get(j).floatValue());
                }
            }
            for (int i = 0; i < ledColors.length; i++) {
                for (int j = 0; j < 3; j++){
                    dos.writeFloat(thePositions[i+j]); //xyz LEDPositions 
                }
                for (int j = 0; j < 3; j++){
                    dos.writeFloat(theFrame[i+j]); //RGB values
                }
            }


            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //built the Position data with byte values
    private float[] buildPositions(PVector[] ledPositions) {
        float[] thePositions = new float[ledPositions.length * 3];
        for (int i = 0; i < ledPositions.length; i++) {
            int index = i * 3;
            thePositions[index] = ledPositions[i].x;
            thePositions[index + 1] = ledPositions[i].y;
            thePositions[index + 2] = ledPositions[i].z;
        }
        return thePositions;
    }

    
    //built the Frame with byte values
    private float[] buildFrame(LedColor[] ledColors) {
        float[] theFrame = new float[ledColors.length * 3];
        for (int i = 0; i < ledColors.length; i++) {
            int index = i * 3;
            theFrame[index] = ledColors[i].x;
            theFrame[index + 1] = ledColors[i].y;
            theFrame[index + 2] = ledColors[i].z;
        }
        return theFrame;
    }
}
