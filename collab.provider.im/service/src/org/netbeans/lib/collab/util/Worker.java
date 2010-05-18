/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.collab.util;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 *
 */
public class Worker implements Runnable{

    private LinkedList queue = new LinkedList();
    private ArrayList threads;
    private volatile boolean RUN = true;
    private Runnable _lastRunnable = null;
    private int maxThreads = 1;
    private int waitingThreads;
    private String thrNamePrefix = "org.netbeans.lib.collab.util.Worker";
    private int _capacity = -1;

    private static final boolean DEBUG = false;

    /**
     * 
     * 
     * @param 
     */
    public Worker(){
        init(maxThreads, maxThreads, -1, null);
    }

    
    /**
     * creates a Thread Pool
     * @param threadCnt number of threads in this pool
     */
    public Worker(int threadCnt) {
        init(threadCnt,threadCnt,-1,null);
    }
    
    /**
     * creates a Thread Pool
     * @param minThreads initial number of threads
     * @param maxThreads maximum number of threads
     * In this implementation the number of threads in the pool
     * never goes downn.
     */
    public Worker(int minThreads, int maxThreads){
        init(minThreads,maxThreads,-1,null);
    }
    
    /**
     * creates a Thread Pool
     * @param minThreads initial number of threads
     * @param maxThreads maximum number of threads
     * @param capacity maximum number of pending jobs in the
     * queue.  if the queue size reaches this value, 
     * addRunnable blocks until the queue goes back below capacity.
     */
    public Worker(int minThreads, int maxThreads, int capacity){
        init(minThreads,maxThreads,capacity,null);
    }
    
     /**
     * creates a Thread Pool
     * @param minThreads initial number of threads
     * @param maxThreads maximum number of threads
     * @param capacity maximum number of pending jobs in the
     * queue.  if the queue size reaches this value, 
     * addRunnable blocks until the queue goes back below capacity.
     * @param thrNamePrefix prefix for pool thread names.  This is useful
     * for diagnostic if you have multiple Worker pools.
     */
    public Worker(int minThreads, int maxThreads, int capacity, String thrNamePrefix){
        init(minThreads,maxThreads,capacity,thrNamePrefix);
    }
    /**
     * creates a Thread Pool
     * @param minThreads initial number of threads
     * @param maxThreads maximum number of threads
     * @param thrNamePrefix prefix for pool thread names.  This is useful
     * for diagnostic if you have multiple Worker pools.
     */
    public Worker(int minThreads, int maxThreads, 
		  String thrNamePrefix){
        init(minThreads,maxThreads,-1,thrNamePrefix);
    }
    
    private void init(int minThreads, int maxThreads, int capacity,
		      String thrNamePrefix) {
	_capacity = capacity;
        if (maxThreads < minThreads) {
            this.maxThreads = minThreads;
        } else {
            this.maxThreads = maxThreads;  
        }
        
        if(thrNamePrefix != null) {
            this.thrNamePrefix = thrNamePrefix;
        }
        threads = new ArrayList(maxThreads);
        synchronized (this){
            for (int i = 0; i < minThreads; i++) {
                Thread t = new Thread(this, this.thrNamePrefix + " " + i);
                threads.add(t);
                t.start();
                waitingThreads ++;
            }
        }
    }


    /**
     * @param r runnable to run 
     * @return number of elements in the job queue, including the
     * one added by this method
     */
    public synchronized int addRunnable(Runnable r) {
        if (!RUN) return queue.size();

        queue.addLast(r);
        if (waitingThreads > 0) {
            notify();
        }
        if (threads.size() < maxThreads && waitingThreads < queue.size()) {
            Thread t = new Thread(this, thrNamePrefix + " " + threads.size());
            threads.add(t);
            t.start();
            // bump the count.
            waitingThreads ++;
        }
	return queue.size();
    }


    /**
     * @param r runnable to run 
     * @return number of elements in the job queue, including the
     * one added by this method
     */
    public synchronized int addRunnableIfPossible(Runnable r) {
        if (!RUN) return queue.size();

	if (_capacity > maxThreads && queue.size() >= _capacity) {
	    Thread.yield();
	    return -1;
	}

	return addRunnable(r);
    }

    public synchronized boolean isFull() {
	return (_capacity > maxThreads && queue.size() >= _capacity);
    }

    public synchronized int backlog() {
	return queue.size();
    }

    /**
     * 
     * 
     * @param 
     */
    public void run() {
       synchronized (this) {
           waitingThreads --;
       }

       while(RUN) { 
           Runnable r = null;
	   synchronized (this) {
	       while (queue.size() == 0) {
		   try {
		       waitingThreads++;
		       wait(); 
		   } catch(InterruptedException ex){
		       ex.printStackTrace();
                   }finally {
                       waitingThreads--;
                   }
                   if (!RUN) {
                       return;
                   }
	       }
	       r = (Runnable)queue.removeFirst();
               if (_lastRunnable == r){
                   RUN = false;
                   // Wake up all threads.
                   notifyAll();
               }
	   }
            
	   try {                    
	       if (r != null) {
                   Thread t = null;
                   String oldName = null;
                   if(DEBUG) {
                    t = Thread.currentThread();
                    oldName = t.getName();
                    t.setName(r.toString());
                   }
                   r.run();
                   if(DEBUG) {
                    t.setName(oldName);
                   }
               }                    
	   } catch(Throwable t){
	       t.printStackTrace();
	   }            
       }
    }
    
    
    
    /**
     * Stops and joins all threads of this worker
     */
    final public void stop(){
        RUN = false;
        synchronized (this) { notifyAll(); }
    }
    
    /**
     * Stop the worker thread with this last runnable object
     * @param runnable last job that will be run.  After this runnable
     * is dequeued, no other job is dequeued or enqueued.
     */
    final public void stop(Runnable r) {
        addRunnable(_lastRunnable = r);
    }
    
    //for test
    ArrayList getThreads() {
        return threads;
    }

    @Override
    public String toString(){
        return thrNamePrefix + ":" + super.toString();
    }

}


