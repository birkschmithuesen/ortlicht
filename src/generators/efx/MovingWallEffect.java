import processing.core.PApplet;
import processing.core.PVector;

public class MovingWallEffect implements runnableLedEffect {
	LedColor[] ledColors;
	PVector[] ledPositions;
        String name = "wall";
        String id;

	RemoteControlledFloatParameter 	wallPosition;
	RemoteControlledFloatParameter fullOnWidth;
	RemoteControlledFloatParameter decayWidth;
	RemoteControlledFloatParameter decayGamma;

	RemoteControlledColorParameter wallColor;

	// used to tilt the wall in space
	RemoteControlledFloatParameter wallNormalX;	
	RemoteControlledFloatParameter wallNormalY;
	RemoteControlledFloatParameter wallNormalZ;

	MovingWallEffect(PVector[] ledPositions_, String id_, float wallNormalX_, float wallNormalY_, float wallNormalZ_, float pos_, float fadeOut_, float width_){
		ledPositions=ledPositions_;
		ledColors=LedColor.createColorArray(ledPositions.length);
                id=id_;

		wallPosition=new RemoteControlledFloatParameter("/walls/"+name+"_"+id+"/position",pos_,0f,1f);
		fullOnWidth=new RemoteControlledFloatParameter("/walls/"+name+"_"+id+"/width",width_,0f,1f);
		decayWidth=new RemoteControlledFloatParameter("/walls/"+name+"_"+id+"/fadeOutWidth",fadeOut_,0f,1f);
		decayGamma=new RemoteControlledFloatParameter("/walls/"+name+"_"+id+"/fadeOutGamma",2f,0.01f,10f);

		wallNormalX= new RemoteControlledFloatParameter("/walls/"+name+"_"+id+"/normal/X",wallNormalX_,-1f,1f);
		wallNormalY= new RemoteControlledFloatParameter("/walls/"+name+"_"+id+"/normal/Y",wallNormalY_,-1f,1f);
		wallNormalZ= new RemoteControlledFloatParameter("/walls/"+name+"_"+id+"/normal/Z",wallNormalZ_,-1f,1f);
                
                wallColor = new RemoteControlledColorParameter("/walls/"+name+"_"+id+"/color/",0,0,0.5f);
	}
	public LedColor[] drawMe() {
		
		PVector wallNormal= new PVector(wallNormalX.getValue(),wallNormalY.getValue(),wallNormalZ.getValue());
		if(wallNormal.magSq()>0.0001)		wallNormal.normalize(); // avoid dev/0
		
		float wallPos=PApplet.map(wallPosition.getValue(),0f, 1f, -1f, 1f);
		float startFade=-(fullOnWidth.getValue()+decayWidth.getValue())/2.0f;
		float startFull=-(fullOnWidth.getValue())/2.0f;
		float endFull=+(fullOnWidth.getValue())/2.0f;
		float endFade=+(fullOnWidth.getValue()+decayWidth.getValue())/2.0f;
		float gamma=decayGamma.getValue();
		LedColor wallColor_=wallColor.getColor();
		for(int i=0;i<ledPositions.length;i++) {
			float fadeMult=0;
			float relPos=ledPositions[i].dot(wallNormal)-wallPos;//change this to make the wall travel in another direction
			if(relPos>startFade&&relPos<startFull) {
				fadeMult=(float)Math.pow(PApplet.map(relPos,startFade, startFull, 0f, 1f ),gamma);
			}
			if(relPos>startFull&&relPos<endFull) {
				fadeMult=1f;
			}			
			if(relPos>endFull&&relPos<endFade) {
				fadeMult=(float)Math.pow(PApplet.map(relPos,endFull, endFade, 1f, 0f ),gamma);
			}
			ledColors[i].set(wallColor_.x*fadeMult,wallColor_.y*fadeMult,wallColor_.z*fadeMult);

		}
		return ledColors;
	}

	public String getName() {
		return name+id;
	}
}
