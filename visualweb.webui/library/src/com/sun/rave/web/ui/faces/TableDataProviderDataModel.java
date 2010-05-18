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
package com.sun.rave.web.ui.faces;

import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableDataProvider;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.faces.FacesException;
import javax.faces.model.DataModel;
import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;

/**
 * <p><code>DataModel</code> implementation that wraps a specified
 * {@link TableDataProvider} with the standard JavaServer Faces API.
 * Note that setting the <code>rowIndex</code> property of this
 * <code>DataModel</code> does <strong>NOT</strong> cause the cursor of
 * the wrapped {@link TableDataProvider} to be repositioned.</p>
 */
public class TableDataProviderDataModel extends DataModel {

    
    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct an unitialized {@link TableDataProviderDataModel}.</p>
     */
    public TableDataProviderDataModel() {
        this(null);
    }


    /**
     * <p>Construct an {@link TableDataProviderDataModel} that wraps the
     * specified {@link TableDataProvider}.</p>
     */
    public TableDataProviderDataModel(TableDataProvider tdp) {
        setTableDataProvider(tdp);
    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The set of {@link FieldKey}s for the currently wrapped
     * {@link TableDataProvider}.</p>
     */
    private FieldKey fieldKeys[] = null;


    /**
     * <p>The row index to which this <code>DataModel</code>
     * is positioned.</p>
     */
    private int rowIndex = -1;


    // -------------------------------------------------------------- Properties


    /**
     * <p>The {@link TableDataProvider} that we are wrapping.</p>
     */
    private TableDataProvider tdp = null;


    /**
     * <p>Return the {@link TableDataProvider} we are wrapping.</p>
     */
    public TableDataProvider getTableDataProvider() { return this.tdp; }


    /**
     * <p>Set the {@link TableDataProvider} we are wrapping.</p>
     *
     * @param tdp The {@link TableDataProvider} to be wraapped
     */
    public void setTableDataProvider(TableDataProvider tdp) {
        this.tdp = tdp;
        if (tdp == null) {
            this.fieldKeys = null;
            this.rowIndex = -1;
        } else {
            this.fieldKeys = tdp.getFieldKeys();
        }
    }


    // ---------------------------------------------------- DataModel Properties


    /**
     * <p>Return <code>true</code> if the wrapped {@link TableDataProvider}
     * has an available row at the currently specified <code>rowIndex</code>.</p>
     */
    public boolean isRowAvailable() {
        if (getTableDataProvider() == null) {
            return false;
        }
        return getTableDataProvider().isRowAvailable(getRowKey());
    }


    /**
     * <p>Return the number of rows available in the wrapped
     * {@link TableDataProvider}, or <code>-1</code> if unknown.</p>
     */
    public int getRowCount() {
        if (getTableDataProvider() == null) {
            return -1;
        }
        return getTableDataProvider().getRowCount();
    }


    /**
     * <p>Return a <code>Map</code> representing the data elements in the
     * current row, keyed by the canonical identifier for each element.
     * Any call to <code>get()</code> or <code>put()</code> operations on
     * this <code>Map</code> will be delegated to corresponding
     * <code>getValue()</code> and <code>setValue()</code> calls on the
     * wrapped {@link TableDataProvider}.  Operations that attempt to add,
     * delete, or replace keys will be rejected.</p>
     */
    public Object getRowData() {
        if (getTableDataProvider() == null) {
            return null;
        }
        if (!getTableDataProvider().isRowAvailable(getRowKey())) {
            throw new IllegalArgumentException("" + getRowIndex());
        }
        Map map = new TableDataProviderMap();
        for (int i = 0; i < fieldKeys.length; i++) {
            map.put(fieldKeys[i].getFieldId(), tdp.getValue(fieldKeys[i], getRowKey()));
        }
        return map;
    }


    /**
     * <p>Return the currently selected <code>rowIndex</code>, or -1 for
     * no current position.</p>
     */
    public int getRowIndex() {
        return this.rowIndex;
    }

    public RowKey getRowKey() {
        int i = getRowIndex();
        RowKey[] rks = tdp.getRowKeys(i + 1, null);
        if (rks.length > i) {
            return rks[i];
        }
        return null;
    }

    /**
     * <p>Set the currently selected <code>rowIndex</code>.  The cursor
     * position of the wrapped {@link TableDataProvider} is <strong>NOT</strong>
     * updated.</p>
     *
     * @param rowIndex The new selected row index, or -1 for no selection
     */
    public void setRowIndex(int rowIndex) {
        if (rowIndex < -1) {
            throw new IllegalArgumentException("" + rowIndex);
        }
        int oldIndex = this.rowIndex;
        this.rowIndex = rowIndex;
        if (getTableDataProvider() == null) {
            return;
        }
        DataModelListener listeners[] = getDataModelListeners();
        if ((oldIndex != rowIndex) && (listeners != null)) {
            Object rowData = null;
            if (isRowAvailable()) {
                rowData = getRowData();
            }
            DataModelEvent event =
              new DataModelEvent(this, rowIndex, rowData);
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].rowSelected(event);
            }
        }
    }


    /**
     * <p>Return the wrapped {@link TableDataProvider} instance, if any.</p>
     */
    public Object getWrappedData() {
        return getTableDataProvider();
    }


    /**
     * <p>Set the wrapped {@link TableDataProvider} instance (if any).</p>
     *
     * @param data New {@link TableDataProvider} instance, or <code>null</code>
     *  to disassociate from any instance
     */
    public void setWrappedData(Object data) {
        setTableDataProvider((TableDataProvider) data);
    }


    // --------------------------------------------------------- Private Classes


    /**
     * <p>Private implementation of <code>Map</code> that delegates
     * <code>get()</code> and <code>put()</code> operations to
     * <code>getValue()</code> and <code>setValue()</code> calls on the
     * underlying {@link TableDataProvider}.</p>
     */
    private class TableDataProviderMap extends AbstractMap {

        public TableDataProviderMap() {
            this.rowIndex = TableDataProviderDataModel.this.getRowIndex();
            this.rowKey = TableDataProviderDataModel.this.getRowKey();
        }

        private int rowIndex;
        private RowKey rowKey;
        private FieldKey fieldKeys[] = TableDataProviderDataModel.this.fieldKeys;
        private TableDataProvider tdp = TableDataProviderDataModel.this.tdp;

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean containsValue(Object value) {
            Iterator keys = keySet().iterator();
            while (keys.hasNext()) {
                Object key = keys.next();
                Object contained = get(key);
                if (value == null) {
                    if (contained == null) {
                        return true;
                    }
                } else {
                    if (value.equals(contained)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Set entrySet() {
            return new TableDataProviderEntries(this);
        }

        public Object get(Object key) {
            int columnIndex = index(key);
            if (columnIndex < 0) {
                return null;
            }
            return tdp.getValue(fieldKeys[columnIndex], rowKey);
        }

        public Set keySet() {
            return new TableDataProviderKeys(this);
        }

        public Object put(Object key, Object value) {
            int columnIndex = index(key);
            if (columnIndex < 0) {
                return null;
            }
            Object previous = tdp.getValue(fieldKeys[columnIndex], rowKey);
            tdp.setValue(fieldKeys[columnIndex], rowKey, value);
            return previous;
        }

        public void putAll(Map map) {
            Iterator keys = map.keySet().iterator();
            while (keys.hasNext()) {
                Object key = keys.next();
                put(key, map.get(key));
            }
        }

        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        public Collection values() {
            return new TableDataProviderValues(this);
        }

        private int index(Object key) {
            int index = -1;
            for (int i = 0; i < fieldKeys.length; i++) {
                if (key.equals(fieldKeys[i].getFieldId())) {
                    index = i;
                    break;
                }
            }
            return index;
        }

        Object realKey(Object key) {
            return super.get(key);
        }

        Iterator realKeys() {
            return super.keySet().iterator();
        }

    }


    /**
     * <p>Private implementation of <code>Set</code> for implementing the
     * <code>entrySet()</code> behavior of <code>TableDataProviderMap</code>.</p>
     */
    private class TableDataProviderEntries extends AbstractSet {

        public TableDataProviderEntries(TableDataProviderMap map) {
            this.map = map;
        }

        private TableDataProviderMap map = null;

        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Object o) {
            if (o == null) {
                throw new NullPointerException();
            }
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry) o;
            Object k = e.getKey();
            Object v = e.getValue();
            if (!map.containsKey(k)) {
                return false;
            }
            if (v == null) {
                return map.get(k) == null;
            } else {
                return v.equals(map.get(k));
            }
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public Iterator iterator() {
            return new TableDataProviderIterator(map);
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return map.size();
        }

    }


    /**
     * <p>Private implementation of <code>Iterator</code> for the
     * <code>Set</code> returned by <code>entrySet()</code>.</p>
     */
    private class TableDataProviderIterator implements Iterator {

        public TableDataProviderIterator(TableDataProviderMap map) {
            this.map = map;
            this.keys = map.keySet().iterator();
        }

        private TableDataProviderMap map = null;
        private Iterator keys = null;

        public boolean hasNext() {
            return keys.hasNext();
        }

        public Object next() {
            Object key = keys.next();
            return (new TableDataProviderEntry(map, key));
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }


    /**
     * <p>Private implementation of <code>Map.Entry</code> that implements
     * the behavior for a single entry from the <code>Set</code> that is
     * returned by <code>entrySet()</code>.</p>
     */
    private class TableDataProviderEntry implements Map.Entry {

        public TableDataProviderEntry(TableDataProviderMap map, Object key) {
            this.map = map;
            this.key = key;
        }

        private TableDataProviderMap map = null;
        private Object key = null;

        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry) o;
            if (key == null) {
                if (e.getKey() != null) {
                    return false;
                }
            } else {
                if (!key.equals(e.getKey())) {
                    return false;
                }
            }
            Object v = map.get(key);
            if (v == null) {
                if (e.getValue() != null) {
                    return false;
                }
            } else {
                if (!v.equals(e.getValue())) {
                    return false;
                }
            }
            return true;
        }

        public Object getKey() {
            return this.key;
        }

        public Object getValue() {
            return map.get(key);
        }

        public int hashCode() {
            Object value = map.get(key);
            return ((key == null) ? 0 : key.hashCode()) ^
                    ((value == null) ? 0 : value.hashCode());
        }

        public Object setValue(Object value) {
            Object previous = map.get(key);
            map.put(key, value);
            return previous;
        }

    }


    /**
     * <p>Private implementation of <code>Set</code> that implements the
     * <code>keySet()</code> behavior.</p>
     */
    private class TableDataProviderKeys extends AbstractSet {

        public TableDataProviderKeys(TableDataProviderMap map) {
            this.map = map;
        }

        private TableDataProviderMap map = null;

        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Object o) {
            return map.containsKey(o);
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public Iterator iterator() {
            return new TableDataProviderKeysIterator(map);
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return map.size();
        }

    }


    /**
     * <p>Private implementation of <code>Iterator</code> that implements the
     * <code>iterator()</code> method returned by <code>keySet()</code>.</p>
     */
    private class TableDataProviderKeysIterator implements Iterator {

        public TableDataProviderKeysIterator(TableDataProviderMap map) {
            this.map = map;
            this.keys = map.realKeys();
        }

        private TableDataProviderMap map = null;
        private Iterator keys = null;

        public boolean hasNext() {
            return keys.hasNext();
        }

        public Object next() {
            return keys.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }


    /**
     * <p>Private implementation of <code>Collection</code> that implements
     * the <code>values()</code> method.</p>
     */
    private class TableDataProviderValues extends AbstractCollection {

        public TableDataProviderValues(TableDataProviderMap map) {
            this.map = map;
        }

        private TableDataProviderMap map = null;

        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Object value) {
            return map.containsValue(value);
        }

        public Iterator iterator() {
            return new TableDataProviderValuesIterator(map);
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return map.size();
        }

    }


    /**
     * <p>Private implementation of <code>Iterator</code> that implements the
     * <code>behavior for the <code>Iterator</code> returned by
     * <code>values().iterator()</code>.</p>
     */
    private class TableDataProviderValuesIterator implements Iterator {

        public TableDataProviderValuesIterator(TableDataProviderMap map) {
            this.map = map;
            this.keys = map.keySet().iterator();
        }

        private TableDataProviderMap map = null;
        private Iterator keys = null;

        public boolean hasNext() {
            return keys.hasNext();
        }

        public Object next() {
            return map.get(keys.next());
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }






}
