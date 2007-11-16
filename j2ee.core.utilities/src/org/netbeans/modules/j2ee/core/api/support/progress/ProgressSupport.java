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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.core.utilities.ProgressPanel;
import org.openide.util.Cancellable;
import org.openide.util.Mutex;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * A class providing support for running synchronous (in the event dispathing
 * thread) and asynchronous (outside the EDT) actions. Multiple
 * actions can be posted at the same time, switching between synchronous and
 * asynchronous ones as needed. The actions are run sequentially -- one at most one action
 * may be running at any moment in time. A progress panel is displayed for asynchronous actions.
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

    private static final Logger LOGGER = Logger.getLogger(ProgressSupport.class.getName()); // NOI18N

    private ProgressSupport() {
    }

    /**
     * Invokes the actions without allowing them to be cancelled.
     *
     * @param  actions the actions to invoke; never null.
     */
    public static void invoke(Collection<? extends Action> actions) {
        invoke(actions, false);
    }

    /**
     * Invokes the actions while possibly allowing them to be cancelled and returns
     * the cancellation status.
     *
     * @param  actions the action to invoke; never null.
     * @param  cancellable true whether to allow cancellable actions to be cancelled,
     *         false otherwise.
     * @return true if the actions were not cancelled, false otherwise.
     */
    public static boolean invoke(Collection<? extends Action> actions, boolean cancellable) {
        Parameters.notNull("actions", actions); // NOI18N
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("This method must be called in the event thread."); // NOI18N
        }

        return new ActionInvoker(new ArrayList<Action>(actions), cancellable).invoke();
    }

    /**
     * The class that actually invokes the actions.
     */
    private static final class ActionInvoker implements ActionListener {

        private final RequestProcessor rp = new RequestProcessor("ProgressSupport", 1); // NOI18N
        private final List<Action> actions;
        private final boolean cancellable;

        private volatile Context actionContext;
        private AtomicInteger nextActionIndex = new AtomicInteger();
        private volatile Action currentAction;
        private volatile boolean cancelled;

        public ActionInvoker(List<Action> actions, boolean cancellable) {
            this.actions = actions;
            this.cancellable = cancellable;
        }

        /**
         * Returns true if the invocation was not cancelled, false otherwise.
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

            actionContext = new Context(progressPanel, progressHandle);

            // Contains the exception, if any, thrown by an action invocation
            // in either EDT or the RP thread
            final AtomicReference<Throwable> exceptionRef = new AtomicReference<Throwable>();

            // The RequestProcessor task for asynchronous actions
            RequestProcessor.Task task = rp.create(new Runnable() {
                public void run() {
                    try {
                        invokeNextActionsOfSameKind();
                    } catch (Throwable t) {
                        exceptionRef.set(t);
                    } finally {
                        // We are done running asynchronous actions, so we must close the progress panel
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                progressPanel.close();
                            }
                        });
                    }
                }
            });

            try {
                // True if we are running synchronous actions in this round, false otherwise.
                boolean runInEDT = true;

                // Every round of the loop invokes a bunch of actions. The first
                // round invokes synchronous ones, stopping at the first asynchronous one.
                // The second invokes asynchronous ones, stopping at the first synchronous one.
                // The third invokes synchronous ones, etc.
                // This avoids hiding/showing the progress panel after/before each
                // asynchronous action.
                while (nextActionIndex.get() < actions.size() && !cancelled) {
                    if (runInEDT) {
                        try {
                            invokeNextActionsOfSameKind();
                        } catch (Throwable t) {
                            exceptionRef.set(t);
                        }
                    } else {
                        // The equivalent of invokeNextActionsOfSameKind() above, but in the background
                        // and under the progress panel.

                        // Schedule the RP task for asychronous actions. The task
                        // also sets exceptionRef if an exception occured.
                        task.schedule(0);

                        // Open the progress panel. It will be closed at the end of the task for
                        // asynchronous actions. Therefore the call will block and will return when
                        // the RP task's run() method returns.
                        progressPanel.open(progressComponent);

                        // The RP might be still running (e.g. preempted by the AWT thread
                        // thread just after the SW.invokeLater()).
                        task.waitFinished();
                    }

                    Throwable exception = exceptionRef.get();
                    if (exception != null) {
                        if (exception instanceof RuntimeException) {
                            throw (RuntimeException)exception;
                        } else {
                            RuntimeException re = new RuntimeException(exception.getMessage());
                            re.initCause(exception);
                            throw re;
                        }
                    }

                    runInEDT = !runInEDT;
                }
            } finally {
                progressHandle.finish();
            }

            return !cancelled;
        }

        /**
         * Invokes the next actions of the same kind (all synchronous or all asynchronous),
         * starting with nextActionIndex, while skipping disabled actions. That is,
         * when called in the EDT it will run all enabled synchronous actions,
         * stopping at the first asynchronous one. When called in a RP thread, it
         * will run all enabled asynchronous actions, stopping as the first synchronous one.
         */
        private void invokeNextActionsOfSameKind() {
            boolean isEventThread = SwingUtilities.isEventDispatchThread();

            while (!cancelled) {
                int currentActionIndex = nextActionIndex.get();
                if (currentActionIndex >= actions.size()) {
                    break;
                }

                currentAction = actions.get(currentActionIndex);

                // Skip the action if disabled.
                if (!currentAction.isEnabled()) {
                    nextActionIndex.incrementAndGet();
                    LOGGER.log(Level.FINE, "Skipping " + currentAction);
                    continue;
                }

                // The current action is not of the current kind, finish.
                if (currentAction.getRunInEventThread() != isEventThread) {
                    break;
                }

                LOGGER.log(Level.FINE, "Running " + currentAction);

                // Only enable/disable the cancel button for asynchronous actions.
                if (!isEventThread) {
                    final boolean cancelEnabled = currentAction instanceof Cancellable;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            actionContext.getPanel().setCancelEnabled(cancelEnabled);
                        }
                    });
                }

                currentAction.run(actionContext);

                nextActionIndex.incrementAndGet();
            }

        }

        /**
         * Invoked when the Cancel button is pressed in the progress dialog.
         */
        public void actionPerformed(ActionEvent event) {
            // Just in case the user managed to click Cancel twice.
            if (cancelled) {
                return;
            }

            Action action = currentAction;

            // All actions could have been invoked by now.
            if (action == null) {
                return;
            }

            // There is no guarantee that the current action is asynchronous or that it
            // implements Cancellable (maybe the action before it did and the user clicked Cancel
            // just before it finished). If it doesn't we can't do better than
            // just ignore the Cancel request.
            if (!action.isEnabled() || action.getRunInEventThread() || !(action instanceof Cancellable)) {
                return;
            }

            cancelled = ((Cancellable)action).cancel();
            if (cancelled) {
                actionContext.getPanel().setCancelEnabled(false);
            }
        }
    }

    /**
     * Encapsulates the "context" the action is it run under. Currently contains
     * methods for controlling the progress bar in the progress dialog
     * for asynchronous actions.
     */
    public static final class Context {

        private final ProgressPanel panel;
        private final ProgressHandle handle;

        private Context(ProgressPanel panel, ProgressHandle handle) {
            this.panel = panel;
            this.handle = handle;
        }

        /**
         * Switches the progress bar to a determinate one.
         *
         * @param workunits a definite number of complete units of work out of the total
         */
        public void switchToDeterminate(int workunits) {
            handle.switchToDeterminate(workunits);
        }

        /**
         * 
         * 
         * @param message
         */
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
     * Describes an action. See also {@link SynchronousAction} and
     * {@link AsynchronousAction}.
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

        public final boolean getRunInEventThread() {
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

        public final boolean getRunInEventThread() {
            return false;
        }

        public boolean isEnabled() {
            return true;
        }
    }
}
