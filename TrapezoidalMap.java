import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TrapezoidalMap {
    public record Point (int id, int x, int y, boolean isStart){}

    public record Segment (int id, Point start, Point end) {}

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1){
            System.out.println("Usage: ./TrapezoidalMap \"input.txt\"");
        } else {
            File in = new File(args[0]);
            Scanner scan = new Scanner(new File(args[0]));
            int n = Integer.parseInt(scan.nextLine());
            String[] start = scan.nextLine().split(" ");
            ArrayList<ArrayList<Integer>> matrix = new ArrayList<>();
            while (scan.hasNextLine()){
                String[] line = scan.nextLine().split(" ");
                ArrayList<Integer> i = new ArrayList<>();
                for(String l : line){
                    i.add(Integer.parseInt(l));
                }
                matrix.add(i);
            }
        }
    }
}
