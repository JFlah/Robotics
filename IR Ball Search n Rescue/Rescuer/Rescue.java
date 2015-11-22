/*
 * IP: 10.200.11.178
 */

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;

/**
 * Created by Jack on 11/16/2015.
 */

public class Rescue {
	
	public static Waypoint ball;
	public static Waypoint next;
	public static int ballX;
	public static int ballY;
	public static int nextX;
	public static int nextY;
	public static int ballHeading;
	public static Queue<Point> path = new LinkedList<Point>();
	
	public static final int WALL = 9999;
	
	public static final int GRID_SIZE = 6;
    static int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    static ArrayList<Point> obstacleList = new ArrayList<Point>();
	
	static DifferentialPilot pilot;
	static RegulatedMotor motor;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		
		// Make the grid
        int grid[][] = createGrid();

        // Filing in the obstacle list
        try {
            readArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Set obstacle, goal, start values
        setGridVals(grid, obstacleList);
		
		// Do all the pilot, navigator, motor, poseprovider stuff
		
	     DifferentialPilot pilot = new DifferentialPilot(6.8, 13, Motor.B, Motor.A);
	     RegulatedMotor motor = Motor.C;
	     motor.setSpeed(40);
	     
	     PoseProvider pp;
	     Navigator nav = new Navigator(pilot);

	     pilot.setTravelSpeed(10);
	     pilot.setRotateSpeed(20);
	        
	     pp = nav.getPoseProvider();
	     
	     pilot.addMoveListener((MoveListener) pp);
		
		// Read in the point from the seeker and make the waypoint
		
		Socket s = null;
		ObjectInputStream b = null;
		Queue<Point> q = new LinkedList<Point>(); // Point next to ball with HEADING of ball
		
		try {
            s = new Socket("10.200.9.194", 5000);
            b = new ObjectInputStream(s.getInputStream());
            q =  (Queue<Point>) b.readObject();
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
        
        Point ballFound = q.peek();
        ballX = ballFound.getX()*30+15;
        ballY = ballFound.getY()*30+15;
        ballHeading = ballFound.getHeading();
        
        ball = new Waypoint(ballX, ballY, ballHeading);
		
        // Make the normal path it will follow until it reaches the waypoint from the seeker
        
        int curr_y = ballY;
        int curr_x = ballX;

        // Up first hallway
        for (int y = 0; y < 6; y++) {
            path.add(new Point(y, 1));
        }

        // Bridge over to second hallway
        path.add(new Point(5, 2));

        // Down second hallway
        for (int y = 5; y >= 0 ; y--) {
            path.add(new Point(y, 3));
        }

        path.add(new Point (0, 4));

        for (int y = 0; y < 8; y++) {
            path.add(new Point(y, 5));
        }
        
        Point start = path.remove();
    	
	    int oldX = start.getX()*30+15;
	    int oldY = start.getY()*30+15;
	     
	    pp.setPose(new Pose(oldX, oldY, 0));
        
        // Go from Points in our path to waypoints in our Navigator so bot can follow them
        
		for (Point nextPoint : path) {
			nextX = nextPoint.getX()*30+15;
			nextY = nextPoint.getY()*30+15;
			
			next = new Waypoint(nextX, nextY);
			nav.addWaypoint(next);
		}
        
        // Start following path until we are next to ball and rescue

		nav.followPath();
		int checkCount = 0;
        while (!nav.pathCompleted()) {
        	// NEXT TO THE BALL CHECK
            if(!nav.pathCompleted() && nav.getWaypoint().getX() == ball.getY()+30 && nav.getWaypoint().getY() == ball.getX() && checkCount < 1){
            	checkCount++;
            	Sound.playTone(500, 200);
            	nav.stop();
            	if(ball.getHeading() > 0){
            		Motor.A.resetTachoCount();
            		Motor.B.resetTachoCount();
            		
            		pilot.rotate(-90);
            		
            		Motor.A.resetTachoCount();
            		Motor.B.resetTachoCount();
            		
            		Motor.A.rotateTo(360, true);
            		Motor.B.rotateTo(360);
            		
            		Motor.A.waitComplete();
            		Motor.B.waitComplete();
            		
            		Motor.A.resetTachoCount();
            		Motor.B.resetTachoCount();
            		
            		motor.rotate(100);
            		
            		motor.waitComplete();
            		
            		Motor.A.rotateTo(-360, true);
            		Motor.B.rotateTo(-360);
            		
            		Motor.A.waitComplete();
            		Motor.B.waitComplete();
            		
            		Motor.A.resetTachoCount();
            		Motor.B.resetTachoCount();
            		
            		pilot.rotate(90);
            		
            	} else{
            		Motor.A.resetTachoCount();
            		Motor.B.resetTachoCount();
            		
            		pilot.rotate(90);
            		
            		Motor.A.resetTachoCount();
            		Motor.B.resetTachoCount();
            		
            		Motor.A.rotateTo(360, true);
            		Motor.B.rotateTo(360);
            		
            		Motor.A.resetTachoCount();
            		Motor.B.resetTachoCount();
            		
            		motor.rotate(100);
            		motor.waitComplete();
            		
            		Motor.A.resetTachoCount();
            		Motor.B.resetTachoCount();
            		
            		Motor.A.rotateTo(-360, true);
            		Motor.B.rotateTo(-360);
            		
            		Motor.A.waitComplete();
            		Motor.B.waitComplete();
            		
            		Motor.A.resetTachoCount();
            		Motor.B.resetTachoCount();
            		
            		pilot.rotate(-90);
            	}
            	nav.followPath();
            }
        }
        motor.close();
	}
	
	// Makes our 2D grid
    public static int[][] createGrid(){
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                grid[y][x] = -1;
            }
        }
        return grid;
    }
    
    // Reads in the obstacle file and fills in a list of obstacles
    public static void readArray() throws FileNotFoundException {
        Scanner in = new Scanner(new FileReader("obstacles3.txt"));

        while (in.hasNextInt()) {
            int x = in.nextInt();
            int y = in.nextInt();

            Point ob = new Point(x, y);
            obstacleList.add(getObstacleCell(ob));
        }
        in.close();
    }
    
    public static int[][] setGridVals(int[][] grid, ArrayList<Point> obstacles){
        // Set obstacles

        for (Point obstacle : obstacles) {
            int obX = obstacle.getX()/30;
            int obY = obstacle.getY()/30;
            grid[obY][obX] = WALL;
        }
        return grid;
    }
	
	// Helper fcn to convert from centimeters to a cell coordinate
    public static Point getObstacleCell(Point obstacle) {
        int x = obstacle.getX()/30*30;
        int y = obstacle.getY()/30*30;

        return new Point(x, y);
    }
}
