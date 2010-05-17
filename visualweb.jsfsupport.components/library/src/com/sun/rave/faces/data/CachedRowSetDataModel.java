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

/*
 * $Id$
 */


package com.sun.rave.faces.data;


import java.beans.Beans;
import java.sql.ResultSet;
import javax.sql.rowset.CachedRowSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.faces.FacesException;
import javax.faces.el.PropertyNotFoundException;
import com.sun.rave.faces.util.ComponentBundle;

import javax.faces.model.DataModel;
import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;


/**
 * <p><strong>CachedRowSetDataModel</strong> is a convenience implementation of
 * {@link DataModel} that wraps a <code>CachedRowSet</code> of Java objects.
 * Note that the specified <code>CachedRowSet</code> <strong>MUST</strong>
 * be scrollable.  In addition, if input components (that will be updating
 * model values) reference this object in value binding expressions, the
 * specified <code>CachedRowSet</code> <strong>MUST</strong> be updatable.</p>
 */

public class CachedRowSetDataModel extends DataModel {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a new {@link CachedRowSetDataModel} with no specified
     * wrapped data.</p>
     */
    public CachedRowSetDataModel() {

        this(null);

    }


    /**
     * <p>Construct a new {@link CachedRowSetDataModel} wrapping the specified
     * <code>CachedRowSet</code>.</p>
     *
     * @param cachedRowSet <code>CachedRowSet</code> to be wrapped (if any)
     */
    public CachedRowSetDataModel(CachedRowSet cachedRowSet) {

        super();
        setWrappedData(cachedRowSet);

    }


    // ------------------------------------------------------ Instance Variables

    /**
     * <p>Localization resources for this package.</p>
     */
    private static final ComponentBundle bundle =
        ComponentBundle.getBundle(CachedRowSetDataModel.class);


    /**
     * <p>The number of fake data rows we will compose at
     * design time.</p>
     */
    private static final int DESIGN_TIME_ROWS = 5;


    // The row index for the row whose column values may be read or written
    //private int current = -1;


    // The current row index (zero relative)
    private int index = -1;


    // The metadata for the CachedRowSet we are wrapping (lazily instantiated)
    private ResultSetMetaData metadata = null;


    // The CachedRowSet we are wrapping
    private CachedRowSet cachedRowSet = null;


    // Has the row at the current index been updated?
    private boolean updated = false;


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return <code>true</code> if there is <code>wrappedData</code>
     * available, and the result of calling <code>absolute()</code> on the
     * underlying <code>CachedRowSet</code>, passing the current value of
     * <code>rowIndex</code> plus one (to account for the fact that
     * <code>CachedRowSet</code> uses one-relative indexing), returns
     * <code>true</code>.  Otherwise, return <code>false</code>.</p>
     *
     * @exception FacesException if an error occurs getting the row availability
     */ 
    public boolean isRowAvailable() {

        if (Beans.isDesignTime()) {
            return (index >= 0) && (index < DESIGN_TIME_ROWS);
        }

        executeIfNecessary();

        if (cachedRowSet == null) {
	    return (false);
        } else if (index < 0) {
            return (false);
        }
        try {
            if (cachedRowSet.absolute(index + 1)) {
                return (true);
            } else {
                return (false);
            }
        } catch (SQLException e) {
            throw new FacesException(e);
        }

    }


    /**
     * <p>Return -1, since <code>CachedRowSet</code> does not provide a
     * standard way to determine the number of available rows without
     * scrolling through the entire <code>CachedRowSet</code>, and this can
     * be very expensive if the number of rows is large.</p>
     *
     * @exception FacesException if an error occurs getting the row count
     */
    public int getRowCount() {

        if (Beans.isDesignTime()) {
            return DESIGN_TIME_ROWS;
        }

	return (-1);

    }


