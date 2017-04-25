/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package streams;

import java.util.Map;

/**
 *
 * @author mph
 * @param <K> first element
 * @param <V> second element
 * 
 */
public class Pair <K,V> implements Map.Entry {
  private K key;
  private V value;  

 public Pair(K aKey, V aValue) {
   key = aKey;
   value = aValue;
 }

  @Override
  public Object setValue(Object value) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  /**
   * @return the key
   */
  @Override
  public K getKey() {
    return key;
  }

  /**
   * @param key the key to set
   */
  public void setKey(K key) {
    this.key = key;
  }

  /**
   * @return the value
   */
  @Override
  public V getValue() {
    return value;
  }
}
