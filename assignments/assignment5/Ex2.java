import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ex2 {
    private static int[][] PIXELS;
    private static int WIDTH;
    private static int HEIGHT;

    private static double[][] KERNEL = new double[3][3];

    private static int NUM_THREADS;

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

    private static void sequentialApplyEffect() {
        long startTime = System.nanoTime();

        int[][] tmpPixels = new int[WIDTH][HEIGHT];

        for (int y = 1; y < (HEIGHT - 1); y++) {
            for (int x = 1; x < (WIDTH - 1); x++) {
                double[][] neighborhood = neighborhoodMatrix(x, y);
                tmpPixels[x][y] = applyKernel(neighborhood);
            }
        }
        PIXELS = tmpPixels;

        long runTime = System.nanoTime() - startTime;
        double runTimeMs = runTime / 1000000.0;
        System.out.println("Runtime: " + runTimeMs + "ms");
    }

    private static void parallelApplyEffect() {

    }

    private static int applyKernel(double[][] neighborhood) {
        double value = 0;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                value += (KERNEL[x][y] * neighborhood[x][y]);
            }
        }

        return (int) Math.round(value);
    }

    private static double[][] neighborhoodMatrix(int x, int y) {
        double[][] mat = new double[3][3];
        mat[0] = new double[]{PIXELS[x - 1][y - 1], PIXELS[x - 1][y], PIXELS[x - 1][y + 1]};
        mat[1] = new double[]{PIXELS[x][y - 1], PIXELS[x][y], PIXELS[x][y + 1]};
        mat[2] = new double[]{PIXELS[x + 1][y - 1], PIXELS[x + 1][y], PIXELS[x + 1][y + 1]};
        return mat;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Ex1 <IMAGE> <EFFECT>");
            return;
        }

        String path = args[0];
        String effect = args[1];

        switch (effect.toLowerCase()) {
            case "blur":
                KERNEL[0] = new double[]{0.0625, 0.125, 0.0625};
                KERNEL[1] = new double[]{0.0125, 0.25, 0.125};
                KERNEL[2] = new double[]{0.0625, 0.125, 0.0625};
                break;
            case "sharpen":
                KERNEL[0] = new double[]{0, -1, 0};
                KERNEL[1] = new double[]{-1, 5, -1};
                KERNEL[2] = new double[]{0, -1, 0};
                break;
            case "outline":
                KERNEL[0] = new double[]{-1, -1, -1};
                KERNEL[1] = new double[]{-1, 8, -1};
                KERNEL[2] = new double[]{-1, -1, -1};
                break;
            case "emboss":
                KERNEL[0] = new double[]{-2, -1, 0};
                KERNEL[1] = new double[]{-1, 1, 1};
                KERNEL[2] = new double[]{0, 1, 2};
                break;
            default:
                System.out.printf("Unknown effect \"%s\". Use blur, sharpen, outline or emboss.%n", effect);
                System.exit(1);
        }

        NUM_THREADS = 4;

        loadImage(path);
        sequentialApplyEffect();
        writeImage("out.pgm");
    }
}