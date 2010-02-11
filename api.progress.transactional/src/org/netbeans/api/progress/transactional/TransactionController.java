/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.api.progress.transactional;

import org.netbeans.modules.progress.transactional.TransactionManager;
import org.netbeans.modules.progress.transactional.UI;
import java.awt.EventQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.transactional.TransactionException.NotificationStyle;
import org.netbeans.modules.progress.transactional.ControllerAccessor;
import org.netbeans.modules.progress.transactional.UIImplAccessor;
import org.netbeans.spi.progress.transactional.TransactionHandler;
import org.netbeans.spi.progress.transactional.TransactionUI;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Tim Boudreau
 */
public final class TransactionController {
    private final AtomicBoolean failed;
    private final OncePerThreadBoolean cancelThrown = new OncePerThreadBoolean();
    private final OncePerThreadBoolean failedThrown = new OncePerThreadBoolean();
    private final AtomicBoolean cancelled;
    private final FailureHandler handler;
    private final UI ui;
    private final AtomicBoolean inRollback = new AtomicBoolean(false);
    private final AtomicBoolean rollbackFailed;

    static {
        Class<?> c = TransactionHandler.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        ControllerAccessor.DEFAULT = new ControllerAccessorImpl();
    }

    TransactionController(FailureHandler handler, UI ui) {
        this (new AtomicBoolean(false), new AtomicBoolean(false), handler, ui);
    }

    public TransactionController(AtomicBoolean failed, AtomicBoolean cancelled, FailureHandler handler, UI ui) {
        this.failed = failed;
        this.cancelled = cancelled;
        this.handler = handler == null || handler == FailureHandler.getDefault() ? handler : FailureHandler.wrapDefault(handler);
        this.ui = ui;
        this.rollbackFailed = new AtomicBoolean(false);
        if (failed.get()) {
            failedThrown.set(Thread.currentThread());
        }
    }

    /**
     * Create an instance of TransactionController which talks to a different
     * UI but shares all internal state with this one.
     * @return
     */
    TransactionController cloneWithNewUI () {
        TransactionController result = new TransactionController (failed, cancelled, handler, ui.newUI());
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "[ui=" + ui + " failed=" + failed+ " cancelled=" + cancelled + " rollbackFailed=" + rollbackFailed + "]";
    }

    void reset() {
        failed.set(false);
        cancelThrown.clear();
        cancelled.set(false);
    }

    UI ui() {
        return ui;
    }

    boolean failed() {
        return inRollback.get() ? rollbackFailed.get() : failed.get();
    }

    FailureHandler handler() {
        return handler;
    }

    private boolean cancelThrown() {
        return cancelThrown.get();
    }

    boolean inRollback() {
        return inRollback.get();
    }

    /**
     * Check if this transaction has been cancelled.  This method throws a
     * TransactionException with its <code>isCancellation()</code> method
     * returning true if the user has cancelled this operation since
     * the last call to checkCancelled().  This exception is guaranteed
     * only to be thrown once, on the first call to checkCancelled() after
     * user cancellation.
     *
     * @throws TransactionException
     */
    public final void checkCancelled() throws TransactionException {
        if (!inRollback.get() && failedThrown.get()) {
            System.err.println("throwing alternate fail");
            throw new TransactionException ("Parallel transaction on other thread failed");
        }
        if (cancelThrown() || inRollback.get()) {
            return;
        }
        if (cancelled.get() && !cancelThrown()) {
            TransactionException e = new TransactionException ("Cancel", NotificationStyle.NONE);
            e.setCancel(true);
            throw e;
        }
    }

    synchronized void cancel() {
        cancelled.set(true);
        Logger.getLogger(TransactionHandler.class.getName()).log(Level.FINER, "Cancel transaction");
    }

