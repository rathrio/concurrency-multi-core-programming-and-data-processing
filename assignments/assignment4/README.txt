CONCSYS - ASSIGNMENT 2
======================

Rathesan Iyadurai (10-107-688)

You should find the following files in this directory:

- Ex1.txt
- Ex2.txt
- Ex3.txt
- Ex1.java
- Ex1NonVolatile.java (only removes volatile modifier on shared counter)

Running
-------

The java files can be compiled and run with javac and java as usual.

Usage:
        java Ex1 <NUM_THREADS> <COUNTER_LIMIT> <LOCK>

whereas you can pass LOCK=1 for CAS and LOCK=2 for CCAS.

Example: Count to 300'000 with 8 threads using CCAS lock

        java Ex1 8 300000 2