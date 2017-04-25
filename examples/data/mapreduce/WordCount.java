/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapreduce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mph
 */
public class WordCount {

	static List<String> text;
	static int numThreads = 4;

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: java mapreduce.WordCount filename nb-workers [time]");
			System.exit(1);
		}
		String fileName = args[0];
		numThreads = Math.max(1, Integer.parseInt(args[1]));
		boolean time = (args.length == 3);
		text = readFile(fileName);
		List<List<String>> inputs = splitInputs(text, numThreads);
		MapReduce<List<String>, String, Long, Long> mapReduce
						= new MapReduce<>();
		mapReduce.setMapperSupplier(WordCount.Mapper::new);
		mapReduce.setReducerSupplier(WordCount.Reducer::new);
		mapReduce.setInput(inputs);
		long start = System.nanoTime();
		Map<String, Long> map = mapReduce.call();
		long duration = System.nanoTime() - start;
		if (time) {
			System.out.printf("Time: %d\n", duration);
		} else {
			map.forEach(
							(k, v) -> { if (!k.trim().isEmpty()) System.out.printf("%s\t%d\n", k, v); }
			);
		}
	}

	static List<String> readLines(String fileName) {
		List<String> lines = new LinkedList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			while (line != null) {
				lines.add(line);
				line = reader.readLine();
			}
		} catch (IOException ex) {
			Logger.getLogger(mapreduce.WordCount.class
							.getName()).log(Level.SEVERE, null, ex);
			System.exit(0x1);
		}
		return lines;
	}

	static List<String> readFile(String fileName) {
		List<String> lines = readLines(fileName);
		List<String> words = new LinkedList<>();
		lines.stream().forEach((line) -> {
			words.addAll(
							Arrays.asList(
											line.toLowerCase()
											.split("\\W|\\d|_")));
		});
		return words;
	}

	static List<List<String>> splitInputs(List<String> text, int parts) {
		List<List<String>> inputs = new ArrayList<>(parts);
		int size = text.size();
		int div = size / parts;
		int rem = size % parts;
		int cur = 0;
		for (int i = 0; i < parts; i++) {
			int step = (rem-- > 0) ? div + 1 : div;
			inputs.add(text.subList(cur, cur + step));
			cur += step;
		}
		return inputs;
	}

	static class Mapper
					extends mapreduce.Mapper<List<String>, String, Long> {

		@Override
		public Map<String, Long> compute() {
			Map<String, Long> map = new HashMap<>();
			for (String word : input) {
				map.merge(word, 1L, (x, y) -> x + y);
			}
			return map;
		}

	}

	static class Reducer
					extends mapreduce.Reducer<String, Long, Long> {

		@Override
		public Long compute() {
			long count = 0;
			for (long c : valueList) {
				count += c;
			}
			return count;
		}
	}

}
