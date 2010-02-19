/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.progress.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.progress.spi.RunOffEDTProvider;
import org.netbeans.modules.progress.spi.RunOffEDTProvider.Progress;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Default RunOffEDTProvider implementation for ProgressUtils.runOffEventDispatchThread() methods
 * @author Jan Lahoda, Tomas Holy
 */
@ServiceProvider(service=RunOffEDTProvider.class, position = 100)
public class RunOffEDTImpl implements RunOffEDTProvider, Progress {

    private static final RequestProcessor WORKER = new RequestProcessor(ProgressUtils.class.getName());
    private static final Map<String, Long> CUMULATIVE_SPENT_TIME = new HashMap<String, Long>();
    private static final Map<String, Long> MAXIMAL_SPENT_TIME = new HashMap<String, Long>();
    private static final Map<String, Integer> INVOCATION_COUNT = new HashMap<String, Integer>();
    private static final int CANCEL_TIME = 1000;
    private static final int WARNING_TIME = Integer.getInteger("org.netbeans.modules.progress.ui.WARNING_TIME", 10000);
    private static final Logger LOG = Logger.getLogger(RunOffEDTImpl.class.getName());

    private final boolean assertionsOn;

    @Override
    public void runOffEventDispatchThread(final Runnable operation, final String operationDescr,
            final AtomicBoolean cancelOperation, boolean waitForCanceled, int waitCursorTime, int dlgTime) {
        Parameters.notNull("operation", operation); //NOI18N
        Parameters.notNull("cancelOperation", cancelOperation); //NOI18N
        if (!SwingUtilities.isEventDispatchThread()) {
            operation.run();
            return;
        }
        long startTime = System.currentTimeMillis();
        runOffEventDispatchThreadImpl(operation, operationDescr, cancelOperation, waitForCanceled, waitCursorTime, dlgTime);
        long elapsed = System.currentTimeMillis() - startTime;

        if (assertionsOn) {
            String clazz = operation.getClass().getName();
            Long cumulative = CUMULATIVE_SPENT_TIME.get(clazz);
            if (cumulative == null) {
                cumulative = 0L;
            }
            cumulative += elapsed;
            CUMULATIVE_SPENT_TIME.put(clazz, cumulative);
            Long maximal = MAXIMAL_SPENT_TIME.get(clazz);
            if (maximal == null) {
                maximal = 0L;
            }
            if (elapsed > maximal) {
                maximal = elapsed;
                MAXIMAL_SPENT_TIME.put(clazz, maximal);
            }
            Integer count = INVOCATION_COUNT.get(clazz);
            if (count == null) {
                count = 0;
            }
            count++;
            INVOCATION_COUNT.put(clazz, count);

            if (elapsed > WARNING_TIME) {
                LOG.log(Level.WARNING, "Lengthy operation: {0}:{1}:{2}:{3}:{4}", new Object[] {
                    clazz, cumulative, count, maximal, String.format("%3.2f", ((double) cumulative) / count)});
            }
        }
    }

    private void runOffEventDispatchThreadImpl(final Runnable operation, final String operationDescr,
            final AtomicBoolean cancelOperation, boolean waitForCanceled, int waitCursorTime, int dlgTime) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Dialog> d = new AtomicReference<Dialog>();

        WORKER.post(new Runnable() {

            public @Override void run() {
                if (cancelOperation.get()) {
                    return;
                }
                operation.run();
                latch.countDown();

                SwingUtilities.invokeLater(new Runnable() {

                    public @Override void run() {
                        Dialog dd = d.get();
                        if (dd != null) {
                            dd.setVisible(false);
                        }
                    }
                });
            }
        });

        Component glassPane = ((JFrame) WindowManager.getDefault().getMainWindow()).getGlassPane();

        if (waitMomentarily(glassPane, null, waitCursorTime, latch)) {
            return;
        }

        Cursor wait = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

        if (waitMomentarily(glassPane, wait, dlgTime, latch)) {
            return;
        }

        String title = NbBundle.getMessage(RunOffEDTImpl.class, "RunOffAWT.TITLE_Operation"); //NOI18N
        String cancelButton = NbBundle.getMessage(RunOffEDTImpl.class, "RunOffAWT.BTN_Cancel"); //NOI18N

        DialogDescriptor nd = new DialogDescriptor(operationDescr, title, true, new Object[]{cancelButton},
                cancelButton, DialogDescriptor.DEFAULT_ALIGN, null, new ActionListener() {
            public @Override void actionPerformed(ActionEvent e) {
                cancelOperation.set(true);
                d.get().setVisible(false);
            }
        });

        nd.setMessageType(NotifyDescriptor.INFORMATION_MESSAGE);

        d.set(DialogDisplayer.getDefault().createDialog(nd));
        d.get().setVisible(true);

        if (waitForCanceled) {
            try {
                if (!latch.await(CANCEL_TIME, TimeUnit.MILLISECONDS)) {
                    throw new IllegalStateException("Canceled operation did not finish in time."); //NOI18N
                }
            } catch (InterruptedException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }
    }

