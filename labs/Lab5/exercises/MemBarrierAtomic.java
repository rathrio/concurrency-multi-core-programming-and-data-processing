import java.util.concurrent.atomic.AtomicInteger;

class MemBarrierAtomic {
  int a, b;
  //  volatile int v, u;
  AtomicInteger v = new AtomicInteger(0);
  AtomicInteger u = new AtomicInteger(0);
  void f() {
    int i, j;

    i = a;
    j = b;
    i = v.get();
    // L/L
    j = u.get();
    // L/S
    a = i;
    b = j;

    v.set(i);
    // S/S
    u.set(j);
    // S/L
    i = u.get();


    j = b;
    a = i;
  }


  public static void main(String args[]) {
    MemBarrierAtomic elem = new MemBarrierAtomic();
    for (int i = 0; i < 1000000; i++)
      elem.f();
  }

}