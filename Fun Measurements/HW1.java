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
import lejos.hardware.sensor.NXTTouchSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.hardware.Sound;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class HW1 {
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
		
		LCD.drawString("HW 1 | Press", 0, 0);
		Delay.msDelay(2000);
		LCD.clear();
		
		Button.waitForAnyPress();
		
		Brick brick = BrickFinder.getDefault(); //get specifics about this robot
		RegulatedMotor left = new NXTRegulatedMotor(brick.getPort("A"));
		RegulatedMotor right = new NXTRegulatedMotor(brick.getPort("B"));
		left.setSpeed(200);
		right.setSpeed(200);

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
			
			while (loop == true){
				distance.fetchSample(sample, 0);
				
				if (sample[0] > 0.2) {
					left.stop(true);
					right.stop();
					
					left.rotate(250, true);
					right.rotate(250);
					
					left.waitComplete();
					right.waitComplete();
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					Sound.systemSound(true, 3);
					degrees=(int)Math.round((((robotTrack*Math.PI)/4)/wheelCircumference)*360.0);
					
					left.startSynchronization();
					
					left.rotate((int)(degrees-25), true);
					right.rotate((int)-degrees+25);
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					left.endSynchronization();
					
					left.waitComplete();
					right.waitComplete();
					
					left.rotate(460, true);
					right.rotate(460);
					
					left.waitComplete();
					right.waitComplete();
					
					left.forward();
					right.forward();
				}
				
				if (touchedData.getTouched()) {
					left.stop(true);
					right.stop();
					
					left.rotate(-100,true);
					right.rotate(-100);
					
					left.waitComplete();
					right.waitComplete();
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					Sound.systemSound(true, 3);
					degrees=(int)Math.round((((robotTrack*Math.PI)/4)/wheelCircumference)*360.0);
					
					left.startSynchronization();
					
					left.rotate((int)(-degrees+25), true);
					right.rotate((int)degrees-25);
					
					left.endSynchronization();
					
					left.waitComplete();
					right.waitComplete();
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					LCD.clear();
					
					left.forward();
					right.forward();					

					left.resetTachoCount();
					right.resetTachoCount();
					
					touchedData.resetTouched();
				}
			}
		}
		sensor.close();
	}
}