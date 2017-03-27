import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Ex1Case1 {
    private static volatile int sharedCounter = 0;
    private static IncrementerThread[] threads;
    private static int[] accesses;

    private static int COUNTER_LIMIT;
    private static int NUM_THREADS;

    private static PetersonLock lock;
    private static final ReentrantLock rlock = new ReentrantLock();

    public static class PetersonLock {
        private int n;
        private AtomicInteger[] level;
        private AtomicInteger[] victim;


        public PetersonLock(int n) {
            this.n = n;
            this.level = new AtomicInteger[n];
            this.victim = new AtomicInteger[n - 1];
        }

        public void lock(int id) {
            for (int i = 0; i < n; i++) {
                level[id].set(i);
                victim[i].set(id);

                while (victim[i].get() == id) {
                    // busy wait
                }
            }
        }

        public void unlock(int id) {
//            level[id].set(0);
        }
    }

    public static class IncrementerThread extends Thread {
        private int id;

        public IncrementerThread(int id) {
            this.id = id;
        }

        public void run() {
            while (sharedCounter < COUNTER_LIMIT) {
                lock.lock(id);

                if (sharedCounter < COUNTER_LIMIT) {
                    sharedCounter++;
                    accesses[id]++;
                }

                lock.unlock(id);
            }
        }
    }


    public static void main(String[] args) {
        COUNTER_LIMIT = 1000;
        NUM_THREADS = 4;

        threads = new IncrementerThread[NUM_THREADS];
        accesses = new int[NUM_THREADS];

        lock = new PetersonLock(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            IncrementerThread t = new IncrementerThread(i);
            threads[i] = t;
            t.start();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            IncrementerThread t = threads[i];

            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Thread " + i + ": " + accesses[i]);
        }

        System.out.println("Shared counter: " + sharedCounter);
    }
}
