/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapreduce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 * @author mph
 */
public class SSSP {

	static Map<Integer, List<Integer>> graph;
	static Map<Integer, Double> distances;
	static final Double EPSILON = 0.0001;

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java mapreduce.SSSP nb-points");
			System.exit(1);
		}
		Integer N = Math.max(1, Integer.parseInt(args[0]));
		long start;
		long duration;
		int iterations = 0;
		graph = makeGraph(N);
		distances = new TreeMap();
		Map<Integer, Double> newDistances = new TreeMap<>();
		newDistances.put(0, 0.0);
		for (int i = 1; i < N; i++) {
			newDistances.put(i, Double.MAX_VALUE);
		}
		MapReduce<Integer, Integer, Double, Double> mapReduce
						= new MapReduce<>();
		mapReduce.setMapperSupplier(SSSP.Mapper::new);
		mapReduce.setReducerSupplier(SSSP.Reducer::new);
		start = System.nanoTime();
		boolean done = false;
		while (!done) {
			distances.putAll(newDistances);
			mapReduce.setInput(
							listOfFiniteDistanceNodes(distances)
			);
			newDistances.putAll(mapReduce.call());
			done = withinEpsilon(distances, newDistances);
			iterations++;
			if (iterations > 2 * N) {
				System.out.println("Something's wrong");
				System.out.printf("Distances: %s\n", distances);
				System.exit(0);
			}
		}
		duration = System.nanoTime() - start;
		System.out.printf("Time: %d\n", duration);
		System.out.printf("iterations: %d\tpoints: %d\n", iterations, N);
	}

	static class Mapper extends mapreduce.Mapper<Integer, Integer, Double> {

		@Override
		public Map<Integer, Double> compute() {
			Map<Integer, Double> map = new HashMap<>();
			double myDistance = distances.get(input);
			for (Integer neighbor : graph.get(input)) {
				map.put(neighbor, myDistance + 1.0);
			}
			return map;
		}
	}

	static class Reducer extends mapreduce.Reducer<Integer, Double, Double> {

		@Override
		public Double compute() {
			Double shortest = Double.MAX_VALUE;
			for (double value : valueList) {
				shortest = Math.min(shortest, value);
			}
			return shortest;
		}
	}

	static Map<Integer, List<Integer>> makeGraph(int size) {
		Random random = new Random(0);
		Map<Integer, List<Integer>> g = new TreeMap();
		for (int i = 0; i < size; i++) {
			g.put(i, new ArrayList<>(size));
		}
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (random.nextBoolean() && random.nextBoolean()) {
					g.get(j).add(i);
				}
			}
		}
		return g;
	}

	static List<Integer> listOfFiniteDistanceNodes(Map<Integer, Double> distances) {
		return distances
						.entrySet()
						.stream()
						.filter(e -> (e.getValue() < Integer.MAX_VALUE))
						.map(e -> e.getKey())
						.collect(
										Collectors.toList()
						);
	}

	static boolean withinEpsilon(Map<Integer, Double> c0, Map<Integer, Double> c1) {
		return c0.keySet()
						.stream()
						.map(key -> ((c0.get(key) - c1.get(key)) < EPSILON))
						.reduce(true, (a, b) -> a && b);
	}

	static double distance(Map<Integer, Double> m0, Map<Integer, Double> m1) {
		return m0.keySet()
						.stream()
						.filter(key -> m1.containsKey(key))
						.map(key
										-> Math.abs(m0.get(key) - m1.get(key)))
						.reduce(0.0, (a, b) -> a + b);
	}

	static double d2(Map<Integer, Double> m0, Map<Integer, Double> m1) {
		Double sum = 0.0;
		for (int key : m0.keySet()) {
			if (m1.containsKey(key)) {
				sum += Math.abs(m0.get(key) - m1.get(key));
			}
		}
		return sum;
	}

}
