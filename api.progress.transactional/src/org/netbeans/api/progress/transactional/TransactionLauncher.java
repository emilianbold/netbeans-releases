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

package org.netbeans.api.progress.transactional;

import org.netbeans.modules.progress.transactional.TransactionManager;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.progress.transactional.TransactionHandlerAccessor;
import org.openide.util.RequestProcessor;

/**
 * Class which can launch a transaction on a background thread.  If an instance
 * of this class is held in memory, and the transaction completes successfully,
 * the rollback() method can be used to undo the transaction (assuming individual
 * transactions implement rollback() correctly).
 *
 * @author Tim Boudreau
 */
public final class TransactionLauncher<ArgType, ResultType> {
    private final TransactionManager<? extends Transaction<ArgType, ResultType>, ArgType, ResultType> delegate;
    private final AtomicBoolean launched = new AtomicBoolean(false);
    private final AtomicBoolean failed = new AtomicBoolean(false);
    private final String jobName;
    private final RequestProcessor threadPool;
    TransactionLauncher(String jobName, Transaction<ArgType, ResultType> t) {
        this (jobName, t, null);
    }

    TransactionLauncher(String jobName, Transaction<ArgType, ResultType> t, RequestProcessor threadPool) {
        delegate = createDelegate(t);
        this.jobName = jobName;
        this.threadPool = threadPool;
    }

    private LaunchStateDelegate<ArgType, ResultType> createDelegate(Transaction<ArgType, ResultType> t) {
        TransactionManager<? extends Transaction<ArgType, ResultType>, ArgType, ResultType> mgr = t.createRunner();
        return new LaunchStateDelegate<ArgType, ResultType>(mgr);
    }

    /**
     * Launch a transaction to be run on a background thread.
     * @param argument The argument to this transaction (use Void if not needed)
     * @param mode A hint to the UI provider for whether a progress bar should
     * be visible or invisible, and whether the main UI should be blocked until
     * completion
     * @param canCancel Hint to the UI about whether a cancel button should
     * be displayed (does not determine whether it is enabled or not - that
     * is up to the constructor argument of the transaction, or all contained
     * transactions in a compound transaction)
     * @return A future which can be waited on and queried for the end result
     */
    public final Future<? extends ResultType> launch(ArgType argument, UIMode mode, boolean canCancel) {
        return launch(argument, FailureHandler.getDefault(), mode, canCancel);
    }

    /**
     * Launch a transaction to be run on a background thread.
     * @param argument The argument to this transaction (use Void if not needed)
     * @param mode A hint to the UI provider for whether a progress bar should
     * be visible or invisible, and whether the main UI should be blocked until
     * completion
     * @return A future which can be waited on and queried for the end result
     */
    public final Future<? extends ResultType> launch(ArgType argument, UIMode mode) {
        return launch(argument, FailureHandler.getDefault(), mode, delegate.transaction().canCancel());
    }

    /**
     * Launch a transaction to be run on a background thread.
     *
     * If any transaction fails, all previous transactions (in the case of a
     * compound transaction created with <code>Transaction.add()</code> will
     * be rolled back.
     *
     * @param argument The argument to this transaction (use Void if not needed)
     * @param mode A hint to the UI provider for whether a progress bar should
     * be visible or invisible, and whether the main UI should be blocked until
     * completion
     * @param handler A custom handler for exceptions or failures
     * @param canCancel A hint to the UI as to whether it should display a
     * cancel button or not.  This does not guarantee that the cancel button
     * will necessarily be visible or not - that depends on this transaction
     * (and any nested transactions) actually <i>being</i> cancellable.
     * @return A future which can be waited on and queried for the end result
     */
    public final Future<? extends ResultType> launch(ArgType argument, FailureHandler handler, UIMode mode, boolean canCancel) {
        if (launched.get()) {
            throw new IllegalStateException(this + " is not reusable.  launch() called twice");
        }
        launched.set(true);
        return TransactionHandlerAccessor.DEFAULT.launch(delegate, argument, handler, jobName, mode, canCancel, threadPool);
    }
    /**
     * Roll back a transaction, causing it to be rolled back on a background thread.
     * May only be called on completed transactions.  If this is a compound
     * transaction, each nested transaction's rollback() method is invoked in
     * reverse sequence.
     * <p/>
     * Rollback is not cancellable.
     * <p/>
     * If an exception occurs during rollback of one element of a compound
     * transaction, rollback of earlier transactions will continue.  All
     * transactions will be attempted to be rolled back despite any errors
     * rolling back other transactions.
     *
     * @param handler A failure handler (may be null) to handle exceptions
     * @param mode The way the progress should be presented to the user
     * @return A future which can be waited on for rollback completion.  Will
     * return true if all transactions were rolled back successfully.
     */
    public final Future<? extends Boolean> rollback(FailureHandler handler, UIMode mode) {
        if (!delegate.hasRun()) {
            throw new IllegalStateException("Cannot roll back a transaction " + //NOI18N
                    "which has not been run"); //NOI18N
        }
        return TransactionHandlerAccessor.DEFAULT.rollback(delegate, handler, jobName, mode, threadPool);
    }

    /**
     * Roll back a transaction, causing it to be rolled back on a background thread.
     * May only be called on completed transactions.  If this is a compound
     * transaction, each nested transaction's rollback() method is invoked in
     * reverse sequence.
     * <p/>
     * Rollback is not cancellable.
     * <p/>
     * If an exception occurs during rollback of one element of a compound
     * transaction, rollback of earlier transactions will continue.  All
     * transactions will be attempted to be rolled back despite any errors
     * rolling back other transactions.
     *
     * @param mode The way the progress should be presented to the user
     * @return A future which can be waited on for rollback completion.  Will
     * return true if all transactions were rolled back successfully.
     */
    public final Future<? extends Boolean> rollback(UIMode mode) {
        return rollback(FailureHandler.getDefault(), mode);
    }

    /**
     * Determine if this launcher's background operation is still running.
     * @return True if this launcher's background thread is running, either
     * performing a run or a rollback
     */
    public boolean isRunning() {
        return delegate.isRunning() || delegate.isRollingBack();
    }

    /**
     * Determine if it is safe to call launch.  If a background operation is
     * still running, or the transaction failed
     * @return true if this launcher can be launched
     */
    public boolean canRun() {
        return (!delegate.hasRun() || delegate.hasRolledBack()) && !failed.get();
    }

    private final class LaunchStateDelegate<ArgType, ResultType> extends TransactionManager<Transaction<ArgType, ResultType>, ArgType, ResultType> {
        private TransactionManager<?, ArgType, ResultType> del;
        LaunchStateDelegate(TransactionManager<?, ArgType, ResultType> del) {
            super (del.transaction());
            this.del = del;
        }

        @Override
        public ResultType run(TransactionController controller, ArgType argument) throws TransactionException {
            try {
                return del.doRun(controller, argument);
            } catch (TransactionException e) {
                if (!failed.get()) {
                    failed.set(!e.isCancellation());
                }
                throw e;
            } catch (RuntimeException e) {
                failed.set(true);
                throw e;
            }
        }

        @Override
        public boolean rollback(TransactionController controller) throws TransactionException {
            try {
                return del.doRollback(controller);
            } finally {
                launched.set(false);
                del.reset();
            }
        }

        @Override
        public void reset() {
            super.reset();
            del.reset();
        }
    }
}
