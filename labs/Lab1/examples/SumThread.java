
public class SumThread extends Thread {
	
	int from, to, delay;
	long sum;
	long time;
	
	public SumThread (int from, int to, int delay) {
		this.from = from;
		this.to = to;
		sum = 0;
		time = 0;
		this.delay = delay;
	}
	
	public void run() {
		long start, stop;
		
		start = System.currentTimeMillis();
		for (int i = from; i <= to; i++) {
			sum += i; 
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		stop = System.currentTimeMillis();
		
		time = stop - start;
		//System.out.println(time);
	}
	
	public long getSum() {
		return sum;
	}
	
	public long getTime() {
		return time;
	}

}
