/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.performance.platform;

import java.awt.EventQueue;
import org.netbeans.performance.Benchmark;

/**
 * Benchmark measuring speed of EventQueue methods.
 * @author Jesse Glick
 */
public class EventQueueTest extends Benchmark {
    
    public static void main(String[] args) {
        simpleRun(EventQueueTest.class);
    }
    
    public EventQueueTest(String name) {
        super(name);
    }
    
    public void testIsDispatchThreadWhenFalse() throws Exception {
        int count = getIterationCount();
        for (int i = 0; i < count; i++) {
            assertTrue(!EventQueue.isDispatchThread());
        }
    }
    
    public void testIsDispatchThreadWhenTrue() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                int count = getIterationCount();
                for (int i = 0; i < count; i++) {
                    assertTrue(EventQueue.isDispatchThread());
                }
            }
        });
    }
    
    public void testInvokeAndWait() throws Exception {
        int count = getIterationCount();
        for (int i = 0; i < count; i++) {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    assertTrue(EventQueue.isDispatchThread());
                }
            });
        }
    }
    
    public void testAcquireMonitor() throws Exception {
        Object lock = new Object();
        int count = getIterationCount();
        for (int i = 0; i < count; i++) {
            synchronized (lock) {
                assertTrue(true);
            }
        }
    }
    
    public void testCheckAcquiredMonitor() throws Exception {
        Object lock = new Object();
        synchronized (lock) {
            int count = getIterationCount();
            for (int i = 0; i < count; i++) {
                assertTrue(Thread.holdsLock(lock));
            }
        }
    }
    
}
