import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.NXTColorSensor;
import lejos.robotics.SampleProvider;

public class Calibrate {
	
	int white;
	int black;
	int avg;
	
	SampleProvider lightDist;
	
	public Calibrate (SampleProvider ls) {
		//this.ls = ls;
		this.lightDist = ls;
	}
	public int calWhite(){
		int sum=0, cnt=0;
		LCD.clear();
		LCD.drawString("Press LEFT", 0, 2);
		LCD.drawString("to stop calibrate", 0, 3);
		LCD.drawString("white", 0, 4);
		
		float[] lightSample = new float[lightDist.sampleSize()];
		while (!Button.LEFT.isDown()) {
			lightDist.fetchSample(lightSample, 0);
			sum = sum + (int)(lightSample[0]*100);
			cnt++;
			LCD.drawInt((int)(lightSample[0]*100), 0, 5);
		}

		white = sum/cnt;
		return white;
	}
	
	public int calBlack(){
		int sum=0, cnt=0;
		LCD.clear();
		LCD.drawString("Press RIGHT", 0, 2);
		LCD.drawString("to stop calibrate", 0, 3);
		LCD.drawString("black", 0, 4);
		
		float[] lightSample = new float[lightDist.sampleSize()];
		while (!Button.RIGHT.isDown()) {
			lightDist.fetchSample(lightSample, 0);
			LCD.drawInt((int)(lightSample[0]*100), 0, 5);
			sum = sum + (int)(lightSample[0]*100);
			cnt++;
		}
		black = sum/cnt;
		return black;
	}
	public int getAvgEx(){
		return (white + black)/2;
	}
}