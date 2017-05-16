import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ex1 {
    private static int[][] PIXELS;
    private static int WIDTH;
    private static int HEIGHT;

    // If a pixel has >= D live neighbors, it will not be turned off.
    private static int D;

    private static int ALIVENESS_THRESHOLD;

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

            WIDTH = Integer.parseInt(dimensionsArray[0]);
            HEIGHT = Integer.parseInt(dimensionsArray[1]);

            PIXELS = new int[WIDTH][HEIGHT];

            int x = 0;
            int y = 0;

            for (String line : lines) {
                PIXELS[x][y] = Integer.parseInt(line);

                x = (x + 1) % WIDTH;
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
        lines.add(WIDTH + " " + HEIGHT);
        lines.add("255");

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                lines.add(Integer.toString(PIXELS[x][y]));
            }
        }

        try {
            Files.write(filePath, lines, Charset.forName("ASCII"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sequentialSmoothEdges() {
        long startTime = System.nanoTime();

        int[][] tmpPixels = null;

//        while (tmpPixels != PIXELS) {

        for (int i = 0; i < 30; i++) {
            tmpPixels = PIXELS.clone();

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    int value = PIXELS[x][y];

                    // Skip dead pixels.
                    if (value <= ALIVENESS_THRESHOLD) {
                        continue;
                    }

                    // If current pixel doesn't have at least D alive neighbors,
                    // turn it off.
                    if (numAliveNeighbors(x, y) < D) {
                        tmpPixels[x][y] = ALIVENESS_THRESHOLD;
                    }
                }
            }

            PIXELS = tmpPixels;
        }

//        }

        long runTime = System.nanoTime() - startTime;
        double runTimeMs = runTime / 1000000.0;
        System.out.println("Runtime: " + runTimeMs + "ms");
    }

    private static int numAliveNeighbors(int x, int y) {
        ArrayList<Integer> neighbors = new ArrayList<>();

        if (x == 0 && y == 0) {
            // Top-left corner
            neighbors.add(PIXELS[x + 1][y]);
            neighbors.add(PIXELS[x + 1][y + 1]);
            neighbors.add(PIXELS[x][y + 1]);
        } else if ((x == (WIDTH - 1)) && (y == (HEIGHT - 1))) {
            // Bottom-right corner
            neighbors.add(PIXELS[x - 1][y]);
            neighbors.add(PIXELS[x - 1][y - 1]);
            neighbors.add(PIXELS[x][y - 1]);
        } else if ((x == (WIDTH - 1)) && (y == 0)) {
            // Top-right corner
            neighbors.add(PIXELS[x - 1][y]);
            neighbors.add(PIXELS[x - 1][y + 1]);
            neighbors.add(PIXELS[x][y + 1]);
        } else if ((x == 0) && (y == (HEIGHT - 1))) {
            // Bottom-left corner
            neighbors.add(PIXELS[x][y - 1]);
            neighbors.add(PIXELS[x + 1][y - 1]);
            neighbors.add(PIXELS[x + 1][y]);
        } else if (x == 0) {
            // Left edge
            neighbors.add(PIXELS[x][y - 1]);
            neighbors.add(PIXELS[x + 1][y - 1]);
            neighbors.add(PIXELS[x + 1][y]);
            neighbors.add(PIXELS[x + 1][y + 1]);
            neighbors.add(PIXELS[x][y + 1]);
        } else if (x == (WIDTH - 1)) {
            // Right edge
            neighbors.add(PIXELS[x][y - 1]);
            neighbors.add(PIXELS[x - 1][y - 1]);
            neighbors.add(PIXELS[x - 1][y]);
            neighbors.add(PIXELS[x - 1][y + 1]);
            neighbors.add(PIXELS[x][y + 1]);
        } else if (y == 0) {
            // Top edge
            neighbors.add(PIXELS[x - 1][y]);
            neighbors.add(PIXELS[x - 1][y + 1]);
            neighbors.add(PIXELS[x][y + 1]);
            neighbors.add(PIXELS[x + 1][y + 1]);
            neighbors.add(PIXELS[x + 1][y]);
        } else if (y == (HEIGHT - 1)) {
            // Bottom edge
            neighbors.add(PIXELS[x - 1][y]);
            neighbors.add(PIXELS[x - 1][y - 1]);
            neighbors.add(PIXELS[x][y - 1]);
            neighbors.add(PIXELS[x + 1][y - 1]);
            neighbors.add(PIXELS[x + 1][y]);
        } else {
            // Somewhere in the middle
            neighbors.add(PIXELS[x - 1][y]);
            neighbors.add(PIXELS[x + 1][y]);
            neighbors.add(PIXELS[x][y - 1]);
            neighbors.add(PIXELS[x][y + 1]);
            neighbors.add(PIXELS[x - 1][y - 1]);
            neighbors.add(PIXELS[x + 1][y + 1]);
            neighbors.add(PIXELS[x + 1][y - 1]);
            neighbors.add(PIXELS[x - 1][y + 1]);
        }

        // Count alive neighbors.
        int count = 0;
        for (int n : neighbors) {
            if (n > ALIVENESS_THRESHOLD) {
                count++;
            }
        }

        return count;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Ex1 <D> <ALIVENESS_THRESHOLD>");
            return;
        }

        D = Integer.parseInt(args[0]);
        ALIVENESS_THRESHOLD = Integer.parseInt(args[1]);

        loadImage("images/hasselhoff.pgm");
        sequentialSmoothEdges();
        writeImage("out.pgm");
    }
}
