import lists.*;
import java.util.Random;

class Driver
{
  static final int RANGE = 65536;
  static final int DURATION = 5 * 1000; // 5 seconds

  static volatile boolean stop;

  static class ListThread extends Thread {

    private List<Integer> list;
    private int updateRate;
    private Random random;
    private long nbContains;
    private long nbUpdates;

    public ListThread(List<Integer> l, int u) {
      list = l;
      updateRate = u;
      random = new Random();
      nbContains = nbUpdates = 0;
    }

    public void run() {
      int last = -1;
      while (!stop) {
        int op = random.nextInt(100);
        if (op < updateRate) {
          // Update
          if (last < 0) {
            // Add random
            int i = random.nextInt(RANGE);
            if (list.add(new Integer(i)))
              last = i;
          } else {
            // Remove last
            list.remove(new Integer(last));
            last = -1;
          }
          nbUpdates++;
        } else {
          // Read-only
          int i = random.nextInt(RANGE);
          list.contains(new Integer(i));
          nbContains++;
        }
      }
    }

    public long getNbContains() {
      return nbContains;
    }

    public long getNbUpdates() {
      return nbUpdates;
    }
  }

  public static void usage() {
    System.out.println("java Driver class nb-threads update-rate initial-size");
    System.out.println("  class: CoarseList | FineList | OptimisticList | LazyList | LockFreeList");
    System.exit(1);
  }

  public static void main(String[] args) {

    List<Integer> list = null;
    int nbThreads;
    int updateRate;
    int initialSize;

    if (args.length < 4)
      usage();

    if ("CoarseList".equalsIgnoreCase(args[0]))
      list = new CoarseList<Integer>();
    else if ("FineList".equalsIgnoreCase(args[0]))
      list = new FineList<Integer>();
    else if ("OptimisticList".equalsIgnoreCase(args[0]))
      list = new OptimisticList<Integer>();
    else if ("LazyList".equalsIgnoreCase(args[0]))
      list = new LazyList<Integer>();
    else if ("LockFreeList".equalsIgnoreCase(args[0]))
      list = new LockFreeList<Integer>();

    nbThreads = Integer.parseInt(args[1]);
    updateRate = Integer.parseInt(args[2]);
    initialSize = Integer.parseInt(args[3]);

    if (list == null || nbThreads < 1 || updateRate < 0 || updateRate > 100 || initialSize < 0)
      usage();

    System.out.println("List class   : " + list.getClass().getName());
    System.out.println("Nb. threads  : " + nbThreads);
    System.out.println("Update rate  : " + updateRate);
    System.out.println("Initial size : " + initialSize);

    Random random = new Random();

    // Populate list
    for (int i = 0; i < initialSize; ) {
      if (list.add(new Integer(random.nextInt(RANGE))))
        i++;
    }

    // Create threads
    ListThread[] t = new ListThread[nbThreads];
    for (int i = 0; i < t.length; i++)
      t[i] = new ListThread(list, updateRate);

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
    long contains = 0;
    long updates = 0;
    for (int i = 0; i < t.length; i++) {
      contains += t[i].getNbContains();
      updates += t[i].getNbUpdates();
    }
    long ops = ((contains + updates) / ((end - start) / 1000));

    System.out.println("Nb. contains : " + contains);
    System.out.println("Nb. updates  : " + updates);
    System.out.println("Nb. op/s     : " + ops);

    System.out.println("==> " + String.format("%-20s", list.getClass().getName()) +
                       " t=" + String.format("%-2d", nbThreads) +
                       " u=" + String.format("%-3d", updateRate) +
                       " i=" + String.format("%-5d", initialSize) +
                       " : " + ops);
  }
}
