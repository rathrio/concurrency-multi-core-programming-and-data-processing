#!/bin/sh

initial=256
update=20

# Note: sleep of 1 ms added to add, remove, contains operations

mkdir -p data

for class in CoarseHashSet StripedHashSet SplitOrderHashSet
do
echo "0 0" > data/${class}.dat
done

for class in CoarseHashSet StripedHashSet SplitOrderHashSet
do

for threads in 1 2 4 8 16 24 32 48
do

echo "########################################################################"
java Driver ${class} ${threads} ${update} ${initial} | tee /tmp/hash.log
n=`tail -1 /tmp/hash.log | awk '{print $NF}'`
echo "${threads} ${n}" >> data/${class}.dat
rm -f /tmp/hash.log

done
done
