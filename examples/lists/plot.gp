set term dumb 119 39 enhanced

set title "Lists"

set key top left

set tic scale 0
set xrange [0:48]
set xtics (1,2,4,8,16,24,32,48)
set logscale y
set yrange [1e3:1e7]

plot "data/CoarseList.dat" using 1:2 title "Coarse" with lines, \
     "data/FineList.dat" using 1:2 title "Fine" with lines, \
     "data/OptimisticList.dat" using 1:2 title "Optimistic" with lines, \
     "data/LazyList.dat" using 1:2 title "Lazy" with lines, \
     "data/LockFreeList.dat" using 1:2 title "LockFree" with lines

pause 1
reread
