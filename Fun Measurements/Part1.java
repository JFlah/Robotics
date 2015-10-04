/*
 * LAB 2 PART 1
 * Author: Jack Flaherty
 * 
 */

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.hardware.Sound;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class Part1 {
	static double wheelDiameter = 5.6;
	static double robotTrack= 13.9;

	public static void main(String[] args) {		
		double wheelCircumference=wheelDiameter*Math.PI;
		boolean loop = true;
		int degrees;
		
		LCD.drawString("Part 1 | Press", 0, 0);
		Delay.msDelay(2000);
		LCD.clear();
		
		Button.waitForAnyPress();
		
		Brick brick = BrickFinder.getDefault(); //get specifics about this robot
		RegulatedMotor left = new NXTRegulatedMotor(brick.getPort("A"));
		RegulatedMotor right = new NXTRegulatedMotor(brick.getPort("B"));
		left.setSpeed(360);
		right.setSpeed(360);

		// Get an instance of the Ultrasonic sensor
		NXTUltrasonicSensor sensor = new NXTUltrasonicSensor(SensorPort.S2);

		// get an instance of this sensor in measurement mode
		SampleProvider distance = sensor.getDistanceMode();

		// Initialize an array of floats for fetching samples
		float[] sample = new float[distance.sampleSize()];

		left.synchronizeWith(new RegulatedMotor[] {right});	 
		left.startSynchronization();
		left.forward();
        right.forward();

		left.endSynchronization();        
        
		while (!Button.ESCAPE.isDown()) {
			// fetch a sample
			
			while (loop == true){
				distance.fetchSample(sample, 0);
				if (sample[0] > 0.3) {
					left.stop(true);
					right.stop();
					double tacho = left.getTachoCount();
					LCD.drawString("Deg: " + tacho, 0, 0);
					LCD.drawString("C of Wheel: " + wheelCircumference, 0, 1);
					double rotations = tacho/360.0;
					double distanceTravelled = rotations * wheelCircumference;
					LCD.drawString("Dist: " + distanceTravelled + "cm", 0, 2);
					Sound.systemSound(true, 1);
					loop = false;
				}
			}
		}
		sensor.close();
	}
}