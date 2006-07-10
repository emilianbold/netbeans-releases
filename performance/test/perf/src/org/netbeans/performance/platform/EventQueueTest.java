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
