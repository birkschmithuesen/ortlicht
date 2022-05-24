
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscProperties;
import netP5.*;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.KeyEvent;

import java.awt.event.*;

public class Ortlicht extends PApplet {

    public static void main(String[] args) {
        PApplet.main("Ortlicht");
    }

    // layout of Stripes
    int numStripes = 24;
    int numLedsPerStripe = 576;
    int numStripesPerController = 8;
    StripeConfigurator stripeConfiguration = new StripeConfigurator(numStripes, numLedsPerStripe,
            numStripesPerController); // used to generate per led info.

    // ip -configuration of Art-Net-Interface
    String ipPrefix = "2.0.0."; // first three numbers of IP adreess of
    // controlles
    int startIP = 10; // last number of first controller IP
    ArtNetSender artNetSender = new ArtNetSender(stripeConfiguration, ipPrefix, startIP); // used

    LedVisualizer visualizer = new LedVisualizer(this);

    // all data about our leds
    public static PVector[] ledPositions, ledPositionsNormalized;
    public static PVector[] ledNormals;
    public static LedColor[] ledColors;
    LedInStripeInfo[] stripeInfos;
    Mixer mixer;
    VideoRecorder videoRecorder;
    VideoPlayer videoPlayer;
    public static NNListener nnListener;
    TrainingsVideoRecorder trainingsVideoRecorder;
    //TrainingsVideoRecorderPosBased trainingsVideoRecorderPosBased;

    // Information about the sculpture dimensions
    public static float sculptureRadius = 0.77f; // <<<<----------------------this must be calculated automatically

    public static LedBoundingBox boundingBox;

    // BPM Master
    //public static BpmClock bpmClock = new BpmClock();
    // OSC Remote Control
    public static OscP5 oscP5;
    public static NetAddress soundGeneratorLocation;

    // One PApplet object to hand it over to every class that needs it.
    PApplet papplet;

    TrainingsRecorder trainingsRecorder;
    boolean record = false;
    RecordPlayer recordPlayer;
    String playerFileName, videoFileName;
    boolean playRecorded = true;
    boolean nn_sw = false; //if the network works black and white
    boolean playVideo = false;
    boolean writeStream = true;
    int oscFramePart = 0;
    

    public void settings() {
        size(512, 512, P3D);
        
    }

