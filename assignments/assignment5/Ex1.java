import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class Ex1 {
    private static int[][] PIXELS;
    private static int WIDTH;
    private static int HEIGHT;

    // If a pixel has >= D live neighbors, it will not be turned off.
    private static int D;

    private static int ALIVENESS_THRESHOLD;
    private static int NUM_THREADS;

    static class Worker extends Thread {
        private int start, end, width;
        private int[][] pixels;
        private ArrayBlockingQueue<int[]> sendLeft, sendRight;
        private ArrayBlockingQueue<int[]> receiveLeft, receiveRight;

        public Worker(int start,
                      int end,
                      ArrayBlockingQueue<int[]> sendLeft,
                      ArrayBlockingQueue<int[]> sendRight,
                      ArrayBlockingQueue<int[]> receiveLeft,
                      ArrayBlockingQueue<int[]> receiveRight) {

            this.start = start;
            this.end = end;
            this.width = end - start;

            // Initialize strip
            this.pixels = new int[width + 2][HEIGHT];

            for (int x = 1; x <= width; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    // Java ints are immutable, so no need for clone.
                    pixels[x][y] = PIXELS[x][y];
                }
            }

            this.sendLeft = sendLeft;
            this.sendRight = sendRight;
            this.receiveLeft = receiveLeft;
            this.receiveRight = receiveRight;
        }

        @Override
        public void run() {
            int[][] tmpPixels = new int[width + 2][HEIGHT];

            if (sendLeft != null) {
                try {
                    sendLeft.put(tmpPixels[1]);
                    System.out.println("sent to left = " + start);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (sendRight != null) {
                try {
                    sendRight.put(tmpPixels[width]);
                    System.out.println("sent to right = " + start);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (receiveRight != null) {
                try {
                    tmpPixels[width + 1] = receiveRight.take();
                    System.out.println("taken from right = " + start);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (receiveLeft != null) {
                try {
                    tmpPixels[0] = receiveLeft.take();
                    System.out.println("taken from left = " + start);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

//
//            for (int y = 0; y < HEIGHT; y++) {
//                for (int x = 0; x < width; x++) {
//                    tmpPixels[x][y] = pixels[x][y];
//
//                    // Skip dead pixels.
//                    if (pixels[x][y] <= ALIVENESS_THRESHOLD) {
//                        continue;
//                    }
//
//                    // If current pixel doesn't have at least D alive neighbors,
//                    // turn it off.
//                    if (numAliveNeighbors(x, y, pixels) < D) {
//                        tmpPixels[x][y] = ALIVENESS_THRESHOLD;
//                    }
//                }
//            }
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

    private static void parallelRemoveHotPixels() {
        System.out.println("Running concurrently...");
        long startTime = System.nanoTime();

        Worker[] workers = new Worker[NUM_THREADS];
        int stripSize = (int) Math.ceil(WIDTH / NUM_THREADS);

        // First workers left hand queues will be null, since there's noone to
        // communicate with.
        ArrayBlockingQueue<int[]> sendLeft = null;
        ArrayBlockingQueue<int[]> receiveLeft = null;

        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * stripSize;
            int end = start + stripSize;

            ArrayBlockingQueue<int[]> sendRight = new ArrayBlockingQueue<>(1);
            ArrayBlockingQueue<int[]> receiveRight = new ArrayBlockingQueue<>(1);

            // For worker that deals with last strip...
            if (i == (NUM_THREADS - 1)) {
                // make sure we don't iterate too far
                end = WIDTH - 1;
                // and that we don't try the send and receive stuff at the very right.
                sendRight = null;
                receiveRight = null;
            }

            Worker w = new Worker(start, end, sendLeft, sendRight, receiveLeft, receiveRight);
            workers[i] = w;

            // Next workers left hand queues should be this workers right hand queues.
            sendLeft = receiveRight;
            receiveLeft = sendRight;
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            workers[i].start();
        }


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

    // Sequential version that was used for comparison. Feel free to ignore this.
    private static void sequentialRemoveHotPixels() {
        System.out.println("Running sequentially...");
        long startTime = System.nanoTime();

        int[][] tmpPixels = null;
        boolean changes = true;

        while(changes) {
            tmpPixels = new int[WIDTH][HEIGHT];

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    tmpPixels[x][y] = PIXELS[x][y];

                    // Skip dead pixels.
                    if (PIXELS[x][y] <= ALIVENESS_THRESHOLD) {
                        continue;
                    }

                    // If current pixel doesn't have at least D alive neighbors,
                    // turn it off.
                    if (numAliveNeighbors(x, y, PIXELS) < D) {
                        tmpPixels[x][y] = ALIVENESS_THRESHOLD;
                    }
                }
            }

            if (Arrays.deepEquals(PIXELS, tmpPixels)) {
                changes = false;
            }

            PIXELS = tmpPixels;
        }

        long runTime = System.nanoTime() - startTime;
        double runTimeMs = runTime / 1000000.0;
        System.out.println("Runtime: " + runTimeMs + "ms");
    }

    private static int numAliveNeighbors(int x, int y, int[][] pixels) {
        ArrayList<Integer> neighbors = new ArrayList<>();

        if (x == 0 && y == 0) {
            // Top-left corner
            neighbors.add(pixels[x + 1][y]);
            neighbors.add(pixels[x + 1][y + 1]);
            neighbors.add(pixels[x][y + 1]);
        } else if ((x == (WIDTH - 1)) && (y == (HEIGHT - 1))) {
            // Bottom-right corner
            neighbors.add(pixels[x - 1][y]);
            neighbors.add(pixels[x - 1][y - 1]);
            neighbors.add(pixels[x][y - 1]);
        } else if ((x == (WIDTH - 1)) && (y == 0)) {
            // Top-right corner
            neighbors.add(pixels[x - 1][y]);
            neighbors.add(pixels[x - 1][y + 1]);
            neighbors.add(pixels[x][y + 1]);
        } else if ((x == 0) && (y == (HEIGHT - 1))) {
            // Bottom-left corner
            neighbors.add(pixels[x][y - 1]);
            neighbors.add(pixels[x + 1][y - 1]);
            neighbors.add(pixels[x + 1][y]);
        } else if (x == 0) {
            // Left edge
            neighbors.add(pixels[x][y - 1]);
            neighbors.add(pixels[x + 1][y - 1]);
            neighbors.add(pixels[x + 1][y]);
            neighbors.add(pixels[x + 1][y + 1]);
            neighbors.add(pixels[x][y + 1]);
        } else if (x == (WIDTH - 1)) {
            // Right edge
            neighbors.add(pixels[x][y - 1]);
            neighbors.add(pixels[x - 1][y - 1]);
            neighbors.add(pixels[x - 1][y]);
            neighbors.add(pixels[x - 1][y + 1]);
            neighbors.add(pixels[x][y + 1]);
        } else if (y == 0) {
            // Top edge
            neighbors.add(pixels[x - 1][y]);
            neighbors.add(pixels[x - 1][y + 1]);
            neighbors.add(pixels[x][y + 1]);
            neighbors.add(pixels[x + 1][y + 1]);
            neighbors.add(pixels[x + 1][y]);
        } else if (y == (HEIGHT - 1)) {
            // Bottom edge
            neighbors.add(pixels[x - 1][y]);
            neighbors.add(pixels[x - 1][y - 1]);
            neighbors.add(pixels[x][y - 1]);
            neighbors.add(pixels[x + 1][y - 1]);
            neighbors.add(pixels[x + 1][y]);
        } else {
            // Somewhere in the middle
            neighbors.add(pixels[x - 1][y]);
            neighbors.add(pixels[x + 1][y]);
            neighbors.add(pixels[x][y - 1]);
            neighbors.add(pixels[x][y + 1]);
            neighbors.add(pixels[x - 1][y - 1]);
            neighbors.add(pixels[x + 1][y + 1]);
            neighbors.add(pixels[x + 1][y - 1]);
            neighbors.add(pixels[x - 1][y + 1]);
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
        if (args.length < 4) {
            System.out.println("Usage: java Ex1 <IMAGE> <D> <NUM_THREADS> <ALIVENESS_THRESHOLD>");
            return;
        }

        String path = args[0];
        D = Integer.parseInt(args[1]);
        NUM_THREADS = Integer.parseInt(args[2]);
        ALIVENESS_THRESHOLD = Integer.parseInt(args[3]);

        loadImage(path);

        if (NUM_THREADS == 1) {
            sequentialRemoveHotPixels();
            writeImage("out_sequential.pgm");
        } else {
            parallelRemoveHotPixels();
            writeImage("out.pgm");
        }
    }
}
