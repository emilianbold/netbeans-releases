/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.sun.data.provider;

/**
 * <p>Behavioral interface that is implemented by {@link DataProvider} classes
 * that offer commit/revert support.  In such environments, methods that modify
 * the data element values (such as <code>setValue()</code>) must cause the new
 * values to be cached, and the underlying data elements are not actually
 * updated until <code>commitChanges()</code> is called.  An application may
 * also call <code>revertChanges()</code> to throw away any cached updates.  In
 * spite of this caching, however, <code>valueChanged()</code> events must still
 * be sent to registered {@link DataListener}s -- an instance of
 * {@link TransactionalDataListener} will also be notified when the
 * actual <code>commitChanges()</code> or <code>revertChanges()</code> occurs.
 * </p>
 *
 * <p>During the time between when a modification method (such as
 * <code>setValue()</code> is called, and a later call to
 * <code>commitChanges()</code>, any calls to <code>getType()</code> or
 * <code>getValue()</code> will reflect the modified value from the cache, not
 * the original value from the underlying data structure.</p>
 *
 * @author Craig McClanahan
 * @author Joe Nuxoll
 */
public interface TransactionalDataProvider extends DataProvider {

    // ----------------------------------------------------- Flush/Clear Methods

    /**
     * <p>Cause any cached changes to values of data elements supported by this
     * {@link DataProvider} to be passed through to the underlying data
     * structure.</p>
     *
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException).  Consult
     *         the documentation of the specific DataProvider implementation for
     *         details on what exceptions might be wrapped by a DPE.
     */
    public void commitChanges() throws DataProviderException;

    /**
     * <p>Cause any cached changes to values of data elements supported by this
     * {@link DataProvider} to be thrown away, so that the initial values are
     * again visible.</p>
     *
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException).  Consult
     *         the documentation of the specific DataProvider implementation for
     *         details on what exceptions might be wrapped by a DPE.
     */
    public void revertChanges() throws DataProviderException;

    // ---------------------------------------------- Event Registration Methods

    /**
     * <p>Register a new {@link TransactionalDataListener} to this
     * {@link TransactionalDataProvider} instance.</p>
     *
     * @param listener New {@link TransactionalDataListener} to register
     */
    public void addTransactionalDataListener(TransactionalDataListener listener);

    /**
     * <p>Deregister an existing {@link TransactionalDataListener} from
     * {@link TransactionalDataProvider} instance.</p>
     *
     * @param listener Old {@link TransactionalDataListener} to remove
     */
    public void removeTransactionalDataListener(TransactionalDataListener listener);

    /**
     * @return An array of the {@link TransactionalDataListener}s
     *         currently registered on this {@link TransactionalDataProvider}.
     *         If there are no registered listeners, a zero-length array is
     *         returned.
     */
    public TransactionalDataListener[] getTransactionalDataListeners();
}
