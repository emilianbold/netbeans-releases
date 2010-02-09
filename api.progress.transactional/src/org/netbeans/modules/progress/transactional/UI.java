/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.progress.transactional;

import org.netbeans.api.progress.transactional.Transaction;
import org.netbeans.api.progress.transactional.UIMode;
import org.netbeans.spi.progress.transactional.TransactionUI;
import sun.swing.UIAction;

/**
 * Wrapper for TransactionUIImplementation which only exposes those
 * methods needed to the API.
 *
 * @author Tim Boudreau
 */
public final class UI {
    private final TransactionUI delegate;
    public UI(TransactionUI delegate) {
        this.delegate = delegate;
    }

    public UIMode getMode() {
        return delegate.getMode();
    }

    public void onBeginTransaction (Transaction<?,?> transaction) {
        UIImplAccessor.DEFAULT.onBeginTransaction(delegate, transaction);
    }

    public void onEndTransaction(Transaction<?,?> transaction) {
        UIImplAccessor.DEFAULT.onEndTransaction(delegate, transaction);
    }

    public void onBeginRollback(Transaction<?,?> transaction) {
        UIImplAccessor.DEFAULT.onBeginRollback(delegate, transaction);
    }

    public void onEndRollback(Transaction<?,?> transaction) {
        UIImplAccessor.DEFAULT.onEndRollback(delegate, transaction);
    }

    public void onFailure (Transaction<?,?> xaction, Throwable t, boolean isRollback) {
        UIImplAccessor.DEFAULT.onFailure(delegate, xaction, t, isRollback);
    }

    public TransactionUI implementation() {
        return delegate;
    }

    public UI newUI() {
        return TransactionHandlerAccessor.DEFAULT.createUI(delegate.getName(), delegate.getMode(), delegate.canCancel());
    }
}
