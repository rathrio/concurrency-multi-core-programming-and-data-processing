/* Parallel processing of a BMP test image
 * General indications: 
 *  -- main thread only starts the other threads and waits for them to finish 
 *  -- use a thread pool to automatically handle the threads (ExecutorService)   
 *  -- 2 types of threads: a master thread and more workers
 *        => 2 types of tasks: a single master task, multiple worker tasks
 *  -- master thread/task: loads the shared BMP image, waits for all workers to start (on barrier1),
 *                    waits for all workers to finish processing (on barrier2), and aggregates and outputs the result
 *  -- worker threads/tasks: sync with the master, get their part of the image, process it and return the result 
 *                    in a shared array on the position given by their id (see explanation below)
 *  -- worker threads process thier image based on an _unique ID_
 *  -- the unique ID has to be generated using atomic primitives, such that each time the worker task is run it gets the next ID in sequence
 *                    without depending on the thread that executes it (this way we decouple the threads from the slices)
 **/



import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.*; 
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageAnalysisExecutor {
  private static final int NUM_THREADS = 9;
  private static BufferedImage img = null;
  private static int[] localBlackPixels = new int[NUM_THREADS-1];

  // HERE: define the shared ID generator object
  public static IdGenerator idGenerator = new IdGenerator();


  //////////////////////////////////////////////////////////////////////////////////////////
  ////////// HELPER FUNCTIONS (_NO_ need to modify) ////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////
  static int analyseImage(BufferedImage img, int height, int w_start, int w_end){
    int blackPixels = 0;
    int rgb, red, green, blue;

    int index = (w_start == 0)?1:w_start;

    for (int h = 1; h<height; h++)
    {
      for (int w = index; w<w_end; w++)
      {
        rgb = img.getRGB(w, h);
        red = (rgb >> 16 ) & 0x000000FF;
        green = (rgb >> 8 ) & 0x000000FF;
        blue = (rgb) & 0x000000FF;

        if (red == 0 && green == 0 && blue == 0)
          blackPixels++;
      }
    }

    return blackPixels;
  }

  static BufferedImage loadImage() {
    BufferedImage img = null;
    try {
      img = ImageIO.read(new File("test.bmp"));
    } catch (IOException e) {

    }
    return img;
  }

  //////////////////////////////////////////////////////////////////////////////////////////
  ////////// END HELPER FUNCTIONS /////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////


  static class IdGenerator {
    private AtomicInteger id = new AtomicInteger(0);

  // HERE: define a method nextId() that returns the integers in sequence with no duplicates
    public int nextId() {
      return id.getAndIncrement();
    }
  }


  static class MasterThreadTask implements Runnable{
    private CyclicBarrier barrier1;
    private CyclicBarrier barrier2;

    // HERE: constructor (receives arguments the two barriers)
    public MasterThreadTask(CyclicBarrier barrier1, CyclicBarrier barrier2) {
      this.barrier1 = barrier1;
      this.barrier2 = barrier2;
    }


    // HERE: override run() method
    // hint: get code from the original implementation with threads
    public void run() {
      img = loadImage();
      try{
        barrier1.await();
        barrier2.await();
      } catch(Exception e){
        System.err.println("Error in await()");
        System.exit(-1);
      }
      int totalBlackPixels = 0;
      for (int i = 0; i < (NUM_THREADS-1); i++)
        totalBlackPixels += localBlackPixels[i];
      System.out.println("Total black pixels: " + totalBlackPixels);
    }
}

  static class WorkerThreadTask implements Runnable{
    private CyclicBarrier barrier1;
    private CyclicBarrier barrier2;
    
    // HERE: constructor (receives arguments the two barriers)
    public WorkerThreadTask(CyclicBarrier barrier1, CyclicBarrier barrier2) {
      this.barrier1 = barrier1;
      this.barrier2 = barrier2;
    }
  
    // HERE: override run() method
    // hint: get code from the original implementation with threads
    // Attention: generate unique id for each thread before computing the size of slice!!
    public void run() {
      int id = idGenerator.nextId();
      try{
        barrier1.await();
      } catch(Exception e){
        System.err.println("Error in await()");
        System.exit(-1);
      }
      // compute the new parameters for your slice
      int height = img.getHeight();
      int width = img.getWidth()/(NUM_THREADS-1);
      int w_start = (id)*width;
      int w_end = w_start + width;
      int blackPixels = analyseImage(img,height,w_start,w_end);
      localBlackPixels[id] = blackPixels;
      try{
        barrier2.await();
      } catch(Exception e){
        System.err.println("Error in await()");
        System.exit(-1);
      }
    }
  }

  public static void main(String[] args) {
    CyclicBarrier barrier1 = new CyclicBarrier(NUM_THREADS); 
    CyclicBarrier barrier2 = new CyclicBarrier(NUM_THREADS); 
    ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

    // HERE: create task for master thread (defined above)
    Runnable masterTask = new MasterThreadTask(barrier1, barrier2);
    // HERE: execute a single master task
    executor.execute(masterTask);

    // HERE: create task for worker thread (defined above)
    Runnable workerTaks = new WorkerThreadTask(barrier1, barrier2);
    // HERE: execute (NUM_THREADS-1) worker tasks
    for (int i = 0; i < NUM_THREADS-1; i++) {
      executor.execute(workerTaks);
    }

    executor.shutdown();
  }
}
