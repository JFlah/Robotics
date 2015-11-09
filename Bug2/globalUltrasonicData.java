public class globalUltrasonicData {
	//protected int distance;
	protected int SonicValue;

	public globalUltrasonicData() {
		this.SonicValue = 0;
	}

	public synchronized int getSonicValue() {
		return SonicValue;
	}
	public synchronized void setUltraValue(int value) {
		SonicValue = value;
	}
}