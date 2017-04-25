import java.util.concurrent.locks.ReentrantLock;

public class Ex2Philosophers {
    public static int NUM_PHILOSOPHERS;

    // Handles access to forks. Forks are basically a bunch of mutexes in an array.
    public static class Table {
        private ReentrantLock[] forks;

        public Table(int numForks) {
            this.forks = new ReentrantLock[numForks];

            for (int i = 0; i < numForks; i++) {
                this.forks[i] = new ReentrantLock();
            }
        }

        public void grabFork(int position) {
            this.forks[position].lock();
        }

        public void dropFork(int position) {
            this.forks[position].unlock();
        }
    }

    public static class PhilosopherThread extends Thread {
        private int id;

        // Indices of forks on table
        private int lForkPosition;
        private int rForkPosition;

        private Table table;

        public PhilosopherThread(int id, Table table) {
            this.id = id;

            // Determine which forks to use
            this.lForkPosition = id;
            this.rForkPosition = id + 1;
            if (rForkPosition == NUM_PHILOSOPHERS) {
                rForkPosition = 0;
            }

            this.table = table;
        }

        public void run() {
            while (true) {
                // Make sure we acquire and drop the forks in a consistent order (first left, then right),
                // so that we avoid a deadlock.
                table.grabFork(lForkPosition);
                table.grabFork(rForkPosition);

                eat();

                table.dropFork(lForkPosition);
                table.dropFork(rForkPosition);

                think();
            }
        }

        private void eat() {
            System.out.println("Philosopher " + id + " is eating.");
            randomSleep();
        }

        private void think() {
            System.out.println("Philosopher " + id + " is thinking.");
            randomSleep();
        }

        private void randomSleep() {
            try {
                Thread.sleep((long)(Math.random() * 500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Ex2Philosophers <NUM_PHILOSOPHERS>");
            return;
        }

        NUM_PHILOSOPHERS = Integer.parseInt(args[0]);

        Table table = new Table(NUM_PHILOSOPHERS);

        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            new PhilosopherThread(i, table).start();
        }

        // The dine forever. Program must be cancelled with CTRL-C.
    }
}
