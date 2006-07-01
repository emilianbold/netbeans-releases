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

package org.openide.util;

import java.awt.AWTEvent;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Dafe Simonek
 */
public class InitJobTest extends NbTestCase {
    /** testing dialog instance */
    Dialog dlg;
    /** arrays which hold calls history */
    ArrayList constructCalls, finishCalls, cancelCalls;
    /** event dispatch thread */
    Thread edThread;
    /** test component */
    SimpleInitComp comp;
    
    /** Creates a new instance of UtilProgressCursorTest */
    public InitJobTest(String testName) {
        super(testName);
    }
    
    /** Run tests in EQ thread, as it touches Swing */
    protected boolean runInEQ() {
        return true;
    }
    
    /** Basic testing of Utilities.attachInitJob, if calls to AsyncGUIJob
     * impl conforms to the API behaviour described in javadoc *
     */
    public void testInitJob() throws Exception {
        initializeSimple();
        comp = new SimpleInitComp();
        Utilities.attachInitJob(comp, comp);
        Frame f = new Frame();
        f.setVisible(true);
        dlg = new Dialog(f, true);
        dlg.add(comp);
        dlg.setVisible(true);
    }
    
    public void testCancelAbility() throws Exception {
        initializeSimple();
        initCancelResults();
        CancelInitComp comp = new CancelInitComp();
        Utilities.attachInitJob(comp, comp);
        Frame f = new Frame();
        f.setVisible(true);
        dlg = new Dialog(f, true);
        dlg.add(comp);
        dlg.setVisible(true);
    }
    
    
    /**********************************************************************/
    
    private void constructCalled(Thread thread, long time) {
        constructCalls.add(new CallData(thread, time));
    }
    
    private void finishCalled(Thread thread, long time) {
        finishCalls.add(new CallData(thread, time));
    }
    
    private void cancelCalled() {
        cancelCalls.add(new CallData(Thread.currentThread(), System.currentTimeMillis()));
    }
    
    private void checkSimpleResults() {
        if (constructCalls.size() != 1) {
            fail("AsyncGUIJob.construct was called " + constructCalls.size() +
                    " times intead of just once.");
        }
        if (finishCalls.size() != 1) {
            fail("AsyncGUIJob.finish was called " + finishCalls.size() +
                    " times intead of just once.");
        }
        CallData constructCall = (CallData)constructCalls.get(0);
        CallData finishCall = (CallData)finishCalls.get(0);
        if (constructCall.thread.equals(edThread)) {
            fail("AsyncGUIJob.construct *was* called from event dispatch thread, " +
                    "which is wrong.");
        }
        if (!finishCall.thread.equals(edThread)) {
            fail("AsyncGUIJob.finish *was not* called from event dispatch thread, " +
                    "which is wrong.");
        }
        if (constructCall.time > finishCall.time) {
            fail("AsyncGUIJob.finish was called before AsyncGUIJob.construct, " +
                    "which is wrong.");
        }
        AWTEventListener[] awtListeners =
                Toolkit.getDefaultToolkit().getAWTEventListeners(AWTEvent.PAINT_EVENT_MASK);
        for (int i = 0; i < awtListeners.length; i++) {
            if (awtListeners[i].getClass().equals(AsyncInitSupport.class)) {
                fail("Probable memory leak: AsyncInitSupport didn't detached " +
                        "from AWT toolkit.");
            }
        }
        EventListener[] listeners = comp.getListeners(AsyncInitSupport.class);
        if (listeners.length != 0) {
            fail("Probable memory leak: AsyncInitSupport didn't detached " +
                    "from component being inited " + comp);
        }
    }
    
    private void checkCancelResults() {
        if (cancelCalls.size() != 1) {
            fail("Cancellable.cancel was called " + cancelCalls.size() +
                    " times intead of just once.");
        }
        if (finishCalls.size() != 0) {
            fail("AsyncGUIJob.finish should not been called at all, but was called "
                    + finishCalls.size() + " times.");
        }
    }
    
    private void initializeSimple() throws Exception {
        constructCalls = new ArrayList();
        finishCalls = new ArrayList();
        if (SwingUtilities.isEventDispatchThread()) {
            edThread = Thread.currentThread();
        } else {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    edThread = Thread.currentThread();
                }
            });
        }
    }
    
    private void initCancelResults() {
        cancelCalls = new ArrayList();
    }
    
    /** Structure for holding data of method call */
    private final static class CallData {
        Thread thread;
        long time;
        
        public CallData(Thread thread, long time) {
            this.thread = thread;
            this.time = time;
        }
    }
    
    private final class TimerListener implements ActionListener {
        /** true for cancel test, false otherwise */
        private boolean cancel;
        public TimerListener(boolean cancel) {
            this.cancel = cancel;
        }
        public void actionPerformed(ActionEvent e) {
            dlg.dispose();
            if (cancel) {
                checkCancelResults();
            } else {
                checkSimpleResults();
            }
        }
    }
    
    /** Testing component for asynchronous init
     */
    private final class SimpleInitComp extends JPanel implements AsyncGUIJob {
        
        /** Worker method, can be called in any thread but event dispatch thread.
         * Implement your time consuming work here.
         * Always called and completed before {@link #finished} method.
         *
         */
        public void construct() {
            constructCalled(Thread.currentThread(), System.currentTimeMillis());
        }
        
        /** Method to update UI using given data constructed in {@link #construct}
         * method. Always called in event dispatch thread, after {@link #construct}
         * method completed its execution.
         *
         */
        public void finished() {
            finishCalled(Thread.currentThread(), System.currentTimeMillis());
        }
        
        public void paint(Graphics g) {
            super.paint(g);
            Timer timer = new Timer(1000, new TimerListener(false));
            timer.setRepeats(false);
            timer.start();
        }
        
    } // end of SimpleInitComp
    
    /** Testing component for cancel during asynchronous init
     */
    private final class CancelInitComp extends JPanel implements AsyncGUIJob, Cancellable {
        
        /** Worker method, can be called in any thread but event dispatch thread.
         * Implement your time consuming work here.
         * Always called and completed before {@link #finished} method.
         *
         */
        public void construct() {
            // perform loooong task
            try {
                Thread.sleep(2000);
            } catch (InterruptedException exc) {
                // continue...
            }
        }
        
        /** Method to update UI using given data constructed in {@link #construct}
         * method. Always called in event dispatch thread, after {@link #construct}
         * method completed its execution.
         *
         */
        public void finished() {
            finishCalled(Thread.currentThread(), System.currentTimeMillis());
        }
        
        public void paint(Graphics g) {
            super.paint(g);
            Timer timer = new Timer(1000, new TimerListener(true));
            timer.setRepeats(false);
            timer.start();
        }
        
        /** Cancel processing of the job.
         *
         */
        public boolean cancel() {
            cancelCalled();
            return true;
        }
        
    } // end of SimpleInitComp
    
}
