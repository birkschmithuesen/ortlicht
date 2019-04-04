
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import processing.core.PApplet;

/**
 *
 * @author birk
 */
public class VideoRecorder {

    PApplet parent;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    DataOutputStream dos = null;
    RemoteControlledIntParameter remoteRecorderStart;
    RemoteControlledIntParameter remoteNNRecorderStart;
    RemoteControlledIntParameter remoteFrameRecorderCount;
    RemoteControlledIntParameter remoteNNFrameRecorderCount;

    public VideoRecorder(String filename) {
        remoteRecorderStart = new RemoteControlledIntParameter("/Playback/Recorder/record", 0, 0, 1);
        remoteFrameRecorderCount = new RemoteControlledIntParameter("/Playback/Recorder/frameCount", 0, 0, 18000);
      
        try {
            // create file output stream
            fos = new FileOutputStream(filename);
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

    public void run(LedColor[] ledColors, int frameCount) {

        if (remoteRecorderStart.getValue() > 0) {
            //just save a frame, when the frame counter changed
            writeFrameToStream(ledColors, frameCount);

        } //when the record from Ableton is finished
        else if (remoteRecorderStart.getChangedSinceReset()) {
            saveFileToDisk();
            remoteRecorderStart.resetChanged();
        } 

    }

    public void writeFrameToStream(LedColor[] ledColors, int frameCount) {
        byte[] theFrame = buildFrame(ledColors);
        try {
            dos.writeInt(frameCount);
            for (byte b : theFrame) {
                dos.writeByte(b);
            };
            //dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveFileToDisk() {
        try {
            dos.flush();
            bos.flush();
            if (fos != null) {
                fos.close();
            }
            System.out.println("Video saved to Disk!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /* --> CAN BE DELETED
    //built the Frame with byte values
    public byte[] buildFrameSW(LedColor[] ledColors) {
        byte[] theFrame = new byte[ledColors.length];
        for (int i = 0; i < ledColors.length; i++) {
            theFrame[i] = (byte) ((parent.constrain(ledColors[i].x * 255, 0, 255)) - 128);
        }
        return theFrame;
    }
     */
    //built the Frame with byte values
    public byte[] buildFrame(LedColor[] ledColors) {
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
