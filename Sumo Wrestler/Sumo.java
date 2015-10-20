/*
 * 
 * HW3
 * Author: Jack Flaherty
 * 
 */

import java.io.File;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.hardware.sensor.NXTColorSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class Sumo {
	static NXTColorSensor color = new NXTColorSensor(SensorPort.S3);
	static globalColorData colorData = new globalColorData();
	
	static Brick brick = BrickFinder.getDefault();
	static NXTRegulatedMotor left = new NXTRegulatedMotor(brick.getPort("A"));
	static NXTRegulatedMotor right = new NXTRegulatedMotor(brick.getPort("B"));

	static int white, black, blackWhiteThreshold;
	
	public static boolean finish = false;
	
	public static void main(String[] args) {
		Thread c = new Thread(new colorThread(colorData, color));
		c.setDaemon(true);
		c.start();
		
		// Color stuff
		int white, black, blackWhiteThreshold;
		SampleProvider lightDist = color.getRGBMode();
		Calibrate cal = new Calibrate(lightDist);
		white = cal.calWhite();
		Delay.msDelay(1000);
		black = cal.calBlack();
		Delay.msDelay(1000);
		blackWhiteThreshold = cal.getAvgEx();
		
		LCD.clear();
		LCD.drawString("white: " + white,0,1);
		LCD.drawString("black: " + black,0,2);
		LCD.drawString("threshold: " + blackWhiteThreshold,0,3);
		LCD.drawString("Press to continue", 0, 4);
		Button.waitForAnyPress();
		LCD.clear();
		// END Color stuff
		
		brick.getKey("Escape").addKeyListener(new KeyListener() {
			public void keyPressed(Key k) {
				finish = true;
				color.close();
				System.exit(1);
			}
			public void keyReleased(Key k) {
				finish = true;
				System.exit(1);
			}
		});

		
		Behavior b1 = new Drive();
		Behavior b2 = new hitBorder();
		//Behavior b3 = new Terminate();

		Behavior[] bArray = { b1, b2 };
		Arbitrator arby = new Arbitrator(bArray);
		arby.start();
	}
}
