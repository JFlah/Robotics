import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.pathfinding.Path;
import lejos.utility.Delay;

/**
 * Created by Jack on 11/4/2015.
 */


public class Robot {
	private static Waypoint nextWay;
	private static int oldX;
	private static int oldY;
	private static int newX;
	private static int newY;
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
        Socket s = null;
        ObjectInputStream b = null;
        Queue<Point> q = new LinkedList<Point>();
        DifferentialPilot pilot = new DifferentialPilot(5.6, 13.9, Motor.B, Motor.A);
        PoseProvider pp;  //need this since the new version of nav has a built in odometry provider
        Navigator nav = new Navigator(pilot);

        pilot.setTravelSpeed(10);
        pilot.setRotateSpeed(20);

        pp = nav.getPoseProvider();  //needed to set initial pose
        pilot.addMoveListener((MoveListener) pp);
        try {
            s = new Socket("10.200.11.177", 5000);    //create socket with laptop ip and port number
            b = new ObjectInputStream(s.getInputStream());
            q = (Queue<Point>) b.readObject();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            b.close();
            s.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            b.close();
            s.close();
        } catch (IOException e) {
            b.close();
            s.close();
            e.printStackTrace();
        }
        b.close();
        s.close();

        //now you have the entire queue which is the path from start to goal.

        //you need to write the remaining code to create waypoints and start the navigation.

        //the first point in the queue should be the the start position, and you need to set that with a

        //a set pose as such:
        int initial_size = q.size();
        System.out.println("Q size: " + initial_size);
        
        Point start = q.remove();
        oldX = start.getX()*30+15;
        oldY = start.getY()*30+15;
        nextWay = new Waypoint(start.getX()*30+15, start.getY()*30+15, 0);
        nav.addWaypoint(nextWay);

        pp.setPose(new Pose(start.getX()*30+15, start.getY()*30+15, 0));  //this sets the start position to be the start position with heading 0 degrees


        // i then remove each point off the queue and add this as a waypoint, using 0 degrees as a heading.
        int heading = 0;
        int North = 0;
        int East = 1;
        int South = 2;
        int West = 3;
        int lastHeading = North;
        for (int i = 0; i < initial_size-1; i++) {
        	//Sound.playTone(250, 500);
            Point next = q.remove();
            newX = next.getX()*30+15;
            newY = next.getY()*30+15;
            if (Math.abs(newX-oldX) > 30 || Math.abs(newY-oldY) > 30){
            	pilot.rotate(360);
            	Delay.msDelay(8000);
            	Sound.playTone(500, 1000);
            	System.exit(1);
            	break;
            }
            int nextX = next.getX()*30+15;
            int nextY = next.getY()*30+15;
            
            if (oldX - newX == -30){ // North
            	System.out.println("North");
            	if (lastHeading == East) {
            		heading = -0;
            	}
            	if (lastHeading == South){
            		heading = 0;
            	}
            	if (lastHeading == West) {
            		heading = 0;
            	}
            	lastHeading = North;
            }
            if (oldX - newX == 30) { // South
            	System.out.println("South");
            	if (lastHeading == North){
            		heading = 180;
            	}
            	if (lastHeading == East){
            		heading = 180;
            	}
            	if (lastHeading == West) {
            		heading = -180;
            	}
            	lastHeading = South;
            }
            if (oldY - newY == -30) { // East
            	System.out.println("East");
            	if (lastHeading == North){
            		heading = 90;
            	}
            	if (lastHeading == South) {
            		heading = 90;
            	}
            	if (lastHeading == West){
            		heading = 90;
            	}
            	lastHeading = East;
            }
            if (oldY - newY == 30) { // West
            	System.out.print("West");
            	if (lastHeading == North){
            		heading = -90;
            	}
            	if (lastHeading == East){
            		heading = 270;
            	}
            	if (lastHeading == South){
            		heading = 270;
            	}
            	lastHeading = West;
            }

            nextWay = new Waypoint(nextX, nextY, heading);
            nav.addWaypoint(nextWay);
            
            oldX = newX;
            oldY = newY;
        }
        Path path = nav.getPath();
        //System.out.println("Path: " + path.toString());
        nav.followPath();
        while (!nav.pathCompleted()) {
        	Delay.msDelay(1000);
        }

        Sound.playTone(320, 500);
    }

}
