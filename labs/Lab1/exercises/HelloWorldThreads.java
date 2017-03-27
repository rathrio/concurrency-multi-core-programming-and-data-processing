import java.lang.Thread;

public class HelloWorldThreads {

	private static final int NUM_THREADS = 8;

	static class PrinterThread extends Thread {

		// print Hello World from each thread
		// print the name of the thread as well (Thread.getName())
    public void run() {
      System.out.println("Hello World" + this.getName());
    }
	}

	public static void main(String[] args) throws InterruptedException
	{
		Thread threads[] = new Thread[NUM_THREADS];

		for (int i = 0; i < NUM_THREADS; i++){
      Thread t = new PrinterThread();
      threads[i] = t;
      t.start();
		// start threads one by one
		}

	 	Thread.sleep(1000);

		// Wait for all the threads to finish in the main thread
		for (int i = 0; i < NUM_THREADS; i++){
      threads[i].join();
		// start threads one by one
		}
		
	}
}
