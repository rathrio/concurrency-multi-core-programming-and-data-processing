CONCSYS - ASSIGNMENT 5
======================

Rathesan Iyadurai (10-107-688)

You should find the following files in this directory:

- Ex1.java
- Ex2.java
- lena.pgm
- test.pgm
- hasselhoff.pgm

Running
-------

The java files can be compiled and run with javac and java as usual. Running
the files will result in an out.pgm or out_sequential.pgm file in the current
directory. out_sequential.pgm will only be written if you pass NUM_THREADS=1.

Usage Ex1:
        java Ex1 <IMAGE> <D> <NUM_THREADS> <ALIVENESS_THRESHOLD>

For example:
        java Ex1 test.pgm 1 4 0

I introduced an ALIVENESS_THRESHOLD to test the effect on files that do not have
"fully black" pixels, i.e., pixels that have value=0. It doesn't make much sense,
but it was fun experimenting with. lena.pgm for instance doesn't have any
pixels with value=0, so running Ex1 with a higher ALIVENESS_THRESHOLD will
result in some highlights being removed, e.g.:

        java Ex1 lena.pgm 4 2 80

I recommend using test.pgm to have nice results, though.


Usage Ex2:
        java Ex2 <IMAGE> <EFFECT> <NUM_THREADS>

For example:
        java Ex2 hasselhoff.pgm blur 4

You'll find other kernels besides blur in the code, but please only use blur
for your evaluation. The others don't behave that well, even though I followed
the descriptions here: http://setosa.io/ev/image-kernels/.


Notes on Testing
----------------

For testing I initially started out with implementing a sequential version of
the effect to use as a reference.

The images were generated with GIMP, as you recommended. I parsed the files
manually. It requires them to be in the one-column format.

For the examples in this directory it doesn't pay off to use multiple threads.
I deliberately timed the effects end-to-end, and the concurrent versions tend
to be slower, most likely because of the synchronization overhead.

It will probably pay off for larger images.

Increasing the D value for Ex1 will eventually result in a black image, since a
pixel is more likely to become a hot pixel, the more alive neighbors it requires.
