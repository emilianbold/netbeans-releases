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

import java.awt.EventQueue;
import org.netbeans.api.progress.transactional.Transaction;
import org.netbeans.api.progress.transactional.TransactionController;
import org.netbeans.api.progress.transactional.UIMode;
import org.netbeans.modules.progress.transactional.UI;
import org.netbeans.modules.progress.transactional.UIImplAccessor;
import org.openide.util.Exceptions;

/**
 * A callback which is notified as transactions progress, which can update
 * an associated user interface (progress bar or similar).
 * <p/>
 * Typically for any invocation of a transaction via TransactionLauncher, one
 * UI instance is created which will manage UI information specific to that
 * launcher and its associated transactions and state.
 * <p/>
 * Note that this interface may be called for interim transaction objects
 * such as compound transactions which in turn run individual transactions.
 * These can be safely ignored.
 * <p/>
 * Methods of this class will be called by the infrastructure as needed;
 * generally implementations should only directly the following methods:
 * <ul>
 * <li><code>onBeginSeries()</code> before starting to run a new transaction</li>
 * <li><code>onEndSeries()</code> when a series is complete (call only if no exception thrown during the series)</li>
 * <li><code>onBeginRollbackSeries()</code> before starting to roll back a transaction</li>
 * <li><code>onRollbackEndSeries()</code> when a series is completely rolled back (call only if no exception thrown during the series)</li>
 * </ul>
 * <p/>
 * Note that the methods of this class may be accessed by multiple threads,
 * at a minimum, the thread that launched the transaction, the thread running
 * the transaction and the AWT event thread.  Any code which updates UI state
 * in implementations of this class should ensure that it does so on the
 * AWT event thread.  You can use <code>createReplanningImplementation()</code>
 * to wrap a TransactionUI in an implementation that will call all its methods
 * on the event thread.
 *
 * @author Tim Boudreau
 */
public abstract class TransactionUI {
    static {
        Class<?> c = UI.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        UIImplAccessor.DEFAULT = new AccessorImpl();
    }
    private final String name;
    private final UIMode mode;
    private final boolean canCancel;

    public TransactionUI(String name, UIMode mode, boolean canCancel) {
        this.name = name;
        this.mode = mode;
        this.canCancel = canCancel;
    }

    public final boolean canCancel() {
        return canCancel;
    }

    public final UIMode getMode() {
        return mode;
    }

    public final String getName() {
        return name;
    }

    /**
     * Called when the first transaction in what may be multiple composed
     * transactions is about to run
     * @param series The wrapper transaction for all pending transactions
     */
    public void onBeginSeries(Transaction<?,?> series, TransactionController controller) {
        //do nothing
    }

    /**
     * Called the last transaction in what may be multiple composed transactions
     * has completed
     * @param series The wrapper transaction for all completed transactions
     */
    public void onEndSeries(Transaction<?,?> series, TransactionController controller) {
        //do nothing
    }

    /**
     * Called when a transaction is about to be run
     * @param transaction The transaction
     */
    protected void onBeginTransaction (Transaction<?,?> transaction) {
        //do nothing
    }

    /**
     * Called after transaction is completed
     * @param transaction The transaction
     */
    protected void onEndTransaction(Transaction<?,?> transaction) {
        //do nothing
    }

    /**
     * Called before transaction is rolled back
     * @param transaction The transaction
     */
    protected void onBeginRollback(Transaction<?,?> transaction) {
        //do nothing
    }

    /**
     * Called before transaction rolledback is finished
     * @param transaction The transaction
     */
    protected void onEndRollback(Transaction<?,?> transaction) {
        //do nothing
    }

    /**
     * Called when a transaction fails
     * @param transaction The transaction
     * @param isRollback Whether or not the failure occured during a rollback
     * either due to failure of a subsequent transaction, or due to user
     * cancellation
     */
    protected void onFailure (Transaction<?,?> transaction, Throwable t, boolean isRollback) {
        //do nothing
    }

    /**
     * Called from the background thread, when a transaction is about to publish
     * some interim result.
     *
     * The default implementation does nothing.  It is unusual to override
     * this method, but may be necessary in some cases (such as using Jini
     * services) to set the classloader and security contexts.
     *
     * @param transaction The transaction
     * @param toPublish The runnable passed to TransactionController.publish()
     */
    protected void onBeforePublish(Transaction<?,?> transaction, Runnable toPublish, boolean synchronous) {
        //do nothing
    }

