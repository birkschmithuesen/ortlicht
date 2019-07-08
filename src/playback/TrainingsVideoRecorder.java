
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

//this class records trainingsframes in a binary stream. At the end it will be converted and saved as delimited txt file
/**
 *
 * @author birk
 */
public class TrainingsVideoRecorder {

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

    public TrainingsVideoRecorder(String filepath) {
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

    public void run(LedColor[] ledColors, OscMessage fft) {
        //save the frame, when new a new fft mesage arrived
        //System.out.println("receive fft"); 
        if (remoteRecorderStart.getValue() > 0) {
            nLeds=ledColors.length; //this should just be done one time!
            writeFrameToStream(ledColors, fft);
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
                //write the led data to the txt file.The Data in the stream is RGB, we just take the R 
                for (int i=0; i<nLeds;i++){
                    ///float ledValue=(float)i; 
                    float ledValue=parent.map((int)dis.readByte(),-127,127,0f,1f);
                    writer.print(ledValue+"\t");
                    //skip the G and B value from the stream
                    dis.readByte();
                    dis.readByte();
                }
                //frames are seperated in new lines
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

    private void writeFrameToStream(LedColor[] ledColors, OscMessage fftAnalyse) {
        byte[] theFrame = buildFrame(ledColors);
        try {
            for (int i = 0; i < numBins; i++) {
                if (fftAnalyse.getTypetagAsBytes()[i] == 'f') {
                    dos.writeFloat(fftAnalyse.get(i).floatValue());
                }
            }
            for (byte b : theFrame) {
                dos.writeByte(b);
            };
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //built the Frame with byte values
    private byte[] buildFrame(LedColor[] ledColors) {
        byte[] theFrame = new byte[ledColors.length * 3];
        for (int i = 0; i < ledColors.length; i++) {
            int byteIndex = i * 3;
            theFrame[byteIndex] = (byte) ((parent.constrain(ledColors[i].x * 255, 0, 255)) - 128);
            theFrame[byteIndex + 1] = (byte) ((parent.constrain(ledColors[i].y * 255, 0, 255)) - 128);
            theFrame[byteIndex + 2] = (byte) ((parent.constrain(ledColors[i].z * 255, 0, 255)) - 128);
        }
        return theFrame;
    }

}
