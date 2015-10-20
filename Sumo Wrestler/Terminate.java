import java.io.File;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.robotics.subsumption.Behavior;

public class Terminate implements Behavior {
	private boolean suppressed = false;
	
	public boolean takeControl() {
		return (Sumo.finish);
	}
	
	public void action() {
		Sumo.color.close();
		File ff = new File("starwars.wav");
		try {
			Sound.playSample(ff, 100);
		      }
		catch (Exception ex) {}
		System.exit(1);
	}

	public void suppress() {
		suppressed = true;		
	}
}
