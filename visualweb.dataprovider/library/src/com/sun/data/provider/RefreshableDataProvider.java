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
 * that offer refresh support.  Calling the <code>refresh()</code> method on
 * this interface causes the DataProvider to re-fetch whatever data it is
 * representing from the underlying source.  This may be a JDBC call, or an EJB
 * or web service method invocation, etc.  Any cached changes in the
 * DataProvider will be lost.</p>
 *
 * @author Joe Nuxoll
 */
public interface RefreshableDataProvider extends DataProvider {

    /**
     * <p>Cause a re-fetch of whatever data this {@link DataProvider} is
     * representing from the underlying source.  This may result in a JDBC call,
     * or an EJB or web service method invocation, etc.  Any cached changes in
     * the DataProvider will be lost.</p>
     *
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException).  Consult
     *         the documentation of the specific DataProvider implementation for
     *         details on what exceptions might be wrapped by a DPE.
     */
    public void refresh() throws DataProviderException;

    // ---------------------------------------------- Event Registration Methods

    /**
     * <p>Register a new {@link RefreshableDataListener} to this
     * {@link RefreshableDataProvider} instance.</p>
     *
     * @param listener New {@link RefreshableDataListener} to register
     */
    public void addRefreshableDataListener(RefreshableDataListener listener);

    /**
     * <p>Deregister an existing {@link RefreshableDataListener} from
     * {@link RefreshableDataProvider} instance.</p>
     *
     * @param listener Old {@link RefreshableDataListener} to remove
     */
    public void removeRefreshableDataListener(RefreshableDataListener listener);

    /**
     * @return An array of the {@link RefreshableDataListener}s
     *         currently registered on this {@link RefreshableDataProvider}.
     *         If there are no registered listeners, a zero-length array is
     *         returned.
     */
    public RefreshableDataListener[] getRefreshableDataListeners();
}
