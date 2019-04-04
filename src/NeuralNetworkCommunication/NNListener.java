
import oscP5.OscMessage;
import processing.core.PApplet;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

/**
 *
 * @author birk
 */
public class NNListener extends Thread {

    RemoteControlledFloatParameter remoteNNReceive;
    RemoteControlledIntParameter remoteNNPlay;
    int oscFramePart = 0;
    LedColor[] ledColors;
    boolean isPlaying = false;
    int nLeds;
    DatagramSocket serverSocket;
    byte[] receiveData = new byte[1500];

    public NNListener(int nLeds) {
        try {
            this.serverSocket = new DatagramSocket(10005);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.nLeds = nLeds;
        remoteNNReceive = new RemoteControlledFloatParameter("/NN/leds", 0f, 0f, 1f);
        remoteNNPlay = new RemoteControlledIntParameter("/NN/play", 0, 0, 1);
        ledColors = LedColor.createColorArray(nLeds);
    }

    public void run() {
        int frame;
        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                byte[] pktdata = receivePacket.getData();
                ByteBuffer pktb = ByteBuffer.wrap(pktdata);
                frame = pktb.getInt(0);                
                receiveFramePart(pktdata, receivePacket.getLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //check if any data is received from the NN
    public boolean getReceiving() {
        if (remoteNNPlay.getValue()>0) {
            return true;
        } else {
            return false;
        }
    }

    //check if the transfer of the frame is finished
    public boolean getFrameAvailble() {
        if (oscFramePart > 8) {
            return true;
        } else {
            return false;
        }
    }

    //put the received leds in the LedColor Buffer
    public void receiveFramePart(byte[] udpPacket, int len) {
        //isPlaying = true;
        int startLed = udpPacket[4] * 1402;
        oscFramePart = udpPacket[4];
        for (int i = 0; i < len - 6; i++) {
            int numLed = i + startLed;
            if (numLed < nLeds) {
                //float theValue=PApplet.map(theOscMessage.get(i).intValue(), 0, 256, 0, 1);
                float thevalue = PApplet.map(udpPacket[i+6], 0, 255, 0f, 1f);
                ledColors[i + startLed].x = thevalue;
                ledColors[i + startLed].y = thevalue;
                ledColors[i + startLed].z = thevalue;
            }
        }
        
    }

    public LedColor[] getFrame() {
        return ledColors;
    }
}
