/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapreduce;

import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

/**
 *
 * @author mph
 * @param <K> key
 * @param <V> valueListumulator
 * @param <OUT> output value
 */
public abstract class Reducer<K, V, OUT> extends RecursiveTask<OUT> {

  K key;
  List<V> valueList;

  /**
   * @param aKey key for this reducer
   * @param aList list of values
   */
  public void setInput(K aKey, List<V> aList) {
    key = aKey;
    valueList = aList;
  }
};
