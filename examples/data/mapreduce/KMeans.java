/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapreduce;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mph
 */
public class KMeans {

	static int numClusters = 3;
	static int numWorkerThreads = 4;
	static final double EPSILON = 0.01;

	static List<Point> points;
	static Map<Integer, Point> centers;

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Usage: java mapreduce.KMeans filename nb-clusters nb-workers");
			System.exit(1);
		}
		String fileName = args[0];
		numClusters = Math.max(1, Integer.parseInt(args[1]));
		numWorkerThreads = Math.max(1, Integer.parseInt(args[2]));
		long start;
		long duration;
		int iterations = 0;
		points = readFile(fileName);
		centers = Point.randomDistinctCenters(points);
		MapReduce<List<Point>, Integer, List<Point>, Point> mapReduce
						= new MapReduce<>();
		mapReduce.setMapperSupplier(KMeans.Mapper::new);
		mapReduce.setReducerSupplier(KMeans.Reducer::new);
		mapReduce.setInput(splitInput(points, numWorkerThreads));
		double convergence = 1.0;
		start = System.nanoTime();
		while (convergence > EPSILON) {
			System.out.printf("centers\t%s\n", centers);
			Map<Integer, Point> newCenters = mapReduce.call();
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
		MapReduce<List<Point>, Integer, List<Point>, Point> mapReduce
						= new MapReduce<>();
		mapReduce.setMapperSupplier(KMeans.Mapper::new);
		mapReduce.setReducerSupplier(KMeans.Reducer::new);
		mapReduce.setInput(splitInput(points, numWorkerThreads));
		double convergence = 1.0;
		while (convergence > EPSILON) {
			Map<Integer, Point> newCenters = mapReduce.call();
			convergence = distance(centers, newCenters);
			centers = newCenters;
		}
		return centers;
	}

	static class Mapper
					extends mapreduce.Mapper<List<Point>, Integer, List<Point>> {

		@Override
		public Map<Integer, List<Point>> compute() {
			Map<Integer, List<Point>> map = new HashMap<>();
			for (Point point : input) {
				int myCenter = closestCenter(centers, point);
				map.putIfAbsent(myCenter, new LinkedList<>());
				map.get(myCenter).add(point);
			}
			return map;
		}
	}

	static class Reducer
					extends mapreduce.Reducer< Integer, List<Point>, Point> {

		@Override
		public Point compute() {
			List<Point> cluster = new LinkedList<>();
			for (List<Point> list : valueList) {
				cluster.addAll(list);
			}
			return Point.barycenter(cluster);
		}
	}

	public static List<Point> readFile(String fileName) {
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

	static double distance(Map<Integer, Point> c0, Map<Integer, Point> c1) {
		return c0.keySet()
						.stream()
						.mapToDouble((Integer i) -> (c0.get(i).distance(c1.get(i))))
						.sum();
	}

	static void writePoints(Collection<Point> points, String fileName) {
		try {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
				for (Point point : points) {
					writer.write(String.format("%f\t%f\n", point.getX(), point.getY()));

				}
			}
		} catch (IOException ex) {
			Logger.getLogger(ClusterGen.class
							.getName()).log(Level.SEVERE, null, ex);
		}
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

	static List<List<Point>> splitInput(List<Point> points, int parts) {
		List<List<Point>> inputs = new ArrayList<>(parts);
		int size = points.size();
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
