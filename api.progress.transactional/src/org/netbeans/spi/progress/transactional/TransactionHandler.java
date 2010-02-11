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
package org.netbeans.spi.progress.transactional;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.transactional.FailureHandler;
import org.netbeans.api.progress.transactional.Transaction;
import org.netbeans.api.progress.transactional.TransactionController;
import org.netbeans.api.progress.transactional.TransactionException;
import org.netbeans.modules.progress.transactional.TransactionManager;
import org.netbeans.modules.progress.transactional.UI;
import org.netbeans.api.progress.transactional.UIMode;
import org.netbeans.modules.progress.transactional.TransactionAccessor;
import org.netbeans.modules.progress.transactional.TransactionHandlerAccessor;
import org.netbeans.spi.progress.transactional.TransactionUI.ReplanningTransactionUIImplementation;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Class which can be implemented in the default lookup, and handles creating
 * a UI for transactional background work, launching background
 * threads and updating the UI as progress is notified.
 * <p/>
 * Below is a basic implementation of a TransactionHandler, without any
 * particular UI implementation other than logging:
 * <pre>private static final class Trivial extends TransactionHandler {
 *   private final ExecutorService exe = Executors.newCachedThreadPool();
 *   protected TransactionUI newUI(String name, UIMode uiMode, boolean canCancel) {
 *     return new LoggerUIImplementation(null);
 *   }
 *
 *   protected &lt;ArgType, ResultType&gt; Future&lt;ResultType&gt; start(
 *        final TransactionRunner&lt;ArgType, ResultType&gt; runner,
 *        final TransactionController controller, final ArgType argument) {
 *
 *     Callable&lt;ResultType&gt; c = new Callable&lt;ResultType&gt;() {
 *       public ResultType call() throws Exception {
 *         TransactionUI ui = uiFor(controller);
 *         try {
 *           ui.onBeginSeries(runner.getTransaction(), controller);
 *           return runTransaction(runner, controller, argument);
 *         } catch (TransactionException e) {
 *           if (e.isCancellation()) {
 *             LOGGER.log(Level.FINER, &quot;Cancelled &quot; + del, e);
 *             return null;
 *           }
 *           LOGGER.log(Level.FINER, &quot;Transaction &quot; + del + &quot; failed&quot;, e);
 *           return null;
 *         } catch (RuntimeException ex) {
 *           Exceptions.printStackTrace(ex);
 *           return null;
 *         } finally {
 *           ui.onEndSeries(runner.getTransaction(), controller);
 *         }
 *       }
 *     };
 *     return createCancellableFuture(exe.submit(c), controller, del);
 *   }
 *
 *   protected &lt;ArgType, ResultType&gt; Future&lt;Boolean&gt; startRollback(
 *          final TransactionRunner&lt;ArgType, ResultType&gt; runner,
 *          final TransactionController controller) {
 * 
 *     Callable&lt;Boolean&gt; c = new Callable&lt;Boolean&gt;() {
 *       public Boolean call() throws Exception {
 *         TransactionUI ui = uiFor(controller);
 *         try {
 *           ui.onBeginRollbackSeries(runner.getTransaction(), controller);
 *           return rollbackTransaction(runner, controller);
 *         } catch (TransactionException e) {
 *           LOGGER.log(Level.FINER, &quot;Transaction rollback &quot; + runner + &quot; failed&quot;, e);
 *           return null;
 *         } catch (RuntimeException ex) {
 *           Exceptions.printStackTrace(ex);
 *           return null;
 *         } finally {
 *           ui.onEndRollbackSeries(runner.getTransaction());
 *         }
 *       }
 *     };
 *     return exe.submit(c);
 *   }
 * }</pre>
 * <p/>
 * Note that the TransactionUI instances created by <code>newUI</code> may be
 * wrappered in other instances of TransactionUI which, for example, perform
 * logging.  Do not blindly cast a TransactionUI you are passed to the
 * implementation type you use - rather, use <code>uiImplementation()</code>
 * to find the actual implementation class (if any - do a null check).
 *
 * @author Tim Boudreau
 */
public abstract class TransactionHandler<T extends TransactionUI> {
    protected static final Logger LOGGER = Logger.getLogger(TransactionHandler.class.getName());
    private final Class<T> uiType;
    protected final RequestProcessor threadPool;
    protected TransactionHandler(Class<T> uiType) {
        this (uiType, null);
    }

