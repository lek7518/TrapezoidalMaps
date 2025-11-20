/*
 * Trapezoidal Map
 * 
 * Authors: Lydia Klecan, Tyler Lapiana
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TrapezoidalMap {

    public record Point (double x, double y){}

    public record Segment (int id, Point start, Point end) {}

    // trapezoid id and 4 Points of a trapezoid in clockwise order, starting at bottom left
    public record Trapezoid (int tid, Point bl, Point tl, Point tr, Point br) {}

    public static int currTrapezoidId = 0;

    public static int getFreshId(){
        currTrapezoidId += 1;
        return currTrapezoidId;
    }

    // Add an item to the adjacency matrix given its parent's id
    public static void addToAM(String id, String pid, HashMap<String, ArrayList<String>> adjMatrix){
        if (!adjMatrix.containsKey(id)){
            adjMatrix.put(id, new ArrayList<>());
        }
        adjMatrix.get(id).add(pid);
    }

    // Remove a trapezoid from the list given its id
    public static void removeTrapezoid(int tid, ArrayList<Trapezoid> trapezoids){
        for (int t = 0; t < trapezoids.size(); t++){
            if (trapezoids.get(t).tid == tid){
                trapezoids.remove(t);
                break;
            }
        }
    }

    // Calculate the slope of the line between two points
    public static double calculateSlope(Point p1, Point p2){
        return (p2.y - p1.y) / (p2.x - p1.x);
    }

    // Given two points and an x-value, find the y-value where the x-value passes through the line between the points
    public static double calculateY(Point p1, Point p2, double x){
        double slope = calculateSlope(p1, p2);
        return slope * (x - p1.x) + p1.y;
    }

    // Case 1: Start point is in this trapezoid, end point is not in this trapezoid
    public static ArrayList<Trapezoid> singleStart(Trapezoid inTrap, Segment seg, HashMap<String, ArrayList<String>> am){
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
                new Point(seg.start.x, leftTraptr),
                new Point(seg.start.x, leftTrapbr));
            addToAM("T" + x.tid, "P" + seg.id, am);

            addToAM("S" + seg.id, "P" + seg.id, am);

        // Trapezoid above segment
        Trapezoid y = new Trapezoid(getFreshId(),
                seg.start,
                new Point(seg.start.x, leftTraptr),
                inTrap.tr,
                new Point(inTrap.br.x, segBordery));
        addToAM("T" + y.tid, "S" + seg.id, am);

        // Trapezoid below segment
        Trapezoid z = new Trapezoid(getFreshId(),
                new Point(seg.start.x, leftTrapbr),
                seg.start,
                new Point(inTrap.br.x, segBordery),
                inTrap.br);
        addToAM("T" + z.tid, "S" + seg.id, am);

        ArrayList<Trapezoid> result = new ArrayList<>();
        result.add(x);
        result.add(y);
        result.add(z);
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
        am.put("Q" + seg.id, am.get(currTid));
        am.remove(currTid);

        // Trapezoid to right of start point
        Trapezoid x = new Trapezoid(getFreshId(),
                new Point(seg.end.x, rightTrapbl),
                new Point(seg.end.x, rightTraptl),
                inTrap.tr,
                inTrap.br);
        addToAM("T" + x.tid, "Q" + seg.id, am);

        addToAM("S" + seg.id, "Q" + seg.id, am);

        // Trapezoid above segment
        Trapezoid y = new Trapezoid(getFreshId(),
                new Point(inTrap.bl.x, segBordery),
                inTrap.tl,
                new Point(seg.end.x, rightTraptl),
                seg.end);
        addToAM("T" + y.tid, "S" + seg.id, am);

        // Trapezoid below segment
        Trapezoid z = new Trapezoid(getFreshId(),
                inTrap.bl,
                new Point(inTrap.bl.x, segBordery),
                seg.end,
                new Point(seg.end.x, rightTrapbl));
        addToAM("T" + z.tid, "S" + seg.id, am);

        ArrayList<Trapezoid> result = new ArrayList<>();
        result.add(x);
        result.add(y);
        result.add(z);
        return result;
        }

    // Case 2: Both start and end point are in this trapezoid
    public static ArrayList<Trapezoid> bothPts(
            Trapezoid inTrap, Segment seg, HashMap<String, ArrayList<String>> am){

        double tly = calculateY(inTrap.tl, inTrap.tr, seg.start.x);
        double tr_y = calculateY(inTrap.tl, inTrap.tr, seg.end.x);
        double bly = calculateY(inTrap.bl, inTrap.br, seg.start.x);
        double bry = calculateY(inTrap.bl, inTrap.br, seg.end.x);

        // the trapezoid we are currently in will be split into 4 trapezoids
        String currTid = "T" + inTrap.tid;
        am.put("P" + seg.id, am.get(currTid));
        am.remove(currTid);

        Trapezoid u = new Trapezoid(getFreshId(),
                inTrap.bl,
                inTrap.tl,
                new Point(seg.start.x, tly),
                new Point(seg.start.x, bly));
        addToAM("T" + u.tid, "P" + seg.id, am);

        addToAM("Q" + seg.id, "P" + seg.id, am);

        Trapezoid x = new Trapezoid(getFreshId(),
                new Point(seg.end.x, bry),
                new Point(seg.end.x, tr_y),
                inTrap.tr,
                inTrap.br);
        addToAM("T" + x.tid, "Q" + seg.id, am);

        addToAM("S" + seg.id, "Q" + seg.id, am);

        Trapezoid y = new Trapezoid(getFreshId(),
                seg.start,
                new Point(seg.start.x, tly),
                new Point(seg.end.x, tr_y),
                seg.end);
        addToAM("T"+y.tid, "S" + seg.id, am);

        Trapezoid z = new Trapezoid(getFreshId(),
                new Point(seg.start.x, bly),
                seg.start,
                seg.end,
                new Point(seg.end.x, bry));
        addToAM("T" + z.tid, "S"+seg.id, am);

        ArrayList<Trapezoid> result = new ArrayList<>();
        result.add(u);
        result.add(x);
        result.add(y);
        result.add(z);
        return result;
    }

    // Case 3: Neither start nor end point are in this trapezoid
    public static ArrayList<Trapezoid> neitherPts(
            Trapezoid inTrap, Segment seg, HashMap<String, ArrayList<String>> am){
        Point leftSegBorder = new Point(inTrap.bl.x, calculateY(seg.start, seg.end, inTrap.bl.x));
        Point rightSegBorder = new Point(inTrap.br.x, calculateY(seg.start, seg.end, inTrap.br.x));

        String currTid = "T" + inTrap.tid;
        if (am.containsKey("S" + seg.id)){
            if (am.get(currTid) != null){
                am.get("S" + seg.id).addAll(am.get(currTid));
            }
        } else {
            am.put("S" + seg.id, am.get(currTid));
        }
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
                new Point(Integer.parseInt(startingBox[0]), Integer.parseInt(startingBox[1])),
                new Point(Integer.parseInt(startingBox[2]), Integer.parseInt(startingBox[3]))
        };

        List<Segment> segments = new ArrayList<>();
        int id = 1;
        while (scan.hasNextLine()){
            String[] line = scan.nextLine().split(" ");
            segments.add(new Segment(id,
                    new Point(Integer.parseInt(line[0]), Integer.parseInt(line[1])),
                    new Point(Integer.parseInt(line[2]), Integer.parseInt(line[3]))));
            id++;
        }
        scan.close();

        // id and list of parents
        HashMap<String, ArrayList<String>> adjMatrix = new HashMap<>();
        ArrayList<Trapezoid> trapezoids = new ArrayList<>();
        Trapezoid box = new Trapezoid(0, boundingBox[0],
                new Point(boundingBox[0].x, boundingBox[1].y),
                boundingBox[1],
                new Point(boundingBox[1].x, boundingBox[0].y));
        trapezoids.add(box);

        // for each segment, do algorithm
        for (Segment s : segments) {
            boolean done = false;
            ArrayList<Trapezoid> startTraps = inWhatTrapezoid(s.start, trapezoids);
            Trapezoid startT;
            if (startTraps.size() == 1) {
                startT = startTraps.get(0);
                if (s.end.x < startT.br.x) {
                    trapezoids.addAll(bothPts(startT, s, adjMatrix));
                    removeTrapezoid(startT.tid, trapezoids);
                    done = true;
                } else {
                    trapezoids.addAll(singleStart(startT, s, adjMatrix));
                    removeTrapezoid(startT.tid, trapezoids);
                }
            } else {
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
                    double slope = calculateSlope(bottom.tr, bottom.tl);
                    double slopeOfS = calculateSlope(s.start, s.end);
                    if (slope > slopeOfS) {
                        startT = bottom;
                    } else {
                        startT = top;
                    }
                }

                if (s.end.x < startT.br.x) {
                    trapezoids.addAll(singleEnd(startT, s, adjMatrix));
                    removeTrapezoid(startT.tid, trapezoids);
                    done = true;
                } else {
                    trapezoids.addAll(neitherPts(startT, s, adjMatrix));
                    removeTrapezoid(startT.tid, trapezoids);
                }
            }

            while (!done){
                Trapezoid nextTrap = trapezoids.get(trapezoids.size() - 1);
                Point nextPt = nextTrap.tr; // next starting point
                if (nextPt.x == s.end.x && nextPt.y == s.end.y){
                    done = true;
                    break;
                }
                // iterate through sorted list of trapezoids and find the x value that corresponds with this one
                for (Trapezoid t : trapezoids){
                    if (nextPt.x == t.bl.x){
                        // make sure y value corresponds
                        if (nextPt.y < t.tl.y && nextPt.y > t.bl.y){
                            // if end point is in this trapezoid, call that case and break
                            if (s.end.x < t.br.x){
                                trapezoids.addAll(singleEnd(t, s, adjMatrix));
                                removeTrapezoid(t.tid, trapezoids);
                                done = true;
                            } else { // otherwise call correct case and keep going
                                trapezoids.addAll(neitherPts(t, s, adjMatrix));
                                removeTrapezoid(t.tid, trapezoids);
                            }
                            break;
                        }
                    }
                }
            }
        }

        HashMap<String, String> oneThruNIds = generateOutput(adjMatrix);

        // Ask for user input to test query point
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter an x-value: ");
        double queryX = scanner.nextDouble();
        System.out.print("Please enter an y-value: ");
        double queryY = scanner.nextDouble();

        Point queryPoint = new Point(queryX, queryY);
        ArrayList<String> path = queryPointPath(queryPoint, trapezoids, adjMatrix);

        if (path.size() == 1) {
            System.out.println(path.get(0));
        }
        else {
            String finalPath = "";
            for (String pathId : path) {
                finalPath = finalPath + oneThruNIds.get(pathId);
                finalPath = finalPath + " ";
            }
            System.out.println(finalPath);
        }
    }

    // Find the trapezoid (trapezoids if on a boundary) that the point is currently in
    public static ArrayList<Trapezoid> inWhatTrapezoid(Point queryPoint, ArrayList<Trapezoid> trapezoids) {
        // This accounts for inside and verticies, it does not account for edges
        ArrayList<Trapezoid> inTrapezoids = new ArrayList<>();

        // For all trapezoids
        for (Trapezoid currTrapezoid : trapezoids) {
            // Check if the query point is on the vertex of current trapezoid
            if ((queryPoint.x == currTrapezoid.bl.x && queryPoint.y == currTrapezoid.bl.y) ||
                    (queryPoint.x == currTrapezoid.tl.x && queryPoint.y == currTrapezoid.tl.y) ||
                    (queryPoint.x == currTrapezoid.tr.x && queryPoint.y == currTrapezoid.tr.y) ||
                    (queryPoint.x == currTrapezoid.br.x && queryPoint.y == currTrapezoid.br.y)) {
                inTrapezoids.add(currTrapezoid);
                continue;
            } else {
                // Check if outside the current trapezoid
                if ((crossProduct(currTrapezoid.bl, currTrapezoid.tl, queryPoint) > 0) ||
                        (crossProduct(currTrapezoid.tl, currTrapezoid.tr, queryPoint) > 0) ||
                        (crossProduct(currTrapezoid.tr, currTrapezoid.br, queryPoint) > 0) ||
                        (crossProduct(currTrapezoid.br, currTrapezoid.bl, queryPoint) > 0)) {
                    continue;
                } else {
                    // If none of above are true, then it is inside trapezoid and we can quit
                    inTrapezoids.add(currTrapezoid);
                    break;
                }
            }
        }

        return inTrapezoids;
    } 

    // Return a map of all ids with Tids from 1...n given a sorted list of all P,Q,S,T ids
    public static HashMap<String, String> adjustIdMap(ArrayList<String> allIdsList){
        HashMap<String, String> oneThruNIds = new HashMap<>();
        int currId = 1;
        for (String s : allIdsList) {
            String prefix = s.substring(0, 1);
            if (prefix.equals("T")) {
                oneThruNIds.put(s, prefix + currId);
                currId++;
            } else {
                oneThruNIds.put(s, s);
            }
        }
        return oneThruNIds;
    }

    // Find the cross product of three points
    public static double crossProduct(Point a, Point b, Point p) {
        return (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x);
    }

    // Save the adjacency matrix to a csv file. Return the map of adjusted ids
    public static HashMap<String, String> generateOutput(HashMap<String, ArrayList<String>> am) {

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
        allIdsList.sort((o1, o2) -> {
            int cmp = o1.substring(0, 1).compareTo(o2.substring(0, 1));
            if (cmp == 0){
                if (o1.length() < 2 && o2.length() < 2){return 0;}
                if (o1.length() < 2){return -1;}
                if (o2.length() < 2){return 1;}
                int id1 = Integer.parseInt(o1.substring(1));
                int id2 = Integer.parseInt(o2.substring(1));
                return Integer.compare(id1, id2);
            } else {
                return cmp;
            }
        });
        int numRows = allIdsList.size();

        // Map IDs to 1...n IDs
        HashMap<String, String> oneThruNIds = adjustIdMap(allIdsList);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("adjacency_matrix.csv"))) {

            // Write Header row
            String headerString = ",";
            for (String rowId: allIdsList) {
                headerString = headerString + oneThruNIds.get(rowId) + ",";
            }
            headerString = headerString + "Sum";
            writer.write(headerString);
            writer.newLine();

            // Go through each id and create the row for that ID
            HashMap<String, Integer> columnSums = new HashMap<>();
            for (String rowId : allIdsList) {
                
                // If not present in keyset, you are the root node
                if (!am.keySet().contains(rowId)) {
                    String rowString = oneThruNIds.get(rowId) + ",";
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
                    String rowString = oneThruNIds.get(rowId) + ",";
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
            e.printStackTrace();
        }
        return oneThruNIds;
    }

    // Return point path string based on query point
    public static ArrayList<String> queryPointPath(Point queryPoint, ArrayList<Trapezoid> trapezoids, HashMap<String, ArrayList<String>> am) {

        ArrayList<Trapezoid> containsQueryPoint = inWhatTrapezoid(queryPoint, trapezoids);
        if (containsQueryPoint.size() == 0) {
            ArrayList<String> badQuery = new ArrayList<>();
            badQuery.add("Bad Query Point");
            return badQuery;
        }

        Trapezoid startTrap = containsQueryPoint.get(0);
        String startNode = "T" + startTrap.tid;

        HashMap<String, String> parentMap = new HashMap<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        HashSet<String> visited = new HashSet<>();

        queue.add(startNode);
        visited.add(startNode);

        String rootFound = null;

        while (!queue.isEmpty()) {
            String curr = queue.remove();
            ArrayList<String> parents = am.get(curr);

            if (parents == null) {
                rootFound = curr;
                break;
            }

            for (String p : parents) {
                if (!visited.contains(p)) {
                    visited.add(p);
                    queue.add(p);
                    parentMap.put(p, curr);
                }
            }
        }

        ArrayList<String> path = new ArrayList<>();
        String step = rootFound;

        while (true) {
            path.add(step);
            if (!parentMap.containsKey(step)) {
                break;
            }
            step = parentMap.get(step);
        }

        return path;
    }
}
