#!/bin/sh

capacity=256

# Note: sleep of 1 ms added to enq, deq operations

mkdir -p data

for class in CoarseQueue BoundedQueue LockFreeQueue
do
echo "0 0" > data/${class}.dat
done

for class in CoarseQueue BoundedQueue LockFreeQueue
do

for threads in 1 2 4 8 16 24 32 48
do

echo "########################################################################"
java Driver ${class} ${threads} ${capacity} | tee /tmp/queue.log
n=`tail -1 /tmp/queue.log | awk '{print $NF}'`
echo "${threads} ${n}" >> data/${class}.dat
rm -f /tmp/queue.log

done
done
