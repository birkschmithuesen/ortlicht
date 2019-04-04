
import processing.core.PApplet;
import processing.core.PVector;

////////////////////////////////////////////////////////////////////////////
// Lights up a single Stirpe - for example to test each stripe
////////////////////////////////////////////////////////////////////////////
public class SingleStripe implements runnableLedEffect {

    PApplet parent;
    String name = "singleStripe";
    String id;
    float cyclePosition; // the Position in the beat cycle, given by the master
    // bpm. value goes from 0-1
    boolean cycle = false, directionOut = true; // if cycle is true, the sphere
    // axpands and in and out. if
    // false, the sphere just
    // expands out.
    int width;
    int numLedsPerStripe;
    int numLeds;
    int stripeID;
    LedColor[] bufferLedColors;
    LedColor theColor;
    LedColor.LedAlphaMode blendMode = LedColor.LedAlphaMode.NORMAL;
    RemoteControlledColorParameter remoteColor;
    RemoteControlledFloatParameter remoteBlendOut;
    RemoteControlledFloatParameter remoteWidth;
    RemoteControlledFloatParameter remotePos;
    RemoteControlledIntParameter remoteIdStripe;
    float maxRadius, minRadius;
    float pos = 0;
    float blendOut;

    SingleStripe(String _id, int _numStripes, int _numLedsPerStripe, int _numLeds) {
        id = _id;
        numLedsPerStripe = _numLedsPerStripe;
        numLeds = _numLeds;
        bufferLedColors = LedColor.createColorArray(numLeds);
        remoteColor = new RemoteControlledColorParameter("/singleStripe/" + id + "/", 0f, 0f, 0f);
        remoteBlendOut = new RemoteControlledFloatParameter("/singleStripe/" + id + "/BlendOut", 0.5f, 0.f, 1.f);
        remoteWidth = new RemoteControlledFloatParameter("/singleStripe/" + id + "/width", 1f, 0.f, 1.f);
        remotePos = new RemoteControlledFloatParameter("/singleStripe/" + id + "/Pos", 0.5f, 0.f, 1.f);
        remoteIdStripe = new RemoteControlledIntParameter("/singleStripe/" + id + "/Stripe", 0, 0, _numStripes - 1);

    }

    public LedColor[] drawMe() {
        //choose whether auto cycle intern is giving the beat, or remote controlled
        LedColor.mult(bufferLedColors, remoteBlendOut.getValue()); //sets the
        // trace/blendOut for the effect
        blendOut = remoteBlendOut.getValue();
        pos = remotePos.getValue();
        theColor = remoteColor.getColor();

        width = (int) PApplet.map(remoteWidth.getValue(), 0f, 1f, 0, numLedsPerStripe);

        int startLed = remoteIdStripe.getValue() * numLedsPerStripe;
        for (int j = 0; j < bufferLedColors.length; j++) {
            //
            int endLed = startLed + numLedsPerStripe;
            int lineStart = (int) (PApplet.map(pos, 0f, 1f, startLed - width, endLed));
            int lineWidth = lineStart + width;
            if (j < endLed && j > startLed) {
                if (j > lineStart && j < lineWidth) {
                    bufferLedColors[j].mixWithAlpha(theColor, blendMode, 0.5f);
                }
                //else bufferLedColors[j].mixWithAlpha(new LedColor(0f,0f,0f),blendMode, blendOut);	
            }
        }

        return bufferLedColors;
    }

    public String getName() {
        return name;
    }

    public LedColor[] getColorBuffer() {
        return bufferLedColors;
    }

}
