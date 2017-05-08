import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Ex1Savages11 {

    private static volatile int PORTIONS;

    private static volatile boolean[] areSavagesHungry;

    private static ReentrantLock potLock = new ReentrantLock();
    private static ReentrantLock cookLock = new ReentrantLock();

    private static volatile boolean refill = false;

    private static class SavageThread extends Thread {
        private int id;

        public SavageThread(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            // loop as long as this savage is hungry
            while(areSavagesHungry[id]) {
                // check if food is available
                if (PORTIONS > 0) {
                    // let only this savage eat a piece
                    if (potLock.tryLock()) {
                        try {
                            // make sure no other savage has eaten a piece while acquiring the lock.
                            if (PORTIONS > 0) {
                                PORTIONS--;
                                // mark the savage as being fed
                                areSavagesHungry[id] = false;
                                System.out.println("Savage " + this.getId() + " has eaten one portion.");
                            }
                        } finally {
                            // let the next savage eat
                            potLock.unlock();
                        }
                    }
                } else {
                    // order some additional food, when the pot is empty.
                    if(!refill) {
                        // make sure that only one savage orders food
                        if(cookLock.tryLock()) {
                            try {
                                // make sure that the food hasn't been ordered by another thread in the mean time.
                                if(!refill)
                                    refill = true;
                            } finally {
                                cookLock.unlock();
                            }
                        }
                    }
                }
            }
        }
    }

    private static class CookThread extends Thread {

        private int portionsToFillInPot;
        private int refillCounter = 0; // used to check how often the pot was refilled

        public CookThread(int n) {
            portionsToFillInPot = n;
        }

        // check if there is any hungry savage left
        private boolean areSavagesStillHungry() {
            for (boolean isSavageHungry : areSavagesHungry) {
                if (isSavageHungry) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void run() {
            savagesAreHungryLoop:
            // be ready to refill as long as there are hungry savages
            while (areSavagesStillHungry()) {
                // wait until a savage requires additional food
                while(!refill) {
                    // In the case that all Savages have eaten => exit outer loop (otherwise the cook will wait forever)
                    if (!areSavagesStillHungry())
                        break savagesAreHungryLoop;
                }
                // check that the pot is really empty
                if (PORTIONS == 0) {
                    // refill the pot
                    PORTIONS = portionsToFillInPot;
                    // be ready for the next order
                    refill = false;
                    // count the number of refills
                    refillCounter++;
                }
            }
            System.out.println("Is any savage hungry? " + Arrays.toString(areSavagesHungry));
            System.out.println("Total amounts of refills by cook: " + refillCounter);
        }
    }

    public static void main(String[] args) {

        try {
            // parse input parameters
            int savages = Integer.parseInt(args[0]); // number of savages
            PORTIONS = Integer.parseInt(args[1]); // capacity of pot
            if (PORTIONS <= 0) {
                System.out.println("The capacity of the pot must be positive and greater than 0.");
                System.exit(1);
            } else if (savages <= PORTIONS) {
                System.out.println("The numbers of savages must exceed the capacity of the pot.");
                System.exit(1);
            }
            areSavagesHungry = new boolean[savages];
            // Initialize the thread representing the cook
            CookThread cook = new CookThread(PORTIONS);
            // this list will store all threads that are started
            SavageThread[] savageThreads = new SavageThread[savages];

            try {
                for (int i = 0; i < savages; i++) {
                    // store each thread in a list for later use
                    SavageThread savage = new SavageThread(i);
                    savageThreads[i] = savage;
                    areSavagesHungry[i] = true;
                }
                // start timer
                long startTime = System.nanoTime();
                cook.start();
                for (SavageThread savage : savageThreads) {
                    // start each thread in the list
                    savage.start();
                }

                for (SavageThread savage : savageThreads) {
                    // wait for all threads to be finished
                    savage.join();
                }
                cook.join();
                // end timer
                long endTime = System.nanoTime();
                //print the elapsed time from starting the threads until all threads are finished
                System.out.println("Duration of execution [ms]: " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

            } catch (InterruptedException e) {
                System.err.println("Thread was interrupted.");
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Unexpected error");
                e.printStackTrace();
            }


        } catch (NumberFormatException e) {
            System.err.println("Both arguments " + args[0] + " and " + args[1] + " must be integers.");
            System.exit(1);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Argument/s 'number of savages' and/or 'pot capacity' are missing.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error:");
            e.printStackTrace();
        }
    }

}
