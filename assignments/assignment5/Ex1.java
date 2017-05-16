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

    // If a pixel has >= D live neighbors, it will not be turned off.
    private static int D;

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
                pixels[x][y] = Integer.parseInt(line);

                x = (x + 1) % width;
                if (x == 0) {
                    y++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Writes this class' 2D pixel array to a pgm file at path.
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

    private static void sequentialSmoothEdges() {
        int[][] tmpPixels = pixels.clone();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = pixels[x][y];

                // Skip not alive pixels.
                if (value < 255) {
                    continue;
                }

                // If current pixel doesn't have at least D alive neighbors,
                // turn it off.
                if (numAliveNeighbors(x, y) < D) {
                    tmpPixels[x][y] = 255;
                }
            }
        }

        pixels = tmpPixels;
    }

    private static int numAliveNeighbors(int x, int y) {
        ArrayList<Integer> neighbors = new ArrayList<Integer>();

        if (x == 0 && y == 0) {
            // Top-left corner
            neighbors.add(pixels[x + 1][y]);
            neighbors.add(pixels[x + 1][y + 1]);
            neighbors.add(pixels[x][y + 1]);
        } else if ((x == (width - 1)) && (y == (height - 1))) {
            // Bottom-right corner
            neighbors.add(pixels[x - 1][y]);
            neighbors.add(pixels[x - 1][y - 1]);
            neighbors.add(pixels[x][y - 1]);
        } else if ((x == (width - 1)) && (y == 0)) {
            // Top-right corner
            neighbors.add(pixels[x - 1][y]);
            neighbors.add(pixels[x - 1][y + 1]);
            neighbors.add(pixels[x][y + 1]);
        } else if ((x == 0) && (y == (height - 1))) {
            // Bottom-left corner
            neighbors.add(pixels[x][y - 1]);
            neighbors.add(pixels[x + 1][y - 1]);
            neighbors.add(pixels[x + 1][y]);
        }


        return 0;
    }

    public static void main(String[] args) {
        D = 3;
        loadImage("images/lena.pgm");
        sequentialSmoothEdges();
        writeImage("out.pgm");
    }
}
