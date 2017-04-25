/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package streams;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author mph
 */
public class Tests {

  public static void main(String[] args) {
    String[] strings = {"alfa", "bravo", "charlie", "delta", "echo"};

    // sort strings by length
    Arrays.sort(strings, (s, t) -> s.length() - t.length());
    System.out.println(Arrays.asList(strings));

    // sort strings by their second letter
    Arrays.sort(strings, (s, t) -> s.charAt(1) - t.charAt(1));
    System.out.println(Arrays.asList(strings));

    // order  strings that start with 'c' first, then sort normally
    Arrays.sort(strings, (s, t) -> {
      if (s.charAt(0) == 'c') {
        return -1;
      } else if (t.charAt(0) == 'c') {
        return 1;
      } else {
        return s.compareTo(t);
      }
    }
    );
    System.out.println(Arrays.asList(strings));

    Map<String, String> map = new HashMap<>();
    map.put("Cambridge", "02319");
    map.put("Providence", "02912");
    map.put("Palo Alto", "94305");
    map.put("Pittsburgh", "15213");
    System.out.println(map);

    Map<String, String> reverse = map
            .entrySet()
            .stream()
            .collect(
                    Collectors.toMap(
                            Map.Entry::getValue,
                            Map.Entry::getKey
                    )
            );
    System.out.println(reverse);

    List<String> s = Arrays.asList("alfa", "bravo", "charlie", "delta", "echo");
    s.stream()
            .forEach(System.out::println);
    s.parallelStream()
            .forEach(System.out::println);
    s.stream()
            .forEach(x -> System.out.println(x + "!!!")
            );
    s.stream()
            .filter(x -> x.length() > 4)
            .filter(x -> !x.contains("l"))
            .forEach(System.out::println);

    FibStream.get().limit(8).forEach(System.out::println);
  }

  static class FibStream {

    static long fib0 = 1;
    static long fib1 = 1;

    static Stream<Long> get() {
      return Stream.generate(
              () -> {
                long res = fib0 + fib1;
                fib0 = fib1;
                fib1 = res;
                return res;
              });

    }
  }

}
