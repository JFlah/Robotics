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

public class hitBorder implements Behavior {
	private int distance;
	static int colorVal;
	static int trigger;
	static double wheelDiameter = 5.6;
	static double robotTrack = 13.9;
	private boolean suppressed = false;
	private boolean isRunning = false;
	
	public boolean takeControl() {
		colorVal = Sumo.colorData.getColorValue();
		return (colorVal < 25);
	}
	
	public void suppress() {
		suppressed = true;
	}
	
	int i = 0;
	public void action() {
		suppressed = false;
		isRunning = true;
		
		LCD.drawString("Border", 0, 0);
		LCD.drawInt(i, 0, 1);
		i++;
		
		double wheelCircumference = wheelDiameter * Math.PI;
		
		Sumo.left.startSynchronization();
		Sumo.right.startSynchronization();

		Sumo.left.stop();
		Sumo.right.stop();
		
		Sumo.left.endSynchronization();
		Sumo.right.endSynchronization();
		
		Sumo.left.setSpeed(1000);
		Sumo.right.setSpeed(1000);
		Sumo.left.rotate(-150, true);
		Sumo.right.rotate(-150);
		
		int degrees=(int)Math.round((((robotTrack*Math.PI)/2)/wheelCircumference)*360.0);
		Sumo.left.rotate((int)(degrees), true);
		Sumo.right.rotate((int)(-degrees));
		Sumo.left.waitComplete();
		Sumo.right.waitComplete();
		
		//while (!suppressed) {
		//	Thread.yield();
		//}
		
		isRunning = false;
		Sumo.left.forward();
		Sumo.right.forward();
		suppress();	
	}
}
