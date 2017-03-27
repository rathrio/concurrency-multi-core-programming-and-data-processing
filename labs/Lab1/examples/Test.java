
public class Test {

	/**
	 * Hello World .. threadified 
	 * @param delay - the delay per thread between printing the letters;
	 * 				  increase this in case thread interleaving is not obvious enough 
	 */
	public static void HelloWorld(int delay) {
		
		// creating two Printer threads saying hello to the world
		PrinterThread hello = new PrinterThread("hello",delay);
		PrinterThread world = new PrinterThread("world",delay);
		
		// starting the threads
		hello.start();
		world.start();
	}
	
	/**
	 * Multithreaded interval sum computation 
	 * @param from - start of interval
	 * @param to - end of interval
	 * @param threads - number of threads doing the computation
	 * 					(the interval will be split according to the 
	 * 					 threads number being rounded at the lowest value,
	 * 					 with the last thread getting the extra part
	 * 					 example: 1-10 ; 4 threads 
	 * 							  interval = 10 values / 4 = 2 per thread
	 * 							  but 2*4 = 8 .. 
	 * 							  the last 2 up to 10 are given to the last thread)
	 * @param superthread - true for considering one superthread;
	 * 						(in this case the interval will be split as if it was
	 * 						 one more extra thread to do the computation; 
	 * 						 each thread gets one part of the interval and the last
	 *                       one two parts + the eventual division remainder as in the
	 *                       sample above) 
	 * @param delay - delay to be added to each thread iteration when computing the sum
	 * 				  (increase to get more obvious results in case of small intervals)
	 * @throws InterruptedException
	 */
	public static void ComputeSum(int from, int to, 
								  int threads, boolean superthread,
								  int delay) 
		throws InterruptedException {
		SumThread[] sumThread = new SumThread[threads];
		int interval;
		int i = 0;
		long sum = 0;
		long time = 0;
		long max = 0;
		long min = Integer.MAX_VALUE;
		
		if (superthread) {
			interval = (to - from + 1)/(threads+1);
		}
		else
		{
			interval = (to - from + 1)/threads;
		}
		
		for (i = 0; i < threads-1; i++) {
			sumThread[i] = new SumThread(from+i*interval, from+(i+1)*interval-1, delay); 
		}
		sumThread[threads-1] = new SumThread(from+i*interval, to, delay);	
		
		for (i = 0; i < threads; i++) {
			sumThread[i].start();
		}
		
		for (i = 0; i < threads; i++) {
			sumThread[i].join();
		}
		
		for (i = 0; i < threads; i++) {
			//System.out.println("sum is " + sumThread[i].getSum());
			sum+=sumThread[i].getSum();
			time+=sumThread[i].getTime();
			if (sumThread[i].getTime() > max) 
				max = sumThread[i].getTime();
			if (sumThread[i].getTime() < min)
				min = sumThread[i].getTime();
		}
		
		System.out.println ("Sum from "+from+" to "+to+" is "+sum);
		System.out.println ("Computed by "+threads+" threads");
		if (superthread)
		System.out.println (".. one 'superthread' among them ..");
		System.out.println ("Statistics:");
		System.out.println ("Interval per thread "+interval);
		if (superthread)
		System.out.println (".. and for the superthread "+ (to-from-(threads-1)*interval+1));
		System.out.println ("Quickest thread finished in "+min);
		System.out.println ("Slowest thread finished in "+max);
		System.out.println ("Average per thread "+(float)time/threads);
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		// remove/add comments to run different tests
		// (normally should be done with arguments);
		
		// Hello World
		
		HelloWorld(0);
		
		// Sum
		/*
		ComputeSum(1, 1000, 20, true, 10);
		*/
		
	}

}
