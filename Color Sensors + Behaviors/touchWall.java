import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;



public class touchWall implements Behavior {
	private int distance;
	static double wheelDiameter = 5.6;
	static double robotTrack = 13.9;
	private boolean suppressed = false;
	private boolean isRunning = false;

	public boolean takeControl() {
		boolean b = Part5.touchedData.getTouched();
		if(b){
			Part5.touchedData.resetTouched();
		}
		return b;
	}

	public void suppress() {
		suppressed = true;
	}

	public void action() {
		
		suppressed = false;
		isRunning = true;
		LCD.drawString("Wall Touch", 0, 0);
		Part5.left.stop(true);
		Part5.right.stop();
		double wheelCircunference = wheelDiameter * Math.PI;
		Part5.left.rotate(-150,true);
		Part5.right.rotate(-150);
		LCD.drawString("back up now", 0, 0);
		int degrees=(int)Math.round((((robotTrack*Math.PI)/4)/wheelCircunference)*360.0);
		Part5.right.startSynchronization();
		Part5.left.rotate((int)(-degrees+25),true);
		Part5.right.rotate((int)(degrees-25));
		Part5.right.endSynchronization();
		
		Part5.left.waitComplete();

		suppress();
		isRunning = false;
			
	}
}


