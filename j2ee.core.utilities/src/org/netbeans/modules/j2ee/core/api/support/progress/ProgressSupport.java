/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.core.api.support.progress;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.core.utilities.ProgressPanel;
import org.openide.util.Cancellable;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;

/**
 * A class providing support for running synchronous (in the event dispathing
 * thread) and asynchronous (outside the EDT) actions. Multiple
 * actions can be run at the same time, switching between synchronous and
 * asynchronous ones. A progress panel is displayed for asynchronous actions.
 *
 * <p>A typical use case is running an asynchronous action with a progress dialog.
 * For that just create an {@link #AsynchronouosAction} and send it to the {@link #invoke} method.</p>
 *
 * <p>A more complex use case is mixing actions: first you need to run an asynchronous
 * action, the a synchronous one (but in certain cases only) and then another
 * asynchronous one, showing and hiding the progress panel as necessary.</p>
 * 
 * @author Andrei Badea
 */
public final class ProgressSupport {

    // PENDING it may be worth to split Action into:
    // - final class ActionDescriptor containing the enabled and runInEventThread properties
    //   (or consider using/extending org.openide.util.Task instead if possible -- maybe not, since it runs Runnable's)
    // - interface Action containing invoke(Context actionContext)

    // PENDING should use own RequestProcessor, not the default one

    private static final Logger LOGGER = Logger.getLogger(ProgressSupport.class.getName()); // NOI18N

    // not private because used in tests
    ActionInvoker actionInvoker;

    public ProgressSupport() {
    }

    public void invoke(Collection<? extends Action> actions) {
        invoke(actions, false);
    }

