/*
 * LAB 1 PART 3
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


//ip with BC wifi is 136.167.209.65
//ip with home is 192.168.1.15
public class WallHugger {
	static double wheelDiameter = 5.3975; //6.24;
	static double robotTrack= 17.145; //17.145;  //12.2;

	public static NXTTouchSensor ts = new NXTTouchSensor(SensorPort.S1);
	
	public static void main(String[] args) {
		globalData touchedData = new globalData(); // set up global data for
		// touch thread
		Thread t = new Thread(new touchThread(touchedData, ts)); // create touch
						// thread
		t.setDaemon(true);
		
		t.start();
		
		double wheelCircunference=wheelDiameter*Math.PI;
		boolean loop = true;
		int degrees;
		LCD.drawString("Wall Hugger", 0, 1);
		Delay.msDelay(2000);
		LCD.clear();
		Brick brick = BrickFinder.getDefault(); //get specifics about this robot
		
		RegulatedMotor left = new NXTRegulatedMotor(brick.getPort("A"));
		RegulatedMotor right = new NXTRegulatedMotor(brick.getPort("B"));
		
		left.setSpeed(200);
		right.setSpeed(200);
		
		//left.setAcceleration(100);
		//right.setAcceleration(100);

		// Get an instance of the Ultrasonic sensor
		NXTUltrasonicSensor sensor = new NXTUltrasonicSensor(SensorPort.S2);

		// get an instance of this sensor in measurement mode
		SampleProvider distance = sensor.getDistanceMode();

		// Initialize an array of floats for fetching samples
		float[] sample = new float[distance.sampleSize()];

		left.synchronizeWith(new RegulatedMotor[] {right});	 
		
		Button.waitForAnyPress();
		
		left.startSynchronization();
		left.forward();
        right.forward();
		//left.rotate(360, true);
		//right.rotate(360, true);
		left.endSynchronization();
		//left.waitComplete();
		//right.waitComplete();
        
        
		while (!Button.ESCAPE.isDown()) {
			// fetch a sample
			int i = 0;
			
			while (loop == true){
				distance.fetchSample(sample, 0);
				LCD.drawString("d[" + i + "]: " + sample[0], 0, 2);
				
				if (touchedData.getTouched()) {//what to do if touched?
					//put your code here, don't forget to reset the global variable to false
					//think how to do this
					left.stop(true);
					right.stop();
					
					left.rotate(-100,true);
					right.rotate(-100);
					
					left.waitComplete();
					right.waitComplete();
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					Sound.systemSound(true, 3);
					degrees=(int)Math.round((((robotTrack*Math.PI)/6)/wheelCircunference)*360.0);
					LCD.drawString("deg: " + degrees, 0, 3);
					left.startSynchronization();
					left.rotate((int)(-degrees), true);
					right.rotate((int)degrees);
					left.endSynchronization();
					left.waitComplete();
					right.waitComplete();
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					LCD.clear();
					LCD.drawString("deg: " + degrees, 0, 3);
					
					left.forward();
					right.forward();
					
					touchedData.resetTouched();
				}
				
				if (sample[0] > 0.3) {
					left.stop(true);
					right.stop();
					Sound.systemSound(true, 1);
					
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					left.rotate(360,true);
					right.rotate(360);
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					Sound.systemSound(true, 3);
					degrees=(int)Math.round((((robotTrack*Math.PI)/4)/wheelCircunference)*360.0);
					LCD.drawString("deg: " + degrees, 0, 3);
					left.startSynchronization();
					left.rotate((int)(degrees-65), true);
					right.rotate((int)-degrees+65);
					left.endSynchronization();
					left.waitComplete();
					right.waitComplete();
					
					left.resetTachoCount();
					right.resetTachoCount();
					
					LCD.clear();
					LCD.drawString("deg: " + degrees, 0, 3);
					
					left.startSynchronization();
					left.rotate(720, true);
					right.rotate(720);
					left.endSynchronization();
					left.waitComplete();
					right.waitComplete();
					
					loop = false;
				
					//degrees=(int)Math.round((((robotTrack*Math.PI)/4.0)/wheelCircunference)*360.0);
					//LCD.drawString("deg: " + degrees, 0, 3);
					//left.startSynchronization();
					//left.rotate(degrees, true);
					//right.rotate(-degrees);
					//left.endSynchronization();
					//left.waitComplete();
					//right.waitComplete();
					//left.forward();
					//right.forward();
				}
			}
		}
		sensor.close();
	}
}