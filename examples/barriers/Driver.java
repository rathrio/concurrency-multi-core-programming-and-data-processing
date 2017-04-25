import barriers.*;

class Driver
{
  static class BarrierThread extends Thread {

    private Barrier barrier;
    int index;
    int rounds;
    int[] log;

    public BarrierThread(Barrier b, int i, int r, int[] l) {
      barrier = b;
      index = i;
      rounds = r;
      log = l;
      log[index] = -1;
    }

    public void run() {
      ThreadID.set(index);
      for (int round = 0; round < rounds; round++) {
        log[index] = round;
        barrier.await();
        for (int i = 0; i < log.length; i++) {
          int r = log[i];
          if (r != round) {
            System.out.format("[%d]\tError expected %d found %d at %d\n", index, round, r, i);
            System.exit(1);
          }
        }
        barrier.await();
      }
    }
  }

  public static void usage() {
    System.out.println("java Driver class nb-threads rounds");
    System.out.println("  class: SenseBarrier | SenseBarrierYield | TreeBarrier");
    System.exit(1);
  }

  public static void main(String[] args) {

    Barrier barrier = null;
    int nbThreads, nbRounds;

    if (args.length < 3)
      usage();

    nbThreads = Integer.parseInt(args[1]);
    nbRounds = Integer.parseInt(args[2]);

    if (nbThreads < 1 || nbRounds <= 0)
      usage();

    if ("SenseBarrier".equalsIgnoreCase(args[0]))
      barrier = new SenseBarrier(nbThreads);
    else if ("SenseBarrierYield".equalsIgnoreCase(args[0]))
      barrier = new SenseBarrierYield(nbThreads);
    else if ("TreeBarrier".equalsIgnoreCase(args[0])) {
      if ((nbThreads & (nbThreads - 1)) != 0) {
        System.out.println("Number of threads must be a power of 2 with TreeBarrier");
        System.exit(1);
      }
      barrier = new TreeBarrier(nbThreads, 2);
    }

    if (barrier == null)
      usage();

    System.out.println("Barrier class  : " + barrier.getClass().getName());
    System.out.println("Nb. threads    : " + nbThreads);
    System.out.println("Nb. rounds     : " + nbRounds);

    int[] log = new int[nbThreads];

    // Create threads
    BarrierThread[] t = new BarrierThread[nbThreads];
    for (int i = 0; i < t.length; i++)
      t[i] = new BarrierThread(barrier, i, nbRounds, log);

    // Start threads
    System.out.print("Starting threads...");
    for (int i = 0; i < t.length; i++)
      t[i].start();
    System.out.println(" DONE");

    long start = System.currentTimeMillis();

    // Wait for threads to complete
    System.out.print("Stopping threads...");
    for (int i = 0; i < t.length; i++) {
      try {
        t[i].join();
      } catch (InterruptedException e) {
      }
    }
    long end = System.currentTimeMillis();
    System.out.println(" DONE");

    // Print statistics
    long time = (end - start);

    System.out.println("==> " + String.format("%-20s", barrier.getClass().getName()) +
                       " t=" + String.format("%-2d", nbThreads) +
                       " r=" + String.format("%-9d", nbRounds) +
                       " : " + time);
  }
}
