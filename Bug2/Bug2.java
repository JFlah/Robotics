/*
 * 
 * HW4
 * Jack Flaherty
 * 
 */

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.utility.Delay;

public class Bug2 {
	private static globalUltrasonicData sonicData = new globalUltrasonicData();
	private static NXTUltrasonicSensor us = new NXTUltrasonicSensor(SensorPort.S2);
	
	private static globalUltrasonicData sonicData2 = new globalUltrasonicData();
	private static NXTUltrasonicSensor us2 = new NXTUltrasonicSensor(SensorPort.S1);

		private static double wheelDiameter = 5.6;
		private static double trackWidth = 13.9;
	
		@SuppressWarnings("deprecation")
		private static DifferentialPilot pilot = new DifferentialPilot(
				wheelDiameter, trackWidth, Motor.B, Motor.A);
		private static Navigator nav = new Navigator(pilot);	

		private static OdometryPoseProvider opp = new OdometryPoseProvider(pilot);
		
		//some waypoints
		private static Waypoint finish = new Waypoint(200, 0, 0);
		
		public static boolean onTheLine(float x, float y, float b, float m){
			float test_y = m*x + b;
			if(test_y >= y-5 && test_y <= y+5){ // See if we are on/near the line
				LCD.drawString("On Line", 0, 6);
				Sound.systemSound(true, 3);
				return true;
			}
			LCD.drawString("Off Line", 0, 0);
			return false;
			
		}
		
		
		@SuppressWarnings("deprecation")
		public static void main(String[] args) {
			float[] points = new float[opp.sampleSize()];
			int count = 0;

			Thread u = new Thread(new sonicThread(sonicData, us));
			u.setDaemon(true);
			u.start();
			
			Thread u2 = new Thread(new sonicThread(sonicData2, us2));
			u2.setDaemon(true);
			u2.start();

			// establish a fail-safe: pressing Escape quits
			Brick brick = BrickFinder.getDefault(); // get specifics about this
			// robot
			brick.getKey("Escape").addKeyListener(new KeyListener() {
				
				public void keyPressed(Key k) {
					pilot.stop();
					us.close();
					Motor.A.close();	
					Motor.B.close();
					System.exit(1);
				}

				
				public void keyReleased(Key k) {
					System.exit(1);
				}
			});
			
			pilot.setLinearSpeed(10);  //sets straight ahead speed
			pilot.setAngularSpeed(50); //sets turning speed
			pilot.addMoveListener(opp);  //adds the listerner to capture x,y,heading values
			
			nav.goTo(finish);
			int distance = sonicData.getSonicValue();
			int distance2 = sonicData2.getSonicValue();
			LCD.drawString("Starting Up", 0, 1);
			pilot.steer(0);
			
			while ( true ){ // While going
				while (true){
					distance2 = sonicData2.getSonicValue();
					LCD.drawString(String.valueOf(distance2), 0, 1);
					if (distance2 < 10) {
						LCD.drawString("Wall Hit", 0, 1);
						nav.stop();  // And pilot stop
						break;
					}
					if (nav.pathCompleted()){
						LCD.drawString("Done :)", 0, 0);
						Sound.systemSound(true, 3); // Good
						System.exit(1);
					}
				}
				opp.fetchSample(points, 0);
				float m = (finish.y - points[1])/(finish.x - points[0]);
				float b = points[1] - m*points[0];
				pilot.rotate(90);
				LCD.drawString("Wall Hug", 0, 1);
				pilot.travel(15);
				while (true) {
					pilot.steer(0);
					opp.fetchSample(points, 0);
					distance = sonicData.getSonicValue();
					
					while(!onTheLine(points[0],points[1], b, m)){
						distance = sonicData.getSonicValue();

						if (distance < 20) { // When hugging wall still
								LCD.drawString("Follow", 0, 1);
								opp.fetchSample(points, 0);
								distance = sonicData.getSonicValue();
							}
						else {
								Sound.systemSound(true, 3);
								pilot.stop();  //or use nav.stop()
								LCD.drawString("Cornering", 0, 1);
								pilot.travel(10);
								pilot.rotate(-90); // right turn with -
								pilot.travel(20);
								distance = sonicData.getSonicValue();
								pilot.forward();
							}
						opp.fetchSample(points, 0);
					}
					LCD.drawString("On Line", 0, 4);
					pilot.rotate(90);
					nav.goTo(finish);
					break;
					}
				}
			}
		}