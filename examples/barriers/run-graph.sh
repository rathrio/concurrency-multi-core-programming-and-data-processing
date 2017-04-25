#!/bin/sh

rounds=500000

mkdir -p data

for class in SenseBarrier TreeBarrier
do
echo "0 0" > data/${class}.dat
done

for class in SenseBarrier TreeBarrier
do

for threads in 2 4 8 16 32
do

echo "########################################################################"
java Driver ${class} ${threads} ${rounds} | tee /tmp/barrier.log
n=`tail -1 /tmp/barrier.log | awk '{print $NF}'`
echo "${threads} ${n}" >> data/${class}.dat
rm -f /tmp/barrier.log

done
done
