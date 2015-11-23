import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
//import lejos.robotics.pathfinding.Path;
import lejos.utility.Delay;
import lejos.hardware.sensor.HiTechnicIRSeekerV2;

/**
 * Created by Jack on 11/16/2015.
 */
public class Seek {
    static Point next;
    static ArrayList<Point> obstacleList = new ArrayList<Point>();
    static Queue<Point> path = new LinkedList<Point>();
    public static final int GRID_SIZE = 6;
    static int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    static Point startPoint = new Point(30,0);
    static Point goalPoint = new Point(180,180);

    public static final int WALL = 9999;
    
    static DifferentialPilot pilot = new DifferentialPilot(5.6, 13.2, Motor.B, Motor.A);
    static PoseProvider pp;  //need this since the new version of nav has a built in odometry provider
    static Navigator nav = new Navigator(pilot);
    
    public static Waypoint nextWay;
	public static int oldX;
	public static int oldY;
	public static int newX;
	public static int newY;

	public static globalIRData data;
	public static HiTechnicIRSeekerV2 Seeker;
	public static float seek;
	
	public static Point sendPoint = new Point(0,0);
	public static Queue<Point> sendQ = new LinkedList<Point>();
	
	static int IRCount = 0;
	
    public static void main(String[] args) {

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

        // Test print of wavefront-ed grid
        // printGrid2(grid);

        // Create an arraylist of points from start (index 0) to goal (index list.length-1)
        getPath();

        // Display list
        //System.out.println(path.toString());
        
        // Seek
        seek();
        //Point testSend = new Point(3, 3);
        //testSend.setHeading(90);
        //sendQ.add(testSend);

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
    	Seeker = new HiTechnicIRSeekerV2(SensorPort.S1);
		data = new globalIRData();
		
		IRAngle a;
		
		SampleProvider sonicSampleProvider = Seeker.getModulatedMode();
		float[] s1 = new float[sonicSampleProvider.sampleSize()];
    	
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
            if (Math.abs(newX-oldX) > 30 || Math.abs(newY-oldY) > 30){
            	pilot.rotate(360);
            	Delay.msDelay(8000);
            	Sound.playTone(500, 1000);
            	System.exit(1);
            	break;
            }
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
        while (!nav.pathCompleted()) {
        	int cnt = 0;
    		Delay.msDelay(10);
    		
    		while(sendQ.isEmpty()){
    			sonicSampleProvider.fetchSample(s1, 0);
    			seek = s1[0];
    			Delay.msDelay(30);
    			a = new IRAngle(cnt,seek);
    			cnt++;
    			
    			
    			if (Math.abs(seek) <= 91 && Math.abs(seek) >= 70 && IRCount < 1){
    				Sound.playTone(500, 200);
    				Waypoint ourWay = nav.getWaypoint();
    				int x = (int) (ourWay.getX()-15)/30;
    				int y = (int) (ourWay.getY()-15)/30;
    				
    				if (seek > 0 && IRCount < 1){ // Ball to right
    					sendPoint = new Point(y, x);
    					sendPoint.setHeading(90);
    					LCD.drawString("X: " + sendPoint.getX() + " Y: " + sendPoint.getY(), 2, 2);
    					IRCount++;
    					sendQ.add(sendPoint);
    					Seeker.close();
    					break;
    					//}
    				}
    				if (seek < 0 && IRCount < 1) { // Ball to left
    					sendPoint = new Point(y, x);
    					sendPoint.setHeading(-90);
    					LCD.drawString("X: " + sendPoint.getX() + " Y: " + sendPoint.getY(), 2, 2);
    					IRCount++;
    					sendQ.add(sendPoint);
    					Seeker.close();
    					break;
    				}
    				
    			}	
    		}
        }
        
        Sound.playTone(320, 500);
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

    public static Queue<Point> getPath() {
        int curr_x = startPoint.getX();

        // Up first hallway
        for (int y = 0; y < 6; y++) {
            path.add(new Point(y, curr_x/30));
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

        return path;
    }

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