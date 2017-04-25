class Spin {

	static volatile int counter;

	static class LockThread extends Thread {
		Lock lock;
		int iterations;
		int computations;

		public LockThread(Lock l, int n, int c) {
			lock = l;
			iterations = n;
			computations = c;
		}

		public void run() {
			while (true) {
				lock.lock();
				try {
					if (counter >= iterations)
						break;
					double d;
					for (int i = 0; i < computations; i++)
						d = Math.sin(i);
					counter++;
				} finally {
					lock.unlock();
				}
			}
		}
	}

	public static void main(String[] args) {
		Lock l;
		int max_threads = 2;
		int iterations = 1000000;
		int computations = 1000;
		if (args.length == 0) {
			System.out.println("Usage: java Spin (TaS|TaTaS|CLH) [max-threads [iterations [computations]]]");
			System.exit(0);
		}
		if ("tatas".equalsIgnoreCase(args[0])) {
			System.out.println("TaTaS Lock");
			l = new TaTaSLock();
		} else if ("CLH".equalsIgnoreCase(args[0])) {
			System.out.println("CLH Lock");
			l = new CLHLock();
		} else {
			System.out.println("TaS Lock");
			l = new TaSLock();
		}
		if (args.length > 1) {
			max_threads = Integer.parseInt(args[1]);
		}
		if (args.length > 2) {
			iterations = Integer.parseInt(args[2]);
		}
		if (args.length > 3) {
			computations = Integer.parseInt(args[3]);
		}
		LockThread[] t;
		for (int n = 2; n <= max_threads; n += (max_threads > 16 ? 2 : 1)) {
			counter = 0;
			t = new LockThread[n];
			for (int i = 0; i < t.length; i++)
				t[i] = new LockThread(l, iterations, computations);
			for (int i = 0; i < t.length; i++)
				t[i].start();
			long start = System.currentTimeMillis();
			for (int i = 0; i < t.length; i++) {
				try {
					t[i].join();
				} catch (InterruptedException e) {
				}
			}
			long end = System.currentTimeMillis();
			System.out.println("Threads: " + n + " - Duration: " + (end - start));
		}
	}
}