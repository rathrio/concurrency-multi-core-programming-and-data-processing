#!/bin/sh

for initial in 1024
do

for update in 0 10 50
do

for class in CoarseList FineList OptimisticList LazyList LockFreeList
do

for threads in 1 2 4 8 16 32 48
do

echo "########################################################################"
java Driver ${class} ${threads} ${update} ${initial}

done
done
done
done