    /**
     * Publish a runnable which will be run on the event thread later.  This
     * call is non-blocking.
     * @param source The originating transaction
     * @param run A runnable to run on the event thread
     */
    public void publish (Transaction<?,?> source, Runnable run) {
        Parameters.notNull("run", run);
        Parameters.notNull("source", source);
        try {
            doPublish(source, run, false);
        } catch (InterruptedException ex) { //will not happen with arg false
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Publish a runnable which will do some work on the event thread.  This
     * call will block until the runnable has completed.  Note that this
     * method does <i>not</i> use <code>EventQueue.invokeAndWait()</code>, and
     * so should be considerably more robust with respect to deadlocks.
     *
     * @param source The transaction publishing the work
     * @param run A runnable
     * @throws InterruptedException if the thread is interrupted
     */
    public void publishSynchronously(Transaction<?,?> source, Runnable run) throws InterruptedException {
        doPublish(source, run, true);
    }

    private void doPublish(Transaction<?,?> source, Runnable run, boolean synchronous) throws InterruptedException {
        boolean inEq = EventQueue.isDispatchThread();
        UIImplAccessor.DEFAULT.onBeforePublish(ui.implementation(), source, run, inEq);
        if (inEq) {
            UIImplAccessor.DEFAULT.onPublish(ui.implementation(), source, run, inEq);
            run.run();
            UIImplAccessor.DEFAULT.onAfterPublish(ui.implementation(), source, run, inEq);
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            EventQueue.invokeLater(new Dispatcher(source, ui.implementation(), run, latch));
            if (synchronous) {
                latch.await();
            }
        }
    }

    void enterRollback() {
        inRollback.set(true);
    }

    final boolean failed (TransactionManager<?,?,?> runner, Throwable e, String msg) {
        failed.set(true);
        failedThrown.set(Thread.currentThread());
        inRollback.set(true);
        try {
            ui.onFailure(runner.transaction(), e, false);
            return handler.failed (runner.transaction(), e, msg, false);
        } catch (Exception ex) {
            if (ex.getCause() == null) {
                ex.initCause(e);
            }
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    final boolean rollbackFailed(TransactionManager<?,?,?> runner, Throwable e, String msg) {
        rollbackFailed.set(true);
        inRollback.set(true);
        try {
            ui.onFailure(runner.transaction(), e, true);
            return handler.failed (runner.transaction(), e, msg, false);
        } catch (Exception ex) {
            if (ex.getCause() == null) {
                ex.initCause(e);
            }
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    private static final class Dispatcher implements Runnable {
        private final Runnable wrapped;
        private final CountDownLatch latch;
        private final TransactionUI uiImpl;
        private final Transaction<?,?> source;
        private Dispatcher (Transaction<?,?> source, TransactionUI uiImpl, Runnable run, CountDownLatch latch) {
            this.wrapped = run;
            this.latch = latch;
            this.source = source;
            this.uiImpl = uiImpl;
        }

        @Override
        public void run() {
            try {
                UIImplAccessor.DEFAULT.onPublish(uiImpl, source, wrapped, true);
                wrapped.run();
                UIImplAccessor.DEFAULT.onAfterPublish(uiImpl, source, wrapped, true);
            } finally {
               latch.countDown();
            }
        }
    }

    private static final class ControllerAccessorImpl extends ControllerAccessor {

        @Override
        public boolean failed(TransactionController controller) {
            return controller.failed();
        }

        @Override
        public void checkCancelled(TransactionController controller) throws TransactionException {
            controller.checkCancelled();
        }

        @Override
        public UI ui(TransactionController controller) {
            return controller.ui();
        }

        @Override
        public void enterRollback(TransactionController controller) {
            controller.enterRollback();
        }

        @Override
        public boolean rollbackFailed(TransactionController controller, TransactionManager<?, ?, ?> transaction, Throwable e, String msg) {
            return controller.rollbackFailed(transaction, e, msg);
        }

        @Override
        public boolean failed(TransactionController controller, TransactionManager<?, ?, ?> transaction, Throwable e, String msg) {
            return controller.failed(transaction, e, msg);
        }
    }
}
