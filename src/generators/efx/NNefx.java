import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author macbook
 */
public class NNefx implements runnableLedEffect{
    PApplet papplet;
    String name = "NeuralNetwork";
    NNefx(){
        
    }
    public LedColor[] drawMe() {
            return Ortlicht.nnListener.getFrame();
    }

    public String getName() {
            return name;
    }
}