    protected TransactionHandler(Class<T> uiType, RequestProcessor threadPool) {
        this.uiType = uiType;
        this.threadPool = threadPool == null ? RequestProcessor.getDefault() : threadPool;
    }

    static {
        Class<?> c = Transaction.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
            c = UI.class;
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        TransactionHandlerAccessor.DEFAULT = new TransactionHandlerAccessorImpl();
//        LOGGER.setLevel(Level.ALL);
    }

    static TransactionHandler getDefault() {
        TransactionHandler result = Lookup.getDefault().lookup(TransactionHandler.class);
        return result == null ? new Trivial() : result;
    }

    static UI createUI(String name, UIMode uiMode, boolean canCancel) {
        TransactionUI impl = getDefault().newUI(name, uiMode, canCancel);
        if (!(impl instanceof LoggerUIImplementation) && LOGGER.isLoggable(Level.FINE)) {
            if (impl == null) {
                impl = new LoggerUIImplementation(name, uiMode, canCancel);
            } else {
                impl = new LoggerUIImplementation(impl);
            }
        }
        return new UI (impl);
    }

    protected final T uiImplementation(TransactionUI ui) {
        boolean found;
        while (!uiType.isInstance(ui)) {
            found = false;
            if (ui instanceof ReplanningTransactionUIImplementation) {
                TransactionUI wrapped = ((ReplanningTransactionUIImplementation) ui).wrapped;
                if (wrapped != null) {
                    found = true;
                    ui = wrapped;
                }
            } else if (ui instanceof LoggerUIImplementation) {
                TransactionUI wrapped = ((LoggerUIImplementation) ui).wrapped;
                if (wrapped != null) {
                    found = true;
                    ui = wrapped;
                }
            }
            if (!found) {
                break;
            }
        }
        if (uiType.isInstance(ui)) {
            return uiType.cast(ui);
        }
        return null;
    }

    /**
     * Create a copy of this TransactionHandler which uses a specific thread
     * pool, in order to throttle throughput or adjust thread priority.
     * <p/>
     * Note that if you pass a single-thread thread pool, Transactions
     * created from Transaction.createParallel() may run sequentially rather
     * than in parallel.  A minimum of two threads is recommended.
     *
     * @param p A thread pool
     * @return A new TransactionHandler similar to this one, but which uses
     * the passed thread pool for creating new threads.
     */
    protected abstract TransactionHandler copyWithThreadPool (RequestProcessor p);

    /**
     * Create an object for controlling the user interface.  Not that the UI
     * object should not make any user interface changes until the first time
     * it is called.
     *
     * @param name The localized display name of the entire operation
     * @param uiMode Hint to the UI about how progress should be displayed.
     * Not all implementations will support all possible hints.
     * @param canCancel Whether or not a cancel button should be shown.  Note
     * that the cancel button should only be enabled if all transactions (in the
     * case of a composite transaction - see Transaction.add()) support cancellation.
     * @return a subclass of TransactionUIImplementation
     */
    protected abstract T newUI(String name, UIMode uiMode, boolean canCancel);

    /**
     * Start a background thread and run the transaction.  To implement,
     * start a background thread (using whatever thread pool mechanism, priority,
     * etc., you need) and call
     * <code>super.runTransaction(Transaction,  TransactionController, ArgType)</code>
     * from that thread to run the transaction.
     *
     * @param <ArgType> The type of argument the transaction's run method
     * takes (use Void if no argument)
     * @param <ResultType> The return type of the transaction's run method
     * @param runner The transaction
     * @param controller A TransactionController
     * @param argument An argument, possibly the result of a previous transaction
     * @return A future which can be queried or waited on for the operation to
     * finish
     */
    protected abstract <ArgType, ResultType> Future<ResultType> start(TransactionRunner<ArgType, ResultType> runner, TransactionController controller, ArgType argument, UIMode mode);

    /**
     * Start a background thread and roll back a completed transaction. To implement,
     * start a background thread (using whatever thread pool mechanism, priority,
     * etc., you need) and call
     * <code>super.rollbackTransaction(Transaction,  TransactionController)</code>
     * from that thread to perform the rollback.
     * @param <ArgType>
     * @param <ResultType>
     * @param runner
     * @param controller
     * @return A future that can be waited on or cancelled
     */
    protected abstract <ArgType, ResultType> Future<Boolean> startRollback(TransactionRunner<ArgType, ResultType> runner, TransactionController controller, UIMode mode);

