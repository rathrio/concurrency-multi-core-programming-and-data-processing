import java.util.concurrent.atomic.*;

class CLHLock implements Lock {

	AtomicReference<AtomicBoolean> tail = new AtomicReference<AtomicBoolean>(new AtomicBoolean(false)); 
	ThreadLocal<AtomicBoolean> myNode = new ThreadLocal<AtomicBoolean>();
	ThreadLocal<AtomicBoolean> myNext = new ThreadLocal<AtomicBoolean>() {
		@Override protected AtomicBoolean initialValue() {
			return new AtomicBoolean();
		}
	};

	public void lock() {
		AtomicBoolean node = myNext.get();
		node.set(true);
		AtomicBoolean pred = tail.getAndSet(node);
		while (pred.get()) { }
		myNode.set(node);
		// Recycle node for next time (reduce stress on GC)
		myNext.set(pred);
	}

	public void unlock() {
		AtomicBoolean node = myNode.get();
		node.set(false);
	}
}