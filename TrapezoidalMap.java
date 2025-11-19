/*
 * Trapezoidal Map
 * 
 * Authors: Lydia Klecan, Tyler Lapiana
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Collections;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TrapezoidalMap {
    public record Point (int id, double x, double y, boolean isStart){}

    public record Segment (int id, Point start, Point end) {}

    // trapezoid id and 4 Points of a trapezoid in clockwise order, starting at bottom left
    public record Trapezoid (int tid, Point bl, Point tl, Point tr, Point br) {}

    public static int currTrapezoidId = 1;

    public static int getFreshId(){
        currTrapezoidId += 1;
        return currTrapezoidId;
    }

    public static void addToAM(String id, String pid, HashMap<String, ArrayList<String>> adjMatrix){
        if (!adjMatrix.containsKey(id)){ // TODO all should be new, shouldn't need this check
            adjMatrix.put(id, new ArrayList<>());
        }
        adjMatrix.get(id).add(pid); // TODO does this really change the map? Do we need to 'put' the value back
    }

    public static double calculateSlope(Point p1, Point p2){
        return (p2.y - p1.y) / (p2.x - p1.x);
    }

    public static double calculateY(Point p1, Point p2, double x){
        double slope = calculateSlope(p1, p2);
        return slope * (x - p1.x) + p1.y;
    }


    // Case 1: Start point is in this trapezoid, end point is not in this trapezoid
    public static ArrayList<Trapezoid> singleStart(Trapezoid inTrap, Segment seg, HashMap<String, ArrayList<String>> am){
        // current trapezoid replaced with (curr Trap divided by start pt, curr segment)
        // curr segment gets children (top trap, bottom trap)
        // remove this trapezoid from tree and get parent

        // replace curr trapezoid with start (P)
        // trapezoid to left of start is created
        // to the right of start add S
        // S children are two trapezoids

        double leftTraptr = calculateY(inTrap.tl, inTrap.tr, seg.start.x);
        double leftTrapbr = calculateY(inTrap.bl, inTrap.br, seg.start.x);
        double segBordery = calculateY(seg.start, seg.end, inTrap.br.x);

        // The trapezoid we are in will be split into 3 trapezoids
        String currTid = "T" + inTrap.tid;
        am.put("P" + seg.id, am.get(currTid));
        am.remove(currTid);

        // Trapezoid to left of start point
        Trapezoid x = new Trapezoid(getFreshId(),
                inTrap.bl,
                inTrap.tl,
                new Point(0, seg.start.x, leftTraptr, false),
                new Point(0, seg.start.x, leftTrapbr, false));
            addToAM("T" + x.tid, "P" + seg.id, am);

            addToAM("S" + seg.id, "P" + seg.id, am);

        // Trapezoid above segment
        Trapezoid y = new Trapezoid(getFreshId(),
                seg.start,
                new Point(0, seg.start.x, leftTraptr, false),
                inTrap.tr,
                new Point(0, inTrap.br.x, segBordery, false));
        addToAM("T" + y.tid, "S" + seg.id, am);

        // Trapezoid below segment
        Trapezoid z = new Trapezoid(getFreshId(),
                new Point(0, seg.start.x, leftTrapbr, false),
                seg.start,
                new Point(0, inTrap.br.x, segBordery, false),
                inTrap.br);
        addToAM("T" + z.tid, "S" + seg.id, am);

        ArrayList<Trapezoid> result = new ArrayList<>();
        result.add(z);
        result.add(y);
        result.add(x);
        return result;
    }

    // Case 1.5: Start point is not, but end point is in this trapezoid
    public static ArrayList<Trapezoid> singleEnd(
            Trapezoid inTrap, Segment seg, HashMap<String, ArrayList<String>> am){
        
        double rightTraptl = calculateY(inTrap.tl, inTrap.tr, seg.end.x);
        double rightTrapbl = calculateY(inTrap.bl, inTrap.br, seg.end.x);
        double segBordery = calculateY(seg.start, seg.end, inTrap.bl.x);

        // The trapezoid we are in will be split into 3 trapezoids
        String currTid = "T" + inTrap.tid;
        am.put("Q" + seg.end.id, am.get(currTid));
        am.remove(currTid);

        // Trapezoid to right of start point
        Trapezoid x = new Trapezoid(getFreshId(),
                new Point(0, seg.end.x, rightTrapbl, false),
                new Point(0, seg.end.x, rightTraptl, false),
                inTrap.tr,
                inTrap.br);
        addToAM("T" + x.tid, "Q" + seg.id, am);

        addToAM("S" + seg.id, "Q" + seg.id, am);

        // Trapezoid above segment
        Trapezoid y = new Trapezoid(getFreshId(),
                new Point(0, inTrap.bl.x, segBordery, false),
                inTrap.tl,
                new Point(0, seg.end.x, rightTraptl, false),
                seg.end);
        addToAM("T" + y.tid, "S" + seg.id, am);

        // Trapezoid below segment
        Trapezoid z = new Trapezoid(getFreshId(),
                inTrap.bl,
                new Point(0, inTrap.bl.x, segBordery, false),
                seg.end,
                new Point(0, seg.end.x, rightTrapbl, false));
        addToAM("T" + z.tid, "S" + seg.id, am);

        ArrayList<Trapezoid> result = new ArrayList<>();
        result.add(z);
        result.add(y);
        result.add(x);
        return result;
        }

    // Case 2: Both start and end point are in this trapezoid
    // Baymax case
    public static ArrayList<Trapezoid> bothPts(
            Trapezoid inTrap, Segment seg, HashMap<String, ArrayList<String>> am){

        double tly = calculateY(inTrap.tl, inTrap.tr, seg.start.x);
        double tr_y = calculateY(inTrap.tl, inTrap.tr, seg.end.x);
        double bly = calculateY(inTrap.bl, inTrap.br, seg.start.x);
        double bry = calculateY(inTrap.bl, inTrap.br, seg.end.x);

        // the trapezoid we are currently in will be split into 4 trapezoids
        String currTid = "T" + inTrap.tid;
        am.put("P" + seg.start.id, am.get(currTid));
        am.remove(currTid);

        Trapezoid u = new Trapezoid(getFreshId(),
                inTrap.bl,
                inTrap.tl,
                new Point(0, seg.start.x, tly, false), //todo
                new Point(0, seg.start.x, bly, false)); //todo
        addToAM("T" + u.tid, "P" + seg.id, am);

        addToAM("Q" + seg.id, "P" + seg.id, am);

        Trapezoid x = new Trapezoid(getFreshId(),
                new Point(0, seg.start.x, bry, false),  //todo
                new Point(0, seg.end.x, tr_y, false), //todo
                inTrap.tr,
                inTrap.br);
        addToAM("T" + x.tid, "Q" + seg.id, am);

        addToAM("S" + seg.id, "Q" + seg.id, am);

        Trapezoid y = new Trapezoid(getFreshId(),
                seg.start,
                new Point(0, seg.start.x, tly, false),
                new Point(0, seg.end.x, tr_y, false),
                seg.end);
        addToAM("T"+y.tid, "S" + seg.id, am);

        Trapezoid z = new Trapezoid(getFreshId(),
                new Point(0, seg.start.x, bly, false),
                seg.start,
                seg.end,
                new Point(0, seg.end.x, bry, false));
        addToAM("T" + z.tid, "S"+seg.id, am);

        ArrayList<Trapezoid> result = new ArrayList<>();
        result.add(u);
        result.add(z);
        result.add(y);
        result.add(x);
        return result;
    }

    // Case 3: Neither start nor end point are in this trapezoid
    public static ArrayList<Trapezoid> neitherPts(
            Trapezoid inTrap, Segment seg, HashMap<String, ArrayList<String>> am){

        Point leftSegBorder = new Point(0, inTrap.bl.x, calculateY(seg.start, seg.end, inTrap.bl.x), false);
        Point rightSegBorder = new Point(0, inTrap.br.x, calculateY(seg.start, seg.end, inTrap.br.x), false);

        String currTid = "T" + inTrap.tid;
        am.put("S" + seg.id, am.get(currTid));
        am.remove(currTid);

        Trapezoid x = new Trapezoid(getFreshId(), leftSegBorder, inTrap.tl, inTrap.tr, rightSegBorder);
        addToAM("T" + x.tid, "S" + seg.id, am);

        Trapezoid y = new Trapezoid(getFreshId(), inTrap.bl, leftSegBorder, rightSegBorder, inTrap.br);
        addToAM("T" + y.tid, "S" + seg.id, am);

        ArrayList<Trapezoid> result = new ArrayList<>();
        result.add(x);
        result.add(y);
        return result;
    }

    /*
     *
     *
     * *********   MAIN   *********
     *
     *
     */
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

        List<Segment> segments = new ArrayList<>();
        int id = 1;
        while (scan.hasNextLine()){
            String[] line = scan.nextLine().split(" ");
            segments.add(new Segment(id,
                    new Point(id, Integer.parseInt(line[0]), Integer.parseInt(line[1]), true),
                    new Point(id, Integer.parseInt(line[2]), Integer.parseInt(line[3]), false)));
            id++;
        }
        scan.close();

        // id and list of parents
        HashMap<String, ArrayList<String>> adjMatrix = new HashMap<>();
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
        // TODO If segment start or end point on trap x-boundary, and seg goes through whole trap, should be case 3
        for (Segment s : segments) {
            boolean done = false;
                ArrayList<Trapezoid> startTraps = inWhatTrapezoid(s.start, trapezoids);
                Trapezoid startT;
                if (startTraps.size() == 1) {
                    startT = startTraps.get(0);
                    if (s.end.x < startT.br.x) {
                        bothPts(startT, s, adjMatrix); // TODO add these returned trapezoids to the trapezoid list
                        done = true;
                    } else {
                        singleStart(startT, s, adjMatrix);
                    }
                } else {    // TODO I don't think we are calling the case here once we find the trapezoid
                    // get the correct trapezoid when there are multiple
                    for (Trapezoid t : startTraps) {
                        if (t.bl.x != s.start.x) {
                            startTraps.remove(t);
                        }
                    }
                    if (startTraps.size() == 1) {
                        startT = startTraps.get(0);
                    } else {
                        Trapezoid bottom;
                        Trapezoid top;
                        if (startTraps.get(0).tl.y < startTraps.get(1).tl.y) {
                            bottom = startTraps.get(0);
                            top = startTraps.get(1);
                        } else {
                            bottom = startTraps.get(1);
                            top = startTraps.get(0);
                        }
                        double slope = calculateSlope(bottom.br, bottom.bl);    // TODO I think this logic may be wrong
                        double slopeOfS = calculateSlope(s.start, s.end);
                        if (slope > slopeOfS) {
                            startT = bottom;
                        } else {
                            startT = top;
                        }
                    }

                    if (s.end.x < startT.br.x) {
                        singleEnd(startT, s, adjMatrix);
                        done = true;
                    } else {
                        neitherPts(startT, s, adjMatrix);
                    }
                }

                Point nextPt = new Point (0, 0, 0, false); //TODO

                while (!done){
                    // iterate through sorted list of trapezoids and find the x value that corresponds with this one
                    // TODO binary search for O(n log n)
                    for (Trapezoid t : trapezoids){
                        if (nextPt.x == t.bl.x){
                            // make sure y value corresponds
                            if (nextPt.y < t.tl.y && nextPt.y > t.bl.y){
                                // if end point is in this trapezoid, call that case and break
                                if (s.end.x < t.br.y){
                                    singleEnd(t, s, adjMatrix);
                                    done = true;
                                } else { // otherwise call correct case and keep going
                                    neitherPts(t, s, adjMatrix);
                                }
                                break;
                            }
                        }
                    }
                }

            //    find what trapezoid starting point is in
            //    do 4 cases based on if it hits a vertical line
            //    add info to adjacency matrix
        }

        generateOutput(adjMatrix);
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
                        //continue;
                        inTrapezoids.add(currTrapezoid); //TODO added this line because this checks if point is inside, as does the else case
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

    public static void generateOutput(HashMap<String, ArrayList<String>> am) {

        HashSet<String> allIdsSet = new HashSet<>();

        // Get set of all IDs used in the entire 
        for (String key : am.keySet()) {
            allIdsSet.add(key);
            ArrayList<String> parentIds = am.get(key);
            if (parentIds != null){
                allIdsSet.addAll(parentIds);
            }
        }

        // Sort them in order of P, Q, S, T
        ArrayList<String> allIdsList = new ArrayList<>(allIdsSet);
        Collections.sort(allIdsList);
        int numRows = allIdsList.size();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("adjacency_matrix.csv"))) {

            // Write Header row
            String headerString = ",";
            for (String rowId: allIdsList) {
                headerString = headerString + rowId + ",";
            }
            headerString = headerString + "Sum";
            writer.write(headerString);
            writer.newLine();

            // Go through each id and create the row for that ID
            HashMap<String, Integer> columnSums = new HashMap<>();
            for (String rowId : allIdsList) {
                
                // If not present in keyset, you are the root node
                if (!am.keySet().contains(rowId)) {
                    String rowString = rowId + ",";
                    for (int i = 0; i < numRows; i++) {
                        rowString = rowString + "0,";
                    }
                    // Row sum will be 0 for root node
                    rowString = rowString + "0";
                    writer.write(rowString);
                    writer.newLine();
                }

                // If not root node, you are a child node
                else {
                    ArrayList<String> currIdsParents = am.get(rowId);
                    int rowSum = 0;
                    String rowString = rowId + ",";
                    for (String possibleParent : allIdsList) {
                        // Each object is either a parent or not

                        // True if you are a parent
                        if (currIdsParents != null && currIdsParents.contains(possibleParent)) {
                            rowSum++;
                            rowString = rowString + "1,";

                            // Keep track of how many times each id was a parent
                            Integer currColCount = columnSums.get(possibleParent);
                            if (currColCount == null) {
                                columnSums.put(possibleParent, 1);
                            }
                            else{
                                columnSums.put(possibleParent, currColCount + 1);
                            }
                        }
                        else {
                            rowString = rowString + "0,";
                        }
                    }
                    rowString = rowString + Integer.toString(rowSum);
                    writer.write(rowString);
                    writer.newLine();
                }
            }

            // Write column sum row
            String footerString = "Sum,";
            for (String rowId: allIdsList) {
                Integer rid = columnSums.get(rowId);
                if (rid != null) {
                    footerString = footerString + rid + ",";
                } else {
                    footerString = footerString + "0,";
                }
            }
            writer.write(footerString);
            writer.newLine();

        } catch (IOException e) {
            System.err.println("Error writing to csv file: " + e.getMessage());
            e.printStackTrace();;
        }
    }

}