    final <ArgType, ResultType> Future<ResultType> start(TransactionManager<? extends Transaction<ArgType, ResultType>, ArgType, ResultType> mgr, ArgType argument, FailureHandler failHandler, String name, UIMode uiMode, boolean canCancel) {
        TransactionRunner<ArgType, ResultType> runner = new TransactionRunner<ArgType, ResultType>(mgr);
        if (runner.hasRun()) {
            throw new IllegalStateException("Already ran " + mgr);
        }
        UI ui = createUI(name, uiMode, canCancel);
        TransactionController controller = TransactionAccessor.DEFAULT.createController(failHandler, ui);
        return start(runner, controller, argument, uiMode);
    }

    final <ArgType, ResultType> Future<Boolean> rollback(TransactionManager<? extends Transaction<ArgType, ResultType>, ArgType, ResultType> mgr, FailureHandler failHandler, String name, UIMode uiMode) {
        TransactionRunner<ArgType, ResultType> runner = new TransactionRunner<ArgType, ResultType>(mgr);
        if (!TransactionAccessor.DEFAULT.transactionHasRun(mgr)) {
            throw new IllegalStateException("Already ran " + mgr);
        }
        UI ui = createUI(name, uiMode, false);
        TransactionController controller = TransactionAccessor.DEFAULT.createController(failHandler, ui);
        return startRollback(runner, controller, uiMode);
    }

    /**
     * Actually run the transaction, synchronously, using the passed controller
     * and argument.  Call this method from start() once running
     * on a background thread, to actually run the transaction.
     *
     * @param <ArgType> The type of argument this transaction takes
     * @param <ResultType> The type of argument this transaction returns
     * @param runner A transaction
     * @param controller A controller for publishing transaction status
     * @param argument An argument to the transaction
     * @return The result of the transaction
     */
    protected static final <ArgType, ResultType> ResultType runTransaction(TransactionRunner<ArgType, ResultType> runner, TransactionController controller, ArgType argument) throws TransactionException {
        TransactionManager<? extends Transaction<ArgType, ResultType>, ArgType, ResultType> mgr = runner.manager;
        return TransactionAccessor.DEFAULT.run(mgr, controller, argument);
    }

    /**
     * Actually roll back the transaction, synchronously, using the passed controller and argument.
     * Call this method from your implementation of rollback(), once on a background
     * thread.
     */
    protected static final boolean rollbackTransaction(TransactionRunner<?, ?> del, TransactionController controller) throws TransactionException {
        TransactionManager<?,?,?> runner = del.manager;
        return TransactionAccessor.DEFAULT.rollback(runner, controller);
    }

    /**
     * Get the number of steps in this transaction
     * @param t The transaction, which may be a compound transaction
     * @return The number of steps or work units
     */
    protected static final int stepCount(Transaction<?, ?> t) {
        int result = TransactionAccessor.DEFAULT.transactionSize(t);
        System.err.println("Step count of " + t + " is " + result);
        return result;
    }

    /**
     * Get the index of a child transaction in its parent
     * @param parent The parent transaction (the outermost)
     * @param child An inner transaction
     * @return The index, or -1 if not present
     */
    protected static final int indexOf(Transaction<?, ?> parent, Transaction<?, ?> child) {
        return TransactionAccessor.DEFAULT.indexOf(parent, child);
    }

    /**
     * Cancel a transaction.
     * @param controller The transaction
     */
    protected static final void cancelTransaction(TransactionController controller) {
        Parameters.notNull("controller", controller);
        TransactionAccessor.DEFAULT.cancel(controller);
    }

    /**
     * Get all sub-transactions of this transaction.  It is guaranteed that
     * no compound transactions will be returned - this method unwraps all
     * compound transactions in the chain and returns only those with concrete
     * implementation.
     *
     * @param transaction A transaction, presumably a compound one
     * @return A list of transactions - the names of all of them should make up the
     * sequence of steps involved in this transaction
     */
    protected static final List<Transaction<?, ?>> getContents(Transaction<?, ?> transaction) {
        return TransactionAccessor.DEFAULT.getContents(transaction);
    }

