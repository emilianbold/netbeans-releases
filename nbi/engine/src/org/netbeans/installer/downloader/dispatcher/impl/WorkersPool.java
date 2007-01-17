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

import static java.lang.Thread.State.NEW;
import java.util.Queue;
import java.util.LinkedList;

/**
 * @author Danila_Dugurov
 */
public class WorkersPool {
  
  /////////////////////////////////////////////////////////////////////////////////
  // Instance
  
  private final int capacity;
  
  private int inUse;
  private Queue<Worker> freeWorkers = new LinkedList<Worker>();
  
  public WorkersPool(int poolCapacity) {
    this.capacity = poolCapacity;
  }
  
  public int capacity() {
    return capacity;
  }
  
  public synchronized int remaining() {
    return capacity - inUse;
  }
  
  //noblocking
  public synchronized Worker tryAcquire() {
    if (inUse == capacity) return null;
    inUse++;
    final Worker worker = freeWorkers.poll();
    return worker != null && worker.isAlive() ? worker : new Worker();
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
  
  public synchronized void stopWaitingWorkers(){}
}
