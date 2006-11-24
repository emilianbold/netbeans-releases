package org.netbeans.installer.downloader.dispatcher.impl;

import static java.lang.Thread.State.NEW;
import java.util.Queue;
import java.util.LinkedList;

/**
 * @author Danila_Dugurov
 */
public class WorkersPool {

  private final int poolSize;

  private int inUse;
  private Queue<Worker> freeWorkers = new LinkedList<Worker>();

  public WorkersPool(int poolSize) {
    this.poolSize = poolSize;
  }
  
  public int poolSize() {
    return poolSize;
  }

  //noblocking
  public synchronized Worker tryAcquire() {
    if (inUse == poolSize) return null;
    inUse++;
    final Worker worker = freeWorkers.poll();
    return worker != null ? worker : new Worker();
  }

  public synchronized Worker acquire() throws InterruptedException {
    while (true) {
      final Worker worker = tryAcquire();
      if (worker == null) wait();
      else
        return worker;
    }
  }

  public synchronized void release(Worker worker) {
    inUse--;
    if (worker.isAlive()) freeWorkers.offer(worker);
    else if (NEW == worker.getState()) freeWorkers.offer(worker);
    notify();
  }
}
