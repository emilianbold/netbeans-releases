/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.downloader.dispatcher.impl;

import org.netbeans.installer.downloader.dispatcher.LoadFactor;
import org.netbeans.installer.downloader.dispatcher.Process;
import org.netbeans.installer.downloader.dispatcher.ProcessDispatcher;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.helper.MutualHashMap;
import org.netbeans.installer.utils.helper.MutualMap;
import org.netbeans.installer.utils.helper.ThreadUtil;
import org.netbeans.installer.utils.installation.conditions.TrueCondition;
import org.omg.CORBA.Current;

/**
 * @author Danila_Dugurov
 */
public class RoundRobinDispatcher implements ProcessDispatcher {
  private static final Map<LoadFactor, Byte> quantumToSkip = new HashMap<LoadFactor, Byte>();
  static {
    quantumToSkip.put(LoadFactor.FULL, (byte)0);
    quantumToSkip.put(LoadFactor.AVERAGE, (byte)2);
    quantumToSkip.put(LoadFactor.LOW, (byte)5);
  }
  private final int timeQuantum;
  private final int pollingTime;
  private final WorkersPool pool;
  private final BlockingQueue<Worker> workingQueue;
  private final Queue<Process> waitingQueue;
  private final MutualMap<Process, Worker> proc2Worker;
  private Set<Worker> makedToStop = new HashSet<Worker>();
  
  private Thread dispatcherThread;
  private Terminator terminator = new Terminator();
  private boolean isActive;
  private LoadFactor factor;
  
  public RoundRobinDispatcher(int quantum, int poolSize) {
    if (quantum < 10 || poolSize < 1)
      throw new IllegalArgumentException();
    this.timeQuantum = quantum;
    this.pollingTime = timeQuantum * 5;
    this.pool = new WorkersPool(poolSize);
    workingQueue = new ArrayBlockingQueue<Worker>(poolSize);
    waitingQueue = new LinkedList<Process>();
    proc2Worker = new MutualHashMap<Process, Worker>();
    factor = LoadFactor.FULL;
  }
  
  public synchronized boolean schedule(Process process) {
    synchronized (waitingQueue) {
      waitingQueue.offer(process);
    }
    return true;
  }
  
  public synchronized void terminate(Process process) {
    synchronized (waitingQueue) {
      if (waitingQueue.remove(process)) return;
    }
    final Worker stoped = proc2Worker.get(process);
    makedToStop.add(stoped);
    terminateInternal(process);
  }
  
  public void setLoadFactor(LoadFactor factor) {
    this.factor = factor;
  }
  
  public LoadFactor loadFactor() {
    return factor;
  }
  
  private void terminateInternal(Process process) {
    final Worker worker = proc2Worker.get(process);
    if (worker == null) return;
    if (worker.isFree()) return;
    if (!terminator.isAlive()) terminator.start();
    terminator.terminate(process);
    ThreadUtil.sleep(timeQuantum);
    if (terminator.isBusy()) {
      terminator.stop();
      terminator = new Terminator();
    }
    if (!worker.isFree()) worker.stop();
    proc2Worker.remove(process);
    pool.release(worker);
  }
  
  public synchronized boolean isActive() {
    return isActive;
  }
  
//for tracknig perpose no synchronization so no sure of correctness
  public int activeCount() {
    return proc2Worker.size();
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
      dispatcherThread.join(timeQuantum * pool.size() + pollingTime);
    } catch (InterruptedException exit) {
    } finally {
      //this condition mustn't happens to true
      if (dispatcherThread.isAlive()) dispatcherThread.stop();
    }
    waitingQueue.clear();
    isActive = false;
  }
  
  private class DispathcerWorker implements Runnable {
    Worker current;
    
    public void run() {
      while (true) {
        if (Thread.interrupted()) break;
        try {
          current = workingQueue.poll(pollingTime, TimeUnit.MILLISECONDS);
          if (current == null || makedToStop.contains(current)) {
            filWorkingQueue();
            continue;
          }
          invokeCurrent();
          Thread.sleep(timeQuantum);
          suspendCurrent();
          if (factor != LoadFactor.FULL) {
            Thread.sleep(quantumToSkip.get(factor) * timeQuantum);
          }
        } catch (InterruptedException exit) {
          suspendCurrent();
          break;
        }
      }
      terminateAll();
    }
    
    private void terminateAll() {
      for (Worker worker: workingQueue.toArray(new Worker[0])) {
        terminateInternal(proc2Worker.reversedGet(worker));
      }
    }
    
    private void invokeCurrent() {
      switch (current.getState()) {
        case NEW:
          current.start();
          break;
        case RUNNABLE:
          current.resume();
          break;
        case TERMINATED: break;
        default:
          current.resume();
          //temprorary while blocking queue not impl.
      }
    }
    
    private void suspendCurrent() {
      if (current == null) return;
      if (makedToStop.contains(current)) return;
      current.suspend();
      if (current.isAlive() && !current.isFree()) {
        workingQueue.offer(current);
      } else {
        proc2Worker.reversedRemove(current);
        pool.release(current);
      }
      filWorkingQueue();
    }
    
    private void filWorkingQueue() {
      if (waitingQueue.size() == 0 || workingQueue.remainingCapacity() == 0) return;
      synchronized (waitingQueue) {
        while (workingQueue.remainingCapacity() > 0) {
          final Process process = waitingQueue.poll();
          if (process == null) return;
          final Worker worker = pool.tryAcquire();
          worker.setCurrent(process);
          proc2Worker.put(process, worker);
          makedToStop.remove(worker);
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
      notifyAll();
    }
    
    public void run() {
      while (true) {
        synchronized (this) {
          try {
            Thread.interrupted();
            if (current == null) {
              wait();
              if (current == null) continue;
            }
            final Worker worker = proc2Worker.get(current);
            worker.resume();
            worker.interrupt();
            try {
              current.terminate();
            } catch (Exception ignored) {//may be log?
            }
            current = null;
          } catch (InterruptedException exit) {
            LogManager.log("terminator interrupted");
            break;
          }
        }
      }
    }
    
    public synchronized boolean isBusy() {
      return current == null;
    }
  }
}
