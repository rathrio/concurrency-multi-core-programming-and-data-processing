package steal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Fibonacci using Executor pools
 * @author Maurice Herlihy
 */
public class FibTaskBounded implements Callable<Integer> {
  static ExecutorService exec = Executors.newFixedThreadPool(256);
  int arg;

  public FibTaskBounded(int n) {
    arg = n;
  }

  public static void shutdown() {
    exec.shutdown();
  }

  public Integer call() {
    try {
      if (arg > 2) {
        Future<Integer> left = exec.submit(new FibTaskBounded(arg-1));
        if (arg > 8)
          left.get();
        Future<Integer> right = exec.submit(new FibTaskBounded(arg-2));
        return left.get() + right.get();
      } else {
        return 1;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return 1;
    }
  }
}