    /**
     * Determine if this is a compound transaction (one returned by <code>Transaction.add()</code>).
     * UIs will generally not want to treat entering a compound transaction as a step in
     * the progress of completing the parent transaction, as these are not counted
     * in the number of steps.
     * @param transaction
     * @return true if this transaction wraps more than one internal transactions
     */
    protected static final boolean isCompoundTransaction(Transaction<?, ?> transaction) {
        return TransactionAccessor.DEFAULT.isCompoundTransaction(transaction);
    }

    /**
     * Determine whether a transaction can be cancelled, as expressed in its
     * constructor arguments or those of its constituents.  For the return
     * value to be true, all transactions in a compound transaction must
     * also return true.
     * @param transaction A transaction
     * @return Whether or not the transaction can be cancelled.
     */
    protected static final boolean canCancel(Transaction<?, ?> transaction) {
        return TransactionAccessor.DEFAULT.canCancel(transaction);
    }

    /**
     * Get the UI for a given transaction controller
     * @param controller The controller
     * @return A UI implementation
     */
    protected static TransactionUI uiFor(TransactionController controller) {
        return TransactionAccessor.DEFAULT.uiFor(controller);
    }

    /**
     * Get the name of a transaction, as passed to its constructor (compound
     * transactions will return the name of the first contained transaction).
     * @param transaction A transaction
     * @return A localized display name
     */
    protected static String nameOf(Transaction<?, ?> transaction) {
        return TransactionAccessor.DEFAULT.transactionName(transaction);
    }

    protected static List<? extends Transaction<?,?>> contents(Transaction<?,?> t) {
        return TransactionAccessor.DEFAULT.contents(t);
    }

    private static final class Trivial extends TransactionHandler<LoggerUIImplementation> {
        Trivial() {
            super (LoggerUIImplementation.class);
        }

        private final ExecutorService exe = Executors.newCachedThreadPool();

        @Override
        protected LoggerUIImplementation newUI(String name, org.netbeans.api.progress.transactional.UIMode uiMode, boolean canCancel) {
            return new LoggerUIImplementation(name, uiMode, canCancel);
        }

        @Override
        protected <ArgType, ResultType> Future<ResultType> start(final TransactionRunner<ArgType, ResultType> del, final TransactionController controller, final ArgType argument, UIMode mode) {
            Callable<ResultType> c = new Callable<ResultType>() {

                @Override
                public ResultType call() throws Exception {
                    TransactionUI ui = uiFor(controller);
                    try {
                        ui.onBeginSeries(del.getTransaction(), controller);
                        return runTransaction(del, controller, argument);
                    } catch (TransactionException e) {
                        if (e.isCancellation()) {
                            LOGGER.log(Level.FINER, "Cancelled " + del, e);
                            return null;
                        }
                        LOGGER.log(Level.FINER, "Transaction " + del + " failed", e);
                        return null;
                    } catch (RuntimeException ex) {
                        Exceptions.printStackTrace(ex);
                        return null;
                    } finally {
                        ui.onEndSeries(del.getTransaction(), controller);
                    }
                }
            };
            return createCancellableFuture(exe.submit(c), controller, del);
        }

