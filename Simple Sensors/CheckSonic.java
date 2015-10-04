/*
 * LAB 1 PART 1
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


//ip with BC wifi is 136.167.209.65
//ip with home is 192.168.1.15
public class CheckSonic {
	static double wheelDiameter = 5.3975; //6.24;
	static double robotTrack= 17.145; //17.145;  //12.2;

	public static void main(String[] args) {
		double wheelCircunference=wheelDiameter*Math.PI;
		boolean loop = true;
		int degrees;
		LCD.drawString("ultra Test", 0, 1);
		Delay.msDelay(2000);
		LCD.clear();
		Brick brick = BrickFinder.getDefault(); //get specifics about this robot
		RegulatedMotor left = new NXTRegulatedMotor(brick.getPort("A"));
		RegulatedMotor right = new NXTRegulatedMotor(brick.getPort("B"));
		left.setSpeed(350);
		right.setSpeed(350);
		//left.setAcceleration(100);
		//right.setAcceleration(100);

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
				if (sample[0] > 0.3) {
					left.stop(true);
					right.stop();
					Sound.systemSound(true, 1);
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