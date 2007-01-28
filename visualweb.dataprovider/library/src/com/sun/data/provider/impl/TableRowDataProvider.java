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

package com.sun.data.provider.impl;

import java.io.Serializable;
import com.sun.data.provider.DataProvider;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableDataListener;
import com.sun.data.provider.TableDataProvider;

/**
 * The TableRowDataProvider class provides a single-row {@link DataProvider}
 * view of a {@link TableDataProvider}.  Set the 'tableDataProvider' and
 * 'tableRow' properties to create a single-row view of the table.
 *
 * @author Joe Nuxoll
 */
public class TableRowDataProvider extends AbstractDataProvider {

    /**
     * Constructs a TableRowDataProvider with no tableDataProvider or tableRow
     * setting.
     */
    public TableRowDataProvider() {}

    /**
     * Constructs a TableRowDataProvider with the specified tableDataProvider
     *
     * @param provider TableDataProvider
     */
    public TableRowDataProvider(TableDataProvider provider) {
        setTableDataProvider(provider);
    }

    /**
     * Constructs a TableRowDataProvider with the specified tableDataProvider
     * and tableRow.
     *
     * @param provider TableDataProvider
     * @param tableRow RowKey
     */
    public TableRowDataProvider(TableDataProvider provider, RowKey tableRow) {
        setTableDataProvider(provider);
        setTableRow(tableRow);
    }

    /**
     *
     * @param provider TableDataProvider
     */
    public void setTableDataProvider(TableDataProvider provider) {
        if (this.provider != null) {
            this.provider.removeTableDataListener(dataEars);
        }
        this.provider = provider;
        if (this.provider != null) {
            this.provider.addTableDataListener(dataEars);
        }
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
     * @param tableRow int
     */
    public void setTableRow(RowKey tableRow) {
        this.tableRow = tableRow;
        if (provider != null) {
            fireProviderChanged();
        }
    }

    /**
     *
     * @return RowKey
     */
    public RowKey getTableRow() {
        return tableRow;
    }

    /**
     *
     * @return FieldKey[]
     */
    public FieldKey[] getFieldKeys() throws DataProviderException {
        if (provider != null) {
            return provider.getFieldKeys();
        }
        return FieldKey.EMPTY_ARRAY;
    }

    /**
     *
     * @param fieldId String
     * @return FieldKey
     */
    public FieldKey getFieldKey(String fieldId) throws DataProviderException {
        if (provider != null) {
            return provider.getFieldKey(fieldId);
        }
        return null;
    }

    /**
     *
     * @param fieldKey FieldKey
     * @return Class
     */
    public Class getType(FieldKey fieldKey) throws DataProviderException {
        if (provider != null) {
            return provider.getType(fieldKey);
        }
        return null;
    }

    /**
     *
     * @param fieldKey FieldKey
     * @return boolean
     */
    public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException {
        if (provider != null) {
            return provider.isReadOnly(fieldKey);
        }
        return true;
    }

    /**
     *
     * @param fieldKey FieldKey
     * @return Object
     */
    public Object getValue(FieldKey fieldKey) throws DataProviderException {
        if (provider != null) {
            return provider.getValue(fieldKey, tableRow);
        }
        return null;
    }

    /**
     *
     * @param fieldKey FieldKey
     * @param value Object
     */
    public void setValue(FieldKey fieldKey, Object value) throws DataProviderException {
        provider.setValue(fieldKey, tableRow, value);
    }

    private class DataEars implements TableDataListener, Serializable {
        public void valueChanged(TableDataProvider provider,
            FieldKey fieldKey, RowKey row, Object oldValue, Object newValue) {
            if (row == tableRow || (row != null && row.equals(tableRow))) {
                fireValueChanged(fieldKey, oldValue, newValue);
            }
        }
        public void rowAdded(TableDataProvider provider, RowKey row) {
            if (row == tableRow || (row != null && row.equals(tableRow))) {
                fireProviderChanged();
            }
        }
        public void rowRemoved(TableDataProvider provider, RowKey row) {
            if (row == tableRow || (row != null && row.equals(tableRow))) {
                fireProviderChanged();
            }
        }
        public void valueChanged(DataProvider provider, FieldKey fieldKey,
            Object oldValue, Object newValue) {
            RowKey row = TableRowDataProvider.this.provider.getCursorRow();
            if (row == tableRow || (row != null && row.equals(tableRow))) {
                fireValueChanged(fieldKey, oldValue, newValue);
            }
        }
        public void providerChanged(DataProvider provider) {
            fireProviderChanged();
        }
    };

    private DataEars dataEars = new DataEars();
    private TableDataProvider provider;
    private RowKey tableRow;
}
