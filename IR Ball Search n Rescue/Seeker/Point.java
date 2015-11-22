import java.io.Serializable;

/**
 * Created by Jack on 11/16/2015.
 */
public class Point implements Serializable{
    private int x;
    private int y;
    private int heading;

    /* ALGO:
        q.add(goal);
        q.remove first guy in queue (sort of redundant but part of algo)
        CHECK NESW: add if possible
        check every direction and make sure in bounds
        then check if its -1 (legit point in grid)
        q.add(south or whatever point checked)
        change that points value (Currently -1) to parent +1 = 1
        then check North guy if possible, and increase it by parent + 1 like the south guy
        then loop back and remove first guy in queue and repeat (FIFO)
     */


    public Point(int x, int y){
        this.x=x;
        this.y=y;
        this.heading=0;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
    
    public int getHeading() {
    	return this.heading;
    }
    public void setHeading(int heading) {
    	this.heading = heading;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}