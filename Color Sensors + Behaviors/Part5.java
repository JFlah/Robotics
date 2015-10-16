//import globalUltrasonicData;
//import sonicThread;

/*
 * 
 * HW2 Part 5
 * Author: Jack Flaherty
 * 
 */

import java.io.File;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.hardware.sensor.NXTColorSensor;
import lejos.hardware.sensor.NXTTouchSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

public class Part5 {
	static NXTUltrasonicSensor ultra = new NXTUltrasonicSensor(SensorPort.S2);
	
	static NXTTouchSensor touch = new NXTTouchSensor(SensorPort.S1);
	
	static private int distance, leftD, rightD;
	
	static Brick brick = BrickFinder.getDefault();
	static NXTRegulatedMotor left = new NXTRegulatedMotor(brick.getPort("B"));
	static NXTRegulatedMotor right = new NXTRegulatedMotor(brick.getPort("A"));
	
	static globalData touchedData = new globalData(); 
	static globalUltrasonicData sonicData = new globalUltrasonicData();


	public static boolean finish = false;
	
	public static void main(String[] args) {
		
		
		
		// TODO Auto-generated method stub
		Thread t = new Thread(new touchThread(touchedData, touch)); // create
		// touch
		// thread
		t.setDaemon(true); // when the main thread terminates, the created
		// thread terminates
		// if this is false, the created thread continues
		// when the main thread terminates
		t.start();
		// Delay.msDelay(5000);
		

		Thread u = new Thread(new sonicThread(sonicData, ultra));
		u.setDaemon(true); // when the main thread terminates, the created
		// thread terminates
		// if this is false, the created thread continues
		// when the main thread terminates
		u.start();
		// Delay.msDelay(5000);

		// establish a fail-safe: pressing Escape quits
		 // get specifics about this
		// robot
		
		 // left
		
		brick.getKey("Escape").addKeyListener(new KeyListener() {
			//@Override
			public void keyPressed(Key k) {
				finish = true;

				ultra.close();
				touch.close();
				//File ff = new File("starwars.wav");
				//try {
				//	Sound.playSample(ff, 100);
				//      }
				//catch (Exception ex) {}
				System.exit(1);
			}

		//	@Override
			public void keyReleased(Key k) {
				finish = true;
				System.exit(1);
				

			}
		});

		Behavior b1 = new Drive();
		Behavior b2 = new Turn();
		Behavior b3 = new touchWall(); //used to be avoid

		Behavior b4 = new Terminate();
		Behavior[] bArray = { b1, b2, b3,b4};
		Arbitrator arby = new Arbitrator(bArray);
		arby.start();
	}

}