    private static boolean waitMomentarily(Component glassPane, Cursor wait, int timeout, final CountDownLatch l) {
        Cursor original = glassPane.getCursor();

        try {
            if (wait != null) {
                glassPane.setCursor(wait);
            }

            glassPane.setVisible(true);
            try {
                return l.await(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                LOG.log(Level.FINE, null, ex);
                return true;
            }
        } finally {
            glassPane.setVisible(false);
            glassPane.setCursor(original);
        }
    }

    public RunOffEDTImpl() {
        boolean ea = false;
        assert ea = true;
        assertionsOn = ea;
    }

    @Override
    public <T> Future<T> showProgressDialogAndRunLater (ProgressRunnable<T> operation, ProgressHandle handle, boolean includeDetailLabel) {
       AbstractWindowRunner<T> wr = new ProgressBackgroundRunner<T>(operation, handle, includeDetailLabel, operation instanceof Cancellable);
       Future<T> result = wr.start();
       assert EventQueue.isDispatchThread() == (result != null);
       if (result == null) {
           try {
               result = wr.waitForStart();
           } catch (InterruptedException ex) {
               LOG.log(Level.FINE, "Interrupted/cancelled during start {0}", operation); //NOI18N
               LOG.log(Level.FINER, "Interrupted/cancelled during start", ex); //NOI18N
               return null;
           }
       }
       return result;
    }

    @Override
    public <T> T showProgressDialogAndRun(ProgressRunnable<T> toRun, String displayName, boolean includeDetailLabel) {
        try {
            return showProgressDialogAndRunLater(toRun, toRun instanceof Cancellable ?
                ProgressHandleFactory.createHandle(displayName, (Cancellable) toRun) :
                ProgressHandleFactory.createHandle(displayName), includeDetailLabel).get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            LOG.log(Level.FINER, "Cancelled " + toRun, ex); //NOI18N
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public void showProgressDialogAndRun(Runnable toRun, ProgressHandle handle, boolean includeDetailLabel) {
       boolean showCancelButton = toRun instanceof Cancellable;
       AbstractWindowRunner<Void> wr = new ProgressBackgroundRunner<Void>(toRun, handle, includeDetailLabel, showCancelButton);
       wr.start();
        try {
            try {
                wr.waitForStart().get();
            } catch (CancellationException ex) {
                LOG.log(Level.FINER, "Cancelled " + toRun, ex); //NOI18N
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    static class CancellableFutureTask<T> extends FutureTask<T> implements Cancellable {
        volatile Task task;
        private final Callable<T> c;
        CancellableFutureTask(Callable<T> c) {
            super(c);
            this.c = c;
        }

        @Override
        public boolean cancel() {
            return cancel(true);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean result = c instanceof Cancellable ? ((Cancellable) c).cancel() : false;
            result &= super.cancel(mayInterruptIfRunning) & task.cancel();
            return result;
        }

        @Override
        public String toString() {
            return super.toString() + "[" + c + "]"; //NOI18N
        }
    }

    static final class TranslucentMask extends JComponent { //pkg private for tests
        private static final String PROGRESS_WINDOW_MASK_COLOR = "progress.windowMaskColor"; //NOI18N
        TranslucentMask() {
            setVisible(false); //so we will trigger a property change
        }

        @Override
        public boolean isOpaque() {
            return false;
        }

        @Override
        public void paint (Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            Color translu = UIManager.getColor(PROGRESS_WINDOW_MASK_COLOR);
            if (translu == null) {
                translu = new Color (180, 180, 180, 148);
            }
            g2d.setColor(translu);
            g2d.fillRect (0, 0, getWidth(), getHeight());
        }
    }

    private static final class ProgressBackgroundRunner<T> extends AbstractWindowRunner<T> implements Cancellable {
        private final ProgressRunnable<T> toRun;
        ProgressBackgroundRunner(ProgressRunnable<T> toRun, String displayName, boolean includeDetail, boolean showCancel) {
            super (showCancel ?
                ProgressHandleFactory.createHandle(displayName, (Cancellable) toRun) :
                ProgressHandleFactory.createHandle(displayName), includeDetail, showCancel);
            this.toRun = toRun;
        }

        ProgressBackgroundRunner(ProgressRunnable<T> toRun, ProgressHandle handle, boolean includeDetail, boolean showCancel) {
            super (handle, includeDetail, showCancel);
            this.toRun = toRun;
        }

        ProgressBackgroundRunner(Runnable toRun, ProgressHandle handle, boolean includeDetail, boolean showCancel) {
            this (showCancel ? new CancellableRunnablePR<T>(toRun) : 
                new RunnablePR<T>(toRun), handle, includeDetail, showCancel);
        }

        @Override
        protected T runBackground() {
            handle.start();
            handle.switchToIndeterminate();
            T result;
            try {
                result = toRun.run(handle);
            } finally {
                handle.finish();
            }
            return result;
        }

        @Override
        public boolean cancel() {
            if (toRun instanceof Cancellable) {
                return ((Cancellable) toRun).cancel();
            }
            return false;
        }

        private static class RunnablePR<T> implements ProgressRunnable<T> {
            protected final Runnable toRun;
            RunnablePR(Runnable toRun) {
                this.toRun = toRun;
            }

            @Override
            public T run(ProgressHandle handle) {
                toRun.run();
                return null;
            }
        }

        private static final class CancellableRunnablePR<T> extends RunnablePR<T> implements Cancellable {
            CancellableRunnablePR(Runnable toRun) {
                super (toRun);
            }

            @Override
            public boolean cancel() {
                return ((Cancellable) toRun).cancel();
            }
        }

    }
}
