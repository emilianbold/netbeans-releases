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
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Performance helper class, allows to run post-init task for given component.
 * Can also handle cancel logic if contained in AsyncGUIJob.
 * Class is designed for one time use, can't be used to perform async init
 * more then once.
 * Restrictions: Note that for correct functionality given component must not
 * be showing at construction time of this class, however shouldn't stay hidden
 * forever as memory leak may occur.
 *
 * @author Dafe Simonek
 */
final class AsyncInitSupport implements AWTEventListener, HierarchyListener, Runnable, ActionListener {
    /** lock for access to wasCancelled flag */
    private static final Object CANCELLED_LOCK = new Object();

    /** task in which post init code from AsyncJob is executed */
    private Task initTask;

    /** true after cancel request came, false otherwise */
    private boolean wasCancelled;

    /** Component requesting asynchronous initialization */
    private Component comp4Init;

    /** Job that performs async init task */
    private AsyncGUIJob initJob;
    Timer timer = null;

    /** Creates a new instance of AsyncInitComponent
     * @param comp4Init Component to be initialized. Mustn't be showing at this
     * time. IllegalStateException is thrown if component is already showing.
     * @param initJob Instance of initialization job.
     */
    public AsyncInitSupport(Component comp4Init, AsyncGUIJob initJob) {
        this.comp4Init = comp4Init;
        this.initJob = initJob;

        if (comp4Init.isShowing()) {
            throw new IllegalStateException("Component already shown, can't be inited: " + comp4Init);
        }

        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.PAINT_EVENT_MASK);
        comp4Init.addHierarchyListener(this);

        if (!RepaintManager.currentManager(comp4Init).isDoubleBufferingEnabled()) {
            //We're running with hardware double buffering - cannot rely on an
            //AWT PaintEvent to start the job running - on mac, it will never come
            timer = new Timer(20, this);
            timer.start();
        }
    }

    public void actionPerformed(ActionEvent ae) {
        if (wasCancelled || (initTask != null)) {
            //If cancelled or already started, our job is done, go away.
            detach();

            return;
        }

        if ((comp4Init != null) && comp4Init.isDisplayable()) {
            //If the component has a parent onscreen, we're ready to run.
            start();
        }
    }

    /** Invokes execution of init code in non-ED thread.
     * @param evt ignored
     */
    public void eventDispatched(AWTEvent event) {
        if (
            event.getSource() instanceof Component &&
                SwingUtilities.isDescendingFrom(comp4Init, (Component) (event.getSource()))
        ) {
            start();
        }
    }

    private void start() {
        detach();

        if (initTask == null) {
            initTask = RequestProcessor.getDefault().post(this);
        }
    }

    private void detach() {
        if (timer != null) {
            timer.stop();
        }

        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
    }

    /** Stops listening to asociated component it isn't showing anymore,
     * calls cancel if desirable.
     * @param evt hierarchy event
     */
    public void hierarchyChanged(HierarchyEvent evt) {
        if (((evt.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) && !comp4Init.isShowing()) {
            comp4Init.removeHierarchyListener(this);
            cancel();
        }
    }

    /** Body of task executed in RequestProcessor. Runs AsyncGUIJob's worker
     * method and after its completion posts AsyncJob's UI update method
     * to AWT thread.
     */
    public void run() {
        if (!SwingUtilities.isEventDispatchThread()) {
            // first pass, executed in some of RP threads
            initJob.construct();
            comp4Init.removeHierarchyListener(this);

            // continue to invoke finished method only if hasn't been cancelled 
            boolean localCancel;

            synchronized (CANCELLED_LOCK) {
                localCancel = wasCancelled;
            }

            if (!localCancel) {
                SwingUtilities.invokeLater(this);
            }
        } else {
            // second pass, executed in event dispatch thread
            initJob.finished();
        }
    }

    /** Delegates valid cancel requests to asociated AsyncGUIJob, in the case
     * job supports cancelling. */
    private void cancel() {
        if ((initTask != null) && !initTask.isFinished() && (initJob instanceof Cancellable)) {
            synchronized (CANCELLED_LOCK) {
                wasCancelled = true;
            }

            ((Cancellable) initJob).cancel();
        }
    }
}