    /**
     * Called from the event thread, when a transaction publish Runnable is
     * about to be run.
     *
     * The default implementation does nothing.  It is unusual to override
     * this method, but may be necessary in some cases (such as using Jini
     * services) to set the classloader and security contexts.
     *
     * @param transaction The transaction
     * @param toPublish The runnable which will do the publishing
     */
    protected void onPublish (Transaction<?,?> transaction, Runnable toPublish, boolean synchronous) {
        //do nothing
    }

    /**
     * Called from the event thread, when a transaction publish Runnable is
     * about to be run.
     *
     * The default implementation does nothing.  It is unusual to override
     * this method, but may be necessary in some cases (such as using Jini
     * services) to set the classloader and security contexts.
     *
     * @param transaction The transaction
     * @param toPublish The runnable which will do the publishing
     */
    protected void onAfterPublish(Transaction<?,?> transaction, Runnable toPublish, boolean synchronous) {
        //do nothing
    }

    /**
     * Called when rollback explicitly started in order to undo a transaction
     * that has already been completed.
     *
     * @param transaction The transaction, which may be a compound transaction
     */
    public void onBeginRollbackSeries (Transaction<?,?> transaction, TransactionController controller) {
        //do nothing
    }

    /**
     * Called when a rollback that was explicitly started in order to undo a
     * completed transaction has finished.
     *
     * @param transaction The transaction, which may be a compound transaction
     */
    public void onEndRollbackSeries (Transaction<?,?> transaction) {
        //do nothing
    }

    /**
     * Create a wrapper implementation of TransactionUIImplementation, which
     * ensures that all methods (except *Publish(), which have explicit threading
     * semantics) are called on the AWT event dispatch thread.
     * <p/>
     * Note that the begin/end series methods of the returned controller will
     * always be passed null for the controller.  Access to the controller is
     * restricted to the thread running the transactions.
     *
     * @param impl Another implementation of TransactionUIImplementation
     * @return A wrapper which replans all method calls except those noted
     * to the event dispatch thread
     */
    public static TransactionUI createReplanningImplementation(TransactionUI impl) {
        return impl instanceof ReplanningTransactionUIImplementation ? impl : new ReplanningTransactionUIImplementation(impl);
    }

    static final class ReplanningTransactionUIImplementation extends TransactionUI {
        final TransactionUI wrapped;
        public ReplanningTransactionUIImplementation(TransactionUI wrapped) {
            super (wrapped.getName(), wrapped.getMode(), wrapped.canCancel());
            this.wrapped = wrapped;
        }

        private enum CallbackType {
            BEGIN_SERIES,
            END_SERIES,
            BEGIN_TRANSACTION,
            END_TRANSACTION,
            BEGIN_ROLLBACK,
            END_ROLLBACK,
            FAILURE,
            BEGIN_ROLLBACK_SERIES,
            END_ROLLBACK_SERIES,
        }

        @Override
        protected void onBeginRollback(Transaction<?,?> transaction) {
            new R(transaction, CallbackType.BEGIN_ROLLBACK).invoke();
        }

        @Override
        public void onBeginSeries(Transaction<?,?> series, TransactionController controller) {
            new R(series, CallbackType.BEGIN_SERIES).invoke();
        }

        @Override
        protected void onBeginTransaction(Transaction<?,?> transaction) {
            new R(transaction, CallbackType.BEGIN_TRANSACTION).invoke();
        }

        @Override
        public void onEndSeries(Transaction<?,?> series, TransactionController controller) {
            new R(series, CallbackType.END_SERIES).invoke();
        }

        @Override
        protected void onEndTransaction(Transaction<?,?> transaction) {
            new R(transaction, CallbackType.END_TRANSACTION).invoke();
        }

        @Override
        protected void onFailure(Transaction<?,?> transaction, Throwable t, boolean isRollback) {
            new R(transaction, CallbackType.FAILURE, t, isRollback).invoke();
        }

        @Override
        protected void onEndRollback(Transaction<?,?> transaction) {
            new R(transaction, CallbackType.END_ROLLBACK).invoke();
        }

        @Override
        protected void onPublish(Transaction<?,?> transaction, Runnable toPublish, boolean synchronous) {
            wrapped.onPublish(transaction, toPublish, synchronous);
        }

