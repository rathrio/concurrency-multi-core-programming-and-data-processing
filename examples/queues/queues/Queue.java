package queues;

public interface Queue<T> {

  public T deq();
  public void enq(T x);

}
