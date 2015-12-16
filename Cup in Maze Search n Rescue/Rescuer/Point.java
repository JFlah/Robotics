import java.io.Serializable;

/**
 * Created by Jack on 11/16/2015.
 */
public class Point implements Serializable{
    private int x;
    private int y;
    private int heading;

    public Point(int x, int y){
        this.x=x;
        this.y=y;
        this.heading = -1;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
    
    public int getHeading(){
    	return this.heading;
    }
    
    public void setHeading(int heading) {
    	this.heading = heading;
    }
    
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}