        @Override
        protected void onAfterPublish(Transaction<?,?> transaction, Runnable toPublish, boolean synchronous) {
            wrapped.onAfterPublish(transaction, toPublish, synchronous);
        }

        @Override
        protected void onBeforePublish(Transaction<?,?> transaction, Runnable toPublish, boolean synchronous) {
            wrapped.onBeforePublish(transaction, toPublish, synchronous);
        }

        @Override
        public void onBeginRollbackSeries(Transaction<?,?> transaction, TransactionController controller) {
            new R(transaction, CallbackType.BEGIN_ROLLBACK_SERIES).invoke();
        }

        @Override
        public void onEndRollbackSeries(Transaction<?,?> transaction) {
            new R(transaction, CallbackType.END_ROLLBACK_SERIES).invoke();
        }

        private class R implements Runnable {
            private final CallbackType cbType;
            private final Transaction<?,?> transaction;
            private final boolean rollback;
            private final Throwable t;
            R(Transaction<?,?> transaction, CallbackType type) {
                this (transaction, type, null, false);
                assert type != CallbackType.FAILURE;
            }

            R(Transaction<?,?> transaction, CallbackType type, Throwable t, boolean isRollback) {
                this.transaction = transaction;
                this.cbType = type;
                this.rollback = isRollback;
                this.t = t;
            }


            void invoke() {
                if (EventQueue.isDispatchThread()) {
                    run();
                } else {
                    EventQueue.invokeLater(this);
                }
            }

            @Override
            public void run() {
                switch (cbType) {
                    case BEGIN_ROLLBACK :
                        wrapped.onBeginRollback(transaction);
                        break;
                    case BEGIN_SERIES :
                        wrapped.onBeginSeries(transaction, null);
                        break;
                    case BEGIN_TRANSACTION :
                        wrapped.onBeginTransaction(transaction);
                        break;
                    case END_ROLLBACK :
                        wrapped.onEndRollback(transaction);
                        break;
                    case END_SERIES :
                        wrapped.onEndSeries(transaction, null);
                        break;
                    case END_TRANSACTION :
                        wrapped.onEndTransaction(transaction);
                        break;
                    case FAILURE :
                        wrapped.onFailure(transaction, t, rollback);
                        break;
                    case END_ROLLBACK_SERIES :
                        wrapped.onBeginRollbackSeries(transaction, null);
                        break;
                    case BEGIN_ROLLBACK_SERIES :
                        wrapped.onBeginRollbackSeries(transaction, null);
                        break;
                    default :
                        throw new AssertionError();
                }
            }
        }
    }

    private static final class AccessorImpl extends UIImplAccessor {

        @Override
        public  void onBeginTransaction(TransactionUI impl, Transaction<?,?> transaction) {
            impl.onBeginTransaction(transaction);
        }

        @Override
        public  void onEndTransaction(TransactionUI impl, Transaction<?,?> transaction) {
            impl.onEndTransaction(transaction);
        }

        @Override
        public  void onBeginRollback(TransactionUI impl, Transaction<?,?> transaction) {
            impl.onBeginRollback(transaction);
        }

        @Override
        public  void onEndRollback(TransactionUI impl, Transaction<?,?> transaction) {
            impl.onEndRollback(transaction);
        }

        @Override
        public void onFailure(TransactionUI impl, Transaction<?,?> transaction, Throwable t, boolean isRollback) {
            impl.onFailure(transaction, t, isRollback);
        }

        @Override
        public void onBeginSeries(TransactionUI impl, Transaction<?,?> transaction) {
            impl.onBeginSeries(transaction, null);
        }

        @Override
        public void onEndSeries(TransactionUI impl, Transaction<?,?> transaction) {
            impl.onEndSeries(transaction, null);
        }

        @Override
        public void onBeforePublish(TransactionUI impl, Transaction<?,?> transaction, Runnable run, boolean synchronous) {
            impl.onBeforePublish(transaction, run, synchronous);
        }

        @Override
        public void onPublish(TransactionUI impl, Transaction<?,?> transaction, Runnable run, boolean synchronous) {
            impl.onPublish(transaction, run, synchronous);
        }

        @Override
        public void onAfterPublish(TransactionUI impl, Transaction<?,?> transaction, Runnable run, boolean synchronous) {
            impl.onAfterPublish(transaction, run, synchronous);
        }
    }
}
