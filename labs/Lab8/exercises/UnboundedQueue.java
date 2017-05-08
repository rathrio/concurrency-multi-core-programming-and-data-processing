package Lab8.exercises;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bounded blocking queue
 * @param T item type
 * @author Maurice Herlihy
 */
public class UnboundedQueue<T> implements Queue<T> {
  /**
   * Lock out other enqueuers (dequeuers)
   */
  ReentrantLock enqLock, deqLock;
  /**
   * Wait/signal when queue is not empty or not full
   */
  Condition notEmptyCondition, notFullCondition;
  /**
   * Number of full slots.
   */
  AtomicInteger size;
  /**
   * First entry in queue.
   */
  Entry head;
  /**
   * Last entry in queue.
   */
  Entry tail;

  public UnboundedQueue() {
    this.head = new Entry(null);
    this.tail = head;
    this.size = new AtomicInteger(0);
    this.enqLock = new ReentrantLock();
    this.notFullCondition = enqLock.newCondition();
    this.deqLock = new ReentrantLock();
    this.notEmptyCondition = deqLock.newCondition();
  }
  /**
   * Remove and return head of queue.
   * @return remove first item in queue
   */
  public T deq() {
    T result;
    boolean mustWakeEnqueuers = true;
    deqLock.lock();
    try { Thread.sleep(1); } catch(InterruptedException e) { }
    try {
      while (size.get() == 0) {
        try {
          notEmptyCondition.await();
        } catch (InterruptedException ex) {}
      }
      result = head.next.value;
      head = head.next;

      size.decrementAndGet();
    } finally {
      deqLock.unlock();
    }

    return result;
  }
  /**
   * Append item to end of queue.
   * @param x item to append
   */
  public void enq(T x) {
    if (x == null) throw new NullPointerException();
    boolean mustWakeDequeuers = false;
    enqLock.lock();
    try { Thread.sleep(1); } catch(InterruptedException e) { }
    try {
      Entry e = new Entry(x);
      tail.next = e;
      tail = e;
      if (size.getAndIncrement() == 1) {
        mustWakeDequeuers = true;
      }
    } finally {
      enqLock.unlock();
    }
    if (mustWakeDequeuers) {
      deqLock.lock();
      try {
        notEmptyCondition.signalAll();
      } finally {
        deqLock.unlock();
      }
    }
  }
  /**
   * Individual queue item.
   */
  protected class Entry {
    /**
     * Actual value of queue item.
     */
    public T value;
    /**
     * next item in queue
     */
    public Entry next;
    /**
     * Constructor
     * @param x Value of item.
     */
    public Entry(T x) {
      value = x;
      next = null;
    }
  }
}
