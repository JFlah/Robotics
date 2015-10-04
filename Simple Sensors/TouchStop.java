/*
 * LAB 1 PART 2
 * Author: Jack Flaherty
 */

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.NXTTouchSensor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;
//bc: 136.167.209.221
//home: 192.168.1.15

public class TouchStop {
	public static NXTTouchSensor ts = new NXTTouchSensor(SensorPort.S1);
	static double wheelDiameter = 5.6;
	static double robotTrack = 16.7;
	static RegulatedMotor left ;
	static RegulatedMotor right ;
	static float turnRadius = (float) wheelDiameter / 2;  //wheel diameter is 5.6 cm (wheel is marked 56 mm)
 
	public static void main(String[] args) {
		double wheelCircunference = wheelDiameter * Math.PI;
		float degrees;

		Brick brick = BrickFinder.getDefault(); // get specifics about this
												// robot
		left = new NXTRegulatedMotor(brick.getPort("A"));
		right = new NXTRegulatedMotor(brick.getPort("B"));


		globalData touchedData = new globalData(); // set up global data for
													// touch thread
		Thread t = new Thread(new touchThread(touchedData, ts)); // create touch
																	// thread
		t.setDaemon(true); // when the main thread terminates, the created
							// thread terminates
							// if this is false, the created thread continues
							// when the main thread terminates
		t.start();
		// Delay.msDelay(5000);
		
		LCD.drawString("Touch Test", 0, 0); // Writes program name to LCD
		
		//first move
		Button.waitForAnyPress(); // Waits for button press

		
		 // establish a fail-safe: pressing Escape quits
        brick.getKey("Escape").addKeyListener(new KeyListener() {
                public void keyPressed(Key k) {
    				left.stop(true);
    				right.stop();
                   	ts.close();
        				left.close();
        				right.close();
                        System.exit(1);
                }

                public void keyReleased(Key k) {
    				left.stop(true);
    				right.stop();
                	ts.close();
    				left.close();
    				right.close();
                    System.exit(1);
                }
        });
        
		left.setSpeed(360);
		right.setSpeed(360);
		left.synchronizeWith(new RegulatedMotor[] { right });

		left.forward();
		right.forward();
		//int i = 0;

		while (true){

			//System.out.println("d[" + i + "]: " + touchedData.getTouched());
			if (touchedData.getTouched()) {//what to do if touched?
				//put your code here, don't forget to reset the global variable to false
				//think how to do this
				left.stop(true);
				right.stop();
				
				left.startSynchronization();
				left.rotate(-200,true);
				right.rotate(-200);
				left.endSynchronization();
				
				left.waitComplete();
				right.waitComplete();
				
				left.resetTachoCount();
				right.resetTachoCount();
				
				Sound.systemSound(true, 3);
				degrees=(int)Math.round((((robotTrack*Math.PI)/3)/wheelCircunference)*360.0);
				LCD.drawString("deg: " + degrees, 0, 3);
				left.startSynchronization();
				left.rotate((int)(-degrees-90), true);
				right.rotate((int)degrees+90);
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
			//i++;
		}
	}

}