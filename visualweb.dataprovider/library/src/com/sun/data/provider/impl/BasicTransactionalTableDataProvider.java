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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import com.sun.data.provider.DataProvider;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableCursorListener;
import com.sun.data.provider.TableCursorVetoException;
import com.sun.data.provider.TableDataListener;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.TransactionalDataListener;
import com.sun.data.provider.TransactionalDataProvider;
import java.util.ResourceBundle;

/**
 *
 * @author Joe Nuxoll
 *         Winston Prakash (Buf Fixes and clean up)
 */
public class BasicTransactionalTableDataProvider
    extends AbstractTableDataProvider implements TransactionalDataProvider {
    private transient ResourceBundle bundle = null;
    /**
     *
     */
    protected TableDataProvider provider;

    /**
     *
     * @param provider TableDataProvider
     */
    public void setTableDataProvider(TableDataProvider provider) {
        if (this.provider != null) {
            this.provider.removeTableDataListener(dataEars);
            this.provider.removeTableCursorListener(cursorEars);
        }
        this.provider = provider;
        this.provider.addTableDataListener(dataEars);
        this.provider.addTableCursorListener(cursorEars);
        changedRowMap.clear();
        fireProviderChanged();
    }

     /**
     * <p>Return the resource bundle containing our localized messages.</p>
     */
    private ResourceBundle getBundle() {

        if (bundle == null) {
            bundle = ResourceBundle.getBundle("com/sun/data/provider/impl/Bundle");
        }
        return bundle;

    }

    /**
     *
     * @return TableDataProvider
     */
    public TableDataProvider getTableDataProvider() {
        return provider;
    }

    /**
     *
     * @return FieldKey[]
     */
    public FieldKey[] getFieldKeys() throws DataProviderException {
        if (provider == null) {
            return FieldKey.EMPTY_ARRAY;
        }
        return provider.getFieldKeys();
    }

    /**
     *
     * @param fieldId String
     * @return FieldKey
     */
    public FieldKey getFieldKey(String fieldId) throws DataProviderException {
        if (provider == null) {
            return null;
        }
        return provider.getFieldKey(fieldId);
    }

    /** {@inheritDoc} */
    public Class getType(FieldKey fieldKey) throws DataProviderException {
        if (provider == null) {
            return null;
        }
        return provider.getType(fieldKey);
    }

    /** {@inheritDoc} */
    public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException {
        if (provider == null) {
            return true;
        }
        return provider.isReadOnly(fieldKey);
    }

    /** {@inheritDoc} */
    public int getRowCount() throws DataProviderException {
        if (provider == null) {
            return -1;
        }
        return provider.getRowCount();
    }

    /** {@inheritDoc} */
    public RowKey[] getRowKeys(int count, RowKey afterRow) throws DataProviderException {
        if (provider == null) {
            return RowKey.EMPTY_ARRAY;
        }
        return provider.getRowKeys(count, afterRow);
    }

    /** {@inheritDoc} */
    public RowKey getRowKey(String rowId) throws DataProviderException {
        if (provider == null) {
            return null;
        }
        return provider.getRowKey(rowId);
    }

    /** {@inheritDoc} */
    public boolean isRowAvailable(RowKey row) throws DataProviderException {
        if (provider == null) {
            return false;
        }
        return provider.isRowAvailable(row);
    }

    /** {@inheritDoc} */
    public Object getValue(FieldKey fieldKey, RowKey row) throws DataProviderException {
        if (provider == null) {
            return null;
        }
        if (changedRowMap.containsKey(row)) {
            HashMap changeMap = (HashMap)changedRowMap.get(row);
            if (changeMap.containsKey(fieldKey)) {
                return changeMap.get(fieldKey);
            }
        }
        return provider.getValue(fieldKey, row);
    }

    /** {@inheritDoc} */
    public void setValue(FieldKey fieldKey, RowKey row, Object value) throws DataProviderException {
        if (provider == null) {
            return;
        }
        if (isReadOnly(fieldKey)) {
            throw new DataProviderException(getBundle().getString("FK_READ_ONLY"));
        }
        HashMap changeMap = null;
        if (changedRowMap.containsKey(row)) {
            changeMap = (HashMap)changedRowMap.get(row);
        } else {
            changeMap = new HashMap();
            changedRowMap.put(row, changeMap);
        }
        Object oldValue = getValue(fieldKey, row);
        changeMap.put(fieldKey, value);
        fireValueChanged(fieldKey, row, oldValue, value);
        if (getCursorRow() == row) {
            fireValueChanged(fieldKey, oldValue, value);
        }
    }

    /** {@inheritDoc} */
    public boolean canInsertRow(RowKey beforeRow) throws DataProviderException {
        if (provider == null) {
            return false;
        }
        return provider.canInsertRow(beforeRow);
    }

    /** {@inheritDoc} */
    public RowKey insertRow(RowKey beforeRow) throws DataProviderException {
        if (provider == null) {
            return null;
        }
        return provider.insertRow(beforeRow);
    }

    /** {@inheritDoc} */
    public boolean canAppendRow() throws DataProviderException {
        if (provider == null) {
            return false;
        }
        return provider.canAppendRow();
    }

    /** {@inheritDoc} */
    public RowKey appendRow() throws DataProviderException {
        if (provider == null) {
            return null;
        }
        return provider.appendRow();
    }

    /** {@inheritDoc} */
    public boolean canRemoveRow(RowKey row) throws DataProviderException {
        if (provider == null) {
            return false;
        }
        return provider.canRemoveRow(row);
    }

    /** {@inheritDoc} */
    public void removeRow(RowKey row) throws DataProviderException {
        if (provider != null) {
            provider.removeRow(row);
        }
    }

    /**
     *
     */
    protected HashMap changedRowMap = new HashMap();

    /**
     *
     */
    public void commitChanges() throws DataProviderException {
        Iterator rowKeys = changedRowMap.keySet().iterator();
        while (rowKeys.hasNext()) {
            RowKey rowKey = (RowKey)rowKeys.next();
            HashMap changeMap = (HashMap)changedRowMap.get(rowKey);
            Iterator changeKeys = changeMap.keySet().iterator();
            while (changeKeys.hasNext()) {
                FieldKey fieldKey = (FieldKey)changeKeys.next();
                provider.setValue(fieldKey, rowKey, changeMap.get(fieldKey));
            }
        }
        changedRowMap.clear();
        fireChangesCommitted();
    }

    /**
     *
     */
    public void revertChanges() throws DataProviderException {
        changedRowMap.clear();
        fireChangesReverted();
        fireProviderChanged();
    }

    // ----------------------------------------------------------- Event Methods

    /** {@inheritDoc} */
    public void addTransactionalDataListener(TransactionalDataListener l) {
        super.addDataListener(l);
    }

    /** {@inheritDoc} */
    public void removeTransactionalDataListener(TransactionalDataListener l) {
        super.removeDataListener(l);
    }

    /** {@inheritDoc} */
    public TransactionalDataListener[] getTransactionalDataListeners() {
        if (dpListeners == null) {
            return new TransactionalDataListener[0];
        } else {
            ArrayList cdpList = new ArrayList();
            for (int i = 0; i < dpListeners.length; i++) {
                if (dpListeners[i] instanceof TransactionalDataListener) {
                    cdpList.add(dpListeners[i]);
                }
            }
            return (TransactionalDataListener[])cdpList.toArray(
                new TransactionalDataListener[cdpList.size()]);
        }
    }

    /**
     * Fires a changesCommtted event to each registered {@link
     * TransactionalDataListener}
     *
     * @see TransactionalDataListener#changesCommitted(TransactionalDataProvider)
     */
    protected void fireChangesCommitted() {
        TransactionalDataListener[] cdpls = getTransactionalDataListeners();
        for (int i = 0; i < cdpls.length; i++) {
            cdpls[i].changesCommitted(this);
        }
    }

    /**
     * Fires a changesReverted event to each registered {@link
     * TransactionalDataListener}
     *
     * @see TransactionalDataListener#changesReverted(TransactionalDataProvider)
     */
    protected void fireChangesReverted() {
        TransactionalDataListener[] cdpls = getTransactionalDataListeners();
        for (int i = 0; i < cdpls.length; i++) {
            cdpls[i].changesReverted(this);
        }
    }

    /**
     *
     */
    private class DataEars implements TableDataListener, Serializable {
        public void valueChanged(TableDataProvider provider,
            FieldKey fieldKey, RowKey row, Object oldValue, Object newValue){
            fireValueChanged(fieldKey, row, oldValue, newValue);
        }
        public void rowAdded(TableDataProvider provider, RowKey row) {
            fireRowAdded(row);
        }
        public void rowRemoved(TableDataProvider provider, RowKey row) {
            fireRowRemoved(row);
        }
        public void valueChanged(DataProvider provider, FieldKey fieldKey,
            Object oldValue, Object newValue) {
            fireValueChanged(fieldKey, oldValue, newValue);
        }
        public void providerChanged(DataProvider provider) {
            fireProviderChanged();
        }
    };

    private class CursorEars implements TableCursorListener, Serializable {
        public void cursorChanging(TableDataProvider provider,
            RowKey oldRow, RowKey newRow) throws TableCursorVetoException {
            fireCursorChanging(oldRow, newRow);
        }
        public void cursorChanged(TableDataProvider provider,
            RowKey oldRow, RowKey newRow) {
            fireCursorChanged(oldRow, newRow);
        }
    };

    private DataEars dataEars = new DataEars();
    private CursorEars cursorEars = new CursorEars();
}
