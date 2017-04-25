/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapreduce;

import java.util.Map;
import java.util.concurrent.RecursiveTask;

/**
 *
 * @author mph
 * @param <IN> input type
 * @param <K> key type
 * @param <V> accumulator type
 */
public abstract class Mapper<IN, K, V> extends RecursiveTask<Map<K, V>> {
  IN input;
  /**
   * @param anInput list of input items
   */
  public void setInput(IN anInput) {
    input = anInput;
  }
}
