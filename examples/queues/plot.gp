set term dumb 119 39 enhanced

set title "Queues"

set key top left

set tic scale 0
set xrange [0:48]
set xtics (1,2,4,8,16,24,32,48)
set logscale y
set yrange [1e2:1e5]

plot "data/CoarseQueue.dat" using 1:2 title "Coarse" with lines, \
     "data/BoundedQueue.dat" using 1:2 title "Bounded" with lines, \
     "data/LockFreeQueue.dat" using 1:2 title "LockFree" with lines

pause 1
reread
