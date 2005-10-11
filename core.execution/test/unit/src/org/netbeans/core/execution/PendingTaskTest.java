/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.execution;

import junit.framework.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.progress.module.Controller;
import org.netbeans.progress.module.ProgressEvent;
import org.netbeans.progress.module.ProgressUIWorker;

/**
 *
 * @author mkleint
 */
public class PendingTaskTest extends TestCase {
    
    public PendingTaskTest(String testName) {
	super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
// TODO add test methods here. The name must begin with 'test'. For example:
// public void testHello() {}
    public void testProgressTasks() {
        Controller.defaultInstance = new Controller(new ProgressUIWorker() {
            public void processProgressEvent(ProgressEvent event) { }
            public void processSelectedProgressEvent(ProgressEvent event) { }
        });
        ProgressHandle proghandle = ProgressHandleFactory.createHandle("a1");
	proghandle.setInitialDelay(0);
	assertEquals(Install.getPendingTasks().size(), 0);
	
        proghandle.start();
	assertEquals(Install.getPendingTasks().size(), 1);
	
	proghandle.finish();
	// we need to sleep because the progress handling is scheduled and processed in quantas.
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException ex) {
	    ex.printStackTrace();
	}
	assertEquals(Install.getPendingTasks().size(), 0);
    }

    public void testActionManagersInvokeAction() {
        assertEquals(Install.getPendingTasks().size(), 0);
        
        assertEquals(Install.getPendingTasks().size(), 1);
        
        assertEquals(Install.getPendingTasks().size(), 1);
	assertEquals(Install.getPendingTasks().size(), 0);
    }

}
