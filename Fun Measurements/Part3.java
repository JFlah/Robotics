/*
 * LAB 2 PART 3
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

public class Part3 {
	static double wheelDiameter = 5.6;
	static double robotTrack= 13.9;

	public static void main(String[] args) {		
		double wheelCircumference=wheelDiameter*Math.PI;
		boolean loop = true;
		int degrees;
		
		LCD.drawString("Part 3 | Press", 0, 0);
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
		
		left.resetTachoCount();
		right.resetTachoCount();
		
		left.startSynchronization();
		left.forward();
        right.forward();

		left.endSynchronization();        
        
		while (!Button.ESCAPE.isDown()) {
			// fetch a sample
			
			int turns = 0;
			double tacho = 0;
			
			while (loop == true){
				distance.fetchSample(sample, 0);
				
				if (sample[0] > 0.3) {
					left.stop(true);
					right.stop();
					
					left.rotate(150, true);
					right.rotate(150);
					
					left.waitComplete();
					right.waitComplete();
					
					tacho += left.getTachoCount();
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					Sound.systemSound(true, 3);
					degrees=(int)Math.round((((robotTrack*Math.PI)/4)/wheelCircumference)*360.0);
					
					left.startSynchronization();
					
					left.rotate((int)(degrees-15), true);
					right.rotate((int)-degrees+15);
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					left.endSynchronization();
					
					left.waitComplete();
					right.waitComplete();
					
					left.rotate(360, true);
					right.rotate(360);
					
					left.waitComplete();
					right.waitComplete();
					
					left.forward();
					right.forward();
					
//					double robotCorrection = 24.5*360.0/wheelCircumference;
//					tacho += robotCorrection*4; // accounting for the rest of the length of the robot
					
					tacho += left.getTachoCount();
					
					turns++;
				}
				
				if (turns == 4){
					left.stop();
					right.stop();
					
					LCD.drawString("I'm back!", 0, 0);
					LCD.clear();

					LCD.drawString("Tacho: " + tacho, 0, 0);
					LCD.drawString("Turns: " + tacho/360, 0, 1);
					LCD.drawString("C of Wheel: " + wheelCircumference, 0, 2);
					double rotations = tacho/360.0;
					double distanceTravelled = rotations * wheelCircumference;
					LCD.drawString("Dist: " + distanceTravelled/2 + "cm", 0, 3);
					Sound.systemSound(true, 1);
					
					
					loop = false;
				}
			}
		}
		sensor.close();
	}
}