import steal.*;

class Driver
{
  public static void usage() {
    System.out.println("java Driver class max-value");
    System.out.println("  class: FibThread | FibTask | FibTaskBounded");
    System.exit(1);
  }

  public static void main(String[] args) {

    if (args.length < 2)
      usage();

    int n = Integer.parseInt(args[1]);
    if ("FibThread".equalsIgnoreCase(args[0])) {
      for (int i = 1; i <= n; i++) {
        long start = System.currentTimeMillis();
        FibThread f = new FibThread(i);
        f.start();
        try {
          f.join();
        } catch (InterruptedException e) {
        }
        int result = f.get();
        long end = System.currentTimeMillis();
        System.out.println("FibThread      : " + i + "->" + result + " ==> " + (end - start) + " ms");
      }
    } else if ("FibTask".equalsIgnoreCase(args[0])) {
      for (int i = 1; i <= n; i++) {
        long start = System.currentTimeMillis();
        FibTask f = new FibTask(i);
        int result = f.call();
        long end = System.currentTimeMillis();
        System.out.println("FibTask        : " + i + "->" + result + " ==> " + (end - start) + " ms");
      }
      FibTask.shutdown();
    } else if ("FibTaskBounded".equalsIgnoreCase(args[0])) {
      for (int i = 1; i <= n; i++) {
        long start = System.currentTimeMillis();
        FibTaskBounded f = new FibTaskBounded(i);
        int result = f.call();
        long end = System.currentTimeMillis();
        System.out.println("FibTaskBounded : " + i + "->" + result + " ==> " + (end - start) + " ms");
      }
      FibTaskBounded.shutdown();
    } else
      usage();
  }
}
