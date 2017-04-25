import queues.*;
import java.util.Random;

class Driver
{
  static final int RANGE = 65536;
  static final int DURATION = 5 * 1000; // 5 seconds

  static volatile boolean stop;

  static class QueueThread extends Thread {

    private Queue<Integer> queue;
    private Random random;
    private long nbUpdates;

    public QueueThread(Queue<Integer> q) {
      queue = q;
      random = new Random();
      nbUpdates = 0;
    }

    public void run() {
      boolean add = true;
      while (!stop) {
        // Update
        if (add) {
          // Enqueue random
          int i = random.nextInt(RANGE);
          queue.enq(new Integer(i));
        } else {
          // Dequeue
          queue.deq();
        }
        add = !add;
        nbUpdates++;
      }
    }

    public long getNbUpdates() {
      return nbUpdates;
    }
  }

  public static void usage() {
    System.out.println("java Driver class nb-threads capacity");
    System.out.println("  class: CoarseQueue | BoundedQueue | LockFreeQueue");
    System.exit(1);
  }

  public static void main(String[] args) {

    Queue<Integer> queue = null;
    int nbThreads;
    int capacity;

    if (args.length < 3)
      usage();

    nbThreads = Integer.parseInt(args[1]);
    capacity = Integer.parseInt(args[2]);

    if ("CoarseQueue".equalsIgnoreCase(args[0]))
      queue = new CoarseQueue<Integer>(capacity);
    else if ("BoundedQueue".equalsIgnoreCase(args[0]))
      queue = new BoundedQueue<Integer>(capacity);
    else if ("LockFreeQueue".equalsIgnoreCase(args[0]))
      queue = new LockFreeQueue<Integer>();

    if (queue == null || nbThreads < 1 || capacity <= 0)
      usage();

    System.out.println("Queue class  : " + queue.getClass().getName());
    System.out.println("Nb. threads  : " + nbThreads);
    System.out.println("Capacity     : " + capacity);

    Random random = new Random();

    // Populate queue
    for (int i = 0; i < capacity / 2; i++) {
      queue.enq(new Integer(random.nextInt(RANGE)));
    }

    // Create threads
    QueueThread[] t = new QueueThread[nbThreads];
    for (int i = 0; i < t.length; i++)
      t[i] = new QueueThread(queue);

    // Start threads
    stop = false;
    System.out.print("Starting threads...");
    for (int i = 0; i < t.length; i++)
      t[i].start();
    System.out.println(" DONE");

    // Let test run for some time
    long start = System.currentTimeMillis();
    try {
      Thread.sleep(DURATION);
    } catch (InterruptedException e) {
    }
    long end = System.currentTimeMillis();

    // Stop threads
    stop = true;

    // Wait for threads to complete
    System.out.print("Stopping threads...");
    for (int i = 0; i < t.length; i++) {
      try {
        t[i].join();
      } catch (InterruptedException e) {
      }
    }
    System.out.println(" DONE");

    // Print statistics
    long updates = 0;
    for (int i = 0; i < t.length; i++) {
      updates += t[i].getNbUpdates();
    }
    long ops = updates / ((end - start) / 1000);

    System.out.println("Nb. updates  : " + updates);
    System.out.println("Nb. op/s     : " + ops);

    System.out.println("==> " + String.format("%-20s", queue.getClass().getName()) +
                       " t=" + String.format("%-2d", nbThreads) +
                       " c=" + String.format("%-5d", capacity) +
                       " : " + ops);
  }
}
