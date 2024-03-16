import java.util.*;

class Point {
    int x = -1;
    int y = -1;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int newX) {
        x = newX;
    }

    public void setY(int newY) {
        y = newY;
    }

    public String toString() {
        return "(" + this.getX() + ", " + this.getY() + ")";
    }

}

class Drone implements Comparable<Drone> {
    Point location = new Point(-1, -1);
    Point targetLocation = new Point(-1, -1);

    char droneID;

    public Drone(int x, int y, char droneID) {
        location = new Point(x, y);
        targetLocation = new Point(-1, -1);
        this.droneID = droneID;
    }

    public void setTargetLocation(int x, int y) {
        targetLocation.setLocation(x, y);
    }

    public char getDroneID() {
        return droneID;
    }

    public String toString() {
        return "D" + droneID + " is located at " + location + " and G" + droneID + " is located at " + targetLocation;
    }

    public int compareTo(Drone other) {
        if (Integer.parseInt(String.valueOf(getDroneID())) < Integer.parseInt(String.valueOf(other.getDroneID()))) {
            return -1;
        } else if (Integer.parseInt(String.valueOf(getDroneID())) == Integer.parseInt(String.valueOf(other.getDroneID()))) {
            return 0;
        }
        return 1;
    }
}

public class Main {

    public static ArrayList<Drone> drones = new ArrayList<Drone>();

    public static ArrayList<Point> droneStartPoints = new ArrayList<Point>();

    public static ArrayList<Point> droneTargetPoints = new ArrayList<Point>();

    public static boolean[][] blockedAreas = new boolean[8][8];

    final public static int[] DX = {-1, 0, 0, 1};
    final public static int[] DY = {0, -1, 1, 0};

    public static int numDrones = -1;

    public static void main(String[] args) {
        Scanner stdin = new Scanner(System.in);

        numDrones = stdin.nextInt();
        String[][] grid = new String[8][8];

        for (int ROW = 0; ROW < 8; ROW++) {
            for (int COL = 0; COL < 8; COL++) {
                grid[ROW][COL] = stdin.next();
            }
        }

        loadGrid(grid);

    }

    public static void loadGrid(String[][] grid) {
        char[][] targetLocationStorage = new char[8][8];
        for (int ROW = 0; ROW < 8; ROW++) {
            for (int COL = 0; COL < 8; COL++) {
                if (grid[ROW][COL].charAt(0) == '_') {

                } else if (grid[ROW][COL].charAt(0) == 'X') {

                    blockedAreas[ROW][COL] = true;
                } else if (grid[ROW][COL].charAt(0) == 'D') {

                    drones.add(new Drone(ROW, COL, grid[ROW][COL].charAt(1)));

                } else if (grid[ROW][COL].charAt(0) == 'G') {

                    blockedAreas[ROW][COL] = true;
                    targetLocationStorage[ROW][COL] = grid[ROW][COL].charAt(1);

                }
            }
        }

        for (int ROW = 0; ROW < 8; ROW++) {
            for (int COL = 0; COL < 8; COL++) {
                for (Drone drone : drones) {
                    if (drone.getDroneID() == targetLocationStorage[ROW][COL]) {
                        drone.setTargetLocation(ROW, COL);
                    }
                }
            }
        }

        Collections.sort(drones);

        for (Drone drone : drones) {
            droneStartPoints.add(new Point(drone.location.getX(), drone.location.getY())); 
            droneTargetPoints.add(new Point(drone.targetLocation.getX(), drone.targetLocation.getY()));
        }

        int result = bfs();  

        if (result >= 0) {
            System.out.println(result);
        } else {
            System.out.println(-1);
        }

    }

    public static int bfs() {

        String initialState = encode(droneStartPoints);
        String finalIntendedState = encode(droneTargetPoints);

        HashMap<String, Integer> table = new HashMap<>();

        LinkedList<String> queue = new LinkedList<>();

        table.put(initialState, 0);
        queue.offer(initialState);

        while (!queue.isEmpty()) {

            String currentState = queue.poll();

            int currentNumMoves = table.get(currentState);

            if (currentState.equals(finalIntendedState)) {

                return currentNumMoves;  
            }

            ArrayList<String> nextStates = getNextStates(currentState, drones.size());

            for (String nextState : nextStates) {

                if (!table.containsKey(nextState)) {
                    table.put(nextState, currentNumMoves + 1);
                    queue.offer(nextState);
                }
            }
        }

        return -1;
    }

    public static String encode(ArrayList<Point> points) {
        StringBuilder binary = new StringBuilder();
        for (Point point : points) {
            int pos = (point.x * 8) + point.y;
            binary.insert(0, toSixDigitBinary(pos)); 
        }
        return binary.toString();
    }

    public static String toSixDigitBinary(int number) {
        String binaryString = Integer.toBinaryString(number);
        return String.format("%6s", binaryString).replace(' ', '0'); 
    }

    public static ArrayList<Point> decode(String binary, int numDrones) {
        ArrayList<Point> positions = new ArrayList<>();

        for (int i = 0; i < numDrones; i++) {

            int end = binary.length() - (i * 6);
            int start = Math.max(end - 6, 0); 

            String droneBinary = binary.substring(start, end);

            int pos = Integer.parseInt(droneBinary, 2);

            int x = pos / 8;
            int y = pos % 8;

            positions.add(new Point(x, y)); 
        }

        return positions;
    }

    public static ArrayList<String> getNextStates(String currentState, int numDrones) {
        ArrayList<String> nextStates = new ArrayList<String>();

        ArrayList<Point> currentPositions = decode(currentState, numDrones);

        for (int dir = 0; dir < 4; dir++) {

            for (int droneIndex = 0; droneIndex < numDrones; droneIndex++) {

                if (currentPositions.get(droneIndex).getX() == droneTargetPoints.get(droneIndex).getX()
                        && currentPositions.get(droneIndex).getY() == droneTargetPoints.get(droneIndex).getY())
                {

                    continue;
                }

                int newX = currentPositions.get(droneIndex).getX() + DX[dir];
                int newY = currentPositions.get(droneIndex).getY() + DY[dir];

                Point newPos = new Point(newX, newY);

                if (isValidMove(newPos, droneIndex)) {

                    currentPositions.set(droneIndex, newPos);
                }
            }

            String newState = encode(currentPositions);
            nextStates.add(newState);

            currentPositions = decode(currentState, numDrones);

        }

        return nextStates;
    }

    public static boolean isValidMove(Point newPos, int droneIndex) {

        if (newPos.getX() < 0 || newPos.getX() >= 8 || newPos.getY() < 0 || newPos.getY() >= 8) {
            return false;
        }

        if (blockedAreas[newPos.x][newPos.y]) {
            if (newPos.getX() == droneTargetPoints.get(droneIndex).getX()
            && newPos.getY() == droneTargetPoints.get(droneIndex).getY()
            ) {
                return true;
            }
            return false;
        }

        return true;
    }

}