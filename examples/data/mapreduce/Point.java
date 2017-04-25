/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapreduce;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import static mapreduce.KMeans.numClusters;

/**
 *
 * @author mph
 */
public class Point {

	static Random random = new Random();

	private final double x;
	private final double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double distance(Point other) {
		return Math.sqrt(
						(this.getX() - other.getX()) * (this.getX() - other.getX())
						+ (this.getY() - other.getY()) * (this.getY() - other.getY()));
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	public Point plus(Point other) {
		if (other == null) {
			return this;
		} else {
			return new Point(this.x + other.x, this.y + other.y);
		}
	}

	public Point scale(double value) {
		return new Point(value * this.x, value * this.y);
	}

	@Override
	public String toString() {
		return String.format("Point[%f,%f]", x, y);
	}

	static public Point barycenter(List<Point> cluster) {
		double numPoints = (double) cluster.size();
		Optional<Point> sum = cluster
						.stream()
						.reduce(Point::plus);
		return sum.get().scale(1 / numPoints);
	}

	static public Point parallelBarycenter(List<Point> cluster) {
		double numPoints = (double) cluster.size();
		Optional<Point> sum = cluster
						.parallelStream()
						.reduce(Point::plus);
		return sum.get().scale(1 / numPoints);
	}

	static public Stream<Point> randomPointStream() {
		return Stream.generate(
						() -> new Point(random.nextDouble(), random.nextDouble())
		);
	}

	static public Map<Integer, Point> randomDistinctCenters(List<Point> points) {
		Map<Integer, Point> myCenters = new HashMap<>(numClusters);
		int size = points.size();
		boolean[] bitmap = new boolean[size];
		int index = 0;
		while (index < numClusters) {
			int i = random.nextInt(size);
			if (!bitmap[i]) {
				myCenters.put(index++, points.get(i));
				bitmap[i] = true;
			}
		}
		return myCenters;
	}

	public static class WeightedPoint {

		public Point point;
		public double weight;

		public WeightedPoint(Point p, double w) {
			point = p;
			weight = w;
		}

	}

	public Point streamBary(Stream<Point> stream) {
		Optional<WeightedPoint> reduce = stream
						.map((Point point) -> new WeightedPoint(point, 1.0))
						.reduce((p, q) -> new WeightedPoint(p.point.plus(q.point), p.weight + q.weight));
		WeightedPoint w = reduce.get();
		return w.point.scale(1.0 / w.weight);
	}

	public static void main(String[] args)  {
		if (args.length < 1) {
			System.out.println("Usage: java mapreduce.Point nb-iter");
			System.exit(1);
		}
		Integer iter = Math.max(1, Integer.parseInt(args[0]));
		long start;
		final double EPSILON = 0.01;
		int length = 14;
		while (iter-- > 0) {
			List<Point> points = randomPointStream()
							.limit(length)
							.collect(toList());
			start = System.nanoTime();
			Point seqBaryCenter = Point.barycenter(points);
			long seqDuration = System.nanoTime() - start;

			start = System.nanoTime();
			Point curBaryCenter = Point.barycenter(points);
			long curDuration = System.nanoTime() - start;

			if (Math.abs(seqBaryCenter.getX() - curBaryCenter.getX()) > EPSILON) {
				System.out.printf("barycenters differ: %s\t%s\n",
								seqBaryCenter,
								curBaryCenter);
			}
			System.out.printf("size: %d\tspeedup: %f\n",
							length,
							((double) seqDuration) / ((double) curDuration)
			);
			length += length;
		}

	}

}
