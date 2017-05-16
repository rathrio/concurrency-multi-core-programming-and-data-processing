import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Ex1NonVolatile {
    private static int sharedCounter = 0;
    private static IncrementerThread[] threads;
    private static int[] accesses;

    private static int COUNTER_LIMIT;
    private static int NUM_THREADS;

    private static Lock lock;

    public static class CASLock implements Lock {
        // 0:unlocked, 1:locked
        protected AtomicInteger state = new AtomicInteger(0);

        @Override
        public void lock() {
            while (!state.compareAndSet(0, 1)) {} // Wait
        }

        @Override
        public void unlock() {
            state.set(0);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {}
        @Override
        public boolean tryLock() { return false;}
        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException { return false; }
        @Override
        public Condition newCondition() { return null; }
    }

    public static class CCASLock extends CASLock {
        @Override
        public void lock() {
            while(true) {
                // Wait until lock looks free
                while(state.get() == 1) {}
                // Try to acquire it
                if (state.compareAndSet(0, 1)) {
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
                lock.lock();

                if (sharedCounter < COUNTER_LIMIT) {
                    sharedCounter++;
                    accesses[id]++;
                }

                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Ex1Case1 <NUM_THREADS> <COUNTER_LIMIT> <LOCK 1=CAS 2=CCAS>");
            return;
        }

        NUM_THREADS = Integer.parseInt(args[0]);
        COUNTER_LIMIT = Integer.parseInt(args[1]);
        int casLockType = Integer.parseInt(args[2]);

        threads = new IncrementerThread[NUM_THREADS];
        accesses = new int[NUM_THREADS];

        if (casLockType == 1) {
            System.out.println("\nUsing CAS Lock...");
            lock = new CASLock();
        } else {
            System.out.println("\nUsing CCAS Lock...");
            lock = new CCASLock();
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

            long runTime = System.nanoTime() - startTime;
            double runTimeMs = runTime / 1000000.0;
            System.out.println("Runtime: " + runTimeMs + "ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // Print number of accesses per thread
        for (int i = 0; i < NUM_THREADS; i++) {
            System.out.println("Thread " + i + ": " + accesses[i]);
        }

        System.out.println("Shared counter: " + sharedCounter);
    }
}
