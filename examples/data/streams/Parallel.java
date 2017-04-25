/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author mph
 */
public class Parallel {

  public class Accumulator {

    long value;

    Accumulator() {
      value = 0;
    }

    void apply(int x) {
      value += x;
    }

    void combine(Accumulator a) {
      value += a.value;
    }
  }

  public static void main(String[] args) {
    String[] stringArray = {"alfa", "bravo", "charlie",
      "delta", "echo", "foxtrot"};
    List<String> listOfStrings
            = new ArrayList<>(Arrays.asList(stringArray));

    System.out.println("Sequential stream:");
    listOfStrings
            .stream()
            .forEach(e -> System.out.printf("%s ", e));
    System.out.println("");
    System.out.println("Parallel stream:");
    listOfStrings
            .parallelStream()
            .forEach(e -> System.out.printf("%s ", e));
    System.out.println("");
  }

}
