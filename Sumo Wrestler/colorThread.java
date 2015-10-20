import lejos.hardware.sensor.NXTColorSensor;
import lejos.robotics.SampleProvider;

public class colorThread implements Runnable{
	NXTColorSensor color;
	globalColorData gcd;
	
	public colorThread(globalColorData gcd, NXTColorSensor color) {
		this.gcd = gcd;
		this.color = color;
	}
	
	public void run() {
		SampleProvider colorSampleProvider = color.getRGBMode();
		float[] s1 = new float[colorSampleProvider.sampleSize()];
		while (true) {
			try {
				colorSampleProvider.fetchSample(s1, 0);
			      }
			catch (Exception ex) {}
			gcd.setColorValue((int)(s1[0]*100));
		}
		
	}

}
