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
