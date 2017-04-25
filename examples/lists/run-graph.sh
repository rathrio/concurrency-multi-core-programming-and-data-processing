#!/bin/sh

initial=2048
update=20

mkdir -p data

for class in CoarseList FineList OptimisticList LazyList LockFreeList
do
echo "0 0" > data/${class}.dat
done

for class in CoarseList FineList OptimisticList LazyList LockFreeList
do

for threads in 1 2 4 8 16 24 32 48
do

echo "########################################################################"
java Driver ${class} ${threads} ${update} ${initial} | tee /tmp/list.log
n=`tail -1 /tmp/list.log | awk '{print $NF}'`
echo "${threads} ${n}" >> data/${class}.dat
rm -f /tmp/list.log

done
done