    public void setup() {
        frameRate(44);
        delay(500);
        OscProperties op = new OscProperties();
        //  op.setDatagramSize(9220);
        op.setListeningPort(8001);
        oscP5 = new OscP5(this, op);  //given variable defines the port
        //soundGeneratorLocation = new NetAddress("127.0.0.1",8000);
        ledPositions = LedPositionFile.readFromFile(dataPath("ledPositions.txt")); // read positions from file
        ledNormals = LedPositionFile.readFromFile(dataPath("regressionNormals.txt")); // read positions from file
        ledColors = LedColor.createColorArray(ledPositions.length);        // build a color buffer with the length of the position file
        stripeInfos = stripeConfiguration.builtStripeInfo();                      // create stripe date for each LED (used only for specific visualizations
        mixer = new Mixer();
        //videoRecorder = new VideoRecorder("videos/video_rec.vid");
        //videoPlayer = new VideoPlayer("videos/scene_", ledColors.length);
        nnListener = new NNListener(ledColors.length);
        nnListener.start();
        trainingsVideoRecorder = new TrainingsVideoRecorder(dataPath(""));
        //trainingsVideoRecorderPosBased = new TrainingsVideoRecorderPosBased(dataPath(""));

        //get Infos about sculpture
        boundingBox = LedBoundingBox.getForPositions(ledPositions);
        
        //normalize the LedPosition Data
        ledPositionsNormalized = boundingBox.normalizeLedPositions(ledPositions);
        
        //add effects to EffectArray
        //mixer.addEffect(new AttractingBalls());
        //mixer.addEffect(new MovingWallEffect(ledPositions, "vertical", -0.05f, 1f, 0.09f, 1f, 0.29f, 0.3f));
        //mixer.addEffect(new MovingWallEffect(ledPositions, "horizontal", 0.08f, 0.09f, -1f,0f, 0.29f, 0.3f));
        //mixer.addEffect(new SingleStripe("1", numStripes, numLedsPerStripe, ledPositions.length));
        
        //mixer.addEffect(new ManualSphere("2", ledPositions, 0.1f));
        
        
        // THE FOLLOWING ARE NEEDED FOR SAI # 2 
   
        mixer.addEffect(new DirectionLight("1", ledNormals));
        mixer.addEffect(new ManualSphere("1", ledPositions, 0.3f));
        //Wall for object 1
        //mixer.addEffect(new MovingWallEffect(ledPositions, "bottom", -0.05f, 1f, 0.09f,0f, 0.19f, 0.05f));
        //Wall for object 2
        mixer.addEffect(new MovingWallEffect(ledPositions, "bottom", 0.09375f, 0.828125f, -0.0703125f,0f, 0.19f, 0.05f));
 
        
        // THE FOLLOWING ARE NEEDED FOR SAI # 1
        /*
        mixer.addEffect(new MovingWallEffect(ledPositions, "middle", 0.08f, 0.09f, -1f, 0.505f, 0f, 0f));
        mixer.addEffect(new MovingWallEffect(ledPositions, "top", -0.05f, 1f, 0.09f, 1f, 0.29f, 0.3f)); //float pos_, float fadeOut_, float width_
        mixer.addEffect(new MovingWallEffect(ledPositions, "left", 0.08f, 0.09f, -1f,0f, 0.29f, 0.3f));
        mixer.addEffect(new MovingWallEffect(ledPositions, "right", 0.08f, 0.09f, -1f,1f, 0.29f, 0.3f));
        mixer.addEffect(new MovingWallEffect(ledPositions, "bottom", -0.05f, 1f, 0.09f,0f, 0.29f, 0.3f));
        mixer.addEffect(new ManualSphere("1", ledPositions, 0.3f));
        */
        
        mixer.addEffect(new NNefx());
        
        //create a Recorder to record the trainingsdata
        trainingsRecorder = new TrainingsRecorder();

        //create Player for recorded/predicted data
        recordPlayer = new RecordPlayer(ledColors.length);
        if (nn_sw) {
            playerFileName = "logbook/predictions/prediction_sw_";
        } else {
            playerFileName = "logbook/predictions/prediction_";
        }
        videoFileName = "video_sw_";
        //recordPlayer.loadNextFrame_sw(playerFileName);

        //to save remote Settings and Preset Data
        try {
            DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(dataPath("remoteSettings.txt")));
            OscAttributeDistributor.dumpParameterInfo(dataOut);
        } catch (FileNotFoundException e) {
            println("file not found");
        }
    }

    public void draw() {
        //background(0);
        /*
        if (playRecorded) {
            ledColors = recordPlayer.get();
            drawScreen();
            artNetSender.sendToLeds(ledColors);
        } //if the Player plays, show the Player
        else 
         */
        //load the next scene file, if the osc command came
       // System.out.println(frameRate);
        //videoPlayer.loadScene();
        /*
        if (videoPlayer.isPlaying()) {
            // if (videoPlayer.getFrameAvailable()) 
                            ledColors = videoPlayer.getFrame();
                background(0);
                drawScreen();
             
            //if the NeuralNetwork sends pictures, show the Neural Network Output
        } 
        artNetSender.sendToLeds(ledColors); 
        */
        if (nnListener.getReceiving()) {
            System.out.println("get NN");
            ledColors = nnListener.getFrame();
            artNetSender.sendToLeds(ledColors);
            background(0);
            drawScreen();
            //else show the mixers efx-generators output
        } else {
            //System.out.println(nnListener.getFrameAvailable());
            ledColors = mixer.mix();
            background(0);
            drawScreen();
            artNetSender.sendToLeds(ledColors);
        }
        

    }

    public void stop() {
        oscP5.stop();
    }

    void drawScreen() {
        // draw the leds on screen 
        visualizer.drawLeds(
                ledPositions, // array of LED-positions (as PVector)
                ledColors, // array of LED Colors (as LedColor) 
                true, //shall we draw a dark gray ring around the Leds? (helps to see structure of unlit stripe
                millis() / 3000.0f //rotation angle around y axis (so we see the model from all sides
        );
    }

    void oscEvent(OscMessage theOscMessage) {
        //check if the OscMessage contains record boolean or fft Analysis or other data
        /*
        if (theOscMessage.checkAddrPattern("/record")) {
            record = false;
            if (theOscMessage.getTypetagAsBytes()[0] == 'i') {
                if (theOscMessage.get(0).intValue() > 0) {
                    record = true;
                }
            }
        }
         */
 /*
        if (theOscMessage.checkAddrPattern("/playRecord")) {
            record = false;
            if (theOscMessage.getTypetagAsBytes()[0] == 'i') {
                if (theOscMessage.get(0).intValue() > 0) {
                    playRecorded = true;
                }
                if (theOscMessage.get(0).intValue() == 0) {
                    playRecorded = false;
                }
            }
        }
        if (theOscMessage.checkAddrPattern("/nextRecord")) {
            record = false;
            if (theOscMessage.getTypetagAsBytes()[0] == 'i') {
                if (theOscMessage.get(0).intValue() > 0) {
                    if (nn_sw) {
                        recordPlayer.loadNextFrame_sw(playerFileName);
                    } else {
                        recordPlayer.loadNextFrame(playerFileName);
                    }
                }
            }
        }
         */

        if (theOscMessage.checkAddrPattern("/frameCount") && theOscMessage.arguments().length > 0) {
            if (videoPlayer.isPlaying()) {
                videoPlayer.checkFrame(theOscMessage.get(0).intValue());
            }
            //videoRecorder.run(ledColors, theOscMessage.get(0).intValue());
        // a fft_train has to send in the end        to save trainings data    
        } else if (theOscMessage.checkAddrPattern("/fft_train") && theOscMessage.arguments().length > 0) {
            trainingsVideoRecorder.run(ledColors, theOscMessage);
            //trainingsVideoRecorderPosBased.run(boundingBox, ledPositionsNormalized, ledColors, theOscMessage);
        } else {
            OscMessageDistributor.distributeMessage(theOscMessage);
        }

    }

    protected void handleKeyEvent(KeyEvent event) {
//    keyEvent = event;
        key = event.getKey();
        keyCode = event.getKeyCode();

        switch (event.getAction()) {
            case KeyEvent.PRESS:
                keyPressed = true;
                keyPressed(event);
                if (keyCode == 39) {
                    if (nn_sw) {
                        recordPlayer.loadNextFrame_sw(playerFileName);
                    } else {
                        recordPlayer.loadNextFrame(playerFileName);
                    }
                }
                if (keyCode == 37) {
                    if (nn_sw) {
                        recordPlayer.loadLastFrame_sw(playerFileName);
                    } else {
                        recordPlayer.loadLastFrame(playerFileName);
                    }
                }
                if (keyCode == 80) {
                    playRecorded = !playRecorded;
                }
                if (keyCode == 86) {
                    playVideo = !playVideo;
                }
                if (keyCode == 83) {
                    writeStream = false;
                    //outStream.saveFileToDisk();
                    System.out.println("Saved Video");
                }

                break;
        }

    }
}
