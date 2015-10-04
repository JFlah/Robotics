/*
 * LAB 2 PART 2
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
import lejos.hardware.sensor.NXTTouchSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.hardware.Sound;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class Part2 {
	static double wheelDiameter = 5.6;
	static double robotTrack= 13.9;

	public static NXTTouchSensor ts = new NXTTouchSensor(SensorPort.S1);
	
	public static void main(String[] args) {
		globalData touchedData = new globalData(); // set up global data for
		// touch thread
		Thread t = new Thread(new touchThread(touchedData, ts)); // create touch
						// thread
		t.setDaemon(true);
		
		t.start();
		
		double wheelCircumference=wheelDiameter*Math.PI;
		boolean loop = true;
		int degrees;
		LCD.drawString("Part 2 | Press", 0, 0);
		Delay.msDelay(2000);
		LCD.clear();
		Brick brick = BrickFinder.getDefault(); //get specifics about this robot
		
		RegulatedMotor left = new NXTRegulatedMotor(brick.getPort("A"));
		RegulatedMotor right = new NXTRegulatedMotor(brick.getPort("B"));
		
		left.setSpeed(100);
		right.setSpeed(100);

		left.synchronizeWith(new RegulatedMotor[] {right});	 
		
		Button.waitForAnyPress();
		
		left.startSynchronization();
		left.forward();
        right.forward();
        
		left.endSynchronization();
        
		while (!Button.ESCAPE.isDown()) {
			// fetch a sample
			int i = 0;
			
			int turns = 0;
			double tacho = 0;
			
			while (loop == true){
				//distance.fetchSample(sample, 0);
				
				if (touchedData.getTouched()) {//what to do if touched?
					//put your code here, don't forget to reset the global variable to false
					//think how to do this
					left.stop(true);
					right.stop();
					
					left.rotate(-100,true);
					right.rotate(-100);
					
					tacho += 100; // accounting for deg lost in above rotations
					
					left.waitComplete();
					right.waitComplete();
					
					double firstCount = left.getTachoCount();
					
					tacho += firstCount;
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					Sound.systemSound(true, 3);
					degrees=(int)Math.round((((robotTrack*Math.PI)/4)/wheelCircumference)*360.0);
					
					left.startSynchronization();
					
					left.rotate((int)(-degrees+40), true);
					right.rotate((int)degrees-40);
					
					left.endSynchronization();
					
					left.waitComplete();
					right.waitComplete();
					
					double secondCount = left.getTachoCount();
					
					tacho += secondCount;
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					LCD.clear();
					
					left.forward();
					right.forward();
					
					turns++;
					
					double thirdCount = left.getTachoCount();
					tacho += thirdCount;
					
					double robotCorrection = 24.5*360.0/wheelCircumference;
					tacho += robotCorrection; // accounting for the rest of the length of the robot
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					touchedData.resetTouched();
					LCD.drawString("Side: " + tacho/wheelCircumference*360.0, 0, 0);
					//Delay.msDelay(5000);
					LCD.clear();
					
				}
				
				if (turns == 4){
					left.stop();
					right.stop();
					
					LCD.drawString("Tacho: " + tacho, 0, 0);
					LCD.drawString("Deg: " + tacho/360, 0, 1);
					LCD.drawString("C of Wheel: " + wheelCircumference, 0, 2);
					double rotations = tacho/360.0;
					double distanceTravelled = rotations * wheelCircumference;
					LCD.drawString("Dist: " + distanceTravelled + "cm", 0, 3);
					Sound.systemSound(true, 1);
					
					loop = false;

				}
				
			}
		}
	}
}