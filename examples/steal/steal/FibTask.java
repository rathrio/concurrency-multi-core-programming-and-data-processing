/*
 * FibTask.java
 *
 * Created on January 21, 2006, 5:53 PM
 *
 * From "Multiprocessor Synchronization and Concurrent Data Structures",
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */

package steal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Fibonacci using Executor pools
 * @author Maurice Herlihy
 */
public class FibTask implements Callable<Integer> {
  static ExecutorService exec = Executors.newCachedThreadPool();
  static private boolean terminated = false;
  int arg;

  public FibTask(int n) {
    arg = n;
  }

  public static void shutdown() {
    exec.shutdown();
  }

  static public synchronized void terminate() {
    if (!terminated) {
      System.err.println("Cannot create thread (OutOfMemoryError): terminating...");
      terminated = true;
    }
  }

  public Integer call() {
    try {
      if (arg > 2) {
        Future<Integer> left = exec.submit(new FibTask(arg-1));
        Future<Integer> right = exec.submit(new FibTask(arg-2));
        return left.get() + right.get();
      } else {
        return 1;
      }
    } catch (Exception e) {
      terminate();
      System.exit(1);
      return 1;
    }
  }
}