    /**
     * <p>If row data is available, return a <code>Map</code> representing
     * the values of the columns for the row specified by <code>rowIndex</code>,
     * keyed by the corresponding column names.  If no wrapped data is
     * available, return <code>null</code>.</p>
     *
     * <p>If a non-<code>null</code> <code>Map</code> is returned, its behavior
     * must correspond to the contract for a mutable <code>Map</code> as
     * described in the JavaDocs for <code>AbstractMap</code>, with the
     * following exceptions and specialized behavior:</p>
     * <ul>

     * <li>The <code>Map</code>, and any supporting objects it returns,
     *     must perform all column name comparisons in a
     *     case-insensitive manner.  This case-insensitivity must be
     *     implemented using a case-insensitive <code>Comparator</code>,
     *     such as
     *     <code>String.CASE_INSENSITIVE_ORDER</code>.</li>

     * <li>The following methods must throw
     *     <code>UnsupportedOperationException</code>:  <code>clear()</code>,
     *     <code>remove()</code>.</li>
     * <li>The <code>entrySet()</code> method must return a <code>Set</code>
     *     that has the following behavior:
     *     <ul>
     *     <li>Throw <code>UnsupportedOperationException</code> for any attempt
     *         to add or remove entries from the <code>Set</code>, either
     *         directly or indirectly through an <code>Iterator</code>
     *         returned by the <code>Set</code>.</li>
     *     <li>Updates to the <code>value</code> of an entry in this
     *         <code>set</code> must write through to the corresponding
     *         column value in the underlying <code>CachedRowSet</code>.</li>
     *     </ul></li>
     * <li>The <code>keySet()</code> method must return a <code>Set</code>
     *     that throws <code>UnsupportedOperationException</code> on any
     *     attempt to add or remove keys, either directly or through an
     *     <code>Iterator</code> returned by the <code>Set</code>.</li>
     * <li>The <code>put()</code> method must throw
     *     <code>IllegalArgumentException</code> if a key value for which
     *     <code>containsKey()</code> returns <code>false</code> is
     *     specified.  However, if a key already present in the <code>Map</code>
     *     is specified, the specified value must write through to the
     *     corresponding column value in the underlying <code>CachedRowSet</code>.
     *     </li>
     * <li>The <code>values()</code> method must return a
     *     <code>Collection</code> that throws
     *     <code>UnsupportedOperationException</code> on any attempt to add
     *     or remove values, either directly or through an <code>Iterator</code>
     *     returned by the <code>Collection</code>.</li>
     * </ul>
     *
     * @exception FacesException if an error occurs getting the row data
     * @exception IllegalArgumentException if now row data is available
     *  at the currently specified row index
     */ 
    public Object getRowData() {

        if (cachedRowSet == null) {
	    return (null);
        } else if (!isRowAvailable()) {
            throw new IllegalArgumentException();
        }
        try {
            getMetaData();
            return (new CachedRowSetMap(String.CASE_INSENSITIVE_ORDER));
        } catch (SQLException e) {
            throw new FacesException(e);
        }

    }


    /**
     * @exception FacesException
     */ 
    public int getRowIndex() {

        return (index);

    }


    /**
     * @exception FacesException 
     * @exception IllegalArgumentException
     */ 
    public void setRowIndex(int rowIndex) {

        if (rowIndex < -1) {
            throw new IllegalArgumentException();
        }

        // Tell the CachedRowSet that the previous row was updated if necessary
        if (!Beans.isDesignTime()) {
            if (updated && (cachedRowSet != null)) {
                try {
		    if (!cachedRowSet.rowDeleted()) {
		        cachedRowSet.updateRow();
		    }
                    updated = false;
                } catch (SQLException e) {
                    throw new FacesException(e);
                }
            }
        }

        int old = index;
        index = rowIndex;
	if (cachedRowSet == null) {
	    return;
	}
	DataModelListener [] listeners = getDataModelListeners();
        if ((old != index) && (listeners != null)) {
            Object rowData = null;
            if (isRowAvailable()) {
                rowData = getRowData();
            }
            DataModelEvent event =
                new DataModelEvent(this, index, rowData);
            int n = listeners.length;
            for (int i = 0; i < n; i++) {
		if (null != listeners[i]) {
		    listeners[i].rowSelected(event);
		}
            }
        }


    }


