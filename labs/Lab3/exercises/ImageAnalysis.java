import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.concurrent.CyclicBarrier;

public class ImageAnalysis {
	private static final int NUM_THREADS = 9;
	private static BufferedImage img = null;
	private static int[] localBlackPixels = new int[NUM_THREADS-1];

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

	static class WorkerThread extends Thread{
		private int id;
		private CyclicBarrier barrier1;
		private CyclicBarrier barrier2;

		public WorkerThread(int id, CyclicBarrier barrier1, CyclicBarrier barrier2){
			this.id = id;
			this.barrier1 = barrier1;
			this.barrier2 = barrier2;
		}

		@Override
		public void run(){
			if (id == 0){	// only one thread loads the image
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
			else{				// all other workers
				try{
					barrier1.await();
				} catch(Exception e){
					System.err.println("Error in await()");
					System.exit(-1);
				}
				// compute the new parameters for your slice
				int height = img.getHeight();
				int width = img.getWidth()/(NUM_THREADS-1);
				int w_start = (id - 1)*width;
				int w_end = w_start + width;
				int blackPixels = analyseImage(img,height,w_start,w_end);
				localBlackPixels[id-1] = blackPixels;
				try{
					barrier2.await();
				} catch(Exception e){
					System.err.println("Error in await()");
					System.exit(-1);
				}
			}
		}
	}

	public static void main(String[] args) {

		Thread threads[] = new Thread[NUM_THREADS];
		CyclicBarrier barrier1 = new CyclicBarrier(NUM_THREADS); 
		CyclicBarrier barrier2 = new CyclicBarrier(NUM_THREADS); 

		// start threads one by one
		for (int i = 0; i < NUM_THREADS; i++){
			threads[i] = new WorkerThread(i,barrier1,barrier2);
			threads[i].start();
		}

		try {
			for (int i = 0; i < NUM_THREADS; i++){
				threads[i].join();
			}
		}catch (InterruptedException e){
			System.err.println("Error in join()");
		}
	}
}
