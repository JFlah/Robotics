import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by Jack on 11/4/2015.
 */
public class Wavefront {
    static Point next;
    static ArrayList<Point> obstacleList = new ArrayList<Point>();
    static Queue<Point> path = new LinkedList<Point>();
    public static final int GRID_SIZE = 9;
    static int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    static Point startPoint = new Point(0,0);
    static Point goalPoint = new Point(0,0);

    static Queue<Point> q = new LinkedList<Point>();
    public static final int WALL = 9999;

    //private static Waypoint nextWay;

    public static void main(String[] args) {

        // Make the grid
        int grid[][] = createGrid();

        // Test print of lower-left Points of each cell (their contents) and then which cell in the grid it is
        //printGrid(grid);

        // Filing in the obstacle list
        try {
            readArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Test print of obstacle list array
        //printObstacleList(obstacleList);

        // Set obstacle, goal, start values
        setGridVals(grid, obstacleList);

        // Test print to check new grid vals
        //printGrid(grid);

        // Propagate
        propagate();

        // Test print of wavefront-ed grid
        printGrid2(grid);

        // Create an arraylist of points from start (index 0) to goal (index list.length-1)
        getPath();

        // Display list
        System.out.println(path.toString());

        // Send path queue to robot 
        try {
            sendPoints(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[][] createGrid(){
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                grid[y][x] = -1;
            }
        }
        return grid;
    }

    public static int[][] setGridVals(int[][] grid, ArrayList<Point> obstacles){
        // Set obstacles

        for (Point obstacle : obstacles) {
            int obX = obstacle.getX()/30;
            int obY = obstacle.getY()/30;
            grid[obY][obX] = WALL;
        }


        // Set goal
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Enter your goal X(cm in column):");
        int x = reader.nextInt();
        System.out.println("Enter your goal Y(cm in row):");
        int y = reader.nextInt();

        // Floor and then * by 30 to get lower left corner of cell for grid reference
        int gridX = x/30;
        int gridY = y/30;
        
        if (x == 270){
        	gridX = 8;
        }
        if (y == 270){
        	gridY = 8;
        }

        goalPoint = new Point(gridX, gridY);
        grid[gridY][gridX] = 0;

        // Set start
        System.out.println("Enter your start X(cm in column):");
        int sx = reader.nextInt();
        System.out.println("Enter your start Y(cm in row):");
        int sy = reader.nextInt();

        // Floor and then * by 30 to get lower left corner of cell for grid reference
        int gridSX = sx/30;
        int gridSY = sy/30;
        
        if (sx == 270){
        	gridSX = 8;
        }
        if (sy == 270){
        	gridSY = 8;
        }

        startPoint = new Point(gridSX, gridSY);

        return grid;
    }

    public static void sendPoints(Queue<Point> q) throws IOException {
        Point temp;
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
        
        int curr_y = startPoint.getY();
        int curr_x = startPoint.getX();
        
        if (curr_x == 9) {
        	curr_x = 8;
        }
        if (curr_y == 9) {
        	curr_y = 8;
        }
        
        path.add(new Point(curr_y, curr_x));

        int north = 800;
        int south = 800;
        int east = 800;
        int west = 800;
        
        if (grid[curr_y][curr_x] == 0){
            System.out.println("Started on goal");
            return path;
        }

        while (true) {
            // North
            if (curr_y - 1 >= 0) {
                //System.out.println("Setting a north, curr_y-1 is: " + Integer.toString(curr_y-1));
                north = grid[curr_y - 1][curr_x];
                if (north == 0){
                    next = new Point(curr_y-1, curr_x);
                    System.out.println("North is the goal");
                    path.add(next);
                    break;
                }
            }
            // South
            if (curr_y + 1 < grid.length) {
                //System.out.println("Setting a south, curr_y+1 is: " + Integer.toString(curr_y+1));
                south = grid[curr_y + 1][curr_x];
                if (south == 0) {
                    next = new Point(curr_y+1, curr_x);
                    System.out.println("South is the goal");
                    path.add(next);
                    break;
                }
            }
            // East
            if (curr_x + 1 < grid[curr_x].length) {
                //System.out.println("Setting an east, curr_x+1 is: " + Integer.toString(curr_x+1));
                east = grid[curr_y][curr_x + 1];
                if (east == 0) {
                    next = new Point(curr_y, curr_x+1);
                    System.out.println("East is the goal");
                    path.add(next);
                    break;
                }
            }
            // West
            if (curr_x - 1 >= 0) {
                //System.out.println("Setting a west, curr_x-1 is: " + Integer.toString(curr_x-1));
                west = grid[curr_y][curr_x - 1];
                if (west == 0) {
                    next = new Point(curr_y, curr_x-1);
                    System.out.println("West is the goal");
                    path.add(next);
                    break;
                }
            }
            System.out.println("North: " + north + " East: " + east + " South: " + south + " West: " + west);
            // North is smallest
            if (north <= south && north <= east && north <= west) {
                System.out.println("North is smallest");
                next = new Point(curr_y-1, curr_x);
                curr_y--;
            }
            // South is smallest
            else if (south <= north && south <= east && south <= west){
                System.out.println("South is smallest");
                next = new Point(curr_y+1, curr_x);
                curr_y++;
            }
            // East is smallest
            else if (east <= north && east <= south && east <= west){
                System.out.println("East is smallest");
                next = new Point(curr_y, curr_x+1);
                curr_x++;
            }
            // West is smallest
            else if (west <= north && west <= south && west <= east){
                System.out.println("West is smallest");
                next = new Point(curr_y, curr_x-1);
                curr_x--;
            } else {
            	continue;
            }
            //System.out.println("One loop, adding: " + next.toString() + " to path list");
            if (next != null)
            	path.add(next);
        }
        return path;
    }

    public static void readArray() throws FileNotFoundException {
        Scanner in = new Scanner(new FileReader("obstacles.txt"));

        while (in.hasNextInt()) {
            int x = in.nextInt();
            int y = in.nextInt();

            Point ob = new Point(x, y);
            obstacleList.add(getObstacleCell(ob));
        }
        in.close();
    }

    public static void propagate() {
        q.add(goalPoint);
        while (!q.isEmpty()) {
            Point current = q.remove();
            int curr_x = current.getX();
            int curr_y = current.getY();
            int curr_val = grid[curr_y][curr_x];

            if (curr_val==WALL) continue;

            // North
            if (curr_y - 1 >= 0) {
                if (grid[curr_y - 1][curr_x] != WALL
                        && (grid[curr_y - 1][curr_x] > curr_val+1
                        ||  grid[curr_y - 1][curr_x] == -1)) {
                    grid[curr_y-1][curr_x] = curr_val + 1;
                    q.add(new Point(curr_x,curr_y-1));
                }
            }
            // South
            if (curr_y + 1 < grid.length) {
                if (grid[curr_y + 1][curr_x] == -1
                        || (grid[curr_y + 1][curr_x] != WALL
                        &&  grid[curr_y + 1][curr_x] > curr_val+1)) {
                    grid[curr_y + 1][curr_x] = curr_val + 1;
                    q.add(new Point(curr_x,curr_y+1));
                }
            }
            // East
            if (curr_x + 1 < grid[curr_x].length) {
                if (grid[curr_y][curr_x + 1] == -1
                        || (grid[curr_y][curr_x+1] != WALL
                        &&  grid[curr_y][curr_x+1] > curr_val+1)) {
                    grid[curr_y][curr_x + 1] = curr_val+1;
                    q.add(new Point(curr_x+1, curr_y));
                }
            }
            // West
            if (curr_x - 1 >= 0) {
                if (grid[curr_y][curr_x - 1] == -1
                        || (grid[curr_y][curr_x-1] != WALL
                        &&  grid[curr_y][curr_x-1] > curr_val+1)) {
                    grid[curr_y][curr_x - 1] = curr_val + 1;
                    q.add(new Point(curr_x-1, curr_y));
                }
            }
        }
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
