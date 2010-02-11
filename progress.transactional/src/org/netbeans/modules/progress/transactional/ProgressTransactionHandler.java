/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.progress.transactional;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.progress.transactional.Transaction;
import org.netbeans.api.progress.transactional.TransactionController;
import org.netbeans.api.progress.transactional.TransactionException;
import org.netbeans.api.progress.transactional.UIMode;
import org.netbeans.modules.progress.transactional.ProgressTransactionHandler.UiImpl;
import org.netbeans.spi.progress.transactional.TransactionHandler;
import org.netbeans.spi.progress.transactional.TransactionRunner;
import org.netbeans.spi.progress.transactional.TransactionUI;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default TransactionHandler implementation;  uses the Progress API under
 * the hood to show visual transaction feedback.
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service = TransactionHandler.class)
public class ProgressTransactionHandler extends TransactionHandler<UiImpl> {

    public ProgressTransactionHandler() {
        super(UiImpl.class);
    }

    public ProgressTransactionHandler(RequestProcessor threadPool) {
        super(UiImpl.class, threadPool);
    }

    @Override
    protected UiImpl newUI(String name, UIMode uiMode, boolean canCancel) {
        return new UiImpl(name, uiMode, canCancel);
    }

    @Override
    protected <ArgType, ResultType> Future<ResultType> start(final TransactionRunner<ArgType, ResultType> xaction, final TransactionController controller, final ArgType argument, UIMode mode) {
        final TransactionUI ui = uiFor(controller);
        if (mode != UIMode.BLOCKING) {
            UiImpl impl = uiImplementation(ui);
            Callable<ResultType> c = impl.canCancel() ? new CancellablePlainProgressRunner<ResultType, ArgType>(impl, controller, xaction, argument)
                    : new PlainProgressRunner<ResultType, ArgType>(impl, controller, xaction, argument);
            RpFutureTask<ResultType> f = new RpFutureTask<ResultType>(c);
            f.task = threadPool.create(f);
            Future<ResultType> result = createCancellableFuture(f, controller, xaction);
            f.task.schedule(0);
            return result;
        } else {
            return startBlocking(xaction, controller, argument, mode, uiImplementation(ui));
        }
    }

    protected <ArgType, ResultType> Future<ResultType> startBlocking(final TransactionRunner<ArgType, ResultType> xaction, final TransactionController controller, final ArgType argument, UIMode mode, final UiImpl ui) {
        ProgressRunnable<ResultType> r = ui.canCancel()
                ? new CancellableBlockingProgressRunnable<ResultType, ArgType>(ui, controller, xaction, argument)
                : new BlockingProgressRunnable<ResultType, ArgType>(ui, controller, xaction, argument);
        final ProgressHandle handle = canCancel(xaction.getTransaction()) && ui.canCancel() ? ProgressHandleFactory.createHandle(ui.getName(), ui)
                : ProgressHandleFactory.createHandle(ui.getName());
        return ProgressUtils.showProgressDialogAndRunLater(r, handle, true);
    }

