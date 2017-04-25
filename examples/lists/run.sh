#!/bin/sh

for initial in 2048
do

for update in 20
do

for class in CoarseList FineList LazyList
do

for threads in 8 16 32
do

echo "########################################################################"
java Driver ${class} ${threads} ${update} ${initial}

done
done
done
done
