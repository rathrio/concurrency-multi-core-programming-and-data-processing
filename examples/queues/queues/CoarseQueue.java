/*
 * CoarseQueue.java
 *
 * Created on December 27, 2005, 7:14 PM
 *
 * The Art of Multiprocessor Programming, by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */
package queues;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CoarseQueue<T> implements Queue<T> {
  T items[];
  int head, size;
  int capacity;
  /**
   * Constructor.
   * @param capacity Max number of items allowed in queue.
   */
  public CoarseQueue(int capacity) {
    items = (T[]) new Object[capacity];
    head = size = 0;
    this.capacity = capacity;
  }
  /**
   * Remove and return head of queue.
   * @return remove first item in queue
   */
  public synchronized T deq() {
    try { Thread.sleep(1); } catch(InterruptedException e) { }
    while (size == 0) {
      try {
        wait();
      } catch (InterruptedException ex) {
      }
    }
    notifyAll();
    size--;
    return items[head++ % capacity];
  }
  /**
   * Append item to end of queue.
   * @param x item to append
   */
  public synchronized void enq(T x) {
    try { Thread.sleep(1); } catch(InterruptedException e) { }
    while (size == capacity) {
      try {
        wait();
      } catch (InterruptedException ex) {
      }
    }
    notifyAll();
    items[(head + size) % capacity] = x;
    size++;
  }
}
