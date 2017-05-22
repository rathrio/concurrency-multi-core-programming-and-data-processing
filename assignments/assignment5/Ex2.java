import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Ex2 {
    private static int[][] PIXELS;
    private static int WIDTH;
    private static int HEIGHT;

    private static double[][] KERNEL = new double[3][3];

    private static int NUM_THREADS;

    static class Worker extends Thread {
        private int start, end;
        private CyclicBarrier barrier;

        public Worker(int start, int end, CyclicBarrier barrier) {
            this.start = start;
            this.end = end;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            int[][] tmpPixels = new int[WIDTH][HEIGHT];

            // Apply the kernel on local strip.
            for (int y = start; y < end; y++) {
                for (int x = 1; x < (WIDTH - 1); x++) {
                    double[][] neighborhood = neighborhoodMatrix(x, y, PIXELS);
                    tmpPixels[x][y] = applyKernel(neighborhood);
                }
            }

            // Wait for others to be done.
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }

            // Update global pixels with strip value.
            for (int y = start; y < end; y++) {
                for (int x = 1; x < (WIDTH - 1); x++) {
                    PIXELS[x][y] = tmpPixels[x][y];
                }
            }
        }
    }

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

    // Sequential version that was used for comparison. Feel free to ignore this.
    private static void sequentialApplyEffect() {
        System.out.println("Running sequentially...");

        long startTime = System.nanoTime();

        int[][] tmpPixels = new int[WIDTH][HEIGHT];

        for (int y = 1; y < (HEIGHT - 1); y++) {
            for (int x = 1; x < (WIDTH - 1); x++) {
                double[][] neighborhood = neighborhoodMatrix(x, y, PIXELS);
                tmpPixels[x][y] = applyKernel(neighborhood);
            }
        }

        PIXELS = tmpPixels;

        long runTime = System.nanoTime() - startTime;
        double runTimeMs = runTime / 1000000.0;
        System.out.println("Runtime: " + runTimeMs + "ms");
    }

    // Since we're not dealing with "iterative" data parallelism, there's no
    // need for a heartbeat approach. Threads do not depend on updated neighbor
    // values, so they may apply the kernel on their strip, and simply wait for
    // all other threads before updating the global pixels.
    //
    // The waiting is done with a CyclicBarrier.
    private static void parallelApplyEffect() {
        System.out.println("Running concurrently...");

        long startTime = System.nanoTime();

        Worker[] workers = new Worker[NUM_THREADS];
        CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS);

        int stripSize = (int) Math.ceil(HEIGHT / NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * stripSize;
            int end = start + stripSize;

            // Don't try to work on border strips. We'll ignore them, as described
            // here: http://setosa.io/ev/image-kernels/.
            if (start == 0) {
                start = 1;
            }

            if (i == (NUM_THREADS - 1)) {
                end = HEIGHT - 1;
            }

            Worker w = new Worker(start, end, barrier);
            workers[i] = w;
            w.start();
        }

        // Wait for all workers to update PIXELS.
        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                workers[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long runTime = System.nanoTime() - startTime;
        double runTimeMs = runTime / 1000000.0;
        System.out.println("Runtime: " + runTimeMs + "ms");
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

    private static double[][] neighborhoodMatrix(int x, int y, int[][] matrix) {
        double[][] mat = new double[3][3];

        mat[0] = new double[]{matrix[x - 1][y - 1], matrix[x - 1][y], matrix[x - 1][y + 1]};
        mat[1] = new double[]{matrix[x][y - 1], matrix[x][y], matrix[x][y + 1]};
        mat[2] = new double[]{matrix[x + 1][y - 1], matrix[x + 1][y], matrix[x + 1][y + 1]};

        return mat;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Ex1 <IMAGE> <EFFECT> <NUM_THREADS>");
            System.exit(1);
        }

        String path = args[0];
        String effect = args[1];
        NUM_THREADS = Integer.parseInt(args[2]);

        switch (effect.toLowerCase()) {
            case "blur":
                KERNEL[0] = new double[]{0.0625, 0.125, 0.0625};
                KERNEL[1] = new double[]{0.125, 0.25, 0.125};
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

        loadImage(path);

        if (NUM_THREADS == 1) {
            sequentialApplyEffect();
            writeImage("out_sequential.pgm");
        } else {
            parallelApplyEffect();
            writeImage("out.pgm");
        }
    }
}
