/*
 * FibThread.java
 *
 * Created on January 21, 2006, 5:46 PM
 *
 * From "Multiprocessor Synchronization and Concurrent Data Structures",
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */

package steal;

/**
 * Fibonacci implementation using threads
 * @author Maurice Herlihy
 */
public class FibThread extends Thread {
  static private boolean terminated = false;
  private int arg;
  private int result;

  public FibThread(int n) {
    arg = n;
    result = -1;
  }

  public int get() {
    return result;
  }

  static public synchronized void terminate() {
    if (!terminated) {
      System.err.println("Cannot create thread (OutOfMemoryError): terminating...");
      terminated = true;
    }
  }

  public void run() {
    try {
      FibThread left, right;
      if (arg < 2) {
        result = arg;
      } else {
        left = new FibThread(arg-1);
        right = new FibThread(arg-2);
        left.start();
        right.start();
        try {
          left.join();
          right.join();
        } catch (InterruptedException e) { };
        result = left.result + right.result;
      }
    } catch(OutOfMemoryError err) {
      terminate();
      System.exit(1);
    }
  }
}