#!/bin/sh

for threads in 1 2 4 8 16 24 32 48
do

echo "${threads}"
./tinySTM/test/intset/intset-sl -u 10 -d 5000 -n ${threads} | grep '^#' | head -4

done
