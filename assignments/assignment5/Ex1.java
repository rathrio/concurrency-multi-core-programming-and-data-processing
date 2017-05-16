import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ex1 {
    private static int[][] pixels;
    private static int width;
    private static int height;

    // If a pixel has >= d live neighbors, it will not be turned off.
    private static int d;

    // Loads pgm image to the 2D int array pixels in this class.
    private static void loadImage(String path) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));

            // Remove file type info
            lines.remove(0);

            // Remove comment
            lines.remove(0);

            String dimensions = lines.remove(0);

            // Remove max value
            lines.remove(0);

            String[] dimensionsArray = dimensions.split("\\s");

            width = Integer.parseInt(dimensionsArray[0]);
            height = Integer.parseInt(dimensionsArray[1]);

            pixels = new int[width][height];

            int x = 0;
            int y = 0;

            for (String line : lines) {
                int value = Integer.parseInt(line);
                pixels[x][y] = value;

                x = (x + 1) % width;
                if (x == 0) {
                    y++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeImage(String path) {
        Path filePath = Paths.get(path);
        ArrayList<String> lines = new ArrayList<>();

        lines.add("P2");
        lines.add("# CREATOR: Radi");
        lines.add(width + " " + height);
        lines.add("255");

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                lines.add(Integer.toString(pixels[x][y]));
            }
        }

        try {
            Files.write(filePath, lines, Charset.forName("ASCII"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void smoothEdges() {
    }

    public static void main(String[] args) {
        d = 3;
        loadImage("images/lena.pgm");
        writeImage("out.pgm");
    }
}
