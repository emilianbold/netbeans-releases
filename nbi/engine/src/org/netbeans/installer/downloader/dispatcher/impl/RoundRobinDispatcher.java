package org.netbeans.installer.downloader.dispatcher.impl;

import java.util.HashSet;
import org.netbeans.installer.downloader.dispatcher.Process;
import org.netbeans.installer.downloader.dispatcher.ProcessDispatcher;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.netbeans.installer.utils.helper.MutualHashMap;
import org.netbeans.installer.utils.helper.MutualMap;
import org.netbeans.installer.utils.helper.ThreadUtil;
import org.omg.CORBA.Current;

/**
 * @author Danila_Dugurov
 */
public class RoundRobinDispatcher implements ProcessDispatcher {
  private final int timeQuantum;
  private final int pollingTime;
  private final WorkersPool pool;
  private final BlockingQueue<Worker> workingQueue;
  private final Queue<Process> waitingQueue;
  private final MutualMap<Process, Worker> proc2Worker;
  private final Set<Process> processes = new HashSet<Process>();

  private Thread dispatcherThread;
  private Terminator terminator = new Terminator();
  private boolean isActive;

  public RoundRobinDispatcher(int quantum, int poolSize) {
    if (quantum < 10 || poolSize < 1)
      throw new IllegalArgumentException();
    this.timeQuantum = quantum;
    this.pollingTime = timeQuantum * 5;
    this.pool = new WorkersPool(poolSize);
    workingQueue = new ArrayBlockingQueue<Worker>(poolSize);
    waitingQueue = new LinkedList<Process>();
    proc2Worker = new MutualHashMap<Process, Worker>();
  }

  public synchronized boolean schedule(Process process) {
    synchronized (waitingQueue) {
      if (processes.contains(process)) return false;
      waitingQueue.offer(process);
      processes.add(process);
    }
    return true;
  }
  
  public synchronized void terminate(Process process) {
    terminateInternal(process);
  }

  private void terminateInternal(Process process) {
    synchronized (waitingQueue) {
      if (waitingQueue.contains(process)) {
        waitingQueue.remove(process);
        return;
      }
    }
    final Worker worker = proc2Worker.get(process);
    if (worker == null) return;
    synchronized (worker) {
      if (worker.isFree()) return;
      worker.resume();
      if (!terminator.isAlive()) terminator.start();
      worker.interrupt();
      terminator.terminate(process);
      ThreadUtil.sleep(timeQuantum);
      if (terminator.isBusy()) {
        terminator.stop();
        terminator = new Terminator();
      }
      if (!worker.isFree()) worker.stop();
    }
  }

  public synchronized boolean isActive() {
    return isActive;
  }

  //for tracknig perpose no synchronization so no sure of correctness
  public int activeCount() {
    return workingQueue.size();
  }

  //for tracknig perpose no synchronization so no sure of correctness
  public int waitingCount() {
    return waitingQueue.size();
  }

  public synchronized void start() {
    if (isActive) return;
    dispatcherThread = new Thread(new DispathcerWorker());
    dispatcherThread.setDaemon(true);
    dispatcherThread.start();
    isActive = true;
  }

  public synchronized void stop() {
    if (!isActive) return;
    dispatcherThread.interrupt();
    try {
      dispatcherThread.join(timeQuantum * pool.poolSize() + pollingTime);
    } catch (InterruptedException exit) {
    } finally {
      //this condition mustn't happens to true
      if (dispatcherThread.isAlive()) dispatcherThread.stop();
    }
    isActive = false;
  }

  private class DispathcerWorker implements Runnable {
    Worker current;

    public void run() {
      while (true) {
        if (Thread.interrupted()) break;
        try {
          current = workingQueue.poll(pollingTime, TimeUnit.MILLISECONDS);
          if (invokeCurrent())
            synchronized (this) {
              wait(timeQuantum);
            }
        } catch (InterruptedException exit) {
          break;
        } finally  {
          suspendCurrent();
        }
      }
      terminateAll();
    }
    
    private void terminateAll() {
      for (Process process: processes) {
        terminateInternal(process);
      }
    }
    
    private boolean invokeCurrent() {
      if (current == null) {
        filWorkingQueue();
        return false;
      }
      switch (current.getState()) {
        case NEW:
          current.start();
          break;
        case RUNNABLE:
          current.resume();
          break;
        default:
           current.resume(); 
          //temrorary while blocking queue not impl.
          return true;
      }
      return true;
    }

    private void suspendCurrent() {
      final Worker current = this.current;
      if (current == null) return;
      synchronized (current) {
        current.suspend();
        if (current.isAlive() && !current.isFree())
          workingQueue.offer(current);
        else {
          processes.remove(proc2Worker.reversedRemove(current));
          pool.release(current);
        }
      }
      filWorkingQueue();
    }

    private void filWorkingQueue() {
      synchronized (waitingQueue) {
        while (workingQueue.remainingCapacity() > 0) {
          final Process process = waitingQueue.poll();
          if (process == null) return;
          final Worker worker = pool.tryAcquire();
          worker.setCurrent(process);
          proc2Worker.put(process, worker);
          workingQueue.add(worker);
        }
      }
    }
  }

  //cool name. don't you think so?
  private class Terminator extends Thread {

    private Process current;

    public Terminator() {
      super();
      setDaemon(true);
    }

    public synchronized void terminate(Process process) {
      current = process;
      notify();
    }

    public void run() {
      while (true) {
        try {
          synchronized (this) {
            if (current == null)
              wait();
          }
          current.terminate();
          synchronized (this) {
            current = null;
          }
        } catch (InterruptedException exit) {
          System.out.println("terminator interrupted");
          break;
        }
      }
    }

    public synchronized boolean isBusy() {
      return current == null;
    }
  }
}