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
    /** parent, main frame */
    private Frame frame;
    
    /** Creates a new instance of UtilProgressCursorTest */
    public InitJobTest(String testName) {
        super(testName);
    }
    
    /** Run tests in EQ thread, as it touches Swing */
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected int timeOut() {
        return 15000;
    }
    
    /** Basic testing of Utilities.attachInitJob, if calls to AsyncGUIJob
     * impl conforms to the API behaviour described in javadoc *
     */
    public void testInitJob() throws Exception {
        System.out.println("Testing simple init job run");
        initializeSimple();
        comp = new SimpleInitComp();
        Utilities.attachInitJob(comp, comp);
        frame = new Frame();
        frame.setSize(100, 100);
        frame.setVisible(true);
        dlg = new Dialog(frame, true);
        dlg.setSize(50, 50);
        dlg.add(comp);
        dlg.setVisible(true);
    }
    
    public void testCancelAbility() throws Exception {
        System.out.println("Testing cancel ability of async init job");
        initializeSimple();
        initCancelResults();
        CancelInitComp comp = new CancelInitComp();
        Utilities.attachInitJob(comp, comp);
        frame = new Frame();
        frame.setSize(100, 100);
        frame.setVisible(true);
        dlg = new Dialog(frame, true);
        dlg.setSize(50, 50);
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
    
    /** Disposer of windows */
    private final class TimerListener implements ActionListener {
        /** true for cancel test, false otherwise */
        private boolean cancel;
        public TimerListener(boolean cancel) {
            this.cancel = cancel;
        }
        public void actionPerformed(ActionEvent e) {
            dlg.dispose();
            frame.dispose();
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
