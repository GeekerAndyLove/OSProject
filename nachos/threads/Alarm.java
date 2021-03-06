package nachos.threads;

import java.util.TreeMap;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 *
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */

	private TreeMap<Long, KThread> waitingThreads;

	public Alarm() {
		waitingThreads = new TreeMap<Long, KThread>();

		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		// Readying threads is an atomic operation
		boolean intStatus = Machine.interrupt().disable();
		
		// Ready all threads whose wait time has expired
		long curTime = Machine.timer().getTime();
		while(!waitingThreads.isEmpty() && waitingThreads.firstKey() <= curTime) {
			waitingThreads.pollFirstEntry().getValue().ready();
		}
		
		Machine.interrupt().restore(intStatus);
		
		// Preempt current thread as normal
		KThread.currentThread().yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 *
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 *
	 * @param x
	 *            the minimum number of clock ticks to wait.
	 *
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
//		long wakeTime = Machine.timer().getTime() + x;
//		while (wakeTime > Machine.timer().getTime())
//			KThread.yield();
		
		//Sleeping a thread is an atomic operation
		boolean intStatus = Machine.interrupt().disable();
		
		//Place current thread on a wait queue and put it to sleep
		waitingThreads.put(Machine.timer().getTime() + x, KThread.currentThread());
		KThread.sleep();
		
		Machine.interrupt().restore(intStatus);
	}
}



