    @Override
    protected <ArgType, ResultType> Future<Boolean> startRollback(final TransactionRunner<ArgType, ResultType> runner, final TransactionController controller, UIMode mode) {
        final TransactionUI ui = uiFor(controller);
        Callable<Boolean> c = new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                try {
                    ui.onBeginRollbackSeries(runner.getTransaction(), controller);
                    return rollbackTransaction(runner, controller);
                } catch (TransactionException e) {
                    LOGGER.log(Level.FINER, "Transaction " + runner + " failed", e); //NOI18N
                    return false;
                } catch (RuntimeException e) {
                    Exceptions.printStackTrace(e);
                    return false;
                } finally {
                    ui.onEndRollbackSeries(runner.getTransaction());
                }
            }
        };
        RpFutureTask<Boolean> f = new RpFutureTask<Boolean>(c);
        f.task = threadPool.create(f);
        f.task.schedule(0);
        return f;
    }

    @Override
    protected TransactionHandler copyWithThreadPool(RequestProcessor p) {
        return new ProgressTransactionHandler(p);
    }

    static class UiImpl extends TransactionUI implements Cancellable {

        private ProgressHandle handle;
        private Transaction<?, ?> parent;
        private volatile int steps;
        private TransactionController controller;
        volatile boolean rollingBackSeries;

        UiImpl(String name, UIMode mode, boolean showCancel) {
            super(name, mode, showCancel);
        }

        private ProgressHandle createHandle(int count) {
            String name = getName();
            UIMode mode = getMode();
            ProgressHandle result =
                    mode == UIMode.NONE ? null
                    : mode == UIMode.INVISIBLE
                    ? canCancel() ? ProgressHandleFactory.createSystemHandle(name, this)
                    : ProgressHandleFactory.createSystemHandle(name)
                    : canCancel() ? ProgressHandleFactory.createHandle(name, this)
                    : ProgressHandleFactory.createHandle(name);
            if (result != null) {
                result.setInitialDelay(250);
                if (count > 1) {
                    result.start(count);
                } else {
                    result.start();
                }
            }
            return result;
        }

        void onBeginSeries(Transaction<?, ?> series, TransactionController controller, ProgressHandle provided) {
            rollingBackSeries = false;
            parent = series;
            this.controller = controller;
            if (provided != null) {
                handle = provided;
                steps = stepCount(series);
                handle.switchToDeterminate(steps);
            } else {
                if (handle != null) {
                    handle.finish();
                }
                handle = createHandle(steps = stepCount(series));
            }
        }

        @Override
        public void onBeginSeries(Transaction<?, ?> series, TransactionController controller) {
            onBeginSeries(series, controller, null);
        }

        @Override
        public void onBeginRollbackSeries(Transaction<?, ?> series, TransactionController controller) {
            parent = series;
            if (handle != null) {
                handle.finish();
            }
            this.controller = controller;
            this.parent = series;
            handle = createHandle(steps = stepCount(series));
            rollingBackSeries = true;
        }

        @Override
        public void onEndRollbackSeries(Transaction<?, ?> transaction) {
            handle = null;
        }

        @Override
        public void onEndSeries(Transaction<?, ?> series, TransactionController controller) {
            if (handle != null) {
                handle.finish();
            }
            handle = null;
        }

        @Override
        protected void onBeginRollback(Transaction<?, ?> transaction) {
            if (handle != null && rollingBackSeries && steps > 1) {
                int ix = steps - indexOf(parent, transaction);
                handle.progress(NbBundle.getMessage(
                        ProgressTransactionHandler.class,
                        "ROLLBACK_DISPLAY_NAME", //NOI18N
                        nameOf(transaction),
                        ix + 1,
                        stepCount(parent)), ix);
            }
        }

        @Override
        protected void onBeginTransaction(Transaction<?, ?> transaction) {
            if (handle != null && !isCompoundTransaction(transaction)) {
                if (steps > 1) {
                    int ix = indexOf(parent, transaction);
                    if (ix >= 0) {
                        handle.progress(NbBundle.getMessage(
                                ProgressTransactionHandler.class,
                                "STEP_DISPLAY_NAME", //NOI18N
                                nameOf(transaction),
                                ix + 1,
                                stepCount(parent)), ix);
                    }
                }
            }
        }

        @Override
        protected void onEndRollback(Transaction<?, ?> transaction) {
        }

        @Override
        protected void onEndTransaction(Transaction<?, ?> transaction) {
        }

        @Override
        protected void onFailure(Transaction<?, ?> xaction, Throwable t, boolean rollback) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ProgressTransactionHandler.class, rollback
                    ? "ERR_ROLLBACK" : "ERR_TRANSACTION", nameOf(xaction))); //NOI18N
        }

        @Override
        public boolean cancel() {
            boolean result = canCancel();
            cancelTransaction(controller);
            return result;
        }
    }

    private static class PlainProgressRunner<T, R> implements Callable<T> {

        protected final UiImpl ui;
        private final TransactionController controller;
        private final TransactionRunner<R, T> runner;
        private final R argument;

        public PlainProgressRunner(UiImpl ui, TransactionController controller, TransactionRunner<R, T> runner, R argument) {
            this.ui = ui;
            this.controller = controller;
            this.runner = runner;
            this.argument = argument;
        }

        @Override
        public T call() throws Exception {
            try {
                ui.onBeginSeries(runner.getTransaction(), controller);
                T result = runTransaction(runner, controller, argument);
                return result;
            } catch (TransactionException e) {
                if (e.isCancellation()) {
                    LOGGER.log(Level.FINER, "Transaction cancelled: {0}", runner); //NOI18N
                    LOGGER.log(Level.FINEST, nameOf(runner.getTransaction()), e);
                } else {
                    LOGGER.log(Level.WARNING, "Transaction failed: {0}", runner);
                    LOGGER.log(Level.INFO, nameOf(runner.getTransaction()), e); //NOI18N
                }
                return null;
            } catch (RuntimeException e) {
                Exceptions.printStackTrace(e);
                return null;
            } finally {
                ui.onEndSeries(runner.getTransaction(), controller);
            }
        }
    }

    private static final class CancellablePlainProgressRunner<T, R> extends PlainProgressRunner<T, R> implements Cancellable {

        public CancellablePlainProgressRunner(UiImpl ui, TransactionController controller, TransactionRunner<R, T> runner, R argument) {
            super(ui, controller, runner, argument);
        }

        @Override
        public boolean cancel() {
            return ui.cancel();
        }
    }

    private static class BlockingProgressRunnable<T, R> implements ProgressRunnable<T> {

        protected final UiImpl ui;
        private final TransactionController controller;
        private final TransactionRunner<R, T> runner;
        private final R argument;

        BlockingProgressRunnable(UiImpl ui, TransactionController controller, TransactionRunner<R, T> runner, R argument) {
            this.ui = ui;
            this.controller = controller;
            this.runner = runner;
            this.argument = argument;
        }

        @Override
        public T run(ProgressHandle handle) {
            ui.onBeginSeries(runner.getTransaction(), controller, handle);
            try {
                return runTransaction(runner, controller, argument);
            } catch (TransactionException e) {
                if (e.isCancellation()) {
                    LOGGER.log(Level.FINER, "Transaction cancelled: {0}", runner); //NOI18N
                    LOGGER.log(Level.FINEST, nameOf(runner.getTransaction()), e);
                } else {
                    LOGGER.log(Level.WARNING, "Transaction failed: {0}", runner); //NOI18N
                    LOGGER.log(Level.INFO, nameOf(runner.getTransaction()), e);
                }
                return null;
            } catch (RuntimeException e) {
                Exceptions.printStackTrace(e);
                return null;
            } finally {
                ui.onEndSeries(runner.getTransaction(), controller);
            }
        }
    }

    private static final class CancellableBlockingProgressRunnable<T, R> extends BlockingProgressRunnable<T, R> implements Cancellable {
        CancellableBlockingProgressRunnable(UiImpl ui, TransactionController controller, TransactionRunner<R, T> runner, R argument) {
            super(ui, controller, runner, argument);
        }

        @Override
        public boolean cancel() {
            return ui.cancel();
        }
    }

    /**
     * Extension of FutureTask which will call a RequestProcessor.Task's
     * cancel method
     * @param <T> The type
     */
    private static final class RpFutureTask<T> extends FutureTask<T> {
        Task task;
        RpFutureTask(Callable<T> c) {
            super(c);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return super.cancel(mayInterruptIfRunning) & task.cancel();
        }
    }
}