    public Object getWrappedData() {

        return (this.cachedRowSet);

    }


    /**
     * @exception ClassCastException 
     */
    public void setWrappedData(Object data) {

        if (data == null) {
            metadata = null;
            cachedRowSet = null;
            setRowIndex(-1);
        } else {
            metadata = null;
            cachedRowSet = (CachedRowSet) data;
            index = -1;
            setRowIndex(0);
        }
    }

    /**
     * <p>Return the <code>CachedRowSet</code> we are connected with,
     * if any; otherwise, return <code>null</code>.  This is a
     * type=safe alias for <code>getWrappedData()</code>.</p>
     */
    public CachedRowSet getCachedRowSet() {

        return ((CachedRowSet)getWrappedData());

    }

    /**
     * <p>Set the <code>CachedRowSet</code> we are connected with,
     * or pass <code>null</code> to disconnect.  This is a
     * type-safe alias for <code>setWrappedData()</code>.</p>
     *
     * @param rowSet The <code>CachedRowSet</code> we are connected to,
     *  or <code>null</code> to disconnect
     */
    public void setCachedRowSet(CachedRowSet rowSet) {

        setWrappedData(rowSet);

    }


    // --------------------------------------------------------- Private Methods

    /**
     * <p>If not designtime, execute the rowset if necessary.
     */
    private void executeIfNecessary() {

        if (Beans.isDesignTime()) {
            return;
        }

        if (getCachedRowSet() == null) {
            throw new FacesException(bundle.getMessage("cachedRowSetIsNull")); // NOI18N
        }

        try {
            getCachedRowSet().isBeforeFirst();
        } catch (SQLException e) {
            try {
                getCachedRowSet().execute();
            } catch (SQLException e2) {
                throw new FacesException(e2);
            }
        }
    }


    /**
     * <p>Return the <code>ResultSetMetaData</code> for the
     * <code>CachedRowSet</code> we are wrapping, caching it the first time
     * it is returned.</p>
     *
     * @exception FacesException if the <code>ResultSetMetaData</code>
     *  cannot be acquired
     */
    private ResultSetMetaData getMetaData() {

        if (metadata == null) {
            try {
                metadata = cachedRowSet.getMetaData();
            } catch (SQLException e) {
                throw new FacesException(e);
            }
        }
        return (metadata);

    }


    /**
     * <p>Mark the current row as having been updated, so that we will call
     * <code>updateRow()</code> before moving elsewhere.</p>
     */
    private void updated() {

        this.updated = true;

    }


    // --------------------------------------------------------- Private Classes


    // Private implementation of Map that delegates column get and put
    // operations to the underlying CachedRowSet, after setting the required
    // row index
    private class CachedRowSetMap extends TreeMap {

        public CachedRowSetMap(Comparator comparator) throws SQLException {
            super(comparator);
            index = CachedRowSetDataModel.this.index;
            if (!Beans.isDesignTime()) {
                cachedRowSet.absolute(index + 1);
            }
            int n = metadata.getColumnCount();
            for (int i = 1; i <= n; i++) {
                super.put(metadata.getColumnName(i),
                          metadata.getColumnName(i));
            }
        }

        // The zero-relative row index of our row
        private int index;

