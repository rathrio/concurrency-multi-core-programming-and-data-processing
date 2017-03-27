import java.util.concurrent.*;

public class Main {

	public static void main(String[] args) throws Exception {

		// Callable is an alternative interface to Runnable;
		//
		// // the difference is that you can return a value from a Callable task instance, once it finishes processing
		//
		 Callable<String> callableTask = () -> {
		
	 	String threadName = Thread.currentThread().getName();
		
		 System.out.println("Hello the task is running in thread named " + threadName);
		
		 System.out.println();
		
		 return "42";
		
		 };
		
		 System.out.println("Now the task's code will run in the main thread");
		//
		// // since we use Callable, we need to override call() and not run()
		//
		// // (check the method signature of java.util.concurrent.Callable.call()
		//
		// // to get an idea of how it works
		//
		 callableTask.call();
		//
		// // since there is no constructor Thread(Callable) using a Callable with
		//
		// // a Thread without an ExecutorService requires slightly more code:
		//
		 FutureTask<String> futureTask = new FutureTask<>(callableTask);
		
		 Thread t=new Thread(futureTask);
		
		 t.start();
		
		 Thread manualThread = new Thread(futureTask);
		
		 System.out.println("Now the task's code will run in a new manually created thread");
		
		 manualThread.start();
		
		 futureTask.get(); // will wait for the async completion
		
		 System.out.println("The manually created thread dies here");
		
		 System.out.println();
		
		 ExecutorService executor = Executors.newSingleThreadExecutor();
		
		 System.out.println("Now the task's code will run in a thread from a thread pool");
		
		 Future<String> futureResult=executor.submit(callableTask);
		
		 futureResult.get(); // will wait for the async completion
		
		 System.out.println("Now the task's code will run in the same thread as before, since the pool did not kill it");
		
		 futureResult=executor.submit(callableTask);
		
		 String result=futureResult.get(); // will wait for the async completion
		
		 System.out.println("Callables are great, since you can get results of computations in other threads, which you \n" +
	
		 "cannot do with Runnables; For instance\n");
		
		 System.out.println("The result of the computation is "+result);
		
		 System.out.println();
		
		 System.out.print("Shutting down the thread pool....");
		
		 executor.shutdown();
		
		 System.out.println("done");
		
		 }
		
	}
