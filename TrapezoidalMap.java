import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TrapezoidalMap {
    public record Point (int id, int x, int y, boolean isStart){}

    public record Segment (int id, Point start, Point end) {}

    // trapezoid id and 4 Points of a trapezoid in clockwise order, starting at bottom left
    public record Trapezoid (int tid, Point bl, Point tl, Point tr, Point br) {}

    public int getFreshId(){return 0;}

    // Case 1: Start point is in this trapezoid, end point is not in this trapezoid
    public static void singleStart(Trapezoid inTrap, Segment seg){
        // current trapezoid replaced with (curr Trap divided by start pt, curr segment)
        // curr segment gets children (top trap, bottom trap)

        // remove this trapezoid from tree and get parent
        int trapParent;

    }

    // Case 1.5: Start point is not, but end point is in this trapezoid
    public static void singleEnd(){}
    // Case 2: Both start and end point are in this trapezoid
    public static void bothPts(){}
    // Case 3: Neither start nor end point are in this trapezoid
    public static void neitherPts(){}


    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1){
            System.out.println("Usage: ./TrapezoidalMap \"input.txt\"");
            return;
        }

        Scanner scan = new Scanner(new File(args[0]));
        int numPts = Integer.parseInt(scan.nextLine());
        String[] startingBox = scan.nextLine().split(" ");
        Point[] boundingBox = {
                new Point(0, Integer.parseInt(startingBox[0]), Integer.parseInt(startingBox[1]), false),
                new Point(0, Integer.parseInt(startingBox[2]), Integer.parseInt(startingBox[3]), false)
        };

        List<Segment> segments = new ArrayList<Segment>();
        int id = 1;
        while (scan.hasNextLine()){
            String[] line = scan.nextLine().split(" ");
            segments.add(new Segment(id,
                    new Point(id, Integer.parseInt(line[0]), Integer.parseInt(line[1]), true),
                    new Point(id, Integer.parseInt(line[2]), Integer.parseInt(line[3]), false)));
            id++;
        }
        scan.close();

        int[][] adjMatrix = new int[numPts*4][];
        ArrayList<Trapezoid> trapezoids = new ArrayList<>();
        Trapezoid box = new Trapezoid(0, boundingBox[0],
                new Point(0, boundingBox[0].x, boundingBox[1].y, false),
                boundingBox[1],
                new Point(0, boundingBox[1].x, boundingBox[0].y, false));
        trapezoids.add(box);
        ArrayList<Segment> verticalLines = new ArrayList<>();
        verticalLines.add(new Segment(0, box.bl, box.tl));
        verticalLines.add(new Segment(0, box.br, box.tr));


        // for each segment, do algorithm
        for (Segment s : segments) {
            ArrayList<Trapezoid> startTraps = inWhatTrapezoid(s.start, trapezoids);
            Trapezoid startT;
            if (startTraps.size() == 1){
                startT = startTraps.get(0);
                if (s.end.x < startT.br.x) {
                    bothPts();
                } else {
                    singleStart(startT, s);
                    // TODO keep going from here, maybe single start calls next func?
                }
            } else {
                // get the correct trapezoid when there are multiple
                for (Trapezoid t : startTraps){
                    if (t.bl.x != s.start.x){
                        startTraps.remove(t);
                    }
                }
                if (startTraps.size() == 1){
                    startT = startTraps.get(0);
                } else {
                    Trapezoid bottom;
                    Trapezoid top;
                    if (startTraps.get(0).tl.y < startTraps.get(1).tl.y){
                        bottom = startTraps.get(0);
                        top = startTraps.get(1);
                    } else {
                        bottom = startTraps.get(1);
                        top = startTraps.get(0);
                    }
                    double slope = 1.0 * (bottom.br.y -bottom.bl.y) / (bottom.br.x - bottom.bl.x);
                    double slopeOfS = 1.0 * (s.end.y - s.start.y) / (s.end.x - s.start.x);
                    if (slope > slopeOfS){
                        startT = bottom;
                    } else {
                        startT = top;
                    }
                }

                if (s.end.x < startT.br.x){
                    singleEnd();
                } else {
                    neitherPts();
                    // TODO same as above, need to keep going from here
                }
            }


            //    find what trapezoid starting point is in
            //    do 4 cases based on if it hits a vertical line
            //    add info to adjacency matrix
        }

    }

    public static ArrayList<Trapezoid> inWhatTrapezoid(Point queryPoint, ArrayList<Trapezoid> trapezoids) {
        // This accounts for inside and verticies, it does not account for edges

        ArrayList<Trapezoid> inTrapezoids = new ArrayList<>();

        // For all trapezoids
        for (int i = 0; i < trapezoids.size(); i++) {
            Trapezoid currTrapezoid = trapezoids.get(i);

            // Check if the query point is on the vertex of current trapezoid
            if ((queryPoint.x == currTrapezoid.bl.x && queryPoint.y == currTrapezoid.bl.y) ||
                (queryPoint.x == currTrapezoid.tl.x && queryPoint.y == currTrapezoid.tl.y) ||
                (queryPoint.x == currTrapezoid.tr.x && queryPoint.y == currTrapezoid.tr.y) ||
                (queryPoint.x == currTrapezoid.br.x && queryPoint.y == currTrapezoid.br.y)) {
                    inTrapezoids.add(currTrapezoid);
                    continue;
            }

            else {
                // Check if inside the current trapezoid
                if ((crossProduct(currTrapezoid.bl, currTrapezoid.tl, queryPoint) < 0) ||
                    (crossProduct(currTrapezoid.tl, currTrapezoid.tr, queryPoint) < 0) ||
                    (crossProduct(currTrapezoid.tr, currTrapezoid.br, queryPoint) < 0) ||
                    (crossProduct(currTrapezoid.br, currTrapezoid.bl, queryPoint) < 0)) {
                        continue;
                    }
                else {
                    // If none of above are true, then it is inside trapezoid and we can quit
                    inTrapezoids.add(currTrapezoid);
                    break;
                }
            }
        }

        return inTrapezoids;
    } 

    public static double crossProduct(Point a, Point b, Point p) {
        return (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x);
    }
}
