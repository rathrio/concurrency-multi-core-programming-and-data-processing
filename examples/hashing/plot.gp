set term dumb 119 39 enhanced

set title "Hashing"

set key top left

set tic scale 0
set xrange [0:48]
set xtics (1,2,4,8,16,24,32,48)
set logscale y
set yrange [1e2:1e6]

plot "data/CoarseHashSet.dat" using 1:2 title "Coarse" with lines, \
     "data/StripedHashSet.dat" using 1:2 title "Striped" with lines, \
     "data/SplitOrderHashSet.dat" using 1:2 title "SplitOrder" with lines

pause 1
reread
