import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
//import lejos.robotics.pathfinding.Path;
import lejos.utility.Delay;

/**
 * Created by Jack on 11/16/2015.
 */
public class Seek7 {
	
	private static globalUltrasonicData sonicData = new globalUltrasonicData();
	// front sensor
	private static NXTUltrasonicSensor us = new NXTUltrasonicSensor(SensorPort.S2);
	
	private static double wheelDiameter = 5.6;
	private static double trackWidth = 13.2;
	
	private static globalUltrasonicData sonicData2 = new globalUltrasonicData();
	// right sensor
	private static NXTUltrasonicSensor us2 = new NXTUltrasonicSensor(SensorPort.S1);
	
    static Point next;
    static Queue<Point> path = new LinkedList<Point>();
    public static final int GRID_SIZE = 5;
    static int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    
    // Warehouse cells
    static Point black2 = new Point(30,90); // top left
    static Point black1 = new Point(0,120); // bottom right
    static Point white2 = new Point(30,120); // top right
    static Point white1 = new Point(0,90); // bottom left
    
    //DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, trackWidth, Motor.B, Motor.A);
    //static PoseProvider pp;  //need this since the new version of nav has a built in odometry provider
    
    public static Waypoint nextWay;
	public static int oldX;
	public static int oldY;
	public static int newX;
	public static int newY;

	public static float seek;
	
	public static Point sendPoint;
	public static Queue<Point> sendQ = new LinkedList<Point>();

    public static void main(String[] args) {
        // Make the grid
        int grid[][] = createGrid();

        // Set obstacle, goal, start values
        setGridVals(grid);

        // Test print of wavefront-ed grid
        // printGrid2(grid);

        // Create an arraylist of points from start (index 0) to goal (index list.length-1)
        getPath();

        // Display list
        //System.out.println(path.toString());
        
        // Seek
        //seek();
        
        // BEGIN SEEK =========================================================================================
        DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, trackWidth, Motor.B, Motor.A);
        Navigator nav = new Navigator(pilot);
        PoseProvider pp;
        
        OdometryPoseProvider opp = new OdometryPoseProvider(pilot);
    	
    	float[] points = new float[opp.sampleSize()];
		int count = 0;

		Thread u = new Thread(new sonicThread(sonicData, us));
		u.setDaemon(true);
		u.start();
		
		Thread u2 = new Thread(new sonicThread(sonicData2, us2));
		u2.setDaemon(true);
		u2.start();
		
		//pilot.setLinearSpeed(10); //sets straight ahead speed
		//pilot.setAngularSpeed(50); //sets turning speed
		pilot.addMoveListener(opp);  //adds the listerner to capture x,y,heading values
    	
    	int initial_size = path.size();
    	Point start = path.remove();
    	
    	oldX = start.getX()*30+15;
        oldY = start.getY()*30+15;
        nextWay = new Waypoint(oldX, oldY);
        nav.addWaypoint(nextWay);
        
        pp = nav.getPoseProvider();
        
        pp.setPose(new Pose(oldX, oldY, 0));
        
        for (int i = 0; i < initial_size-1; i++) {
        	
            Point next = path.remove();
            newX = next.getX()*30+15;
            newY = next.getY()*30+15;
//            if (Math.abs(newX-oldX) > 30 || Math.abs(newY-oldY) > 30){
//            	pilot.rotate(360);
//            	Delay.msDelay(8000);
//            	Sound.playTone(500, 1000);
//            	System.exit(1);
//            	break;
//            }
            int nextX = next.getX()*30+15;
            int nextY = next.getY()*30+15;

            nextWay = new Waypoint(nextX, nextY);
            nav.addWaypoint(nextWay);
            
            oldX = newX;
            oldY = newY;
        }
        
        pilot.setRotateSpeed(20);
        pilot.setTravelSpeed(10);
        nav.followPath();
   
        // The seeker is now moving, check for ball on left and right frequently
        int cnt = 0;

