package lab5;


import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class GourmetPizzaLoversHouse {

    ReentrantLock pizzaLock = new ReentrantLock();
    Condition meatPizzaArrivedCondition = pizzaLock.newCondition();
    Condition veganPizzaArrivedCondition = pizzaLock.newCondition();

    private boolean meatPizzaArrived = false;
    private boolean veganPizzaArrived = false;

    public void eatVeganPizza(Person person) throws InterruptedException {
        pizzaLock.lock();
        while (!veganPizzaArrived)
            veganPizzaArrivedCondition.await();
        System.out.println("vegan pizza is the best..");
        if (person.theHungriest)
            veganPizzaArrived = false; // eat the last slice and wait again
        pizzaLock.unlock();
    }

    public void eatMeatPizza(Person person) throws InterruptedException {
        pizzaLock.lock();
        while (!meatPizzaArrived)
            meatPizzaArrivedCondition.await();
        System.out.println("no way! meaty pizza is the way to go!");
        if (person.theHungriest)
            meatPizzaArrived = false; // eat the last slice and wait again
        pizzaLock.unlock();
    }

    public void pizzaGuyArrived(Pizza withPizza) {
        pizzaLock.lock();
        if (withPizza.pizzaType.equals(Pizza.PizzaType.VEGAN)) {
            veganPizzaArrived = true;
            veganPizzaArrivedCondition.signalAll();
        } else {
            meatPizzaArrived = true;
            meatPizzaArrivedCondition.signalAll();
        }
        pizzaLock.unlock();
    }

    public static void main(String[] args) throws InterruptedException {

        LinkedList<Person> pizzaEaters = new LinkedList<>();
        GourmetPizzaLoversHouse house = new GourmetPizzaLoversHouse();

        for (int i = 0; i < 5; i++) {
            pizzaEaters.add(new Person(house, false, Pizza.PizzaType.MEAT));
        }
        for (int i = 0; i < 5; i++) {
            pizzaEaters.add(new Person(house, false, Pizza.PizzaType.VEGAN));
        }

        ExecutorService peopleRunner = Executors.newCachedThreadPool();
        pizzaEaters.forEach(peopleRunner::submit);
        TimeUnit.SECONDS.sleep(1);
        System.out.println("Some guys arrived but did not find the pizza in the kitchen and decided to wait for the delivery guy");
        TimeUnit.SECONDS.sleep(2);

        ExecutorService deliveryGuyRunner=Executors.newSingleThreadExecutor();
        deliveryGuyRunner.execute(()->{
            System.out.println("Delivering a vegan pizza");
            house.pizzaGuyArrived(new Pizza(Pizza.PizzaType.VEGAN));
        });

        TimeUnit.SECONDS.sleep(2);

        deliveryGuyRunner.execute(()->{
            System.out.println("Delivering a meat pizza");
            house.pizzaGuyArrived(new Pizza(Pizza.PizzaType.MEAT));
            house.meatPizzaArrived=true;
        });

        TimeUnit.SECONDS.sleep(2);

        System.out.println("Sending the hungriest kids to finish all pizzas");

        LinkedList<Person> hungryPeople = new LinkedList<>();
        hungryPeople.add(new Person(house, true, Pizza.PizzaType.MEAT));
        hungryPeople.add(new Person(house, true, Pizza.PizzaType.VEGAN));
        hungryPeople.forEach(peopleRunner::submit);

        TimeUnit.SECONDS.sleep(2);

        System.out.println("Not lets check if everybody has eaten");

        pizzaEaters.addAll(hungryPeople); // *all* the people are now in one collection

        pizzaEaters.forEach((person) -> {
            if (person.eaten)
                System.out.println("I'm not hungry!");
            else
                System.out.println("we have a hungry person - something is not right");
        });

        peopleRunner.shutdown();
        deliveryGuyRunner.shutdown();

    }
}


class Pizza {

    public final PizzaType pizzaType;

    public Pizza(PizzaType pizzaType) {
        this.pizzaType = pizzaType;
    }

    public enum PizzaType {
        VEGAN, MEAT
    }
}

class Person implements Runnable {
    public final boolean theHungriest;
    public final Pizza.PizzaType canEatPizzaType;
    public final GourmetPizzaLoversHouse house;
    public volatile boolean eaten = false;

    public Person(GourmetPizzaLoversHouse house, boolean theHungriest, Pizza.PizzaType canEatPizzaType) {
        this.theHungriest = theHungriest;
        this.canEatPizzaType = canEatPizzaType;
        this.house = house;
    }

    @Override
    public void run() {
        System.out.println("Trying to eat pizza of type " + canEatPizzaType);
        try {
            if (canEatPizzaType.equals(Pizza.PizzaType.MEAT))
                house.eatMeatPizza(this);
            else
                house.eatVeganPizza(this);
        } catch (InterruptedException e) {
            System.out.println("Something went wrong when trying to eat pizza");
        }
        eaten = true;
    }
}
