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

package com.sun.data.provider.impl;

import java.util.ArrayList;
import java.util.List;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;

/**
 * <p>This {@link com.sun.data.provider.TableDataProvider} implementation
 * wraps the contents of a {@link List}.  This DataProvider ignores FieldKeys
 * entirely, and maintains a single list of objects.</p>
 *
 * <p>NOTE about Serializable:  By default, this class uses an {@link ArrayList}
 * as its internal data storage, which is a Serializable implementation of
 * {@link List}.  The internal storage can be swapped out using the
 * <code>setList(List)</code> method.  For this class to remain Serializable,
 * the contained List must be a Serializable implementation.  Also, and more
 * importantly, the contents of the storage List must be Serializable as well
 * for this class to successfully be serialized.</p>
 *
 * @author Joe Nuxoll
 */
public class ListDataProvider extends AbstractTableDataProvider {

    /**
     * Storage for the internal {@link List} ({@link ArrayList} by default)
     */
    protected List list = new ArrayList();

    /**
     * Constructs a new ListDataProvider using the default internal storage
     */
    public ListDataProvider() {}

    /**
     * <p>Constructs a new ListDataProvider using the specified List as internal
     * storage.</p>
     *
     * <p>NOTE about Serializable:  By default, this class uses an {@link
     * ArrayList} as its internal data storage, which is a Serializable
     * implementation of {@link List}.  The internal storage can be swapped out
     * using the <code>setList(List)</code> method.  For this class to remain
     * Serializable, the contained List must be a Serializable implementation.
     * Also, and more importantly, the contents of the storage List must be
     * Serializable as well for this class to successfully be serialized.</p>
     *
     * @param list List to use for internal storage
     */
    public ListDataProvider(List list) {
        this.list = list;
    }

    /**
     * @return List used for internal storage
     */
    public List getList() {
        return list;
    }

    /**
     * <p>Sets the List to use for internal storage.</p>
     *
     * <p>NOTE about Serializable:  By default, this class uses an {@link
     * ArrayList} as its internal data storage, which is a Serializable
     * implementation of {@link List}.  The internal storage can be swapped out
     * using the <code>setList(List)</code> method.  For this class to remain
     * Serializable, the contained List must be a Serializable implementation.
     * Also, and more importantly, the contents of the storage List must be
     * Serializable as well for this class to successfully be serialized.</p>
     *
     * @param list List to use for internal storage
     */
    public void setList(List list) {
        this.list = list;
        fireProviderChanged();
    }

    /**
     * <p>NOTE: FieldKey is ignored in this class.</p>
     *
     * {@inheritDoc}
     */
    public Object getValue(FieldKey fieldKey, RowKey row)
        throws DataProviderException {

        if( java.beans.Beans.isDesignTime() && (list == null || list.isEmpty() ) ) {
            // Fill the object with design time fake data
            list = (List)AbstractDataProvider.getFakeData(list.getClass());
        }
        
        return list.get(getRowIndex(row)); // ignoring fieldKey
    }

    /**
     * <p>NOTE: FieldKey is ignored in this class.</p>
     *
     * {@inheritDoc}
     */
    public Class getType(FieldKey fieldKey) throws DataProviderException {
        Object o = getValue(fieldKey);
        return o != null ? o.getClass() : null;
    }

    /**
     * <p>NOTE: FieldKey is ignored in this class.</p>
     *
     * <p>NOTE: This method always returns false, as the storage List can be edited
     * at any row.</p>
     *
     * {@inheritDoc}
     */
    public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException {
        return false;
    }

    /**
     * <p>NOTE: FieldKey is ignored in this class.</p>
     *
     * {@inheritDoc}
     */
    public void setValue(FieldKey fieldKey, RowKey row, Object value)
        throws DataProviderException {

        Object oldValue = getValue(fieldKey, row);
        list.set(getRowIndex(row), value); // ignoring fieldKey
        fireValueChanged(fieldKey, row, oldValue, value);
        if (getRowIndex(row) == getCursorIndex()) {
            fireValueChanged(fieldKey, oldValue, value);
        }
    }

    /** {@inheritDoc} */
    public int getRowCount() throws DataProviderException {
        return list.size();
    }

    /** {@inheritDoc} */
    public boolean isRowAvailable(RowKey row) throws DataProviderException {
        if (row instanceof IndexRowKey) {
            return list.size() > ((IndexRowKey)row).getIndex();
        }
        return false;
    }

    public int getCursorIndex() throws DataProviderException {
        return getRowIndex(getCursorRow());
    }

    public int getRowIndex(RowKey row) throws DataProviderException {
        if (row instanceof IndexRowKey) {
            return ((IndexRowKey)row).getIndex();
        }
        try {
            return Integer.parseInt(row.getRowId());
        } catch (NumberFormatException nfx) {
            // its not an index
        }
        return -1;
    }

    /**
     * <p>NOTE: This implementation always returns <code>false</code> from this
     * method.  To resize the data provider, access the List directly.</p>
     *
     * {@inheritDoc}
     */
    public boolean canInsertRow(RowKey beforeRow) throws DataProviderException {
        return false;
    }

    /** {@inheritDoc} */
    public RowKey insertRow(RowKey beforeRow) throws DataProviderException {
        return null;
    }

    /**
     * <p>NOTE: This implementation always returns <code>false</code> from this
     * method.  To resize the data provider, access the List directly.</p>
     *
     * {@inheritDoc}
     */
    public boolean canAppendRow() throws DataProviderException {
        return false;
    }

    /** {@inheritDoc} */
    public RowKey appendRow() throws DataProviderException {
        return null;
    }

    /**
     * <p>NOTE: This implementation always returns <code>true</code> from this
     * method.</p>
     *
     * {@inheritDoc}
     */
    public boolean canRemoveRow(RowKey row) throws DataProviderException {
        return true;
    }

    /** {@inheritDoc} */
    public void removeRow(RowKey row) throws DataProviderException {
        list.remove(row);
    }
}
