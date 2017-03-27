import java.util.concurrent.*;

public class HelloWorldExecutor {

    private static final int NUM_THREADS = 8;
    private static final int NUM_RUNS = 20;

    // implement the Runnable task
    static class MyRun implements Runnable {
        // print Hello World from the current thread
        // to get the thread name use Thread.currentThread().getName()

        public void run() {
            System.out.println("Hello World-" + Thread.currentThread().getName());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // create Runnable task
        Runnable r = new MyRun();

        // create executor service with NUM_THREADS fixed thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

        // execute task NUM_RUNS times
        for (int i = 0; i < NUM_RUNS; i++) {
            executorService.execute(r);
        }

        Thread.sleep(1000);

        // don't forget to shut the executor down
        executorService.shutdown();
    }
}
