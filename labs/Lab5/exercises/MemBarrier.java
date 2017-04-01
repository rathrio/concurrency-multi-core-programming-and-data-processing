class MemBarrier {
  int a, b;
  volatile int v, u;
  void f() {
    int i, j;
   
    i = a;
    j = b;
    i = v;
    // L/L
    j = u;
    // L/S
    a = i;
    b = j;
   
    v = i;
    // S/S
    u = j;
    // S/L
    i = u;
   
   
    j = b;
    a = i;
  }


  public static void main(String args[]) {
   MemBarrier elem = new MemBarrier();
   for (int i = 0; i < 1000000; i++)
	elem.f(); 
  }

}