    /**
     * Returns true if all actions were invoked, false otherwise
     * (e.g. if the invocation was cancelled).
     */
    public boolean invoke(Collection<? extends Action> actions, boolean cancellable) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("This method must be called in the event thread."); // NOI18N
        }
        if (this.actionInvoker != null) {
            throw new IllegalStateException("The invoke() method is running."); // NOI18N
        }

        actionInvoker = new ActionInvoker(new ArrayList<Action>(actions), cancellable);
        boolean success;

        try {
            success = actionInvoker.invoke();
        } finally {
            actionInvoker = null;
        }

        return success;
    }

    private static final class ActionInvoker implements ActionListener {

        private final List<Action> actions;
        private final boolean cancellable;

        private Context actionContext;
        private int currentActionIndex;
        private boolean cancelled;

        public ActionInvoker(List<Action> actions, boolean cancellable) {
            this.actions = actions;
            this.cancellable = cancellable;
        }

        /**
         * Returns true if all actions were invoked, false otherwise
         * (e.g. if the invocation was cancelled).
         */
        public boolean invoke() {
            assert SwingUtilities.isEventDispatchThread();

            final ProgressPanel progressPanel = new ProgressPanel();
            progressPanel.setCancelVisible(cancellable);
            progressPanel.addCancelActionListener(this);

            ProgressHandle progressHandle = ProgressHandleFactory.createHandle(null);
            JComponent progressComponent = ProgressHandleFactory.createProgressComponent(progressHandle);
            progressHandle.start();
            progressHandle.switchToIndeterminate();

            actionContext = new Context(new Progress(progressPanel, progressHandle));

            // exceptions[0] contains the exception, if any, thrown by an action invocation
            // in either EDT or the RP thread
            final Throwable[] exceptions = new Throwable[1];

            try {

                RequestProcessor.Task task = RequestProcessor.getDefault().create(new Runnable() {
                    public void run() {
                        try {
                            invokeActionsUntilThreadSwitch();
                        } catch (Throwable t) {
                            exceptions[0] = t;
                        } finally {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    progressPanel.close();
                                }
                            });
                        }
                    }
                });

                boolean runInEDT = true;

                for (;;) {

                    if (currentActionIndex >= actions.size() || cancelled) {
                        break;
                    }

                    try {
                        if (!runInEDT) {
                            // the action at currentActionIndex is an asynchronous one
                            // must be run outside EDT
                            task.schedule(0);
                            progressPanel.open(progressComponent);
                        }

                        invokeActionsUntilThreadSwitch();

                        if (!runInEDT) {
                            // the RP might be still running (e.g. preempted by the AWT thread
                            // thread just after the SW.invokeLater())
                            // also ensures correct visibility of the fields
                            // modified by the RP thread
                            task.waitFinished();
                        }
                    } catch (Throwable t) {
                        exceptions[0] = t;
                    }

                    if (exceptions[0] != null) {
                        if (exceptions[0] instanceof RuntimeException) {
                            throw (RuntimeException)exceptions[0];
                        } else {
                            RuntimeException exception = new RuntimeException(exceptions[0].getMessage());
                            exception.initCause(exceptions[0]);
                            throw exception;
                        }
                    }

                    runInEDT = !runInEDT;
                }
            } finally {
                progressHandle.finish();
            }

            return !cancelled;
        }

        private void invokeActionsUntilThreadSwitch() {
            boolean isEventThread = SwingUtilities.isEventDispatchThread();

            for (;;) {

                synchronized (this) {
                    // synchronized because cancelled can be set at any time
                    // by actionPerformed() in the EDT
                    if (cancelled) {
                        break;
                    }
                }

                if (currentActionIndex >= actions.size()) {
                    break;
                }

                Action action = actions.get(currentActionIndex);
                if (!action.isEnabled()) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Skipping " + action); // NOI18N
                    }
                    synchronized (this) {
                        // synchronizd for the AWT thread to be able to read it
                        // in actionPerformed()
                        currentActionIndex++;
                    }
                    continue;
                }

                if (action.getRunInEventThread() != isEventThread) {
                    break;
                }

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Running " + action);
                }

                // only enable/disable the cancel button for async actions
                if (!isEventThread) {
                    final boolean cancelEnabled = action instanceof Cancellable;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            actionContext.getProgress().getPanel().setCancelEnabled(cancelEnabled);
                        }
                    });
                }

                action.run(actionContext);

                synchronized (this) {
                    // synchronizd for the AWT thread to be able to read it
                    // in actionPerformed()
                    currentActionIndex++;
                }
            }

        }

        public void actionPerformed(ActionEvent event) {
            synchronized (this) {

                // just in case the user managed to click Cancel twice
                if (cancelled) {
                    return;
                }

                // all actions could have been invoked by now
                if (currentActionIndex >= actions.size()) {
                    return;
                }

                Action currentAction = actions.get(currentActionIndex);

                // there is no guarantee that currentAction is asynchronous or that it
                // implements Cancellable (maybe the action before it did and the user clicked Cancel
                // just before it finished). If it doesn't we can't do better than
                // just ignore the Cancel request.
                if (!currentAction.getRunInEventThread() && currentAction instanceof Cancellable) {
                    // calling cancel() in the EDT although the action was async
                    cancelled = ((Cancellable)currentAction).cancel();
                    if (cancelled) {
                        actionContext.getProgress().getPanel().setCancelEnabled(false);
                    }
                }
            }
        }
    }

    /**
     * This class encapsulates the context in which the actions are run.
     * It can be used to obtain a {@link Progress} instance.
     */
    public static final class Context {

        private final Progress progress;

        public Context(Progress progress) {
            this.progress = progress;
        }

        public Progress getProgress() {
            return progress;
        }
    }

    /**
     * This class is used to give information about the progress of the actions.
     */
    public static final class Progress {

        private final ProgressPanel panel;
        private final ProgressHandle handle;

        private Progress(ProgressPanel panel, ProgressHandle handle) {
            this.panel = panel;
            this.handle = handle;
        }

        public void switchToDeterminate(int workunits) {
            handle.switchToDeterminate(workunits);
        }

        public void progress(final String message) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    panel.setText(message);
                }
            });
            handle.progress(message);
        }

        public void progress(int workunit) {
            handle.progress(workunit);
        }

        /**
         * Used in tests.
         */
        ProgressPanel getPanel() {
            return panel;
        }
    }

    /**
     * Describes an action. See also {@link Synchronous} and
     * {@link Asynchronous}.
     */
    public interface Action {

        /**
         * Returns true if the action should be run in the EDT and
         * false otherwise.
         */
        public boolean getRunInEventThread();

        /**
         * Returns true if the actions is enabled (should be run). This is
         * useful when having e.g. a {@link Asynchronous asynchronous}
         * action between two {@link Synchronous synchronous} actions. If the
         * asynchronous action is false the progress dialog
         * is not displayed at all (if it was displayed it would just blink
         * for a short time, which does not look good).
         */
        public boolean isEnabled();

        /**
         * This method is invoked when the action should be run.
         */
        public void run(Context actionContext);
    }

    public interface CancellableAction extends Action, Cancellable {

    }

    /**
     * Describes a synchronous action, that is, one that should be run
     * in the EDT.
     */
    public static abstract class SynchronousAction implements Action {

        public boolean getRunInEventThread() {
            return true;
        }

        public boolean isEnabled() {
            return true;
        }
    }

    /**
     * Describes an asynchronous action, that is, one that should be run
     * outside the EDT and with a progress dialog.
     */
    public static abstract class AsynchronousAction implements Action {

        public boolean getRunInEventThread() {
            return false;
        }

        public boolean isEnabled() {
            return true;
        }
    }
}
