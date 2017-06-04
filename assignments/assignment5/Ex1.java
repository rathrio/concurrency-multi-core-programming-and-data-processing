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

    // 0. Can be set to higher values when processing images that don't have
    // any 0 black values.
    private static int ALIVENESS_THRESHOLD;

    // Number of workers.
    private static int NUM_THREADS;

    static class Coordinator extends Thread {
        private ArrayBlockingQueue<Boolean>[] results, answers;

        public Coordinator(ArrayBlockingQueue<Boolean>[] results, ArrayBlockingQueue<Boolean>[] answers) {
            this.results = results;
            this.answers = answers;
        }

        @Override
        public void run() {
            boolean chg, changes = true;

            while (changes) {
                changes = false;

                for (int i = 0; i < results.length; i++) {
                    try {
                        chg = results[i].take();
                        changes = changes || chg;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < answers.length; i++) {
                    try {
                        answers[i].put(changes);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    static class Worker extends Thread {
        private int id, start, end, stripSize, paddedStripSize;
        private int[][] pixels, tmpPixels;
        private boolean changes;
        private ArrayBlockingQueue<int[]> sendLeft, sendRight, receiveLeft, receiveRight;
        private ArrayBlockingQueue<Boolean> result, answer;

        public Worker(int id,
                      int start,
                      int end,
                      ArrayBlockingQueue<int[]> sendLeft,
                      ArrayBlockingQueue<int[]> sendRight,
                      ArrayBlockingQueue<int[]> receiveLeft,
                      ArrayBlockingQueue<int[]> receiveRight,
                      ArrayBlockingQueue<Boolean> result,
                      ArrayBlockingQueue<Boolean> answer) {

            this.id = id;

            this.start = start;
            this.end = end;

            // The original size
            this.stripSize = end - start;

            this.paddedStripSize = stripSize + 2;

            // Initialize strip with padding for left and right column. Since
            // Java arrays int arrays are instantiated with 0 by default,
            // we don't have to handle the border cases here. 0 values don't
            // impair the neighborhood lookup.
            this.pixels = new int[paddedStripSize][HEIGHT];

            // Copy my strip from global pixels
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = start; x < end; x++) {
                    // Translate to pixels coordinates.
                    int xx = x - start + 1;
                    pixels[xx][y] = PIXELS[x][y];
                }
            }

            // Use array blocking queues for communicating with neighbors.
            // Main thread takes care of correctly setting the queues.
            this.sendLeft = sendLeft;
            this.sendRight = sendRight;
            this.receiveLeft = receiveLeft;
            this.receiveRight = receiveRight;

            this.result = result;
            this.answer = answer;

            // Have I changed since previous iteration?
            this.changes = true;
        }

        @Override
        public void run() {
            while (changes) {
                // Word on copy to be able to test for changes.
                tmpPixels = new int[paddedStripSize][HEIGHT];

                // Communicate with neighbors in order to correctly count
                // alive pixels on border cols.
                sendLeft();
                sendRight();
                receiveRight();
                receiveLeft();

                // For each pixel on my strip...
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

                changes = false;

                // If anything on my local strip changed compared to the
                // previous iteration, do the whole thing again.
                for (int x = 1; x <= stripSize; x++) {
                    if (!Arrays.equals(pixels[x], tmpPixels[x])) {
                        changes = true;
                        break;
                    }
                }

                try {
                    result.put(changes);
                    changes = answer.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                pixels = tmpPixels;
            }

            updateGlobalPixels();
        }

        private void updateGlobalPixels() {
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 1; x <= stripSize; x++) {
                    // Translate to PIXELS coordinates.
                    int xx = x + start - 1;
                    PIXELS[xx][y] = pixels[x][y];
                }
            }
        }

        private void receiveLeft() {
            // Do nothing if we don't have a left neighbor.
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
            // Do nothing if we don't have a right neighbor.
            if (receiveRight == null) {
                return;
            }

            try {
                pixels[stripSize + 1] = receiveRight.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Send a copy of rightmost col to right neighbor.
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

        // Send a copy of leftmost col to left neighbor.
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

        // For communicating with coordinator, as described in heartbeat.pdf
        ArrayBlockingQueue[] results = new ArrayBlockingQueue[NUM_THREADS];
        ArrayBlockingQueue[] answers = new ArrayBlockingQueue[NUM_THREADS];

        // First workers left hand queues will be null, since there's noone to
        // communicate with.
        ArrayBlockingQueue<int[]> sendLeft = null;
        ArrayBlockingQueue<int[]> receiveLeft = null;

        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * stripSize;
            int end = start + stripSize;

            ArrayBlockingQueue<int[]> sendRight = new ArrayBlockingQueue<>(1);
            ArrayBlockingQueue<int[]> receiveRight = new ArrayBlockingQueue<>(1);
            results[i] = new ArrayBlockingQueue<>(1);
            answers[i] = new ArrayBlockingQueue<>(1);

            // No need to communicate with further right if we're working with
            // rightmost strip.
            if (i == (NUM_THREADS - 1)) {
                end = WIDTH;
                sendRight = null;
                receiveRight = null;
            }

            Worker w = new Worker(i, start, end, sendLeft, sendRight,
                    receiveLeft, receiveRight, results[i], answers[i]);

            workers[i] = w;

            // Next worker's left hand queues should be this worker's right hand queues.
            sendLeft = receiveRight;
            receiveLeft = sendRight;
        }

        Coordinator coordinator = new Coordinator(results, answers);
        coordinator.start();

        for (int i = 0; i < NUM_THREADS; i++) {
            workers[i].start();
        }

        try {
            for (int i = 0; i < NUM_THREADS; i++) {
                workers[i].join();

            }
            coordinator.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
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

        // In hindsight, this would definitely be prettier as a loop...
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