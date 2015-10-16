import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

import java.io.File;
import java.util.Random;

//import src.Animal3;

public class Drive implements Behavior {
	static double wheelDiameter = 5.6;
	static double robotTrack = 13.9;
	private boolean suppressed = false;

	public boolean takeControl() {
		return true;
	}

	public void suppress() {
		suppressed = true;
	}

	public void action() {
		suppressed = false;
		while (!suppressed) {
			
	
			final int desiredDistance = 8;

			// initialize PID constants
			final double Kp = .8;
			final double Ki = 0.000001;
			final double Kd = 2;

			// unaltered turn power
			final int Tp = 100;

			double error;
			double accumError = 0;
			double lastError = 0;
			double errorDiff;
			int distance;
			
			
			
//			if(distance>60 || Part5_main.touchedData.getTouched()){
//				suppress();
//			}
			
	
			Part5.left.setSpeed(Tp);
			Part5.right.setSpeed(Tp);
			Part5.left.forward();
			Part5.right.forward();

			while (!suppressed && Part5.left.isMoving() && Part5.right.isMoving()  ) {
				distance = Part5.sonicData.getSonicValue();
				LCD.drawString(Integer.toString(distance), 0, 0);
				
				error = distance - desiredDistance;
				accumError += error;
				errorDiff = error - lastError;
				lastError = error;
				if (errorDiff >= 30 || accumError>=30){
					errorDiff = 0;
					accumError = 0;
				}
					// set PID values
				double P = Kp * error;
				double I = Ki * accumError;
				double D = Kd * errorDiff;

				double turn = P + I + D;
				
				int upPower = (int) (Tp + turn);
				int downPower = (int) (Tp - turn);
				if (error > 0) {
					Part5.left.setSpeed(upPower);
					Part5.right.setSpeed(downPower);
					}
				else if(error<0) {
					Part5.right.setSpeed(downPower);
					Part5.left.setSpeed(upPower);
					}
				Part5.left.forward();
				Part5.right.forward();
				Thread.yield();
			}
			
		
	}
	}
}


