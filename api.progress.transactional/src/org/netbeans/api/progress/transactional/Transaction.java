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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.netbeans.modules.progress.transactional.TransactionAccessor;
import org.netbeans.modules.progress.transactional.UI;
import org.netbeans.spi.progress.transactional.TransactionHandler;
import org.netbeans.spi.progress.transactional.TransactionUI;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * An undoable operation which may be one of a sequence of undoable operations
 * which is run on a background thread.  A transaction can be used for any
 * work which can be done on a background thread.  Typically one implements
 * one to two methods:  run(TransactionController, ArgType) and optionally
 * <code>rollback(TransactionController, ArgType, ResultType)</code> to undo whatever
 * <code>run()</code> did.
 * <p/>
 * This module provides a simple way to create a chain of transactions, each
 * of which will be represented by one step in a progress bar.  The idea is to
 * make it simpler to work with and provide good feedback from than SwingWorker
 * or raw use of the Progress API, and to encourge making long running operations
 * undoable, so that a runtime error when, say, creating a project, does not
 * leave half-written data on the users' disk.
 * <p/>
 * Transactions may be chained together into a larger transaction using the
 * add method.  For example, to validate a URL, load its contents from the
 * web, and save it as file, you would write something like this (where the
 * concrete names are subclasses you have created):
 * <pre>
 * Transaction&lt;String, URL&gt; urlValidator = new StringToURLConverter();
 * Transaction&lt;URL, CharSequence&gt; urlLoader = new URLLoader();
 * Transaction&lt;CharSequence, File&gt; fileConverter = new FileLoader();
 * </pre>and then wrap them in a single transaction:
 * <pre>Transaction&lt;String, File&gt; wrapper = urlValidator.add(urlLoader.add(fileConverter));</pre>
 * or more simply,
 * <pre>Transaction&lt;String, File&gt; wrapper = new StringToURLConverter().add(new URLLoader().add(new FileLoader()));</pre>
 * and then simply launch it:
 * <pre>Future&lt;File&gt; waitFor = wrapper.launch(...)</pre>
 * This library contains a service-provider interface which allows UI implementations
 * that show a progress bar, or otherwise indicate progress, to be plugged in.
 * <p/>
 * <h4>Error Handling</h4>
 * <code>run()</code> and <code>rollback()</code> throw
 * <code><a href="TransactionException.html">TransactionException</a></code>.
 * Use this exception to indicate an <i>expected</i> failure - for example,
 * not being able to connect to a remote server or read a file - mundane yet
 * normal ways such operations can fail.  A <code>TransactionException</code>
 * has a <code>NotificationStyle</code> to hint to the UI whether something
 * should be notfied to the user in a popup, in the status bar, or not at all.
 * <p/>
 * Any exception during the run of a compound transaction will cause <code>rollback()</code>
 * to be called on all preceding transactions (even if their rollback methods
 * throw exceptions).  <code>TransactionException</code>s are handled politely,
 * being shown in the statusbar or as a popup.  Regular <code>RuntimeException</code>s
 * will result in an error dialog.  In either case, preceding transactions,
 * if any, are rolled back.
 *
 * <h4>Publishing on the Event Thread</h4>
 * The <code>run()</code> and <code>rollback()</code> methods are passed a
 * <code><a href="TransactionController.html">TransactionController</a></code>.  It does three things:
 * <ul>
 * <li>Publish a runnable to be run later on the event thread</li>
 * <li>Publish a runnable and wait for it to be run on the event thread</li>
 * <li>Check for cancellation - see if the user has clicked the cancel button
 * (if available).  If yes, this will trigger a <code>TransactionException</code>
 * which will be transparently handled, rolling back any preceding transactions.
 * </ul>
 *
 * <h4>Cancellation</h4>
 * A transaction may be cancelled before it has completed.
 * There are two values related to cancellation:  The argument to
 * <code>Transaction</code>'s constructor determines if the UI will <i>try</i>
 * to cancel the transaction, if cancel is invoked either via the UI or
 * via <code>Future.cancel()</code>.  The <code>canCancel</code> argument
 * to the <code>launch()</code> methods of
 * <a href="TransactionLauncher.html"><code>TransactionLauncher</code></a>
 * determine if the UI should try to show a cancel button (which may or may
 * not be enabled depending on the former value) - since it is possible to
 * be passed a <code>Transaction</code> you did not create, both values are
 * important.
 * <p/>
 * No special code is needed to handle cancellation - the library will automatically
 * check for cancellation before and after running each individual transaction.
 * However, if you have particularly long-running transactions or looping constructs
 * that provide an opportunity, it may be useful to call <code>TransactionController.checkCancellation()</code>
 * inside of your Transaction's <code>run()</code> methods.
 * 
 * <h4>Statefulness</h4>
 * A <code>Transaction</code> is ideally stateless - all data it needs to do
 * its work should be in the input argument, and all data it needs to undo
 * its work should be in the <code>run()</code> result.  While it is possible
 * to implement Transactions that hold state and work correctly, such transactions
 * are not reusable - you cannot launch them multiple times, because they will
 * contain stale data and, if run from multiple threads at once, may behave
 * unpredictably.  So it is advisable to avoid any instance variables in a
 * Transaction unless it is really necessary.  The infrastructure will wrap
 * a transaction which is run in an object that carries the state of that
 * particular run, and can pass it back in to handle rollback operations.
 *
 * <h4>User Interface</h4>
 * The user interface is provided by the implementation of the SPI - by
 * placing a <a href="../../../spi/progress/transactional/TransactionHandler">
 * <code>TransactionHandler</code></a> in
 * the <a href="http://wiki.netbeans.org/DevFaqLookupDefault"><code>default
 * Lookup</code></a>. How
 * exactly it behaves depends on the implementation of that SPI.  The standard one
 * uses NetBeans Progress API under the hood, but does not expose
 * the Progress API directly, and is implemented in a separate module.
 *
 * <h4>Example</h4>
 *<pre>private static final class LoadURL extends Transaction&lt;URL, String&gt;{
 *  LoadURL() {
 *    super(&quot;Load URL&quot;, URL.class, String.class);
 *  }
 *  protected String run(TransactionController controller, URL argument)
 *                                          throws TransactionException {
 *    sleep();
 *    try {
 *      InputStream in = argument.openStream();
 *      ByteArrayOutputStream out = new ByteArrayOutputStream();
 *      FileUtil.copy(in, out);
 *      return new String(out.toByteArray());
 *    } catch (IOException ex) {
 *      rethrow(null, ex, NotificationStyle.STATUS);
 *      return null;
 *    }
 *  }
 *}
 *private static final class SaveToFile extends Transaction&lt;String, File&gt;{
 *  SaveToFile() {
 *    super(&quot;Save to file&quot;, String.class, File.class);
 *  }
 *  protected File run(TransactionController controller, String argument)
 *                                                 throws TransactionException {
 *    sleep();
 *    try {
 *      File result = File.createTempFile(&quot;&quot; +
 *              System.currentTimeMillis(), &quot;.txt&quot;);
 *      FileOutputStream out = new FileOutputStream(result);
 *      FileUtil.copy(new ByteArrayInputStream(argument.getBytes()), out);
 *      out.close();
 *      return result;
 *    } catch (IOException ex) {
 *      rethrow(null, ex, NotificationStyle.STATUS);
 *      return null;
 *    }
 *  }
 *  protected boolean rollback(TransactionController controller, 
 *          String argument, File prevResult) throws TransactionException {
 *
 *    if (prevResult.exists()) {
 *      if (!prevResult.delete()) {
 *        throw new TransactionException(&quot;Could not delete &quot; + prevResult);
 *      }
 *    }
 *    return true;
 *  }
 *}
 * Transaction&lt;URL,File&gt; t = new LoadURL().add(new SaveToFile());
 * t.launch (&quot;http://netbeans.org&quot;, UIMode.BACKGROUND);
 * </pre>
 * @author Tim Boudreau
 */
public abstract class Transaction<ArgType,ResultType> {
    private final Class<? super ArgType> argType;
    private final Class<? super ResultType> resultType;
    private final String name;
    private final boolean canCancel;
    static {
        Class<?> c = TransactionHandler.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        TransactionAccessor.DEFAULT = new TransactionAccessorImpl();
    }

    /**
     * Create transaction with the specified name and argument and result types
     * @param name The transaction name, which will be used to display information
     * about this step of the transaction
     * @param argType The argument type
     * @param resultType The result type
     */
    protected Transaction(String name, Class<? super ArgType> argType, Class<? super ResultType> resultType) {
        this (name, argType, resultType, true);
    }

    /**
     * Create transaction with the specified name and argument and result types/
     * @param name The transaction name, which will be used to display information
     * about this step of the transaction
     * @param argType The argument type
     * @param resultType The result type
     * @param canCancel If true, it should be possible to cancel the transaction
     */
    protected Transaction(String name, Class<? super ArgType> argType, Class<? super ResultType> resultType, boolean canCancel) {
        this.argType = argType;
        this.resultType = resultType;
        this.name = name;
        this.canCancel = canCancel;
    }

    /**
     * The argument type which should be passed to this transaction's <code>run()</code>
     * and <code>rollback()</code> methods.
     * @return The argument type
     */
    public final Class<? super ArgType> argType() {
        return argType;
    }

    /**
     * The result type which should be returned this transaction's <code>run()</code>
     * method and passed back into its <code>rollback()</code> method.
     * @return The argument type
     */
    public final Class<? super ResultType> resultType() {
        return resultType;
    }

    /**
     * Actually run this transaction.  If the transaction cannot be completed,
     * throw a TransactionException.  If this is a compound transaction, all
     * preceding transactions will be rolled back, and the user will be notified
     * with no exception dialog.
     * <p/>
     * Runtime exceptions thrown from the run() method will result in an
     * exception dialog, but as with TransactionException, all preceding
     * transactions will be rolled back.
     * <p/>
     * The infrastructure will call <code>controller.checkCancelled()</code>
     * before and after invoking the <code>run()</code> method.  Implementations
     * which perform very long-running operations or loops should periodically
     * call <code>controller.checkCancelled()</code> themselves.  If the
     * operation has been cancelled, this will result in an exception being
     * thrown which will be caught by the initiating thread and handled
     * appropriately, and execution of the transaction will be rolled back
     * correctly.
     *
     * @param controller A controller for publishing interim work on the
     * event thread, either synchronously or asynchronously
     * @param argument The argument this transaction takes - use Void if
     * non-applicable.
     * @return The result of the transaction.
     */
    protected abstract ResultType run(TransactionController controller, ArgType argument) throws TransactionException;
    /**
     * Roll back this transaction, undoing any operations possible.  If an
     * Exception is thrown from this method, the user will be
     * notified, but all preceding transactions (in the case of a compound
     * transaction) will proceed to be rolled back.  If a runtime exception is
     * thrown, all preceding transactions will be rolled back, and the user
     * will see an exception dialog.
     * <p/>
     * The default implementation simply returns true.
     *
     * @param controller A transactionController
     * @param argument The argument last passed to run()
     * @param prevResult The return value of the preceding call to run().  May
     * be null if run() exited abnormally.
     */
    protected boolean rollback(TransactionController controller, ArgType argument, ResultType prevResult) throws TransactionException {
        return true;
    }
    /**
     * Determine the size of this transaction - the number of user-supplied
     * transactions that will actually run when this one is run.  Transactions
     * which are simply containers for invoking other transactions should not
     * be counted - only ones explicitly provided by the user.  Thus a transaction
     * which simply delegates to another transaction has a size of 1.
     *
     * @return The number of transactions to run.
     */
    protected int size() {
        return 1;
    }

    /**
     * Index of a transaction within a compound transaction.
     *
     * @param t A transaction.
     * @return Default implementation returns 0 if t == this, otherwise -1
     */
    protected int indexOf(Transaction<?,?> t) {
        return t == this ? 0 : -1;
    }

    /**
     * Populate a list of transactions which are components of this transaction.
     * Implementations which chain together several transactions should add all
     * individual transactions which actually contain user-provided logic here.
     * Implementations which wrapper a single transaction
     * in an outer transaction should return the deepest wrapped transaction.
     * The default implementation simply adds <code>this</code> to the list
     * and returns.
     * <p/>
     * Note that the number of transactions added in this method is <i> not
     * necessarily the same as the return value of <code>size()</code></i>.
     * For example, a transaction which launches a background thread and runs
     * two other transactions in parallel should list both of those transactions
     * here &mdash; but its logical size, for the purposes of a progress bar,
     * is still 1.  A transaction returned from Transaction.unify(Transaction)
     * will have a size() of 1, but its real contents should still be returned
     * by this method.
     */
    protected void listContents(List<? super Transaction<?,?>> contents) {
        contents.add(this);
    }
    /**
     * Rethrows an exception as a TransactionException - indicating to the
     * infrastructure that the exception is a known possible failure mode.
     *
     * @param msg A string message.  If null, the localized message of the
     * exception should be used.
     * @param e An exception triggering transaction failure
     * @param style A hint to the UI for how the exception should be presented
     * to the user (show a popup, set the status bar text, do nothing...)
     * @throws TransactionException
     */
    protected final void rethrow(String msg, Exception e, TransactionException.NotificationStyle style) throws TransactionException {
        throw new TransactionException(e, style);
    }

    /**
     * Add a transaction to this one.  The returned transaction is the equivalent
     * of a linked list of transactions - this transaction will be run, then
     * its return value from run() will be passed to the next transaction.
     * When the resulting transaction is invoked, both transactions will run in
     * sequence, and the final result returned.
     * <p>
     * To chain multiple transactions, simply do
     * <pre>
     * transaction1.add (new Transaction2().add(new Transaction3().add(new Transaction4()));
     * </pre>
     * (make sure the parenthesis are as shown above to ensure correct ordering, i.e.
     * <code>transaction1.add(transaction2.add(transaction3.add(transaction4)));</code>
     * <p/>
     * A transaction may be added to itself harmlessly, if it takes the same
     * input and output types, and assuming the transaction has no internal
     * state of its own (though this is not typically particularly useful).
     *
     * @param <NextResultType> The return type of the passed Transaction
     * @param next The transaction which should be run after this one, which
     * takes the return value of this transaction as its argument.
     * <p/>
     * Note that if any transaction in a compound transaction is not cancellable,
     * then the returned transaction is not cancellable.
     *
     * @return A compound transaction which will run this transaction and then
     * the next sequentially
     */
    public final <NextResultType> Transaction<ArgType, NextResultType> add(Transaction<ResultType, NextResultType> next) {
        Parameters.notNull("next", next);
        return new CompoundTransaction<ArgType, ResultType, NextResultType> (this, next);
    }

    /**
     * Create a transaction which merges two transactions which will run
     * simultaneously, each with its own progress UI.  If either transaction
     * is cancelled, both will be cancelled and rolled back.  If either
     * transaction fails, both will be rolled back.
     * <p/>
     * The resulting transaction will block until both passed transactions have
     * completed.
     * <p/>
     * The argument to a parallel transaction may be null if no value is needed;
     * if the return values of both transactions are null, the return value
     * of the parallel transaction will be null.
     *
     * @param <A> The result type of transaction a
     * @param <B> The result type of transaction b
     * @param name The name of the containing transaction
     * @param a A transaction which takes an argument of AArgType and returns AResultType
     * @param b A transaction which takes an argument of BArgType, and return BResultType
     * @return A transaction which will run both of these transactions in parallel
     */
    public static final <AArgType, BArgType, AResultType, BResultType>
            Transaction<ParallelValue<AArgType, BArgType>, ParallelValue<AResultType, BResultType>>
            createParallelTransaction (Transaction<AArgType, AResultType> a, Transaction<BArgType, BResultType> b) {
        Parameters.notNull("b", b);
        Parameters.notNull("a", a);
        return new ParallelTransaction<AArgType, BArgType, AResultType, BResultType>(a.getName(), a, b);
    }

    boolean contains(Transaction<?, ?> x) {
        Parameters.notNull("x", x);
        return x == this;
    }

    final boolean canCancel() {
        return canCancel;
    }

    final String getName() {
        return name;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getName() + "]";
    }

    /**
     * Create an object responsible for holding the state (input and output
     * arguments, run, rolled back and failed states) for one run of this
     * Transaction.
     *
     * @return A TransactionManager which can run this transaction and roll
     * it back
     */
    TransactionManager<? extends Transaction<ArgType, ResultType>, ArgType, ResultType> createRunner() {
        return new SingleTransactionManager<ArgType, ResultType> (this);
    }

    /**
     * Create a TransactionLauncher which can start this transaction on a
     * background thread, and be held in memory if the transaction should
     * be undoable.
     *
     * @return A TransactionLauncher
     */
    public final TransactionLauncher<ArgType, ResultType> createLauncher(String jobName) {
        Parameters.notNull("jobName", jobName);
        return new TransactionLauncher<ArgType, ResultType>(jobName, this);
    }

    public final TransactionLauncher<ArgType, ResultType> createLauncher(String jobName, RequestProcessor threadPool) {
        Parameters.notNull("jobName", jobName);
        return new TransactionLauncher<ArgType, ResultType>(jobName, this, threadPool);
    }

    /**
     * Launch this transaction on a background thread, with the specified name,
     * argument and UI mode.
     * <p/>
     * For greater control over the UI, failure handling, etc., use
     * <code>createLauncher().launch(...)</code>.
     *
     * @param jobName The name of the overall transaction, as should appear as
     * the title of a progress bar.
     * @param argument An argument to the transaction
     * @param uiMode The way progress should be presented to the user
     * @return A Future that can be waited on for the transaction to complete
     */
    public final Future<? extends ResultType> launch (ArgType argument, String jobName, UIMode uiMode) {
        Parameters.notNull("jobName", jobName);
        Parameters.notNull("uiMode", uiMode);
        return createLauncher(jobName).launch(argument, uiMode);
    }

    //XXX only used by unit tests
    List<? extends Transaction<?,?>> contents() {
        List<Transaction<?,?>> l = new ArrayList<Transaction<?, ?>>();
        listContents(l);
        return l;
    }

    /**
     * Takes another transaction which may contain a number of steps,
     * and returns a transaction which appears to be only a single step.
     * This is mainly useful if you have a multi-step transaction, and
     * you want it to be rendered in the UI using an indeterminate
     * progress bar (which is only done for single-step transactions).
     *
     * @return A transaction which wrappers this one, but which is treated
     * by the UI as being a single step.
     */
    public final Transaction<ArgType, ResultType> unify() {
        return new UnifiedTransaction<ArgType,ResultType>(getName(), this);
    }

    /**
     * Wrapper transaction class for cases where a transaction needs to be
     * wrapped - for example, if the entire transaction should be run inside
     * a synchronized block, or under a mutex or other locking construct.
     * Example:
     * <pre>private static final class FsAtomicTransaction extends Transaction.Wrapper&lt;FileObject, FileObject&gt; {
     *  FsAtomicTransaction(Transaction&lt;FileObject, FileObject&gt; real) {
     *    super(real);
     *  }
     * protected FileObject run(final TransactionController controller, final FileObject argument) throws TransactionException {
     *   if (argument != null) {
     *     class A implements FileSystem.AtomicAction {
     *       public void run() throws IOException {
     *         try {
     *           doRun(controller, argument);
     *         } catch (TransactionException ex) {
     *           throw new IOException(ex);
     *         }
     *       }
     *     }
     *     try {
     *       FileSystem fs = argument.getFileSystem();
     *       fs.runAtomicAction(new A());
     *     } catch (IOException ex) {
     *       if (ex.getCause() instanceof TransactionException) {
     *         throw (TransactionException) ex.getCause();
     *       } else {
     *         throw new TransactionException(ex);
     *       }
     *     }
     *   }
     *   return null;
     * }

     * protected boolean rollback(final TransactionController controller, final FileObject argument, final FileObject prevResult) throws TransactionException {
     *   if (argument != null) {
     *     class A implements FileSystem.AtomicAction {
     *       public void run() throws IOException {
     *         try {
     *           doRollback(controller, argument, prevResult);
     *         } catch (TransactionException ex) {
     *           throw new IOException(ex);
     *         }
     *       }
     *     }
     *     try {
     *       FileSystem fs = argument.getFileSystem();
     *       fs.runAtomicAction(new A());
     *     } catch (IOException ex) {
     *       if (ex.getCause() instanceof TransactionException) {
     *         throw (TransactionException) ex.getCause();
     *       } else {
     *         throw new TransactionException(ex);
     *       }
     *     }
     *   }
     *   return true;
     * }</pre>
     *
     * @param <ArgType> The argument type
     * @param <ResultType> The result type
     */
    public static abstract class Wrapper<ArgType, ResultType> extends Transaction<ArgType, ResultType> {
        private final Transaction<ArgType, ResultType> wrapped;
        /**
         * Create a wrapper transaction over the passed transaction.
         * @param wrapped The wrapped transaction
         */
        protected Wrapper (Transaction<ArgType, ResultType> wrapped) {
            super (wrapped.getName(), wrapped.argType(), wrapped.resultType());
            this.wrapped = wrapped;
        }

        /**
         * Implement this method to acquire whatever lock is necessary, and
         * then run the wrapped transaction by invoking <code>doRollback()</code>.
         * The entire contained transaction will run inside of the lock.
         * @param controller The transaction controller
         * @param argument The argument
         * @return A result
         * @throws TransactionException
         */
        @Override
        protected abstract ResultType run(TransactionController controller, ArgType argument) throws TransactionException;

        /**
         * Implement this method to acquire whatever lock is necessary, and
         * then run the wrapped transaction by invoking <code>doRun()</code>.
         * For example:
         *
         *
         * @param controller
         * @param argument
         * @param prevResult
         * @return
         * @throws TransactionException
         */
        @Override
        protected abstract boolean rollback(TransactionController controller, ArgType argument, ResultType prevResult) throws TransactionException;

        @Override
        protected final int indexOf(Transaction<?, ?> t) {
            return t == this || t == wrapped ? 0 : wrapped.indexOf(t);
        }

        @Override
        protected final void listContents(List<? super Transaction<?, ?>> contents) {
            contents.add(wrapped);
        }

        @Override
        protected final int size() {
            return wrapped.size();
        }

        protected final boolean doRollback(TransactionController controller, ArgType argument, ResultType prevResult) throws TransactionException {
            return wrapped.rollback(controller, argument, prevResult);
        }

        protected final ResultType doRun(TransactionController controller, ArgType argument) throws TransactionException {
            return wrapped.run(controller, argument);
        }
    }

    private static final class UnifiedTransaction<T,R> extends Transaction<T,R> {
        private final Transaction<T,R> delegate;

        public UnifiedTransaction(String name, Class<T> argType, Class<R> resultType, boolean canCancel, Transaction<T, R> delegate) {
            super(name, delegate.argType(), delegate.resultType(), canCancel);
            this.delegate = delegate;
        }

        public UnifiedTransaction(String name, Transaction<T, R> delegate) {
            super(name, delegate.argType(), delegate.resultType());
            this.delegate = delegate;
        }

        @Override
        protected R run(TransactionController controller, T argument) throws TransactionException {
            return delegate.run(controller, argument);
        }

        @Override
        protected int indexOf(Transaction<?, ?> t) {
            return t == this || t == delegate ? 0 : delegate.indexOf(t) >= 0 ? 1 : -1;
        }

        @Override
        protected void listContents(List<? super Transaction<?, ?>> contents) {
            contents.add (delegate);
        }

        @Override
        protected boolean rollback(TransactionController controller, T argument, R prevResult) throws TransactionException {
            return delegate.rollback(controller, argument, prevResult);
        }

        @Override
        protected int size() {
            return 1;
        }

        @Override
        public String toString() {
            return super.toString() + "[" + delegate +"]";
        }
    }

    private static final class TransactionAccessorImpl extends TransactionAccessor {

        @Override
        public <ArgType, ResultType>TransactionManager<? extends Transaction<ArgType, ResultType>, ArgType, ResultType> createState (Transaction<ArgType,ResultType> xaction) {
            return xaction.createRunner();
        }

        @Override
        public TransactionController createController(FailureHandler handler, UI ui) {
            return new TransactionController(handler, ui);
        }

        @Override
        public int transactionSize(Transaction<?, ?> t) {
            return t.size();
        }

        @Override
        public int indexOf(Transaction<?, ?> parent, Transaction<?, ?> child) {
            return parent.indexOf(child);
        }

        @Override
        public String transactionName(Transaction<?, ?> transaction) {
            return transaction.getName();
        }

        @Override
        public void cancel(TransactionController controller) {
            controller.cancel();
        }

        @Override
        public boolean canCancel(Transaction<?, ?> transaction) {
            return transaction.canCancel();
        }

        @Override
        public TransactionUI uiFor(TransactionController controller) {
            return controller.ui().implementation();
        }

        @Override
        public boolean isCompoundTransaction(Transaction<?, ?> transaction) {
            return transaction.size() > 1;
        }

        @Override
        public List<Transaction<?, ?>> getContents(Transaction<?, ?> transaction) {
            List<Transaction<?,?>> result = new ArrayList<Transaction<?,?>>();
            transaction.listContents(result);
            return result;
        }

        @Override
        public <ArgType, ResultType> ResultType run(TransactionManager<?, ArgType, ResultType> t, TransactionController c, ArgType arg) throws TransactionException {
            return t.doRun(c, arg);
        }

        @Override
        public boolean rollback(TransactionManager<?, ?, ?> t, TransactionController c) throws TransactionException {
            return t.doRollback(c);
        }

        @Override
        public boolean transactionHasRun(TransactionManager<?, ?, ?> runner) {
            return runner.hasRun();
        }

        @Override
        public boolean transactionHasRolledBack(TransactionManager<?, ?, ?> runner) {
            return runner.hasRolledBack();
        }

        @Override
        public boolean transactionIsRunning(TransactionManager<?, ?, ?> runner) {
            return runner.isRunning() || runner.isRollingBack();
        }

        @Override
        public List<? extends Transaction<?, ?>> contents(Transaction<?, ?> t) {
            return t.contents();
        }
    }
}