        while (!nav.pathCompleted()) {
            
        	LCD.drawString(sendQ.toString(), 1, 3);
        	LCD.drawInt(sendQ.size(), 1, 4);
        	nav.followPath();
    			//Delay.msDelay(1500);
    			int front_distance = sonicData.getSonicValue();
    			int right_distance = sonicData2.getSonicValue();
    			
    			//Delay.msDelay(30);
    			
    			if ((front_distance < 23 || right_distance < 20) && cnt < 4){
    				//Delay.msDelay(2000);
    				int cntCurrent = 0;
    				us.disable();
    				us2.disable();
    				
    				cnt++;
    				
    				Waypoint ourWay = nav.getWaypoint();
    				//int x = (int) (ourWay.getX()-15)/30;
    				//int y = (int) (ourWay.getY()-15)/30;
    				

    				
    				if (front_distance < 23 && cntCurrent == 0){ // cup in front
    					nav.stop();
    					//LCD.drawString("Cup in front", 1, 1);
    					//x++; // to make point of actual cup 1 up from us
    					
        				int x = (int) opp.getPose().getX()/30;
        				int y = (int) opp.getPose().getY()/30;
        				
        				//int curHeading = (int) opp.getPose().getHeading()/30*30;
//        				if (curHeading == 60) {
//        					curHeading = 90;
//        				}
//        				
        				int curHeading;
        				
        				int headingNow = (int) opp.getPose().getHeading();
        				if (headingNow > -5 && headingNow < 20) {
        					curHeading = 0;
        				}
        				else if (headingNow > 84 && headingNow < 120) {
        					curHeading = 90;
        				}
        				else if (headingNow > 174 && headingNow < 198) {
        					curHeading = 180;
        				}
        				else if (headingNow > 260 && headingNow < 290) {
        					curHeading = 270;
        				} else {
        					curHeading = 360;
        				}
    					
    					sendPoint = new Point(y, x);
    					
    					cntCurrent++;
    					
    					switch (curHeading) {
    						case 0: sendPoint.setHeading(0); break;
    						case 90: sendPoint.setHeading(90); break;
    						case 180: sendPoint.setHeading(180); break;
    						case 270: sendPoint.setHeading(270); break;
    						default: sendPoint.setHeading(-2); break;
    					}
    					
    					
    					LCD.drawString("X: " + sendPoint.getX() + " Y: " + sendPoint.getY(), 2, 2);
    					LCD.drawString("h" + (int) opp.getPose().getHeading()/30*30, 2, 0);
    					LCD.drawString("Heading: " + sendPoint.getHeading(), 2, 1);
    					if (!sendQ.contains(new Point(y,x)) && cntCurrent == 1) {
    						Sound.playTone(500, 200);
    						sendQ.add(sendPoint);
    						us.disable();
    						us2.disable();
    						//Delay.msDelay(2000);
    						us.enable();
    						us2.enable();
    						
    					}
    					
    					// Go around the cup in front
    					us2.disable();
    					
    					//Delay.msDelay(8000);
    					pilot.rotate(-90);
    					pilot.travel(30);
    					pilot.rotate(90);
    					pilot.travel(63);
    					pilot.rotate(90);
    					pilot.travel(30);
    					pilot.rotate(-90);
    					pilot.travel(7);
    				
    					continue;
    					//}
    				}
    				else if (right_distance < 20 && cntCurrent == 0) { // to right
    					Delay.msDelay(1500);
    					//y++; // to make point of actual cup 1 right of us
    					//x++;
    					
        				int x = (int) opp.getPose().getX()/30;
        				int y = (int) opp.getPose().getY()/30;
        				
        				int curHeading;
        				
        				int headingNow = (int) opp.getPose().getHeading();
        				if (headingNow > -5 && headingNow < 20) {
        					curHeading = 0;
        				}
        				else if (headingNow > 84 && headingNow < 120) {
        					curHeading = 90;
        				}
        				else if (headingNow > 174 && headingNow < 198) {
        					curHeading = 180;
        				}
        				else if (headingNow > 260 && headingNow < 290) {
        					curHeading = 270;
        				} else {
        					curHeading = 360;
        				}
        				
        				//curHeading = (int) opp.getPose().getHeading()/90*90;
    					
    					sendPoint = new Point(y, x);
    					
    					cntCurrent++;
    					
    					switch (curHeading) {
							case 0: sendPoint.setHeading(90); break;
							case 90: sendPoint.setHeading(180); break;
							case 180: sendPoint.setHeading(270); break;
							case 270: sendPoint.setHeading(0); break;
							default: sendPoint.setHeading(-2); break;
    					}	
    					
    					LCD.drawString("X: " + sendPoint.getX() + " Y: " + sendPoint.getY(), 2, 2);
    					LCD.drawString("h" + (int) opp.getPose().getHeading()/90*90, 2, 0);
    					LCD.drawString("Heading: " + sendPoint.getHeading(), 2, 1);
    					if (!sendQ.contains(new Point(y,x)) && cntCurrent == 1) {
    						Sound.playTone(500, 200);
    						sendQ.add(sendPoint);
    						Delay.msDelay(200);
    					}
    					continue;
    				}	
    			}
    			us.enable();
    			us2.enable();
        }
        
