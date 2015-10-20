public class globalColorData {

	protected int colorValue;

	public globalColorData() {
		this.colorValue = 0;
	}

	public synchronized int getColorValue() {
		return colorValue;
	}
	public synchronized void setColorValue(int value) {
		colorValue = value;
	}
}
