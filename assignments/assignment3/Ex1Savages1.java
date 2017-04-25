import java.util.concurrent.locks.ReentrantLock;

public class Ex1Savages1 {
    private static volatile int PORTIONS;
    private static volatile boolean potEmpty;

//    private static ReentrantLock cookLock = new ReentrantLock();
    private static ReentrantLock potLock = new ReentrantLock();

    public static class SavageThread extends Thread {
        private int id;
        
        public SavageThread(int id) {
            this.id = id;    
        }
        
        public void run() {
            potLock.lock();

            if (PORTIONS < 1) {
                potEmpty = true;
            }

            while (potEmpty) {
                // Wait for cook to refill
            }

            eat();

            potLock.unlock();
        }

        private void eat() {
            PORTIONS -= 1;
            System.out.println("Savage " + id + " ate.");
            System.out.println("PORTIONS = " + PORTIONS);
        }
    }

    public static class CookThread extends Thread {
        public void run() {
            while (true) {
                while (!potEmpty) {
                    // do nothing
                }

                PORTIONS += 1;
                potEmpty = false;
                System.out.println("Cook refilled portions to " + PORTIONS);
            }
        }
    }

    public static void main(String[] args) {
        PORTIONS = 5;

        CookThread cook = new CookThread();
        cook.start();

        for (int i = 0; i < PORTIONS + 3; i++) {
            new SavageThread().start();
        }
    }
}
