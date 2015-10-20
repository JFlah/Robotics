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

public class Drive implements Behavior {
	private boolean suppressed = false;
	private boolean isRunning = false;
	
	public boolean takeControl() {
		return true;
	}
	
	public void suppress() {
		suppressed = true;
	}
	
	public void action() {
		final int power = 730;
		
		suppressed = false;
		isRunning = true;
		
		//int i = 0;
		while (!suppressed) {
			//LCD.drawString("Drive", 0, 0);
			//LCD.drawInt(i, 0, 1);
			//i++;
			Sumo.left.setSpeed(power);
			Sumo.right.setSpeed(power);
			Sumo.left.forward();
			Sumo.right.forward();
			Thread.yield();
		}
	}
}