        Sound.playTone(320, 500);
        
        // END SEEK ==================================================================================================
        
//        Point testSend = new Point(0, 2);
//        testSend.setHeading(0);
//        Point testSend1 = new Point(1, 4);
//        testSend1.setHeading(90);
//        Point testSend2 = new Point(4, 4);
//        testSend2.setHeading(180);
//        Point testSend3 = new Point(2, 2);
//        testSend3.setHeading(0);
//        sendQ.add(testSend);
//        sendQ.add(testSend1);
//        sendQ.add(testSend2);
//        sendQ.add(testSend3);

        LCD.drawString("1stH" + sendQ.peek().getHeading(), 3, 3);
        
        // Send point of ball to robot
        try {
            sendPoints(sendQ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Make our 2D grid
    public static int[][] createGrid(){
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                grid[y][x] = -1;
            }
        }
        return grid;
    }
    
    // Start looking!
	public static void seek(){
    	
    	
    }

    public static int[][] setGridVals(int[][] grid){
        boolean black = true;
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (black) { grid[y][x] = 0; black = false; }
                else { grid[y][x] = 1; black = true; }
            }
        }
        return grid;
    }

    public static void sendPoints(Queue<Point> q) throws IOException {
        Socket t = null;
        ObjectOutputStream p = null;                       //create an Object stream
        ServerSocket s = new ServerSocket(5000); // create a TCP socket, this is overkill
        System.out.println("Server started");
        t = s.accept();                                               // wait for client (robot) to connect
        System.out.println("server connected");
        p = new ObjectOutputStream(t.getOutputStream());  //wrap the output stream with an object output stream
        p.writeObject(q);                                                        //so we can write the entire serializable  object
        p.flush();                                                                     //flush it all out
        p.close();
        t.close();
        s.close();
    }

    public static Queue<Point> getPath() {;
    	/*
    	 * [(0,0) -> (4,0) -> (4,4) -> (2,4) -> (2,2) -> (0,2) -> (-2,2)]
    	 */
    
    
        // Up left edge to top left corner
        path.add(new Point(0,0));
        path.add(new Point(4,0));

        // Over the top to top right corner
        path.add(new Point(4, 4));

        // Down right edge to bottom right corner above warehouse then over top of warehouse
        path.add(new Point(2,4));
        path.add(new Point(2,2));
        
        // Down to bottom edge of grid to the left of the warehouse
        path.add(new Point(0,2));

        // All cells have now been checked

        // Exit grid
        path.add (new Point(-2,2));

        return path;
    }

    public static void printGrid2(int[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                System.out.printf("%5d", grid[i][j]);
            }
            System.out.println();
        }
    }
    public static Point getObstacleCell(Point obstacle) {
        int x = obstacle.getX()/30*30;
        int y = obstacle.getY()/30*30;

        return new Point(x, y);
    }
}