/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapreduce;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 *
 * @author mph
 * @param <IN> input type
 * @param <K> key type
 * @param <V> value type
 * @param <OUT> output type
 */
public class MapReduce<IN, K, V, OUT>
        implements Callable<Map<K, OUT>> {

  private List<IN> inputList;
  private Supplier<Mapper<IN, K, V>> mapperSupplier;
  private Supplier<Reducer<K, V, OUT>> reducerSupplier;
  private static ForkJoinPool pool;

  /**
   *
   */
  public MapReduce() {
    pool = new ForkJoinPool();
    mapperSupplier
            = () -> {
              throw new UnsupportedOperationException("No mapper supplier");
            };
    reducerSupplier
            = () -> {
              throw new UnsupportedOperationException("No reducer supplier");
            };
  }

  @Override
  public Map<K, OUT> call() {
    Set<Mapper<IN, K, V>> mappers = new HashSet<>();
    for (IN input : inputList) {
      Mapper<IN, K, V> mapper = mapperSupplier.get();
      mapper.setInput(input);
      pool.execute(mapper);
      mappers.add(mapper);
    }
    Map<K, List<V>> mapResults = new HashMap<>();
    for (Mapper<IN, K, V> mapper : mappers) {
      Map<K, V> map = mapper.join();
      for (K key : map.keySet()) {
        mapResults.putIfAbsent(key, new LinkedList<>());
        mapResults.get(key).add(map.get(key));
      }
    }
    Map<K, Reducer<K, V, OUT>> reducers = new HashMap<>();
    mapResults.forEach(
            (k, v) -> {
              Reducer< K, V, OUT> reducer = reducerSupplier.get();
              reducer.setInput(k, v);
              pool.execute(reducer);
              reducers.put(k, reducer);
            }
    );
    Map<K, OUT> result = new HashMap<>();
    reducers.forEach(
            (key, reducer) -> {
              result.put(key, reducer.join());
            }
    );
    return result;
  }

  /**
   * @param aMapperSupplier construct mapper thread
   */
  public void setMapperSupplier(Supplier<Mapper<IN, K, V>> aMapperSupplier) {
    mapperSupplier = aMapperSupplier;
  }

  /**
   * @param aReducerSupplier construct reducer thread
   */
  public void setReducerSupplier(Supplier<Reducer<K, V, OUT>> aReducerSupplier) {
    reducerSupplier = aReducerSupplier;
  }

  /**
   * @param anInput the task's set of inputs
   */
  public void setInput(List<IN> anInput) {
    inputList = anInput;
  }

}
