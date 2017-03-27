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
  private static IdGenerator gen = new IdGenerator();

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


	////////////// ID GENERATOR BEGIN /////////////////////////////
	////////////// THREADLOCAL-VERSION ////////////////////////////

	static class IdGenerator {
		// HERE: define atomic variable that gives integers in sequence
		private static AtomicInteger id = new AtomicInteger(0);

	    // HERE: define new Integer thread local variable
		// and initialize it with the next integer in the sequence
		private static final ThreadLocal<Integer> threadId = new ThreadLocal<Integer> () {
            @Override protected Integer initialValue() {
                return id.getAndIncrement();
            }
        };
			

		public static int nextId() {
          // HERE: return the value of thread local variable
		  return threadId.get();
		}

	}

	////////////// ID GENERATOR END /////////////////////////////////
	/////////////////////////////////////////////////////////////////
	

  static class MasterThreadTask implements Runnable{
    private CyclicBarrier barrier1;
    private CyclicBarrier barrier2;

    public MasterThreadTask(CyclicBarrier barrier1, CyclicBarrier barrier2){
      this.barrier1 = barrier1;
      this.barrier2 = barrier2;
    }

    @Override
      public void run(){
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

    public WorkerThreadTask(CyclicBarrier barrier1, CyclicBarrier barrier2){
      this.barrier1 = barrier1;
      this.barrier2 = barrier2;
    }

    @Override
      public void run(){
        try{
          barrier1.await();
        } catch(Exception e){
          System.err.println("Error in await()");
          System.exit(-1);
        }
        // compute the new parameters for your slice
        int id = gen.nextId();
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

    Runnable masterTask = new MasterThreadTask(barrier1,barrier2);
    executor.execute(masterTask);

    // start threads one by one
    Runnable workerTask = new WorkerThreadTask(barrier1,barrier2);
    for (int i = 0; i < NUM_THREADS-1; i++){
      executor.execute(workerTask);
    }

    executor.shutdown();

  }


}
