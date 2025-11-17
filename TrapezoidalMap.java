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
        List<Trapezoid> trapezoids = new ArrayList<>();
        Trapezoid box = new Trapezoid(0, boundingBox[0],
                new Point(0, boundingBox[0].x, boundingBox[1].y, false),
                boundingBox[1],
                new Point(0, boundingBox[1].x, boundingBox[0].y, false));
        trapezoids.add(box);
        List<Segment> verticalLines = new ArrayList<>();
        verticalLines.add(new Segment(0, box.bl, box.tl));
        verticalLines.add(new Segment(0, box.br, box.tr));


        //creating the tree
        //  trapezoid zero is bounding box coordinates
        //  for each segment, do algorithm
        //    find what trapezoid starting point is in
        //    do 4 cases based on if it hits a vertical line
        //    add info to adjacency matrix

    }
}
