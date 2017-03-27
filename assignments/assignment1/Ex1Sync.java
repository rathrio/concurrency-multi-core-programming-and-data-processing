public class Ex1Sync {
    public static int sharedCounter = 0;
    public static int n;
    public static int m;
    public static int i;

    public static class IncrementerThread extends Thread {
        public void run() {
            // Use class as identifier for synchronisation
            synchronized (Ex1Sync.class) {
                for (int j = 0; j < i; j++) {
                    int counter = sharedCounter;
                    counter++;
                    sharedCounter = counter;
                }
            }
        }
    }

    public static class DecrementerThread extends Thread {
        public void run() {
            // Use class as identifier for synchronisation
            synchronized (Ex1Sync.class) {
                for (int j = 0; j < i; j++) {
                    int counter = sharedCounter;
                    counter--;
                    sharedCounter = counter;
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Ex1NoSync n m i");
            return;
        }

        // Extract n, m and i from CLI args
        n = Integer.parseInt(args[0]);
        m = Integer.parseInt(args[1]);
        i = Integer.parseInt(args[2]);

        IncrementerThread[] incrementerThreads = new IncrementerThread[n];
        DecrementerThread[] decrementerThreads = new DecrementerThread[m];

        long startTime = System.nanoTime();

        // Start n incrementer threads
        for (int j = 0; j < n; j++) {
            IncrementerThread t = new IncrementerThread();
            incrementerThreads[j] = t;
            t.start();
        }

        // Start m decrementer threads
        for (int j = 0; j < m; j++) {
            DecrementerThread t = new DecrementerThread();
            decrementerThreads[j] = t;
            t.start();
        }

        // Wait for incrementer threads
        for (int j = 0; j < n; j++) {
            try {
                incrementerThreads[j].join();
            } catch(Exception e) {
                System.out.println("Error in inrementer threads");
                e.printStackTrace();
            }
        }

        // Wait for decrementer threads
        for (int j = 0; j < m; j++) {
            try {
                decrementerThreads[j].join();
            } catch(Exception e) {
                System.out.println("Error in decrementer threads");
                e.printStackTrace();
            }
        }

        long runTime = System.nanoTime() - startTime;

        System.out.println("Shared counter: " + sharedCounter);
        System.out.println("Duration: " + runTime + "ns");
    }
}