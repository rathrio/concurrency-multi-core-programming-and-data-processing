import java.util.concurrent.Semaphore;

public class Ex2 {
    private static int NUM_THREADS;
    private static int BUFFER_SIZE;

    static class CircularBuffer {
        private int size;
        private int in;
        private int out;
        private String[] buffer;
        private Semaphore sem;

        public CircularBuffer(int size) {
            this.size = size;
            this.in = 0;
            this.out = 0;
            this.buffer = new String[size];

            // One binary semaphore to guard buffer modifications
            this.sem = new Semaphore(1);
        }

        public void produce(String data) throws InterruptedException {
            // Make sure only one thread is producing
            sem.acquire();

            int nextIn = (in + 1) % size;

            // Abort the mission when we'd get ahead of the read pointer, i.e.,
            // the buffer is full. Release the lock so that other threads may
            // continue.
            if (nextIn == out) {
                System.out.println("Buffer full");
                sem.release();
                return;
            }

            System.out.println("Producing at " + in);
            buffer[in] = data;
            in = nextIn;

            sem.release();
        }

        public String consume() throws InterruptedException {
            sem.acquire();

            // Nothing to read yet. Abort mission and release the lock so that
            // other threads may continue.
            if (in == out) {
                System.out.println("Buffer empty");
                sem.release();
                return "";
            }

            System.out.println("Consuming at " + out);
            String data = buffer[out];
            out = (out + 1) % size;

            sem.release();

            return data;
        }

        public void releaseLock() {
            sem.release();
        }
    }

    static class ProducerThread extends Thread {
        private CircularBuffer buffer;

        public ProducerThread(CircularBuffer buffer) {
            this.buffer = buffer;
        }

        public void run() {
            try {
                buffer.produce("foobar");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class ConsumerThread extends Thread {
        private CircularBuffer buffer;

        public ConsumerThread(CircularBuffer buffer) {
            this.buffer = buffer;
        }

        public void run() {
            try {
                this.buffer.consume();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Ex2 t n");
            return;
        }

        // Extract number of threads (t) and circular buffer size (n) from CLI args
        NUM_THREADS = Integer.parseInt(args[0]);
        BUFFER_SIZE = Integer.parseInt(args[1]);

        ProducerThread producerThreads[] = new ProducerThread[NUM_THREADS];
        ConsumerThread consumerThreads[] = new ConsumerThread[NUM_THREADS];

        CircularBuffer buffer = new CircularBuffer(BUFFER_SIZE);

        // Start t number of producer and consumer threads
        for (int i = 0; i < NUM_THREADS; i++) {
            ProducerThread producerThread = new ProducerThread(buffer);
            producerThreads[i] = producerThread;
            producerThread.start();

            ConsumerThread consumerThread = new ConsumerThread(buffer);
            consumerThreads[i] = consumerThread;
            consumerThread.start();
        }

        // Wait for all threads to finish
        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                producerThreads[i].join();
                consumerThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}