        @Override
        protected <ArgType, ResultType> Future<Boolean> startRollback(final TransactionRunner<ArgType, ResultType> runner, final TransactionController controller, UIMode mode) {
            Callable<Boolean> c = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    TransactionUI ui = uiFor(controller);
                    try {
                        ui.onBeginRollbackSeries(runner.getTransaction(), controller);
                        return rollbackTransaction(runner, controller);
                    } catch (TransactionException e) {
                        LOGGER.log(Level.FINER, "Transaction rollback " + runner + " failed", e);
                        return null;
                    } catch (RuntimeException ex) {
                        Exceptions.printStackTrace(ex);
                        return null;
                    } finally {
                        ui.onEndRollbackSeries(runner.getTransaction());
                    }
                }
            };
            return exe.submit(c);
        }

        @Override
        protected TransactionHandler copyWithThreadPool(RequestProcessor p) {
            return this;
        }
    }

    /**
     * Create a wrapper for a Future object provided by a standard JDK execution
     * service, which knows how to cancel a transaction in progress.
     *
     * @param <T> The type of the return value of the transaction.
     * @param f The real Future object that will provide the result
     * @param controller The TransactionController running this transaction
     * @param t The transaction itself
     * @return A Future which delegates to the passed one, but also properly
     * flags the transaction to cancel itself the next time it callse
     * TransactionController.checkCancelled().
     */
    protected static final <T> Future<T> createCancellableFuture(Future<T> f, TransactionController controller, TransactionRunner<?, T> t) {
        return new CancelWrapperFuture<T>(f, controller, TransactionAccessor.DEFAULT.canCancel(t.getTransaction()));
    }

    private static class CancelWrapperFuture<T> implements Future<T> {
        private final Future<T> delegate;
        private final TransactionController controller;
        private final boolean canCancel;

        private CancelWrapperFuture(Future<T> delegate, TransactionController controller, boolean canCancel) {
            Parameters.notNull("delegate", delegate);
            Parameters.notNull("controller", controller);
            this.controller = controller;
            this.canCancel = canCancel;
            this.delegate = delegate;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean tryCancel = canCancel;
            if (tryCancel) {
                boolean result = delegate.cancel(false);
                if (result) {
                    TransactionHandler.cancelTransaction(controller);
                }
                return result;
            }
            return false;
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        @Override
        public boolean isDone() {
            return delegate.isDone();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            try {
                return delegate.get();
            } catch (CancellationException e) {
                return null;
            }
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return delegate.get(timeout, unit);
        }

        @Override
        public String toString() {
            return super.toString() + "[" + delegate + "]"; //NOI18N
        }
    }

    private static final class TransactionHandlerAccessorImpl extends TransactionHandlerAccessor {

        @Override
        public <ArgType, ReturnType> Future<ReturnType> launch(TransactionManager<? extends Transaction<ArgType, ReturnType>, ArgType, ReturnType> transaction, ArgType argument, FailureHandler failHandler, String name, UIMode uiMode, boolean canCancel, RequestProcessor threadPool) {
            TransactionHandler th = TransactionHandler.getDefault();
            if (threadPool != null) {
                th = th.copyWithThreadPool(threadPool);
            }
            return th.start(transaction, argument,
                    failHandler == null ? FailureHandler.getDefault() : failHandler,
                    name, uiMode, canCancel);
        }

        @Override
        public <ArgType, ReturnType> Future<Boolean> rollback(TransactionManager<? extends Transaction<ArgType, ReturnType>, ArgType, ReturnType> transaction, FailureHandler failHandler, String name, UIMode uiMode, RequestProcessor threadPool) {
            TransactionHandler th = TransactionHandler.getDefault();
            if (threadPool != null) {
                th = th.copyWithThreadPool(threadPool);
            }
            return th.rollback(transaction, failHandler, name, uiMode);
        }

        @Override
        public <ArgType, ResultType> Future<ResultType> start(TransactionRunner<ArgType, ResultType> runner, TransactionController controller, ArgType argument, UIMode mode, RequestProcessor threadPool) {
            TransactionHandler th = TransactionHandler.getDefault();
            if (threadPool != null) {
                th = th.copyWithThreadPool(threadPool);
            }
            return th.start(runner, controller, argument, uiFor(controller).getMode());
        }

        @Override
        public <ArgType, ResultType> Future<Boolean> startRollback(TransactionRunner<ArgType, ResultType> runner, TransactionController controller, UIMode mode) {
            return TransactionHandler.getDefault().startRollback(runner, controller, uiFor(controller).getMode());
        }

        @Override
        public <ArgType, ResultType> TransactionRunner<ArgType, ResultType> createRunner (TransactionManager<?, ArgType, ResultType> mgr) {
            return new TransactionRunner<ArgType, ResultType>(mgr);
        }

        @Override
        public UI createUI(String name, UIMode uiMode, boolean canCancel) {
            return TransactionHandler.createUI(name, uiMode, canCancel);
        }
    }

    static final class LoggerUIImplementation extends TransactionUI {

        final TransactionUI wrapped;

        LoggerUIImplementation(String name, UIMode mode, boolean canCancel) {
            super (name, mode, canCancel);
            this.wrapped = null;
        }

        LoggerUIImplementation(TransactionUI wrapped) {
            super (wrapped.getName(), wrapped.getMode(), wrapped.canCancel());
            LOGGER.log(Level.FINE, "Created a UI impl for {0}", new Object[]{wrapped});
            this.wrapped = wrapped;
        }

        @Override
        public void onBeginTransaction(Transaction<?, ?> transaction) {
            LOGGER.log(Level.FINE, "Begin transaction {0} on {1}", new Object[]{transaction, Thread.currentThread()});
            if (wrapped != null) {
                wrapped.onBeginTransaction(transaction);
            }
        }

        @Override
        public void onEndTransaction(Transaction<?, ?> transaction) {
            LOGGER.log(Level.FINE, "End transaction {0} on {1}", new Object[]{transaction, Thread.currentThread()});
            if (wrapped != null) {
                wrapped.onEndTransaction(transaction);
            }
        }

        @Override
        public void onBeginRollback(Transaction<?, ?> transaction) {
            LOGGER.log(Level.FINE, "Begin rollback of {0} on {1}", new Object[]{transaction, Thread.currentThread()});
            if (wrapped != null) {
                wrapped.onBeginRollback(transaction);
            }
        }

        @Override
        public void onEndRollback(Transaction<?, ?> transaction) {
            LOGGER.log(Level.FINE, "End rollback of {0} on {1}", new Object[]{transaction, Thread.currentThread()});
            if (wrapped != null) {
                wrapped.onEndRollback(transaction);
            }
        }

        @Override
        public void onFailure(Transaction<?, ?> transaction, Throwable t, boolean isRollback) {
            LOGGER.log(Level.FINE, "Failure of {0} on {1}", new Object[]{transaction, Thread.currentThread()});
            if (wrapped != null) {
                wrapped.onFailure(transaction, t, isRollback);
            }
        }

        @Override
        protected void onAfterPublish(Transaction<?, ?> transaction, Runnable toPublish, boolean synchronous) {
            LOGGER.log(Level.FINE, "After publish of {0} by {1} on {2}", new Object[]{toPublish, transaction, Thread.currentThread()});
            if (wrapped != null) {
                wrapped.onAfterPublish(transaction, toPublish, synchronous);
            }
        }

        @Override
        protected void onBeforePublish(Transaction<?, ?> transaction, Runnable toPublish, boolean synchronous) {
            LOGGER.log(Level.FINE, "Before publish of {0} by {1} on {2}", new Object[]{toPublish, transaction, Thread.currentThread()});
            if (wrapped != null) {
                wrapped.onBeforePublish(transaction, toPublish, synchronous);
            }
        }

        @Override
        public void onBeginRollbackSeries(Transaction<?, ?> transaction, TransactionController controller) {
            LOGGER.log(Level.FINE, "Before rollback series of {0} on {1}", new Object[]{transaction, Thread.currentThread()});
            if (wrapped != null) {
                wrapped.onBeginRollbackSeries(transaction, controller);
            }
        }

        @Override
        public void onBeginSeries(Transaction<?, ?> transaction, TransactionController controller) {
            LOGGER.log(Level.FINE, "Begin series of {0} on {1}", new Object[]{transaction, Thread.currentThread()});
            if (wrapped != null) {
                wrapped.onBeginSeries(transaction, controller);
            }
        }

        @Override
        public void onEndRollbackSeries(Transaction<?, ?> transaction) {
            LOGGER.log(Level.FINE, "End rollback series of {0} on {1}", new Object[]{transaction, Thread.currentThread()});
            if (wrapped != null) {
                wrapped.onEndRollbackSeries(transaction);
            }
        }

        @Override
        public void onEndSeries(Transaction<?, ?> transaction, TransactionController controller) {
            LOGGER.log(Level.FINE, "End series of {0} on {1}", new Object[]{transaction, Thread.currentThread()});
            if (wrapped != null) {
                wrapped.onEndSeries(transaction, controller);
            }
        }

        @Override
        protected void onPublish(Transaction<?, ?> transaction, Runnable toPublish, boolean synchronous) {
            LOGGER.log(Level.FINE, "Publish of {0} by {1} on {2}", new Object[]{toPublish, transaction, Thread.currentThread()});
            if (wrapped != null) {
                wrapped.onBeforePublish(transaction, toPublish, synchronous);
            }
        }
    }
}
