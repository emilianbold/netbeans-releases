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
import com.sun.data.provider.DataProvider;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableCursorVetoException;
import com.sun.data.provider.TableDataFilter;
import com.sun.data.provider.TableDataListener;
import com.sun.data.provider.TableDataProvider;

/**
 * <p>Specialized <code>TableDataProvider</code> that is filtered by a
 * specified <code>TableDataFilter</code>.</p>
 */
public class FilteredTableDataProvider extends AbstractTableDataProvider {

    // ---------------------------------------------------------- Constructors

    /**
     *
     */
    protected TableDataProvider provider;

    /**
     *
     */
    protected TableDataFilter filter;

    /**
     *
     * @param provider TableDataProvider
     */
    public void setTableDataProvider(TableDataProvider provider) {
        if (this.provider != null) {
            this.provider.removeTableDataListener(dataEars);
        }
        this.provider = provider;
        this.provider.addTableDataListener(dataEars);
        filterMap.reset();
        fireProviderChanged();
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
     * @param filter TableDataFilter
     */
    public void setTableDataFilter(TableDataFilter filter) {
        this.filter = filter;
        filterMap.reset();
        fireProviderChanged();
    }

    /**
     *
     * @return TableDataFilter
     */
    public TableDataFilter getTableDataFilter() {
        return filter;
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
            return 0;
        }
        return filterMap.getRowCount();
    }

    /** {@inheritDoc} */
    public RowKey[] getRowKeys(int count, RowKey afterRow) throws DataProviderException {
        if (provider == null) {
            return RowKey.EMPTY_ARRAY;
        }
        return filterMap.getRowKeys(count, afterRow);
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
        return filterMap.isIncluded(row) && provider.isRowAvailable(row);
    }

    /** {@inheritDoc} */
    public Object getValue(FieldKey fieldKey, RowKey row) throws DataProviderException {
        if (provider == null) {
            return null;
        }
        return provider.getValue(fieldKey, row);
    }

