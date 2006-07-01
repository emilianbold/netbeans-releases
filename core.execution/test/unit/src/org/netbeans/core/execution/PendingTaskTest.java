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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import junit.framework.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.progress.module.Controller;
import org.netbeans.progress.module.ProgressEvent;
import org.netbeans.progress.module.ProgressUIWorker;
import org.openide.actions.ActionManager;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
public class PendingTaskTest extends NbTestCase {
    
    public PendingTaskTest(String testName) {
	super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    
    public void testActionManagersInvokeAction() throws InterruptedException {
        class BlockingAction extends AbstractAction implements Runnable {
            public synchronized void actionPerformed(ActionEvent e) {
                notifyAll();
                try {
                    wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    fail("No InterruptedException please");
                }
            }

            
            public void run() {
                ActionManager.getDefault().invokeAction(this, new ActionEvent(this, 0, ""));
            }
        }
        
        BlockingAction b = new BlockingAction();
        

        assertEquals("No tasks now", Install.getPendingTasks().size(), 0);
        
        RequestProcessor.Task t;
        synchronized (b) {
            t = RequestProcessor.getDefault().post(b);
            b.wait();
        }
        
        assertEquals("One action in progress", 1, Install.getPendingTasks().size());
        
        synchronized (b) {
            b.notifyAll();
        }
        t.waitFinished();
        
    	assertEquals("Action finished", Install.getPendingTasks().size(), 0);
    }

    
    public void testProgressTasks() throws InterruptedException {
        class MyWorker implements ProgressUIWorker {
            int cnt;
        
            public synchronized void processProgressEvent(ProgressEvent event) {
                cnt++;
                getLog().println("processProgressEvent: " + event);
                notifyAll();
            }
            public void processSelectedProgressEvent(ProgressEvent event) {
                getLog().println("processSelectedProgressEvent: " + event);
            }

            public synchronized void waitForEvent() throws InterruptedException {
                int prev = cnt;
                getLog().println("waitForEvent before wait");
                wait(5000);
                getLog().println("waitForEvent after wait");
                if (prev == cnt) {
                    fail("Time out - no event delivered");
                }
            }
        }
        
        MyWorker worker = new MyWorker();
        Controller.defaultInstance = new Controller(worker);
        
        ProgressHandle proghandle = ProgressHandleFactory.createHandle("a1");
        proghandle.setInitialDelay(0);
        
        assertEquals("None before", 0, Install.getPendingTasks().size());

        synchronized (worker) {
            getLog().println("proghandle - start");
            proghandle.start();
            worker.waitForEvent();
        }
            
        assertEquals("One now", 1, Install.getPendingTasks().size());
	
        // waiting a while to overcome possible optimizations in progress api
        // that prevent the finish event to be delivered
        Thread.sleep(1000);
        
        synchronized (worker) {
            getLog().println("proghandle - finish");
            proghandle.finish();
            worker.waitForEvent();
        }
        
        assertEquals("None after", 0, Install.getPendingTasks().size());
    }

}
