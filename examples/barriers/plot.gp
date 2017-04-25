set term dumb 119 39 enhanced

set title "Barriers"

set key top left

set tic scale 0
set xrange [2:32]
set xtics (2,4,8,16,32)
set yrange [0:2e4]

plot "data/SenseBarrier.dat" using 1:2 title "Sense" with lines, \
     "data/TreeBarrier.dat" using 1:2 title "Tree" with lines

pause 1
reread
