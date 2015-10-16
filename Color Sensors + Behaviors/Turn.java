import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.TachoMotorPort;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;



public class Turn implements Behavior {
	private int distance;
	static double wheelDiameter = 5.6;
	static double robotTrack = 13.9;
	private boolean suppressed = false;
	private boolean isRunning1 = false;

	public boolean takeControl() {
		distance = Part5.sonicData.getSonicValue();
		if(distance> 30){
			return true;
		}
		return false;
	}

	public void suppress() {
		suppressed = true;
	}

	public void action() {
		suppressed = false;
		isRunning1 = true;
		double wheelCircunference = wheelDiameter * Math.PI;
		Part5.left.flt(true);
		Part5.right.flt();
		
		Delay.msDelay(1000);
		
		int Tp = 100;
		Part5.left.setSpeed(Tp);
		Part5.right.setSpeed(Tp);
		Part5.left.rotate(180,true);
		Part5.right.rotate(180);
		Delay.msDelay(1000);

			
		int degrees=(int)Math.round((((robotTrack*Math.PI)/4)/wheelCircunference)*360.0);
		//Part5.left.startSynchronization();
		Part5.left.rotate((int)(degrees-25),true);
		Part5.right.rotate((int)(-degrees+25));
		//Part5.left.endSynchronization();
		Part5.left.waitComplete();
		Part5.right.waitComplete();
		Delay.msDelay(2000);
		Part5.left.rotate(400,true);
		Part5.right.rotate(400);
		Part5.left.waitComplete();
		Part5.right.waitComplete();
		while (!suppressed && Part5.left.isMoving() && Part5.right.isMoving()){
			Thread.yield();
	}
		isRunning1 = false;
		Part5.left.forward();
		Part5.right.forward();
		suppress();
//			Part5.left.close();
//			Part5.right.close();
//			
//			TachoMotorPort Part5.left = (TachoMotorPort) MotorPort.A.open(TachoMotorPort.class); // Part5.right
//			TachoMotorPort Part5.right =(TachoMotorPort) MotorPort.B.open(TachoMotorPort.class);
//			
			
	
	}
}


