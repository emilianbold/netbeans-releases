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

package org.netbeans.core.execution;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.SwingUtilities;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;

/**
 * Test that a task thread group is cleared when it is done.
 * @see "#36395"
 * @author Jesse Glick
 */
public class TaskThreadGroupGCTest extends NbTestCase {

    public TaskThreadGroupGCTest(String name) {
        super(name);
    }

	protected void setUp() throws Exception {
		super.setUp();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
			}
		});
	}
	
	
    public void testTTGGC() throws Exception {
        final Reference/*<Thread>*/[] t = new Reference[4];
        Runnable r = new Runnable() {
            public void run() {
                System.out.println("Running a task in the execution engine...");
                t[0] = new WeakReference(Thread.currentThread());
                Runnable r1 = new Runnable() {
                    public void run() {
                        System.out.println("Ran second thread.");
                    }
                };
                Thread nue1 = new Thread(r1);
                nue1.start();
                try {
                    nue1.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                t[1] = new WeakReference(nue1);
                Runnable r2 = new Runnable() {
                    public void run() {
                        System.out.println("Ran third thread.");
                    }
                };
                Thread nue2 = new Thread(r2);
                nue2.start();
                t[2] = new WeakReference(nue2);
                Runnable r3 = new Runnable() {
                    public void run() {
                        fail("Should not have even run.");
                    }
                };
                Thread nue3 = new Thread(r3);
                t[3] = new WeakReference(nue3);
                System.out.println("done.");
            }
        };
        ExecutorTask task = ExecutionEngine.getDefault().execute("foo", r, null);
        assertEquals(0, task.result());
        assertNotNull(t[0]);
        assertNotNull(t[1]);
        assertNotNull(t[2]);
        assertNotNull(t[3]);
        r = null;
        task = null;
        assertGC("Collected task thread", t[0]);
        assertGC("Collected secondary task thread too", t[1]);
        assertGC("Collected forked task thread too", t[2]);
        assertGC("Collected unstarted task thread too", t[3]);
    }
    
}
