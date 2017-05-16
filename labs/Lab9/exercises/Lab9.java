public class Lab9 {
    private static volatile int sharedCounter = 0;
    private static IncrementerThread[] threads;
    private static ReaderThread[] readerThreads;
    private static int[] accesses;

    private static int COUNTER_LIMIT;
    private static int NUM_THREADS;

    private static ReadWriteLock lock;

    public static class ReadWriteLock {
        private int readers = 0;
        private int writers = 0;

        private boolean wannaWrite = false;

        public synchronized void lockRead() throws InterruptedException {
            while(wannaWrite || writers > 0) {
                wait();
            }
            readers++;
        }

        public synchronized void unlockRead() {
            readers--;
            notifyAll();
        }

        public synchronized void lockWrite() throws InterruptedException {
            // Request write. Reader lock will consider this flag.
            // Otherwise writers would starve as long as readers keep reading.
            wannaWrite = true;
            while(readers > 0 || writers > 0) {
                wait();
            }
            writers++;
        }

        public synchronized void unlockWrite() throws InterruptedException {
            writers--;
            wannaWrite = false;
            notifyAll();
        }
    }

    public static class ReaderThread extends Thread {
        public void run() {
            while (true) {
                try {
                    lock.lockRead();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Read and do nothing
                int counter = sharedCounter;
                System.out.println("Reading counter: " + counter);

                lock.unlockRead();

                if (sharedCounter == COUNTER_LIMIT) {
                    return;
                }
            }
        }
    }

    public static class IncrementerThread extends Thread {
        private int id;

        public IncrementerThread(int id) {
            this.id = id;
        }

        public void run() {
            while (sharedCounter < COUNTER_LIMIT) {
                try {
                    lock.lockWrite();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (sharedCounter < COUNTER_LIMIT) {
                    System.out.println("Writing counter: " + sharedCounter);
                    sharedCounter++;
                    accesses[id]++;
                }

                try {
                    lock.unlockWrite();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Ex1Case1 <NUM_THREADS> <COUNTER_LIMIT>");
            return;
        }

        NUM_THREADS = Integer.parseInt(args[0]);
        COUNTER_LIMIT = Integer.parseInt(args[1]);

        threads = new IncrementerThread[NUM_THREADS];
        readerThreads = new ReaderThread[5];

        accesses = new int[NUM_THREADS];

        lock = new ReadWriteLock();

        // Initialize and start 5 reader threads that keep on reading
        for (int i = 0; i < 5; i++) {
            ReaderThread t = new ReaderThread();
            readerThreads[i] = t;
            t.start();
        }

        // Initialize NUM_THREADS threads
        for (int i = 0; i < NUM_THREADS; i++) {
            IncrementerThread t = new IncrementerThread(i);
            threads[i] = t;
        }

        // Start threads, wait for them to finish and print the time
        // it took to increment the counter.
        try {
            long startTime = System.nanoTime();

            for (int i = 0; i < NUM_THREADS; i++) {
                threads[i].start();
            }

            for (int i = 0; i < NUM_THREADS; i++) {
                threads[i].join();
            }

            for (int i = 0; i < 5; i++) {
                readerThreads[i].join();
            }

            long runTime = System.nanoTime() - startTime;
            double runTimeMs = runTime / 1000000.0;
            System.out.println("Runtime: " + runTimeMs + "ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
