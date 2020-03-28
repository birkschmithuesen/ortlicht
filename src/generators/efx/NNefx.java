import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author macbook
 */
public class NNefx implements runnableLedEffect{
    PApplet papplet;
    String name = "neuralNetwork";
    RemoteControlledColorParameter remoteColor;
    LedColor theColor;
    LedColor[] bufferLedColors;
    NNefx(PVector[] _ledPositions){
        remoteColor = new RemoteControlledColorParameter("/colors"+"/NN/", 0f, 0.f, 1f);
        bufferLedColors = LedColor.createColorArray(_ledPositions.length);
    }
    public LedColor[] drawMe() {
            theColor = remoteColor.getColor();
            bufferLedColors = Ortlicht.nnListener.getFrame();
            LedColor.mult(bufferLedColors, theColor);
            return bufferLedColors;
    }

    public String getName() {
            return name;
    }
}
