/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