    /** {@inheritDoc} */
    public void setValue(FieldKey fieldKey, RowKey row, Object value) throws DataProviderException {
        if (provider == null) {
            return;
        }
        filterMap.reset();
        provider.setValue(fieldKey, row, value);
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
        filterMap.reset();
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
        filterMap.reset();
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
            filterMap.reset();
            provider.removeRow(row);
        }
    }

    // ---------------------------------------------------------- Cursor Methods

    /** {@inheritDoc} */
    public boolean cursorFirst() throws DataProviderException {
        RowKey first = filterMap.findFirst();
        if (first != null) {
            try {
                setCursorRow(first);
                return true;
            }
            catch (TableCursorVetoException x) {
                return false;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean cursorPrevious() throws DataProviderException {
        RowKey cursor = getCursorRow();
        RowKey previous = cursor != null ? filterMap.findPrevious(cursor) : filterMap.findFirst();
        if (previous != null) {
            try {
                setCursorRow(previous);
                return true;
            }
            catch (TableCursorVetoException x) {
                return false;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean cursorNext() throws DataProviderException {
        RowKey cursor = getCursorRow();
        RowKey next = cursor != null ? filterMap.findNext(cursor) : filterMap.findLast();
        if (next != null) {
            try {
                setCursorRow(next);
                return true;
            }
            catch (TableCursorVetoException x) {
                return false;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean cursorLast() throws DataProviderException {
        RowKey last = filterMap.findLast();
        if (last != null) {
            try {
                setCursorRow(last);
                return true;
            }
            catch (TableCursorVetoException x) {
                return false;
            }
        }
        return false;
    }

    // --------------------------------------------------------- Event Listeners

    /**
     *
     */
    private class DataEars implements TableDataListener, Serializable {
        public void valueChanged(TableDataProvider provider,
            FieldKey fieldKey, RowKey row, Object oldValue, Object newValue){
            filterMap.reset();
            fireValueChanged(fieldKey, row, oldValue, newValue);
        }
        public void rowAdded(TableDataProvider provider, RowKey row) {
            filterMap.reset();
            fireRowAdded(row);
        }
        public void rowRemoved(TableDataProvider provider, RowKey row) {
            filterMap.reset();
            fireRowRemoved(row);
        }
        public void valueChanged(DataProvider provider, FieldKey fieldKey,
            Object oldValue, Object newValue) {
            filterMap.reset();
            fireValueChanged(fieldKey, oldValue, newValue);
        }
        public void providerChanged(DataProvider provider) {
            filterMap.reset();
            fireProviderChanged();
        }
    };

    private DataEars dataEars = new DataEars();
    private FilterMap filterMap = new FilterMap();

    private class FilterMap implements Serializable {

        public int getRowCount() {
            if (filterRows == null) {
                scanAndFilterRows();
            }
            return filterRows.size();
        }

        private void scanAndFilterRows() {
            allRows = new ArrayList();
            filterRows = new ArrayList();
            if (provider == null) {
                return;
            }
            int count = provider.getRowCount();
            if (count < 0) {
                count = 10000; // arbitrary large number
            }
            RowKey[] rows = provider.getRowKeys(count, null);
            for (int i = 0; i < rows.length; i++) {
                allRows.add(rows[i]);
            }
            RowKey[] frows = filter.filter(provider, rows);
            for (int i = 0; i < frows.length; i++) {
                filterRows.add(frows[i]);
            }
            // fix up cursor position
            boolean cursorOk = false;
            boolean altCursorFound = false;
            RowKey cursor = getCursorRow();
            if (filterRows.contains(cursor)) {
                cursorOk = true;
            } else if (allRows.contains(cursor)) { // check if filtered out
                // move it to the next available row
                int index = allRows.indexOf(cursor);
                for (int i = index + 1; i < allRows.size(); i++) {
                    if (filterRows.contains(allRows.get(i))) {
                        cursor = (RowKey)allRows.get(i);
                        altCursorFound = true;
                        break;
                    }
                }
            }
            if (!cursorOk) {
                if (!altCursorFound) {
                    //time to ensure an alternative cursor is found!
                    cursor = findFirst();
                }
                try {
                    if (cursor != null) {
                        setCursorRow(cursor);
                    }
                } catch (TableCursorVetoException x) {
                    // we tried :-(
                }
            }
        }

        public boolean isIncluded(RowKey row) {
            if (filter == null || provider == null) {
                return true;
            }
            if (filterRows == null) {
                scanAndFilterRows();
            }
            return filterRows.contains(row);
        }

        public RowKey[] getRowKeys(int count, RowKey afterRow) {
            if (filterRows == null) {
                scanAndFilterRows();
            }
            int startIndex = 0;
            if (afterRow != null) {
                for (int i = 0; i < filterRows.size(); i++) {
                    if (afterRow.equals(filterRows.get(i))) {
                        startIndex = i + 1;
                        break;
                    }
                }
            }
            ArrayList rows = new ArrayList();
            for (int i = startIndex; i < count; i++) {
                if (i < filterRows.size()) {
                    rows.add(filterRows.get(i));
                }
            }
            return (RowKey[])rows.toArray(new RowKey[rows.size()]);
        }

        public RowKey findFirst() {
            if (filterRows == null) {
                scanAndFilterRows();
            }
            if (filterRows.size() > 0) {
                return (RowKey)filterRows.get(0);
            }
            return null;
        }

        public RowKey findPrevious(RowKey row) {
            if (filterRows == null) {
                scanAndFilterRows();
            }
            int index = filterRows.indexOf(row);
            if (index > 0) {
                return (RowKey)filterRows.get(index - 1);
            } else if (index < 0) {
                // check if this row was filtered out
                index = allRows.indexOf(row);
                if (index > -1) {
                    for (int i = index - 1; i >= 0; i--) {
                        if (filterRows.contains(allRows.get(i))) {
                            return (RowKey)allRows.get(i);
                        }
                    }
                }
            }
            return findFirst();
        }

        public RowKey findNext(RowKey row) {
            if (filterRows == null) {
                scanAndFilterRows();
            }
            int index = filterRows.indexOf(row);
            if (index >= 0 && (index + 1) < filterRows.size()) {
                return (RowKey)filterRows.get(index + 1);
            } else if (index < 0) {
                // check if this row was filtered out
                index = allRows.indexOf(row);
                if (index > -1) {
                    for (int i = index + 1; i < allRows.size(); i++) {
                        if (filterRows.contains(allRows.get(i))) {
                            return (RowKey)allRows.get(i);
                        }
                    }
                }
            }
            return findLast();
        }

        public RowKey findLast() {
            if (filterRows == null) {
                scanAndFilterRows();
            }
            if (filterRows.size() > 0) {
                return (RowKey)filterRows.get(filterRows.size() - 1);
            }
            return null;
        }

        public void reset() {
            allRows = null;
            filterRows = null;
        }

        private ArrayList allRows = null;
        private ArrayList filterRows = null;
    }
}
