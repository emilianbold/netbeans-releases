/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.api.progress.transactional;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Handles failures during transactions.
 * <p/>
 * When starting a transaction, the caller can (optionally) provide their own
 * <code>FailureHandler</code>, which is notified of any exceptions thrown
 * while running a transaction (it should distinguish betweeen TransactionExceptions,
 * which represent predictable failures such as not being able to communicate with a
 * remote server, and ordinary RuntimeExceptions, which represent program
 * errors).
 * <p/>
 * There is also a default, application-wide <code>FailureHandler</code>, which
 * will handle notification of exceptions appropriately for most cases.
 * <p/>
 * If you pass your own FailureHandler when starting a transaction, and want
 * to bypass the standard reporting of an exception, simply return true from
 * your implementation of <code>failed()</code>, and the default notification
 * mechanism will not be called.
 * <p/>
 * Note that you cannot <i>block</i> rollback or enable a transaction to
 * continue despite an exception, by returning true from <code>failed()</code> -
 * you can only determine whether the user is shown an error message of some
 * kind, or use your own mechanism to notify the user.
 * <p/>
 * Methods on this class may be called from any thread.  Subclasses should
 * be designed to be thread-safe.
 * @author Tim Boudreau
 */
public abstract class FailureHandler {
    /**
     * Handle a failure caused by an exception during a transaction
     *
     * @param xaction The transaction
     * @param e An exception, may be null
     * @param msg An error message.  May be null
     * @return Whether or not the failure has been handled (including logging it)
     */
    public abstract boolean failed(Transaction<?,?> xaction, Throwable e, String msg, boolean inRollback);

    /**
     * Create a failure handler with another.  If that other does not handle
     * the exception, delegate to the default one.
     * @param mine A failure handler
     * @return A failure handler
     */
    static FailureHandler wrapDefault(FailureHandler mine) {
        return mine == getDefault() ? mine : new WrapFH(mine);
    }

    /**
     * Get the default FailureHandler instance. It will typically
     * <ul>
     * <li>Check if the transaction is a TransactionException.
     *  <ul>
     *      <li>If yes, then
     *      <ul>
     *          <li>Check if it is user-cancellation.  If so, show no message</li>
     *          <li>Check its NotificationStyle, and show a popup dialog, set the
     *              status bar text or just log the failure depending on it</li>
     *      </ul>
     *      </li>
     *      <li>If no, then show the user an exception dialog or the equivalent -
     *          a runtime exception has been thrown - this is an error.
     *      </li>
     *   </ul>
     * </ul>
     *
     * @return The default FailureHandler for the application
     */
    public static final FailureHandler getDefault() {
        FailureHandler result = Lookup.getDefault().lookup(FailureHandler.class);
        if (result == null) {
            result = new TrivialFH();
        }
        return result;
    }

    private static final class WrapFH extends FailureHandler {
        private final FailureHandler delegate;

        public WrapFH(FailureHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean failed(Transaction<?, ?> xaction, Throwable e, String msg, boolean inRollback) {
            boolean result = delegate.failed(xaction, e, msg, inRollback);
            if (!result) {
                FailureHandler.getDefault().failed(xaction, e, msg, inRollback);
            }
            return result;
        }
    }

    private static final class TrivialFH extends FailureHandler {
        @Override
        public boolean failed(Transaction<?,?> xaction, Throwable e, String msg, boolean inRollback) {
            if (e instanceof TransactionException) {
                TransactionException te = (TransactionException) e;
                if (te.isCancellation()) {
                    Logger.getLogger(FailureHandler.class.getName()).log(Level.FINE, "User cancelled {0}", new Object[] { xaction });
                    return true;
                } else {
                    Logger.getLogger(FailureHandler.class.getName()).log(Level.FINER, null, e);
                }
            }
            if (e != null) {
                Exceptions.printStackTrace(e);
            } else {
                Logger.getLogger(FailureHandler.class.getName()).log(Level.FINE, "Failed {0} - {1}", new Object[] { xaction, msg });
            }
            return true;
        }
    };
}
