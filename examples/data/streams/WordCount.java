/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package streams;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author mph
 */
public class WordCount {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java streams.WordCount filename");
			System.exit(1);
		}
		String fileName = args[0];
		long start;
		List<String> text = readFile(fileName);
		Map<String, Long> map = null;

		start = System.nanoTime();
		map = text
						.stream()
						.collect(
										Collectors.groupingBy(
														Function.identity(),
														Collectors.counting())
						);
		double seqDuration = (double) (System.nanoTime() - start);

		start = System.nanoTime();
		map = text
						.stream()
						.collect(Collectors.groupingByConcurrent(x -> x,
														Collectors.counting())
						);
		double curDuration = (double) (System.nanoTime() - start);
		System.out.printf("Concurrent speedup:\t%f\n", curDuration / seqDuration);

		start = System.nanoTime();
		map = text
						.parallelStream()
						.collect(Collectors.groupingBy(x -> x,
														Collectors.counting())
						);
		double parDuration = (double) (System.nanoTime() - start);
		System.out.printf("Parallel speedup:\t%f\n", parDuration / seqDuration);

		start = System.nanoTime();
		map = text
						.parallelStream()
						.collect(Collectors.groupingByConcurrent(x -> x,
														Collectors.counting())
						);
		double botDuration = (double) (System.nanoTime() - start);
		System.out.printf("Both speedup:\t%f\n", botDuration / seqDuration);
	}

	static List<String> readFile(String fileName) {
		try {
			Pattern pattern = Pattern.compile("\\W|\\d|_");
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			return reader
							.lines()
							.map(String::toLowerCase)
							.flatMap(s -> pattern.splitAsStream(s))
							.collect(Collectors.toList());
		} catch (FileNotFoundException ex) {
			Logger.getLogger(mapreduce.WordCount.class
							.getName()).log(Level.SEVERE, null, ex);
			System.exit(0x1);
			return null;
		}
	}

}
