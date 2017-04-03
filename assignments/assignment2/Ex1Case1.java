import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Ex1Case1 {
    private static volatile int sharedCounter = 0;
    private static IncrementerThread[] threads;
    private static int[] accesses;

    private static int COUNTER_LIMIT;
    private static int NUM_THREADS;

    private static PetersonLock lock;

    public static class PetersonLock {
        private int n;
        private AtomicIntegerArray level;
        private AtomicIntegerArray victim;

        public PetersonLock(int n) {
            this.n = n;
            this.level = new AtomicIntegerArray(n);
            this.victim = new AtomicIntegerArray(n);
        }

        public void lock(int id) {
            ArrayList<Integer> otherThreadIds = threadIdsExcept(id);

            for (int l = 1; l < n; l++) {
                // I'm interested and waiting in room l
                level.set(id, l);

                // Let me be the victim
                victim.set(l, id);

                // While I'm the victim in room l and other guys are waiting in this
                // or "higher" rooms...
                while (anotherThreadWaiting(otherThreadIds, l) && victim.get(l) == id) {
                    // ...busy wait.
                }
                // As soon as someone else offers to be the victim, I can proceed.
                // That's also the case when nobody else is waiting with or above me.
            }
        }

        // I'm no longer in any room.
        public void unlock(int id) {
            level.set(id, 0);
        }

        // i.e. another thread is on the same or higher level.
        private boolean anotherThreadWaiting(ArrayList<Integer> threadIds, int currentLevel) {
            boolean otherWaiting = false;

            for (int threadId : threadIds) {
                if (level.get(threadId) >= currentLevel) {
                    otherWaiting = true;
                    continue;
                }
            }

            return otherWaiting;
        }

        // Helper to determine IDs of other threads.
        private ArrayList<Integer> threadIdsExcept(int id) {
            ArrayList<Integer> otherThreadIds = new ArrayList<Integer>();

            for (int i = 0; i < n; i++) {
                if (i == id) {
                    continue;
                }

                otherThreadIds.add(i);
            }

            return otherThreadIds;
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
        if (args.length < 2) {
            System.out.println("Usage: java Ex1Case1 <NUM_THREADS> <COUNTER_LIMIT>");
            return;
        }

        NUM_THREADS = Integer.parseInt(args[0]);
        COUNTER_LIMIT = Integer.parseInt(args[1]);

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
