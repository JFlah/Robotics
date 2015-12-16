import lejos.hardware.sensor.HiTechnicEOPD;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;



public class sonicThread implements Runnable{
	NXTUltrasonicSensor us;
	globalUltrasonicData data;
	
	public sonicThread(globalUltrasonicData data, NXTUltrasonicSensor us) {
		this.data = data;
		this.us = us;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	//@Override
	public void run() {

		SampleProvider sonicSampleProvider = us.getDistanceMode();
		float[] s1 = new float[sonicSampleProvider.sampleSize()];
		while (true) {
			try {
				sonicSampleProvider.fetchSample(s1, 0);
			      }
			catch (Exception ex) {}
			data.setUltraValue((int)(s1[0]*100));
		}
		
	
		
	}

}