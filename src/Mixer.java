
import java.util.ArrayList;

import processing.core.PApplet;

class Mixer {

    PApplet papplet;
    LedColor[] outputBufferLedColors;
    // Array of effects for the mixer and their mixer specific values
    ArrayList<runnableLedEffect> effectList;
    ArrayList<RemoteControlledFloatParameter> opacityList;
    RemoteControlledFloatParameter traceControlRed;
    RemoteControlledFloatParameter traceControlGreen;
    RemoteControlledFloatParameter traceControlBlue;
    RemoteControlledFloatParameter masterControlOpacity;
    //float trace;
    LedColor trace;
    float masterOpacity;

    public Mixer() {
        outputBufferLedColors = LedColor.createColorArray(Ortlicht.ledPositions.length); //creates a new ledColorBuffer as output of the mixer
        effectList = new ArrayList<runnableLedEffect>();
        opacityList = new ArrayList<RemoteControlledFloatParameter>();
        traceControlRed = new RemoteControlledFloatParameter("/mixer/master/trace/red", 0f, 0f, 1f);
        traceControlGreen = new RemoteControlledFloatParameter("/mixer/master/trace/green", 0f, 0f, 1f);
        traceControlBlue = new RemoteControlledFloatParameter("/mixer/master/trace/blue", 0f, 0f, 1f);
        masterControlOpacity = new RemoteControlledFloatParameter("/mixer/master/brightness", 1f, 0f, 1f);
        
    }

    //adds an effect to the effect ArrayList
    public void addEffect(runnableLedEffect _theEffect) {
        effectList.add(_theEffect);
        opacityList.add(new RemoteControlledFloatParameter("/mixer/opacity/" + _theEffect.getName(), 1f, 0, 1f));

    }

    public LedColor[] mix() {
        trace = new LedColor(traceControlRed.getValue(), traceControlGreen.getValue(), traceControlBlue.getValue());
        masterOpacity = masterControlOpacity.getValue();
        LedColor.mult(outputBufferLedColors, trace);
        for (int i = 0; i < effectList.size(); i++) {
            //copies the effects output with the remote opacity and blendmode on the mixerbuffer
            LedColor[] effectOutput = LedColor.createColorArray(Ortlicht.ledPositions.length); //creates a new ledColorBuffer to add filter without breaking the original colorbuffer
            effectOutput = effectList.get(i).drawMe(); //gets the output Buffer of the effect in the effectArray
            /*
      MISSING:: TURN HUE AND SATURATION OF LedColor[] effectOutput
             */
            LedColor.LedAlphaMode blendMode = LedColor.LedAlphaMode.ADD; //sets the blendmode, how the effect output is mixed with the other effects
            float opacity = opacityList.get(i).getValue();
            for (int j = 0; j < Ortlicht.ledColors.length; j++) {
                outputBufferLedColors[j].mixWithAlpha(effectOutput[j], blendMode, opacity);
            }
        }
        LedColor.mult(outputBufferLedColors, masterOpacity);
        //LedColor.substract(outputBufferLedColors, new LedColor(0.05,0.002,0.03)); //colored master fade out
        return outputBufferLedColors;
    }

    public LedColor[] showFirstEffect() {
        outputBufferLedColors = effectList.get(0).drawMe();
        return outputBufferLedColors;
    }
}

// every effect implements the runnableLedEffect interface, so that they can easiely be summerised in an array within the mixer
interface runnableLedEffect {
    // String name;

    LedColor[] drawMe();

    String getName();
}
