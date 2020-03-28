import processing.core.PApplet;
import processing.core.PVector;

public class DirectionLight implements runnableLedEffect {

	PApplet papplet;
	String name;
	String id;
	PVector[] ledNormals;
	LedColor[] bufferLedColors;
	PVector angleFrom, angleTill;
	LedColor theColor;
	RemoteControlledColorParameter remoteColor;
	RemoteControlledFloatParameter remoteBlendOut;
	RemoteControlledFloatParameter remoteDirectionX;
	RemoteControlledFloatParameter remoteDirectionY;
	RemoteControlledFloatParameter remoteDirectionZ;
	RemoteControlledFloatParameter remoteSize;
	RemoteControlledFloatParameter remoteCyclePos;
	LedColor.LedAlphaMode blendMode = LedColor.LedAlphaMode.NORMAL;
	float cyclePos;

	DirectionLight(String _id, PVector[] _ledNormals) {
		id = _id;
                name = "direction" + id;
		ledNormals = _ledNormals;
		bufferLedColors = LedColor.createColorArray(ledNormals.length);
		//theColor = _color;
		remoteColor = new RemoteControlledColorParameter("/colors/"+"direction" + id + "/", 0.0f, 0.0f, 1f);
		remoteBlendOut = new RemoteControlledFloatParameter("/direction" + id + "/BlendOut", 1.f, 0.f, 1.f);
		remoteDirectionX = new RemoteControlledFloatParameter("/direction" + id + "/XFrom", 0.8f, -0.5f, 0.5f);
		remoteDirectionY = new RemoteControlledFloatParameter("/direction" + id + "/YFrom", 0.64f, 0f, 1f);
		remoteDirectionZ = new RemoteControlledFloatParameter("/direction" + id + "/ZFrom", 0.2f, -0.5f, 0.5f);
		remoteSize = new RemoteControlledFloatParameter("/direction" + id + "/Size", 0.2f, 0.f, 1.f);
		remoteCyclePos = new RemoteControlledFloatParameter("/direction" + id + "/cyclePos", 0.0f, 0.f, 1.f);
	}

	public LedColor[] drawMe() {
		//choose whether auto cycle intern is giving the beat, or remote controlled
				cyclePos=remoteCyclePos.getValue();
				//cyclePos=Ortlicht.bpmClock.getCyclePos()
		
		//float direction = papplet.map(Ortlicht.bpmClock.getCyclePos(), 0.f, 1.f, -0.5f, 0.5f);
		
		float directionX = papplet.sin(cyclePos*papplet.TWO_PI)/4f;

		float directionZ = papplet.cos(cyclePos*papplet.TWO_PI)/4f;
		
		//ledDirectionDrawer.drawDirection(ledNormals, bufferLedColors, new PVector(remoteDirectionX.getValue(),remoteDirectionY.getValue(),remoteDirectionZ.getValue()), remoteSize.getValue(),
		//ledDirectionDrawer.drawDirection(ledNormals, bufferLedColors, new PVector(direction,remoteDirectionY.getValue(),remoteDirectionZ.getValue()), remoteSize.getValue(),
		
		LedDirectionDrawer.drawDirection(ledNormals, bufferLedColors, new PVector(directionX, papplet.map(remoteDirectionY.getValue(), 0f, 1f, -1.6f, 0.6f),directionZ), remoteSize.getValue(),
		
		remoteColor.getColor(), blendMode, remoteBlendOut.getValue());
		return bufferLedColors;
	}

	public String getName() {
		return name;
	}
}
