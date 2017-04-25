import java.util.concurrent.atomic.*;

class TaSLock implements Lock {
	AtomicBoolean state = new AtomicBoolean(false);

	public void lock() {
		while (state.getAndSet(true)) { }
	}
	public void unlock() {
		state.set(false);
	}
}