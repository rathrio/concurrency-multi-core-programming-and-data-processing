package Lab8.exercises;

import java.util.concurrent.atomic.AtomicInteger;

public class UnboundedLockfreeQueue<T> implements Queue<T> {

    /**
     * First entry in queue.
     */
    Entry head;
    /**
     * Last entry in queue.
     */
    Entry tail;

    /**
     * Number of full slots.
     */
    AtomicInteger size;

    public UnboundedLockfreeQueue() {
        this.head = new Entry(null);
        this.tail = head;
        this.size = new AtomicInteger(0);
//        this.enqLock = new ReentrantLock();
//        this.notFullCondition = enqLock.newCondition();
//        this.deqLock = new ReentrantLock();
//        this.notEmptyCondition = deqLock.newCondition();
    }

    @Override
    public T deq() {
        T result;

        result = head.next.value;
        head = head.next;

        while (true) {
            if (size.get() == 0) {

            }
        }

        return result;
    }

    @Override
    public void enq(T x) {
        Entry e = new Entry(x);
        tail.next = e;
        tail = e;
    }

    private boolean tryEnq(T x) {
        return true;
    }

    private void backoff() {
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
