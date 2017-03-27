CONCSYS - ASSIGNMENT 1
======================

Rathesan Iyadurai (10-107-688)

You should find the following files in this directory:

- Ex1.txt
- Ex1NoSync.java
- Ex1Sync.java
- Ex1ReentrantLock.java
- Ex2.java
- report.rb


Exercise 1
----------

See Ex1.txt for the requested report. You can compile and run the individual
files with javac and java as usual.

report.rb will generate the report that is found in Ex1.txt. You need a recent
Ruby version if you want to run it. To output the report, run

        ./report.rb


Exercise 2
----------

Compile with

        javac Ex2.java

Run with

        java Ex2 t n

The critical sections of the circular buffer (i.e. read/write of the buffer
contents and pointers) are guarded with a lock by using a binary semaphore, as
hinted in the assignment.
