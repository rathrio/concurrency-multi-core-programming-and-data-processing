Some notes/observations
=======================

The logs were generated on sunfire.

- the behaviour/order is actually pretty similar, since the AtomicInteger
  probably also uses a volatile field to store the value

- both approaches generate "membar" instructions to enforce memory ordering

- it looks like some calls to AtomicInteger were not optimized, e.g.,

    0xffffffff6a339410: ldx  [ %l2  ], %g0   ;*invokevirtual set
                                             ; - MemBarrierAtomic::f@41 (line 20)
                                             ; implicit exception: dispatches to 0xffffffff6a339650
