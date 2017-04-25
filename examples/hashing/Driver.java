import hashing.*;
import java.util.Random;

class Driver
{
  static final int RANGE = 65536;
  static final int CAPACITY = 256;
  static final int DURATION = 5 * 1000; // 5 seconds

  static volatile boolean stop;

  static class HashThread extends Thread {

    private Set<Integer> set;
    private int updateRate;
    private Random random;
    private long nbContains;
    private long nbUpdates;

    public HashThread(Set<Integer> s, int u) {
      set = s;
      updateRate = u;
      random = new Random();
      nbContains = nbUpdates = 0;
    }

    public void run() {
      while (!stop) {
        int op = random.nextInt(100);
        if (op < updateRate) {
          // Update
          if (random.nextBoolean()) {
            // Add random
            int i = random.nextInt(RANGE);
            set.add(new Integer(i));
          } else {
            // Remove random
            int i = random.nextInt(RANGE);
            set.remove(new Integer(i));
          }
          nbUpdates++;
        } else {
          // Read-only
          int i = random.nextInt(RANGE);
          set.contains(new Integer(i));
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
    System.out.println("  class: CoarseHashSet | StripedHashSet | SplitOrderHashSet");
    System.exit(1);
  }

  public static void main(String[] args) {

    Set<Integer> set = null;
    int nbThreads;
    int updateRate;
    int initialSize;

    if (args.length < 4)
      usage();

    nbThreads = Integer.parseInt(args[1]);
    updateRate = Integer.parseInt(args[2]);
    initialSize = Integer.parseInt(args[3]);

    if ("CoarseHashSet".equalsIgnoreCase(args[0]))
      set = new CoarseHashSet<Integer>(Math.max(CAPACITY, initialSize));
    else if ("StripedHashSet".equalsIgnoreCase(args[0]))
      set = new StripedHashSet<Integer>(Math.max(CAPACITY, initialSize));
    else if ("SplitOrderHashSet".equalsIgnoreCase(args[0]))
      set = new SplitOrderHashSet<Integer>(RANGE);

    if (set == null || nbThreads < 1 || updateRate < 0 || updateRate > 100 || initialSize < 0)
      usage();

    System.out.println("Set class    : " + set.getClass().getName());
    System.out.println("Nb. threads  : " + nbThreads);
    System.out.println("Update rate  : " + updateRate);
    System.out.println("Initial size : " + initialSize);

    Random random = new Random();

    // Populate set
    for (int i = 0; i < initialSize; ) {
      if (set.add(new Integer(random.nextInt(RANGE))))
        i++;
    }

    // Create threads
    HashThread[] t = new HashThread[nbThreads];
    for (int i = 0; i < t.length; i++)
      t[i] = new HashThread(set, updateRate);

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

    System.out.println("==> " + String.format("%-20s", set.getClass().getName()) +
                       " t=" + String.format("%-2d", nbThreads) +
                       " u=" + String.format("%-3d", updateRate) +
                       " i=" + String.format("%-5d", initialSize) +
                       " : " + ops);
  }
}
