import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.utility.Delay;


public class Rescue7 {
	private static globalUltrasonicData sonicData = new globalUltrasonicData();
	//private static NXTUltrasonicSensor us = new NXTUltrasonicSensor(SensorPort.S2);
	public static Waypoint cup;
	public static Waypoint next;
	public static int cupX;
	public static int cupY;
	public static final Waypoint WEND1 = new Waypoint(45, 120, 90);
	public static final Waypoint WEND2 = new Waypoint(15, 90, 90);
	public static final Waypoint BEND1 = new Waypoint(15, 120, 90);
	public static final Waypoint BEND2 = new Waypoint(45, 90, 90);
	public static int cupHeading;
	public static Queue<Point> path = new LinkedList<Point>();
	
	public static final int WALL = 9999;
	
	public static final int GRID_SIZE = 5;
    static int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    static ArrayList<Point> obstacleList = new ArrayList<Point>();
	
	static DifferentialPilot pilot;
	static RegulatedMotor motor;
	static RegulatedMotor motorD;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {

		
		// Read in the point from the seeker and make the waypoint
		
		Socket s = null;
		ObjectInputStream b = null;
		Queue<Point> q = new LinkedList<Point>(); // Point next to ball with HEADING of ball
		DifferentialPilot pilot = new DifferentialPilot(7, 12.4, Motor.B, Motor.A);
	     PoseProvider pp;
	     Navigator nav = new Navigator(pilot);

	     pilot.setTravelSpeed(10);
	     pilot.setRotateSpeed(20);
	        
	     pp = nav.getPoseProvider();
	     
	     pilot.addMoveListener((MoveListener) pp);
	     
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
        
        //Delay.msDelay(10000);
	     
		for(int i = 0; i < 4; i++){
    	
      }
      
		//Sound.playTone(200, 200);
		System.out.println("Q size: " + q.size());
		Delay.msDelay(10000);
		
		pp.setPose(new Pose(15, 15, 0 ));
		// Make the grid
        int grid[][] = createGrid();
        //Sound.playTone(200, 200);

        setGridVals(grid);
        //Sound.playTone(200, 200);

        RegulatedMotor motor = Motor.C;
	    motor.setSpeed(40);
	    RegulatedMotor motorD = Motor.D;
	    motorD.setSpeed(40);
	     
	     
	     int distance;
	     //Thread u = new Thread(new sonicThread(sonicData, us));
	     //u.setDaemon(true);
	     //u.start();
		
		int black = 0;
		int white = 0;
		
        while (q.size() > 0) {
        	Point curr_cup = q.remove();
        	cupX = curr_cup.getY()*30+15;
        	cupY = curr_cup.getX()*30+15;
        	cupHeading = curr_cup.getHeading();
          
        	cup = new Waypoint(cupX, cupY, cupHeading);
        	
        	nav.goTo(cup);
        	nav.waitForStop();
        	
        		
        		pilot.travel(17);
        	
            	if(grid[cupY/30][cupX/30] == 0 && white == 0){
            		
            		//pilot.travel(20);
            		
            		motor.rotate(150, true);
            		motorD.rotate(150);
            		pilot.travel(-17);

            		nav.goTo(WEND1);
            		
            		motor.waitComplete();
            		motorD.waitComplete();
            		
            		white++;
            		
            		if(nav.waitForStop()){
            			pilot.travel(13);
            			motor.rotate(-150, true);
                		motorD.rotate(-150);
                    //nav.goTo(45, 130);
                		pilot.travel(-13);
            		}
           
            	}else if(grid[cupY/30][cupX/30]== 0 && white == 1){
            		
            		//pilot.travel(20);
            		
            		motor.rotate(150, true);
            		motorD.rotate(150);
            		
            		pilot.travel(-17);

            		nav.goTo(WEND2);
            		
            		motor.waitComplete();
            		motorD.waitComplete();
            		
            		if(nav.waitForStop()){
            			pilot.travel(13);
            			motor.rotate(-150, true);
                		motorD.rotate(-150);
                    //nav.goTo(15, 100);
                		pilot.travel(-13);
            		}
        		
            	}else if(grid[cupY/30][cupX/30]== 1 && black == 0){

            		//pilot.travel(20);
    
            		motor.rotate(150, true);
            		motorD.rotate(150);
            		
            		pilot.travel(-17);

            		nav.goTo(BEND1);
            		
            		motor.waitComplete();
            		motorD.waitComplete();
            		
            		black++;
            		
            		if(nav.waitForStop()){
            			pilot.travel(13);
            			motor.rotate(-150, true);
                		motorD.rotate(-150);
                    //nav.goTo(15, 130);
                		pilot.travel(-13);
            		}
            	}else{

            		//pilot.travel(20);
            		
            		motor.rotate(150, true);
            		motorD.rotate(150);

            		pilot.travel(-17);
            		
            		nav.goTo(BEND2);
            		
            		motor.waitComplete();
            		
            		if(nav.waitForStop()){
            			pilot.travel(13);
            			motor.rotate(-150, true);
                		motorD.rotate(-150);
                    //nav.goTo(45, 100);
                		pilot.travel(-13);
            		}
            	}
        }
        motor.close();
        motorD.close();
        Sound.playTone(200, 200);
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
//    public static void readArray() throws FileNotFoundException {
//        Scanner in = new Scanner(new FileReader("obstacles3.txt"));
//
//        while (in.hasNextInt()) {
//            int x = in.nextInt();
//            int y = in.nextInt();
//
//            Point ob = new Point(x, y);
//            obstacleList.add(getObstacleCell(ob));
//        }
//        in.close();
//    }
    
    public static int[][] setGridVals(int[][] grid){
        // Set obstacles
        boolean black = true;
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (black) { grid[y][x] = 0; black = false; }
                else { grid[y][x] = 1; black = true; }
            }
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

