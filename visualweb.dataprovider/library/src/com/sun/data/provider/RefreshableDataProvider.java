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
