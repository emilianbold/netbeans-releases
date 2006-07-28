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

package org.netbeans.api.progress;

import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.progress.module.Controller;
import org.netbeans.progress.spi.InternalHandle;
import org.netbeans.progress.spi.ProgressUIWorker;
import org.netbeans.progress.spi.ProgressEvent;
import org.openide.util.Cancellable;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public class ProgressHandleTest extends NbTestCase {
    
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
        OneThreadController control = new OneThreadController(new FailingUI());
        Controller.defaultInstance = control;
        proghandle = ProgressHandleFactory.createHandle("a1");
        proghandle.start();
        proghandle.progress("");
        proghandle.finish();
        
        //simulate timer run
        control.run();
        //after running the timer sould be stopped
        assertTrue(control.tobeRestartedDelay == -1);

        
        proghandle = ProgressHandleFactory.createHandle("a2");
        ProgressHandle h2 = ProgressHandleFactory.createHandle("a3");
        proghandle.start();
        proghandle.progress("");
        try {
            Thread.sleep(300);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        
        //simulate timer run
        control.run();
        // timer should continue
        assertFalse(control.tobeRestartedDelay == -1);
        
        h2.start();
        h2.progress("");
        proghandle.finish();
        
        //simulate timer run
        control.run();
        // timer should be continuing
        assertFalse(control.tobeRestartedDelay == -1);
        
        try {
            Thread.sleep(300);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        h2.finish();
        //simulate timer run
        control.run();
        // timer should be stopped
        assertTrue(control.tobeRestartedDelay == -1);
        
    }
    
    // tasks shorter than the custom init delay should be discarded.
    public void testIfCustomShortOnesGetDiscarded() throws Exception {
        System.out.println("testIfCustomShortOnesGetDiscarded");
        OneThreadController control = new OneThreadController(new FailingUI());
        Controller.defaultInstance = control;
        proghandle = ProgressHandleFactory.createHandle("c1");
        proghandle.setInitialDelay(100);
        proghandle.start();
        proghandle.progress("");
        proghandle.finish();
        
        //simulate timer run
        control.run();
        //after running the timer sould be stopped
        assertTrue(control.tobeRestartedDelay == -1);
        
        proghandle = ProgressHandleFactory.createHandle("c2");
        ProgressHandle h2 = ProgressHandleFactory.createHandle("c3");
        proghandle.setInitialDelay(100);
        proghandle.start();
        proghandle.progress("");
        try {
            Thread.sleep(50);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        //simulate timer run
        control.run();
        // timer should continue
        assertFalse(control.tobeRestartedDelay == -1);
        
        h2.setInitialDelay(1000);
        h2.start();
        h2.progress("");
        proghandle.finish();
        
        //simulate timer run
        control.run();
        // timer should be continuing
        assertFalse(control.tobeRestartedDelay == -1);
        
        try {
            Thread.sleep(600);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        h2.finish();
        control.run();
        // timer should NOT continue
        assertTrue(control.tobeRestartedDelay == -1);
    }    
    
    private class FailingUI implements ProgressUIWorker {
            public void processProgressEvent(ProgressEvent event) {
                fail("How come we are processing a short one - " + event.getSource().getDisplayName());
            }
            public void processSelectedProgressEvent(ProgressEvent event) {
                fail("How come we are processing a short one - " + event.getSource().getDisplayName());
            }
    }
    
    private class OneThreadController extends Controller {
        
        public int tobeRestartedDelay = -1;
        
        public OneThreadController(ProgressUIWorker comp) {
            super(comp);
        }
        
        protected void resetTimer(int initialDelay, boolean restart) {
            timer.setInitialDelay(initialDelay);
            if (restart) {
                tobeRestartedDelay = initialDelay;
            } else {
                tobeRestartedDelay = -1;
            }
        }
    }

  
    public void testIfLongOnesGetProcessed() throws Exception {
        assert !SwingUtilities.isEventDispatchThread();
        PingingUI ui = new PingingUI();
        Controller.defaultInstance = new Controller(ui);
        proghandle = ProgressHandleFactory.createHandle("b1");
        proghandle.start();
        try {
            Thread.sleep(800);
        } catch (InterruptedException exc) {
            System.out.println("interrupted");
        }
        proghandle.finish();
        assertTrue(ui.pinged);
    } 
    
    public void testIfCustomLongOnesGetProcessed() throws Exception {
        assert !SwingUtilities.isEventDispatchThread();
        PingingUI ui = new PingingUI();
        Controller.defaultInstance = new Controller(ui);
        proghandle = ProgressHandleFactory.createHandle("b1");
        proghandle.setInitialDelay(100);
        proghandle.start();
        try {
            Thread.sleep(200);
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
    

/**
 * test switching in non-status bar component
 */
    public void testSwitch() {
        final int WAIT = 1500;

class MyFrame extends JFrame implements Runnable {
    
    JComponent component;
    
    public MyFrame(JComponent component) {
        getContentPane().add(component);
    }
    
    public void run() {
        setVisible(true);
        setBounds(0, 0, 400, 50);
    }
}
        
        assertFalse(SwingUtilities.isEventDispatchThread());
        ProgressHandle handle = ProgressHandleFactory.createHandle("foo");
        JComponent component = ProgressHandleFactory.createProgressComponent(handle);
        

        
        handle.start();
        
        MyFrame frm = new MyFrame(component);
        SwingUtilities.invokeLater(frm);
        
        try {
            Thread.sleep(WAIT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
        
        
        handle.switchToDeterminate(100);
        handle.progress(50);
        
        try {
            Thread.sleep(WAIT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            Method meth = component.getClass().getMethod("isIndeterminate", new Class[0]);
            Boolean bool = (Boolean)meth.invoke(component, new Object[0]);
            assertFalse("The progress bar is still indeterminate!", bool.booleanValue());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
        handle.finish();
        frm.setVisible(false);
        frm.dispose();
    }    

    protected boolean runInEQ() {
        return false;
    }

    
    public void testManyWorkUnits () {
        int units = 500000;
        int step = 500;
        proghandle.start (units * step);
        for (int i = 200; i < units; i = i+ 50) {
            proghandle.progress (i * step);
            assertTrue(handle.getPercentageDone() >= 0);
        }
        proghandle.finish (); 
    }
    
    
}
