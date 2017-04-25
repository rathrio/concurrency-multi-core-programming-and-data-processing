/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package streams;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mapreduce.Point;

/**
 *
 * @author mph
 */
public class KMeans {

	static int numClusters = 3;
	static final double EPSILON = 0.01;

	static List<Point> points;
	static Map<Integer, Point> centers;

	static int iterations = 0;

	static List<Point> readFile(String fileName) {
		List<Point> data = new LinkedList<>();
		try {
			Scanner scanner = new Scanner(new BufferedReader(new FileReader(fileName)));
			while (scanner.hasNext()) {
				double x = scanner.nextDouble();
				double y = scanner.nextDouble();
				data.add(new Point(x, y));

			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(WordCount.class
							.getName()).log(Level.SEVERE, null, ex);
			System.exit(
							0x1);
		}
		return data;
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: java streams.KMeans filename nb-clusters");
			System.exit(1);
		}
		String fileName = args[0];
		numClusters = Math.max(1, Integer.parseInt(args[1]));
		long start;
		long duration;
		points = KMeans.readFile(fileName);
		centers = randomDistinctCenters(points);
		double convergence = 1.0;
		start = System.nanoTime();
		while (convergence > EPSILON) {
			System.out.printf("centers\t%s\n", centers);
			Map<Integer, List<Point>> clusters = points
							.stream()
							.collect(
											Collectors.groupingBy(
															p -> KMeans.closestCenter(centers, p)
											)
							);
			Map<Integer, Point> newCenters = clusters
							.entrySet()
							.stream()
							.collect(
											Collectors.toMap(
															e -> e.getKey(),
															e -> Point.barycenter(e.getValue())
											)
							);
			convergence = distance(centers, newCenters);
			centers = newCenters;
			iterations++;
		}
		duration = System.nanoTime() - start;
		System.out.printf("Time: %d\n", duration);
		System.out.printf("iterations: %d\tdistance: %f\n", iterations, convergence);
	}

	public static Map<Integer, Point> test(
					List<Point> thePoints,
					Map<Integer, Point> theCenters) {
		points = thePoints;
		centers = theCenters;
		double convergence = 1.0;
		while (convergence > EPSILON) {
			Map<Integer, List<Point>> clusters = points
							.stream()
							.collect(
											Collectors.groupingBy(
															p -> KMeans.closestCenter(centers, p)
											)
							);
			Map<Integer, Point> newCenters = clusters
							.entrySet()
							.stream()
							.collect(
											Collectors.toMap(
															e -> e.getKey(),
															e -> Point.barycenter(e.getValue())
											)
							);
			convergence = distance(centers, newCenters);
			centers = newCenters;
		}
		return centers;
	}


	static Map<Integer, Point> randomDistinctCenters(List<Point> points) {
		Random random = new Random(0);
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

	static double distance(Map<Integer, Point> c0, Map<Integer, Point> c1) {
		return c0.keySet()
						.stream()
						.mapToDouble((Integer i) -> (c0.get(i).distance(c1.get(i))))
						.sum();
	}

	static int closestCenter(Map<Integer, Point> centers, Point point) {
		double bestDistance = Double.MAX_VALUE;
		int bestCenter = -1;
		for (Map.Entry<Integer, Point> entry : centers.entrySet()) {
			double distance = entry.getValue().distance(point);
			if (distance < bestDistance) {
				bestDistance = distance;
				bestCenter = entry.getKey();
			}
		}
		return bestCenter;
	}

	static List<List<Point>> splitInput(int size, int parts) {
		List<List<Point>> inputs = new ArrayList<>(parts);
		int div = size / parts;
		int rem = size % parts;
		int cur = 0;
		for (int i = 0; i < parts; i++) {
			int step = (rem-- > 0) ? div + 1 : div;
			inputs.add(points.subList(cur, cur + step));
			cur += step;
		}
		return inputs;

	}

}
