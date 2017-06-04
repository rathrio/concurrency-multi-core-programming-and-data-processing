import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class WordCounter {
    static class Count extends RecursiveTask<HashMap<String, Integer>> {
        static final int SEQUENTIAL_THRESHOLD = 500;
        private String[] lines;

        public Count(String[] lines) {
            this.lines = lines;
        }

        @Override
        protected HashMap<String, Integer> compute() {
            HashMap<String, Integer> counts = new HashMap<>();

            if (lines.length < SEQUENTIAL_THRESHOLD) {
                for (String line: lines) {
                    String[] words = line.split(" ");
                    for (String w: words) {
                        w = w.replaceAll("[?!.,;\"\\-]", "").toLowerCase();

                        if (w.isEmpty()) {
                            continue;
                        }

                        if (counts.containsKey(w)) {
                            counts.put(w, counts.get(w) + 1);
                        } else {
                            counts.put(w, 1);
                        }
                    }
                }
            } else {
                int mid = lines.length / 2;
                Count left = new Count(Arrays.copyOfRange(lines, 0, mid - 1));
                Count right = new Count(Arrays.copyOfRange(lines, mid, lines.length - 1));
                left.fork();

                HashMap<String, Integer> rightAns = right.compute();
                HashMap<String, Integer> leftAns = left.join();

                // Merge rightAns int leftAns while accumulating values.
                rightAns.forEach((k, v) -> leftAns.merge(k, v, (v1, v2) -> (v1 + v2)));

                return leftAns;
            }

            return counts;
        }
    }

    public static void main(String[] args) {
        List<String> lines = null;

        if (args.length < 1) {
            System.out.println("Usage: java WordCounter path/to/file.txt");
            System.exit(1);
        }

        String path = args[0];

        try {
            lines = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] linesArray = new String[lines.size()];
        linesArray = lines.toArray(linesArray);

        HashMap<String, Integer> counts = ForkJoinPool.commonPool().invoke(new Count(linesArray));

        // Sort the entry set as described here: https://stackoverflow.com/a/21054661.
        Object[] a = counts.entrySet().toArray();
        Arrays.sort(a, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<String, Integer>) o2).getValue()
                        .compareTo(((Map.Entry<String, Integer>) o1).getValue());
            }
        });

        for (int i = 0; i < 10; i++) {
            Map.Entry<String, Integer> e = (Map.Entry<String, Integer>) a[i];
            System.out.printf("%s: %d%n", e.getKey(), e.getValue());
        }
    }
}
