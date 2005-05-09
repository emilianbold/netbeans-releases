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


package org.netbeans.api.progress;

import junit.framework.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import org.netbeans.progress.module.Controller;
import org.netbeans.progress.module.InternalHandle;
import org.netbeans.progress.module.ProgressUIWorker;
import org.netbeans.progress.module.ProgressEvent;
import org.openide.util.Cancellable;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public class ProgressHandleTest extends TestCase {
    
    ProgressHandle proghandle;
    InternalHandle handle;
    public ProgressHandleTest(String testName) {
        super(testName);
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    protected void setUp() throws Exception {
        Controller.defaultInstance = new Controller(new ProgressUIWorker() {
            public void processProgressEvent(ProgressEvent event) { }
            public void processSelectedProgressEvent(ProgressEvent event) { }
        });
        proghandle = ProgressHandleFactory.createHandle("displayName",new Cancellable() {
            public boolean cancel() {
                // empty
                return true;
            }
        });
        handle = proghandle.getInternalHandle();
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ProgressHandleTest.class);
        
        return suite;
    }

    /**
     * Test of getDisplayName method, of class org.netbeans.progress.api.ProgressHandle.
     */
    public void testGetDisplayName() {
        assertEquals("displayName", handle.getDisplayName());
    }

    /**
     * Test of getState method, of class org.netbeans.progress.api.ProgressHandle.
     */
    public void testGetState() {
        assertEquals(InternalHandle.STATE_INITIALIZED, handle.getState());

        boolean ok = false;
        try {
            // cannot finish a task before starting.
            proghandle.finish();
        } catch (IllegalStateException exc) {
            ok = true;
        }
        assertTrue(ok);
        
        proghandle.start();
        assertEquals(InternalHandle.STATE_RUNNING, handle.getState());
        ok = false;
        try {
            // cannot start a task repeatedly.
            proghandle.start();
        } catch (IllegalStateException exc) {
            ok = true;
        }
        assertTrue(ok);
        // package private call, user triggered cancel action.
        handle.requestCancel();
        assertEquals(InternalHandle.STATE_REQUEST_STOP, handle.getState());
        proghandle.finish();
        assertEquals(InternalHandle.STATE_FINISHED, handle.getState());
    }

    /**
     * Test of isAllowCancel method, of class org.netbeans.progress.api.ProgressHandle.
     */
    public void testIsAllowCancel() {
        assertTrue(handle.isAllowCancel());
        ProgressHandle h2 = ProgressHandleFactory.createHandle("ds2");
        InternalHandle handle2 = h2.getInternalHandle();
        assertFalse(handle2.isAllowCancel());
    }

    /**
     * Test of isCustomPlaced method, of class org.netbeans.progress.api.ProgressHandle.
     */
    public void testIsCustomPlaced() {
        assertFalse(handle.isCustomPlaced());
        JComponent comp = ProgressHandleFactory.createProgressComponent(proghandle);
        assertTrue(handle.isCustomPlaced());
        boolean ok = false;
        try {
            // cannot get the custom component multiple times..
            comp = ProgressHandleFactory.createProgressComponent(proghandle);
        } catch (IllegalStateException exc) {
            ok = true;
        }
        
        assertTrue(ok);
    }
    
    // tasks shorter than the InternalHandle.INITIAL_DELAY should be discarded.
    public void testIfShortOnesGetDiscarded() throws Exception {
        Controller.defaultInstance = new Controller(new FailingUI());
        proghandle = ProgressHandleFactory.createHandle("a1");
        proghandle.start();
        proghandle.progress("");
        proghandle.finish();
        
        proghandle = ProgressHandleFactory.createHandle("a2");
        ProgressHandle h2 = ProgressHandleFactory.createHandle("a3");
        proghandle.start();
        proghandle.progress("");
        try {
            Thread.sleep(300);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        h2.start();
        h2.progress("");
        proghandle.finish();
        try {
            Thread.sleep(300);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        h2.finish();
    }
    
    
    private class FailingUI implements ProgressUIWorker {
            public void processProgressEvent(ProgressEvent event) {
                fail("How come we are processing a short one");
            }
            public void processSelectedProgressEvent(ProgressEvent event) {
                fail("How come we are processing a short one");
            }
    }
  
    public void testIfLongOnesGetProcessed() throws Exception {
        PingingUI ui = new PingingUI();
        Controller.defaultInstance = new Controller(ui);
        proghandle = ProgressHandleFactory.createHandle("b1");
        proghandle.start();
        try {
            Thread.sleep(1200);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        proghandle.finish();
        assertTrue(ui.pinged);
    }    
    
    private class PingingUI implements ProgressUIWorker {
        public boolean pinged = false;
            public void processProgressEvent(ProgressEvent event) {
                pinged = true;
            }
            public void processSelectedProgressEvent(ProgressEvent event) {
                pinged = true;
            }
    }
    
    
    
}
