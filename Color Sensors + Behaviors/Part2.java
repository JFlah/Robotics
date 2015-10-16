/* 
 * Assignment 2 Part 2
 * Author: Jack Flaherty
 */

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.BasicMotorPort;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.hardware.sensor.NXTTouchSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.utility.Delay;
import lejos.hardware.sensor.NXTColorSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.hardware.sensor.NXTLightSensor;

/**
 * The light sensor should be connected to port 3. The left motor should be
 * connected to port A and the right motor to port B.
 * 
 * controlMotor public void controlMotor(int power,int mode)
 * 
 * Low-level method to control a motor. Specified by: controlMotor in interface
 * BasicMotorPort Parameters: power - power from 0-100 mode - 1=forward,
 * 2=backward, 3=stop, 4=float
 */
public class Part2 {
	static double wheelDiameter = 5.6;
	static double robotTrack= 13.9;
	static double wheelCircumference=wheelDiameter*Math.PI;
	
	// boolean for which way the robot is going
	static boolean switched = false;
	
	static NXTColorSensor light = new NXTColorSensor(SensorPort.S3);
	static NXTUltrasonicSensor us = new NXTUltrasonicSensor(SensorPort.S2);
	
	public static void main(String[] aArg) throws Exception {
		String leftString = "Turn left ";
		String rightString = "Turn right";
		int white,black,blackWhiteThreshold;
		TachoMotorPort left = (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class); // right
		TachoMotorPort right =(TachoMotorPort) MotorPort.B.open(TachoMotorPort.class); // left
		
		Brick brick = BrickFinder.getDefault();
		RegulatedMotor lft;
		RegulatedMotor rght;
		
		// get an instance of this sensor in measurement mode
		SampleProvider dist = us.getDistanceMode();
		// Initialize an array of floats for fetching samples
		float[] sample = new float[dist.sampleSize()];

		final int forward = 1;
		final int backward = 2;
		final int stop = 3;
		final int flt = 4;
		final int power = 70;
		
		// configure values
		final int desiredDistance = 20;
		
		// initialize PID constants
		final double Kp = .9;
		final double Ki = 0.00001;
		final double Kd = 0.45;

		// unaltered turn power
		final int Tp = 40;

		double error;
		double accumError = 0;
		double lastError = 0;
		double errorDiff;
		
		int distance;
		
		// Use the light sensor as a reflection sensor
		SampleProvider lightDist = light.getRGBMode();  //color light with a source
		//SampleProvider lightDist = light.getRedMode();  //light with a source
		
		//now calibrate light/color sensor
		
		Calibrate c = new Calibrate(lightDist);
		//calibrate white
		white = c.calWhite();
		Delay.msDelay(1000);
		//calibrate black
		black = c.calBlack();
		Delay.msDelay(1000);
		//find average
		blackWhiteThreshold = c.getAvgEx();

		LCD.clear();
		LCD.drawString("white: " + white,0,1);
		LCD.drawString("black: " + black,0,2);
		LCD.drawString("threshold: " + blackWhiteThreshold,0,3);
		Button.waitForAnyPress();


		// Follow line until ESCAPE is pressed
		LCD.drawString("Press ESCAPE", 0, 4);
		LCD.drawString("to stop ", 0, 5);
		Delay.msDelay(1000);
		// get an instance of this sensor in measurement mode
		//SampleProvider lightDist = light.getAmbientMode();  //light without a source
		// Initialize an array of floats for fetching samples
		float[] lightSample = new float[lightDist.sampleSize()];

		LCD.clear();
		
		while (!Button.ESCAPE.isDown()) {
			lightDist.fetchSample(lightSample, 0);
			
			dist.fetchSample(sample, 0);
			distance = Math.min((int) (sample[0] * 100), 1000);
			
			// set various error values
			error = lightSample[0]*100 - blackWhiteThreshold;
			accumError += error;
			errorDiff = error - lastError;
			lastError = error;
			// set PID values
			double P = Kp * error;
			double I = Ki * accumError;
			double D = Kd * errorDiff;

			double turn = P + I + D;
						

			int upPower = (int) (Tp + turn);
			int downPower = (int) (Tp - turn);
			
			
			if ((int)(lightSample[0]*100) > error) {
				// On white, turn right
				LCD.drawString(rightString, 0, 1);
				left.controlMotor(upPower, forward);
				right.controlMotor(downPower, forward);
	
				//MotorPort.B.controlMotor(0, stop);
				//MotorPort.A.controlMotor(power, forward);
			} else {
				// On black, turn left
				LCD.drawString(leftString, 0, 1);
				left.controlMotor(downPower, forward);
				right.controlMotor(upPower, forward);

				//MotorPort.B.controlMotor(power, forward);
				//MotorPort.A.controlMotor(0, stop);
			}
			
			 if (sample[0]*100 < desiredDistance){
				 /*
				  * Maybe need to close ports, open as NXTRegulated to use old turn method?
				  * then close those and reopen as original type of TachoMotorPort as shown above
				  */
				 
				 left.controlMotor(0, stop);
				 right.controlMotor(0, stop);
				 
				 left.close();
				 right.close();
				 
				 lft = new NXTRegulatedMotor(brick.getPort("A"));
				 rght = new NXTRegulatedMotor(brick.getPort("B"));
				 
				 int degrees = (int)Math.round((((robotTrack*Math.PI)/2)/wheelCircumference)*360.0);
				 
				 lft.rotate(degrees,true);
				 rght.rotate(-degrees);
				 
				 LCD.clear();
				 
				 lft.close();
				 rght.close();
				 
				 if (switched == false){
					 left = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class); // right
					 right =(TachoMotorPort) MotorPort.A.open(TachoMotorPort.class); // left
					 
					 switched = true;
					 
					 //LCD.drawString("HERE", 0, 0);
				 } else {
					 left.close();
					 right.close();
					 
					 left = (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class);
					 right = (TachoMotorPort) MotorPort.B.open(TachoMotorPort.class);
					 
					 switched = false;
					 
					 //CD.drawString("HERE", 0, 0);
				 }
				 
			 }
			
			//LCD.drawInt(light.readValue(), 3, 9, 0);
			Delay.msDelay(10);

		}
		//light.close();
		// Stop car gently with free wheel drive
		left.controlMotor(0, flt);
		right.controlMotor(0, flt);
		//MotorPort.B.controlMotor(0, flt);
		//MotorPort.A.controlMotor(0, flt);
		LCD.clear();
		LCD.drawString("Program stopped", 0, 0);
		Delay.msDelay(1000);
		left.close();
		right.close();
	}
}