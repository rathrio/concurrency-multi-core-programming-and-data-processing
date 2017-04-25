/*
 * Create synthetic data set to test KMeans cluustering algorithm
 */
package mapreduce;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mph
 */
public class ClusterGen {

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Usage: java mapreduce.ClusterGen nb-clusters nb-points filename");
			System.exit(1);
		}
		int numClusters = Math.max(1, Integer.parseInt(args[0]));
		int numPoints = Math.max(1, Integer.parseInt(args[1]));
		String fileName = args[2];
		Random random = new Random();
		Point[] center = new Point[numClusters];
		Point[] points = new Point[numClusters * numPoints];
		double radians = (2 * Math.PI) / numClusters;
		// easier to plot in Excel if all coordinates are positive
		Point displacement = new Point(5,5);
		for (int i = 0; i < numClusters; i++) {
			double angle = radians * i;
			double radius = numClusters;
			center[i] = new Point(radius * Math.cos(angle), radius * Math.sin(angle)).plus(displacement);
		}
		int cursor = 0;
		for (int i = 0; i < numClusters; i++) {
			for (int j = 0; j < numPoints; j++) {
				points[cursor++] = new Point(
								center[i].getX() + random.nextGaussian(),
								center[i].getY() + random.nextGaussian());
			}
		}
		try {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
				for (Point point : points) {
					writer.write(String.format("%f\t%f\n", point.getX(), point.getY()));
				}
			}
			System.out.println("Wrote file \"" + fileName + "\" with " + numClusters + " clusters and " + numPoints + " points");
		} catch (IOException ex) {
			Logger.getLogger(ClusterGen.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