        // Removing entries is not allowed
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
                        return (true);
                    }
                } else {
                    if (value.equals(contained)) {
                        return (true);
                    }
                }
            }
            return (false);
        }

        public Set entrySet() {
            return (new CachedRowSetEntries(this));
        }

        public Object get(Object key) {
            if (!containsKey(key)) {
                return (null);
            }
            try {
                if (Beans.isDesignTime()) {
                    return getFakeData(metadata, (String)realKey(key));
                } else {
                    cachedRowSet.absolute(index + 1);
                    return (cachedRowSet.getObject((String) realKey(key)));
                }
            } catch (SQLException e) {
                throw new FacesException(e);
            }
        }

        public Set keySet() {
            return (new CachedRowSetKeys(this));
        }

        public Object put(Object key, Object value) {
            if (Beans.isDesignTime()) {
                return get(key);
            }
            if (!containsKey(key)) {
                throw new IllegalArgumentException();
            }
            if (!(key instanceof String)) {
                throw new IllegalArgumentException();
            }
            try {
                cachedRowSet.absolute(index + 1);
                Object previous = cachedRowSet.getObject((String) realKey(key));
                if ((previous == null) && (value == null)) {
                    return (previous);
                } else if ((previous != null) && (value != null) &&
                           previous.equals(value)) {
                    return (previous);
                }
                cachedRowSet.updateObject((String) realKey(key), value);
                CachedRowSetDataModel.this.updated();
                return (previous);
            } catch (SQLException e) {
                throw new FacesException(e);
            }
        }

        public void putAll(Map map) {
            if (Beans.isDesignTime()) {
                return;
            }
            Iterator keys = map.keySet().iterator();
            while (keys.hasNext()) {
                Object key = keys.next();
                put(key, map.get(key));
            }
        }

        // Removing entries is not allowed
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        public Collection values() {
            return (new CachedRowSetValues(this));
        }

        Object realKey(Object key) {
            return (super.get(key));
        }

        Iterator realKeys() {
            return (super.keySet().iterator());
        }

    }


    // Private implementation of Set that implements the entrySet() behavior
    // for CachedRowSetMap
    private class CachedRowSetEntries extends AbstractSet {

        public CachedRowSetEntries(CachedRowSetMap map) {
            this.map = map;
        }

        private CachedRowSetMap map;

        // Adding entries is not allowed
        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        // Adding entries is not allowed
        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        // Removing entries is not allowed
        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Object o) {
            if (o == null) {
                throw new NullPointerException();
            }
            if (!(o instanceof Map.Entry)) {
                return (false);
            }
            Map.Entry e = (Map.Entry) o;
            Object k = e.getKey();
            Object v = e.getValue();
            if (!map.containsKey(k)) {
                return (false);
            }
            if (v == null) {
                return (map.get(k) == null);
            } else {
                return (v.equals(map.get(k)));
            }
        }

        public boolean isEmpty() {
            return (map.isEmpty());
        }

        public Iterator iterator() {
            return (new CachedRowSetEntriesIterator(map));
        }

        // Removing entries is not allowed
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        // Removing entries is not allowed
        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        // Removing entries is not allowed
        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return (map.size());
        }

    }


    // Private implementation of Iterator that implements the iterator()
    // behavior for the Set returned by entrySet() from CachedRowSetMap
    private class CachedRowSetEntriesIterator implements Iterator {

        public CachedRowSetEntriesIterator(CachedRowSetMap map) {
            this.map = map;
            this.keys = map.keySet().iterator();
        }

        private CachedRowSetMap map = null;
        private Iterator keys = null;

        public boolean hasNext() {
            return (keys.hasNext());
        }

        public Object next() {
            Object key = keys.next();
            return (new CachedRowSetEntry(map, key));
        }

        // Removing entries is not allowed
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }


    // Private implementation of Map.Entry that implements the behavior for
    // a single entry from the Set returned by entrySet() from CachedRowSetMap
    private class CachedRowSetEntry implements Map.Entry {

        public CachedRowSetEntry(CachedRowSetMap map, Object key) {
            this.map = map;
            this.key = key;
        }

        private CachedRowSetMap map;
        private Object key;

        public boolean equals(Object o) {
            if (o == null) {
                return (false);
            }
            if (!(o instanceof Map.Entry)) {
                return (false);
            }
            Map.Entry e = (Map.Entry) o;
            if (key == null) {
                if (e.getKey() != null) {
                    return (false);
                }
            } else {
                if (!key.equals(e.getKey())) {
                    return (false);
                }
            }
            Object v = map.get(key);
            if (v == null) {
                if (e.getValue() != null) {
                    return (false);
                }
            } else {
                if (!v.equals(e.getValue())) {
                    return (false);
                }
            }
            return (true);
        }

        public Object getKey() {
            return (key);
        }

        public Object getValue() {
            return (map.get(key));
        }

        public int hashCode() {
            Object value = map.get(key);
            return (((key == null) ? 0 : key.hashCode()) ^
                    ((value == null) ? 0 : value.hashCode()));
        }

        public Object setValue(Object value) {
            Object previous = map.get(key);
            map.put(key, value);
            return (previous);
        }

    }


    // Private implementation of Set that implements the keySet() behavior
    // for CachedRowSetMap
    private class CachedRowSetKeys extends AbstractSet {

        public CachedRowSetKeys(CachedRowSetMap map) {
            this.map = map;
        }

        private CachedRowSetMap map;

        // Adding keys is not allowed
        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        // Adding keys is not allowed
        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        // Removing keys is not allowed
        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Object o) {
            return (map.containsKey(o));
        }

        public boolean isEmpty() {
            return (map.isEmpty());
        }

        public Iterator iterator() {
            return (new CachedRowSetKeysIterator(map));
        }

        // Removing keys is not allowed
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        // Removing keys is not allowed
        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        // Removing keys is not allowed
        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return (map.size());
        }

    }


    // Private implementation of Iterator that implements the iterator()
    // behavior for the Set returned by keySet() from CachedRowSetMap
    private class CachedRowSetKeysIterator implements Iterator {

        public CachedRowSetKeysIterator(CachedRowSetMap map) {
            this.map = map;
            this.keys = map.realKeys();
        }

        private CachedRowSetMap map = null;
        private Iterator keys = null;

        public boolean hasNext() {
            return (keys.hasNext());
        }

        public Object next() {
            return (keys.next());
        }

        // Removing keys is not allowed
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }


    // Private implementation of Collection that implements the behavior
    // for the Collection returned by values() from CachedRowSetMap
    private class CachedRowSetValues extends AbstractCollection {

        public CachedRowSetValues(CachedRowSetMap map) {
            this.map = map;
        }

        private CachedRowSetMap map;

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
            return (map.containsValue(value));
        }

        public Iterator iterator() {
            return (new CachedRowSetValuesIterator(map));
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
            return (map.size());
        }

    }


    // Private implementation of Iterator that implements the behavior
    // for the Iterator returned by values().iterator() from CachedRowSetMap
    private class CachedRowSetValuesIterator implements Iterator {

        public CachedRowSetValuesIterator(CachedRowSetMap map) {
            this.map = map;
            this.keys = map.keySet().iterator();
        }

        private CachedRowSetMap map;
        private Iterator keys;

        public boolean hasNext() {
            return (keys.hasNext());
        }

        public Object next() {
            return (map.get(keys.next()));
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }


    /**
     * <p>Return fake data of the appropriate type for use at design time.
     * (Snarfed from <code>ResultSetPropertyResolver</code>).</p>
     */
    private static Object getFakeData(ResultSetMetaData rsmd, String colName) throws SQLException {

        int colIndex = -1;
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            if (rsmd.getColumnName(i).equals(colName)) {
                colIndex = i;
                break;
            }
        }
        switch (rsmd.getColumnType(colIndex)) {
            case Types.ARRAY:
                return new java.sql.Array() {
                    public Object getArray() {
                        return null;
                    }

                    public Object getArray(long index, int count) {
                        return null;
                    }

                    public Object getArray(long index, int count, Map map) {
                        return null;
                    }

                    public Object getArray(Map map) {
                        return null;
                    }

                    public int getBaseType() {
                        return Types.CHAR;
                    }

                    public String getBaseTypeName() {
                        return "CHAR"; //NOI18N
                    }

                    public ResultSet getResultSet() {
                        return null;
                    }

                    public ResultSet getResultSet(long index, int count) {
                        return null;
                    }

                    public ResultSet getResultSet(long index, int count, Map map) {
                        return null;
                    }

                    public ResultSet getResultSet(Map map) {
                        return null;
                    }

		    public void free() {
		    }
                }
                ;
            case Types.BIGINT:

                //return new Long(rowIndex);
                return new Long(123);
            case Types.BINARY:
                return new byte[] {
                    1, 2, 3, 4, 5};
            case Types.BIT:
                return new Boolean(true);
            case Types.BLOB:
                return new javax.sql.rowset.serial.SerialBlob(new byte[] {
                    1, 2, 3, 4, 5});
            case Types.BOOLEAN:
                return new Boolean(true);
            case Types.CHAR:

                //return new String(colName + rowIndex);
                return new String(bundle.getMessage("arbitraryCharData")); //NOI18N
            case Types.CLOB:
                return new javax.sql.rowset.serial.SerialClob(bundle.getMessage("arbitraryClobData").
                    toCharArray());
            case Types.DATALINK:
                try {
                    return new java.net.URL("http://www.sun.com"); //NOI18N
                } catch (java.net.MalformedURLException e) {
                    return null;
                }
                case Types.DATE:
                    return new java.sql.Date(new java.util.Date().getTime());
            case Types.DECIMAL:
                return new java.math.BigDecimal(java.math.BigInteger.ONE);
            case Types.DISTINCT:
                return null;
            case Types.DOUBLE:

                //return new Double(rowIndex);
                return new Double(123);
            case Types.FLOAT:

                //return new Double(rowIndex);
                return new Double(123);
            case Types.INTEGER:

                //return new Integer(rowIndex);
                return new Integer(123);
            case Types.JAVA_OBJECT:

                //return new String(colName + "_" + rowIndex);  //NOI18N
                return new String(bundle.getMessage("arbitraryCharData")); //NOI18N
            case Types.LONGVARBINARY:
                return new byte[] {
                    1, 2, 3, 4, 5};
            case Types.LONGVARCHAR:

                //return new String(colName + "_" + rowIndex); //NOI18N
                return new String(bundle.getMessage("arbitraryCharData")); //NOI18N
            case Types.NULL:
                return null;
            case Types.NUMERIC:
                return new java.math.BigDecimal(java.math.BigInteger.ONE);
            case Types.OTHER:
                return null;
            case Types.REAL:

                //return new Float(rowIndex);
                return new Float(123);
            case Types.REF:
                return new java.sql.Ref() {
                    private Object data = new String(bundle.getMessage("arbitraryCharData")); //NOI18N
                    public String getBaseTypeName() {
                        return "CHAR"; //NOI18N
                    }

                    public Object getObject() {
                        return data;
                    }

                    public Object getObject(Map map) {
                        return data;
                    }

                    public void setObject(Object value) {
                        data = value;
                    }
                }
                ;
            case Types.SMALLINT:

                //return new Short((short)rowIndex);
                return new Short((short)123);
            case Types.STRUCT:
                return new java.sql.Struct() {
                    private String[] data = {
                        bundle.getMessage("arbitraryCharData"),
                        bundle.getMessage("arbitraryCharData2"),
                        bundle.getMessage("arbitraryCharData3")}; //NOI18N
                    public Object[] getAttributes() {
                        return data;
                    }

                    public Object[] getAttributes(Map map) {
                        return data;
                    }

                    public String getSQLTypeName() {
                        return "CHAR"; //NOI18N
                    }
                }
                ;
            case Types.TIME:
                return new java.sql.Time(new java.util.Date().getTime());
            case Types.TIMESTAMP:
                return new java.sql.Timestamp(new java.util.Date().getTime());
            case Types.TINYINT:

                //return new Byte((byte)rowIndex);
                return new Byte((byte)123);
            case Types.VARBINARY:
                return new byte[] {
                    1, 2, 3, 4, 5};
            case Types.VARCHAR:

                //return new String(colName + "_" + rowIndex); //NOI18N
                return new String(bundle.getMessage("arbitraryCharData")); //NOI18N
        }
        return null;
    }


}
