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
import java.util.HashMap;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableCursorListener;
import com.sun.data.provider.TableCursorVetoException;
import com.sun.data.provider.TableDataListener;
import com.sun.data.provider.TableDataProvider;
import java.util.ResourceBundle;

/**
 * Abstract base implementation of {@link TableDataProvider}.  This class is a
 * convenient base class to use when creating a new {@link TableDataProvider}
 * implementation.
 *
 * @author Joe Nuxoll
 *         Winston Prakash (Buf Fixes and clean up) 
 */
public abstract class AbstractTableDataProvider
    extends AbstractDataProvider implements TableDataProvider {

    // -------------------------------------------------------- Abstract Methods

    /** {@inheritDoc} */
    abstract public Class getType(FieldKey fieldKey) throws DataProviderException;

    /** {@inheritDoc} */
    abstract public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException;

    /** {@inheritDoc} */
    abstract public int getRowCount() throws DataProviderException;

    /** {@inheritDoc} */
    abstract public Object getValue(FieldKey fieldKey, RowKey row) throws DataProviderException;

    /** {@inheritDoc} */
    abstract public void setValue(FieldKey fieldKey, RowKey row, Object value) throws DataProviderException;

    /** {@inheritDoc} */
    abstract public boolean canInsertRow(RowKey beforeRow) throws DataProviderException;

    /** {@inheritDoc} */
    abstract public RowKey insertRow(RowKey beforeRow) throws DataProviderException;

    /** {@inheritDoc} */
    abstract public boolean canAppendRow() throws DataProviderException;

    /** {@inheritDoc} */
    abstract public RowKey appendRow() throws DataProviderException;

    /** {@inheritDoc} */
    abstract public boolean canRemoveRow(RowKey row) throws DataProviderException;

    /** {@inheritDoc} */
    abstract public void removeRow(RowKey row) throws DataProviderException;


    // -------------------------------------------------- ResourceBundle Methods


    private transient ResourceBundle bundle = null;


    /**
     * <p>Return the resource bundle containing our localized messages.</p>
     */
    private ResourceBundle getBundle() {

        if (bundle == null) {
            bundle = ResourceBundle.getBundle("com/sun/data/provider/impl/Bundle");
        }
        return bundle;

    }


    // ---------------------------------------------------------- RowKey Methods

    protected ArrayList rowKeyList = new ArrayList();
    protected HashMap rowKeyMap = new HashMap();

    /** {@inheritDoc} */
    public RowKey[] getRowKeys(int count, RowKey afterRow) throws DataProviderException {
        int startIndex = 0;
        if (afterRow instanceof IndexRowKey) {
            startIndex = ((IndexRowKey)afterRow).getIndex() + 1;
        }
        int sz = count;
        int rowCount = getRowCount();
        if (rowCount > 0) {
            sz = (count > (rowCount - startIndex) ? (rowCount - startIndex) : count);
        }
        RowKey[] rkeys = new RowKey[(rowCount == -1)? 0: sz];
        for (int i = 0; i < rkeys.length; i++) {
            rkeys[i] = new IndexRowKey(startIndex + i);
        }
        return rkeys;
    }

    public RowKey getRowKey(String rowId) throws DataProviderException {
        return IndexRowKey.create(rowId);
    }

    /** {@inheritDoc} */
    public boolean isRowAvailable(RowKey row) throws DataProviderException {
        if (row instanceof IndexRowKey) {
            IndexRowKey indexRowKey = (IndexRowKey) row;
            if(indexRowKey.getIndex() < 0) {
                return false;
            }
            return getRowCount() > ((IndexRowKey)row).getIndex();
        }
        return false;
    }

    // ----------------------------------------------------- Convenience Methods

    /** @see #getValue(FieldKey, RowKey) */
    public Object getValue(String fieldId, RowKey row) throws DataProviderException {
        return getValue(getFieldKey(fieldId), row);
    }

    /** @see #setValue(FieldKey, RowKey, Object) */
    public void setValue(String fieldId, RowKey row, Object value) throws DataProviderException {
        setValue(getFieldKey(fieldId), row, value);
    }

    /** @see #findFirst(FieldKey, Object) */
    public RowKey findFirst(String fieldId, Object value) throws DataProviderException {
        return findFirst(getFieldKey(fieldId), value);
    }

    /** @see #findFirst(FieldKey[], Object[]) */
    public RowKey findFirst(String[] fieldIds, Object[] values) throws DataProviderException {
        FieldKey[] fieldKeys = new FieldKey[fieldIds.length];
        for (int i = 0; i < fieldIds.length; i++) {
            fieldKeys[i] = getFieldKey(fieldIds[i]);
        }
        return findFirst(fieldKeys, values);
    }

    /** @see #findAll(FieldKey, Object) */
    public RowKey[] findAll(String fieldId, Object value) throws DataProviderException {
        return findAll(getFieldKey(fieldId), value);
    }

    /** @see #findAll(FieldKey[], Object[]) */
    public RowKey[] findAll(String[] fieldIds, Object[] values) throws DataProviderException {
        FieldKey[] fieldKeys = new FieldKey[fieldIds.length];
        for (int i = 0; i < fieldIds.length; i++) {
            fieldKeys[i] = getFieldKey(fieldIds[i]);
        }
        return findAll(fieldKeys, values);
    }

    /**
     * Returns all the RowKeys, which may force the underlying dataprovider to
     * go and perform an expensive operation to fetch them.
     *
     * @return RowKey[] All of the row keys in this TableDataProvider
     */
    public RowKey[] getAllRows() throws DataProviderException {
        RowKey[] rowKeys = null;

        // It's possible that the provider returned -1 because it does not
        // actually have all the rows, so it's up to the consumer of the
        // interface to fetch them. Typically, 99% of the data providers will
        // return a valid row count (at least our providers will), but we still
        // need to handle the scenario where -1 is returned.
        int rowCount = getRowCount();
        if (rowCount == -1) {
            int index = 0;
            do {
                // Keep trying until all rows are obtained.
                rowCount = 1000000 * ++index;
                rowKeys = getRowKeys(rowCount, null);
            } while (rowKeys != null && rowKeys.length - 1 == rowCount);
        } else {
            rowKeys = getRowKeys(rowCount, null);
        }
        return rowKeys;
    }

    /**
     * Finds the first row with the specified value stored under the specified
     * field key.
     *
     * @param fieldKey FieldKey
     * @param value Object
     * @return RowKey
     */
    public RowKey findFirst(FieldKey fieldKey, Object value) throws DataProviderException {
        RowKey[] rows = getRowKeys(10, null);
        while (rows.length > 0){
            for (int i = 0; i < rows.length; i++) {
                Object o = getValue(fieldKey, rows[i]);
                if (o == value || (o != null && o.equals(value))) {
                    return rows[i];
                }
            }
            rows = getRowKeys(10, rows[rows.length - 1]);
        }

        return null;
    }

    /**
     * Finds the first row with the specified values stored under the specified
     * field keys.
     *
     * @param fieldKeys FieldKey[]
     * @param values Object[]
     * @return RowKey
     */
    public RowKey findFirst(FieldKey[] fieldKeys, Object[] values) throws DataProviderException {
        RowKey[] rows = getRowKeys(10, null);
        while (rows.length > 0) {
            for (int i = 0; i < rows.length; i++) {
                boolean match = false;
                for (int j = 0; j < fieldKeys.length; j++) {
                    Object o = getValue(fieldKeys[j], rows[i]);
                    match = (o == values[j] || (o != null && o.equals(values[j])));
                    if (!match) {
                        break;
                    }
                }
                if (match) {
                    return rows[i];
                }
            }
            rows = getRowKeys(10, rows[rows.length - 1]);
        }

        return null;
    }

    /**
     * Finds all rows with the specified value stored under the specified field
     * key.
     *
     * @param fieldKey FieldKey
     * @param value Object
     * @return RowKey[]
     */
    public RowKey[] findAll(FieldKey fieldKey, Object value) throws DataProviderException {
        RowKey[] rows = getAllRows();
        ArrayList matches = new ArrayList();
        for (int i = 0; i < rows.length; i++) {
            Object o = getValue(fieldKey, rows[i]);
            if (o == value || (o != null && o.equals(value))) {
                matches.add(rows[i]);
            }
        }
        return (RowKey[])matches.toArray(new RowKey[matches.size()]);
    }

    /**
     * Finds all rows with the specified values stored under the specified field
     * keys.
     *
     * @param fieldKeys FieldKey[]
     * @param values Object[]
     * @return RowKey[]
     */
    public RowKey[] findAll(FieldKey[] fieldKeys, Object[] values) throws DataProviderException {
        RowKey[] rows = getAllRows();
        ArrayList matches = new ArrayList();
        for (int i = 0; i < rows.length; i++) {
            boolean match = false;
            for (int j = 0; j < fieldKeys.length; j++) {
                Object o = getValue(fieldKeys[j], rows[i]);
                match = (o == values[j] || (o != null && o.equals(values[j])));
                if (!match) {
                    break;
                }
            }
            if (match) {
                matches.add(rows[i]);
            }
        }
        return (RowKey[])matches.toArray(new RowKey[matches.size()]);
    }

    // ---------------------------------------------------------- Cursor Methods

    /**
     * storage for the current cursor row
     */
    protected RowKey cursorRow = new IndexRowKey(0);

    protected int getCursorIndex() {
        if (cursorRow instanceof IndexRowKey) {
            return ((IndexRowKey)cursorRow).getIndex();
        }
        return -1;
    }

    protected boolean setCursorIndex(int index) {
        try {
            setCursorRow(new IndexRowKey(index));
            return true;
        } catch (TableCursorVetoException tcvx) {
            return false;
        }
    }

    /** {@inheritDoc} */
    public RowKey getCursorRow() throws DataProviderException {
        return cursorRow;
    }

    /** {@inheritDoc} */
    public void setCursorRow(RowKey row) throws TableCursorVetoException {
        if (!isRowAvailable(row)) {
            throw new IllegalArgumentException(getBundle().getString("ROW_NOT_AVAILABLE"));
        }
        RowKey oldRow = this.cursorRow;
        fireCursorChanging(oldRow, row);
        this.cursorRow = row;
        fireCursorChanged(oldRow, cursorRow);
    }

    /** {@inheritDoc} */
    public boolean cursorFirst() throws DataProviderException {
        try {
            return setCursorIndex(0);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** {@inheritDoc} */
    public boolean cursorPrevious() throws DataProviderException {
        try {
            return setCursorIndex(getCursorIndex() - 1);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** {@inheritDoc} */
    public boolean cursorNext() throws DataProviderException {
        try {
            return setCursorIndex(getCursorIndex() + 1);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** {@inheritDoc} */
    public boolean cursorLast() throws DataProviderException {
        try {
            return setCursorIndex(getRowCount() - 1);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // ---------------------------------------------------- DataProvider Methods

    /** {@inheritDoc} */
    public Object getValue(FieldKey fieldKey) throws DataProviderException {
        return getValue(fieldKey, getCursorRow());
    }

    /** {@inheritDoc} */
    public void setValue(FieldKey fieldKey, Object value) throws DataProviderException {
        setValue(fieldKey, getCursorRow(), value);
    }

    // ----------------------------------------------------------- Event Methods

    /** {@inheritDoc} */
    public void addTableDataListener(TableDataListener l) {
        super.addDataListener(l);
    }

    /** {@inheritDoc} */
    public void removeTableDataListener(TableDataListener l) {
        super.removeDataListener(l);
    }

    /** {@inheritDoc} */
    public TableDataListener[] getTableDataListeners() {
        if (dpListeners == null) {
            return new TableDataListener[0];
        } else {
            ArrayList tdpList = new ArrayList();
            for (int i = 0; i < dpListeners.length; i++) {
                if (dpListeners[i] instanceof TableDataListener) {
                    tdpList.add(dpListeners[i]);
                }
            }
            return (TableDataListener[])tdpList.toArray(
                new TableDataListener[tdpList.size()]);
        }
    }

    /**
     * <p>Array of {@link TableCursorListener} instances registered for
     * this {@link TableDataProvider}.</p>
     */
    protected TableCursorListener tcListeners[] = null;

    /** {@inheritDoc} */
    public void addTableCursorListener(TableCursorListener listener) {
        if (tcListeners == null) {
            tcListeners = new TableCursorListener[1];
            tcListeners[0] = listener;
            return;
        }
        TableCursorListener results[] = new TableCursorListener[tcListeners.length + 1];
        System.arraycopy(tcListeners, 0, results, 0, tcListeners.length);
        results[results.length - 1] = listener;
        tcListeners = results;
    }

    /** {@inheritDoc} */
    public void removeTableCursorListener(TableCursorListener listener) {
        if (tcListeners == null) {
            return;
        }
        ArrayList list = new ArrayList(tcListeners.length);
        for (int i = 0; i < tcListeners.length; i++) {
            if (tcListeners[i] != listener) {
                list.add(tcListeners[i]);
            }
        }
        tcListeners =
          (TableCursorListener[]) list.toArray(new TableCursorListener[list.size()]);
    }

    /** {@inheritDoc} */
    public TableCursorListener[] getTableCursorListeners() {
        if (tcListeners == null) {
            return new TableCursorListener[0];
        } else {
            return tcListeners;
        }
    }

    /**
     * Fires a valueChanged event to all registered
     * {@link TableDataListener}s
     *
     * @param row The row of the value change
     * @param fieldKey The FieldKey of the value change
     * @param oldValue The old value (before the value change)
     * @param newValue The new value (after the value change)
     */
    protected void fireValueChanged(FieldKey fieldKey, RowKey row, Object oldValue, Object newValue) {
        TableDataListener[] tdls = getTableDataListeners();
        for (int i = 0; i < tdls.length; i++) {
            tdls[i].valueChanged(this, fieldKey, row, oldValue, newValue);
        }
    }

    /**
     * Fires a rowAdded event to all registered
     * {@link TableDataListener}s
     *
     * @param newRow The newly added row
     */
    protected void fireRowAdded(RowKey newRow) {
        TableDataListener[] tdls = getTableDataListeners();
        for (int i = 0; i < tdls.length; i++) {
            tdls[i].rowAdded(this, newRow);
        }
    }

    /**
     * Fires a rowRemoved event to all registered
     * {@link TableDataListener}s
     *
     * @param oldRow The removed row
     */
    protected void fireRowRemoved(RowKey oldRow) {
        TableDataListener[] tdls = getTableDataListeners();
        for (int i = 0; i < tdls.length; i++) {
            tdls[i].rowRemoved(this, oldRow);
        }
    }

    /**
     * Fires a cursorChanging event to all registered
     * {@link TableCursorListener}s.  If a TableCursorVetoException is thrown by any
     * listeners, the cursor will not be changed.
     *
     * @param oldRow The old cursor row (before the cursor change)
     * @param newRow The new cursor row (after the cursor change)
     * @throws TableCursorVetoException This method may throw a
     *         TableCursorVetoException if one of registered listeners throws
     *         an exception.  This will prevent the cursor move from occurring.
     */
    protected void fireCursorChanging(RowKey oldRow, RowKey newRow)
        throws TableCursorVetoException {

        TableCursorListener[] tcls = getTableCursorListeners();
        for (int i = 0; i < tcls.length; i++) {
            tcls[i].cursorChanging(this, oldRow, newRow);
        }
    }

    /**
     * Fires a cursorChanged event to all registered
     * {@link TableCursorListener}s
     *
     * @param oldRow The old cursor row (before the cursor change)
     * @param newRow The new cursor row (after the cursor change)
     */
    protected void fireCursorChanged(RowKey oldRow, RowKey newRow) {
        TableCursorListener[] tcls = getTableCursorListeners();
        for (int i = 0; i < tcls.length; i++) {
            tcls[i].cursorChanged(this, oldRow, newRow);
        }
    }
}
