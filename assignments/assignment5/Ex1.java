import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class Ex1 {
    private static int[][] PIXELS;
    private static int WIDTH;
    private static int HEIGHT;

    // If a pixel has >= D live neighbors, it will not be turned off.
    private static int D;

    private static int ALIVENESS_THRESHOLD;
    private static int NUM_THREADS;

    static class Worker extends Thread {
        private int start, end, stripSize, paddedStripSize;
        private int[][] pixels, tmpPixels;
        private boolean changes;
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
            
            // the original right most index, e.g., 3 in a 4 pixel strip.
            this.stripSize = end - start;
            this.paddedStripSize = stripSize + 2;

            // Initialize strip with padding for left and right side
            this.pixels = new int[paddedStripSize][HEIGHT];

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = start; x < end; x++) {
                    int xx = x - start + 1;

                    pixels[xx][y] = PIXELS[x][y];
                }
            }

            this.sendLeft = sendLeft;
            this.sendRight = sendRight;
            this.receiveLeft = receiveLeft;
            this.receiveRight = receiveRight;

            this.changes = true;
        }

        @Override
        public void run() {
            tmpPixels = new int[paddedStripSize][HEIGHT];

//            while (changes) {
                sendLeft();
                sendRight();
                receiveRight();
                receiveLeft();

                for (int y = 0; y < HEIGHT; y++) {
                    for (int x = 1; x <= stripSize; x++) {
                        tmpPixels[x][y] = pixels[x][y];

                        // Skip dead pixels.
                        if (pixels[x][y] <= ALIVENESS_THRESHOLD) {
                            continue;
                        }

                        // If current pixel doesn't have at least D alive neighbors,
                        // turn it off.
                        if (numAliveNeighbors(x, y, pixels) < D) {
                            tmpPixels[x][y] = ALIVENESS_THRESHOLD;
                        }
                    }
                }

                if (Arrays.deepEquals(pixels, tmpPixels)) {
                    changes = false;
                }

                pixels = tmpPixels;


//            }

            updateGlobalPixels();
        }

        private void updateGlobalPixels() {
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 1; x <= stripSize; x++) {
                    int xx = x + start - 1;
                    PIXELS[xx][y] = pixels[x][y];
                }
            }
        }

        private void receiveLeft() {
            if (receiveLeft == null) {
                return;
            }

            try {
                pixels[0] = receiveLeft.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void receiveRight() {
            if (receiveRight == null) {
                return;
            }

            try {
                pixels[stripSize + 1] = receiveRight.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void sendRight() {
            if (sendRight == null) {
                return;
            }

            try {
                int[] send = new int[HEIGHT];
                System.arraycopy(pixels[stripSize],0, send, 0, HEIGHT);
                sendRight.put(send);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void sendLeft() {
            if (sendLeft == null) {
                return;
            }

            try {
                int[] send = new int[HEIGHT];
                System.arraycopy(pixels[1],0, send, 0, HEIGHT);
                sendLeft.put(send);
            } catch (InterruptedException e) {
                e.printStackTrace();
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

            // No need to communicate with further right if we're working with
            // rightmost strip.
            if (i == (NUM_THREADS - 1)) {
                end = WIDTH;
                sendRight = null;
                receiveRight = null;
            }

            Worker w = new Worker(start, end, sendLeft, sendRight, receiveLeft, receiveRight);
            workers[i] = w;

            // Next worker's left hand queues should be this worker's right hand queues.
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

//        while(changes) {
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
//        }

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
