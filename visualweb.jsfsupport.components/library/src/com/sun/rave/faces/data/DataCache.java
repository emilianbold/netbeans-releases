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

package com.sun.rave.faces.data;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Cache for row and column values that supports persisting the data behind
 * a <code>UIData</code> component across HTTP requests, without
 * requiring that the underlying JDBC connection remain open.  It also includes
 * mechanisms to detect which row and column values have been updated, in order
 * to support minimal database activity when synchronizing these changes to
 * the underlying database.</p>
 *
 * <p><code>DataCache</code> declares itself to be <code>Serializable</code>
 * to conform to the J2EE platform requirement that session scope attributes
 * should be serializable on a distributable container.  However, this will
 * only succeed if the actual cached column values are themselves
 * Serializable as well.</p>
 *
 * @author  craigmcc
 */
public class DataCache implements Serializable {

    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The cached <code>Row</code> information, keyed by row index
     * (wrapped in a <code>java.lang.Integer</code>.</p>
     */
    private Map cache = new HashMap();

    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Add a new row entry to the cache, replacing any existing cache entry
     * for the same <code>rowIndex</code> value.</p>
     *
     * @param rowIndex Row index this row corresponds to
     * @param row <code>DataCache.Row</code> to be added
     */
    public void add(int rowIndex, Row row) {

        cache.put(new Integer(rowIndex), row);

    }

    /**
     * <p>Clear any cached row and column data.</p>
     */
    public void clear() {

        cache.clear();

    }

    /**
     * <p>Commit the updated state of all cached rows.  After this
     * method completes, the current values for all columns will
     * appear to be original, any rows marked deleted will be
     * removed, and no row will be considered updated.</p>
     */
    public void commit() {

        List deletedKeys = new ArrayList();

        // Commit any updated rows
        Iterator keys = cache.keySet().iterator();
        while (keys.hasNext()) {
            Integer key = (Integer)keys.next();
            Row row = (Row)cache.get(key);
            if (row.isDeleted()) {
                deletedKeys.add(key);
            } else if (row.isUpdated()) {
                row.commit();
            }
        }

        // Remove any deleted rows
        keys = deletedKeys.iterator();
        while (keys.hasNext()) {
            Integer key = (Integer)keys.next();
            cache.remove(key);
        }

    }

    /**
     * <p>Create and return a <code>Column</code> instance configured
     * with the specified parameters.</p>
     *
     * @param schemaName Schema name of the table containing this
     *  column (if any)
     * @param tableName Table name containing this column (if any)
     * @param columnName Column name of this column
     * @param sqlType SQL type (from java.sql.Types)
     * @param original Original value for this column
     */
    public Column createColumn(String schemaName, String tableName,
        String columnName, int sqlType, Object original) {

        return new Column(schemaName, tableName, columnName, sqlType, original);

    }
    
    /**
     * <p>Create and return a <code>Column</code> instance configured
     * with the specified parameters.</p>
     *
     * @param schemaName Schema name of the table containing this
     *  column (if any)
     * @param tableName Table name containing this column (if any)
     * @param columnName Column name of this column
     * @param sqlType SQL type (from java.sql.Types)
     * @param javaType Java type as would be returned by 
     *  Class.forName(ResultSetMetaData.getColumnClassName)
     * @param original Original value for this column
     */
    public Column createColumn(String schemaName, String tableName,
        String columnName, int sqlType, Class javaType, Object original) {

        return new Column(schemaName, tableName, columnName, sqlType, javaType, original);

    }

    /**
     * <p>Create and return a <code>Row</code> instance configured
     * with the specified parameters.</p>
     *
     * @param columns <code>Column</code> instances for this row
     */
    public Row createRow(Column columns[]) {

        return new Row(columns);

    }

    /**
     * <p>Return the cached row associated with the specified row index,
     * if any; otherwise, return <code>null</code>.</p>
     *
     * @param rowIndex Row index for which to retrieve a row
     */
    public Row get(int rowIndex) {

        return (Row)cache.get(new Integer(rowIndex));

    }

    /**
     * <p>Return an <code>Iterator</code> over the row index values
     * (of type <code>java.lang.Integer</code>) for which cached data
     * is present.</p>
     S     */
    public Iterator iterator() {

        return cache.keySet().iterator();

    }

    /**
     * <p>Remove any row entry corresponding to the specified
     * <code>rowIndex</code>.</p>
     *
     * @param rowIndex Row index for which to remove any cached data
     */
    public void remove(int rowIndex) {

        cache.remove(new Integer(rowIndex));

    }

    /**
     * <p>Reset the updated state of all rows and columns in the cache.</p>
     */
    public void reset() {

        Iterator rows = cache.values().iterator();
        while (rows.hasNext()) {
            Row row = (Row)rows.next();
            row.reset();
        }

    }

    // --------------------------------------------------------- Private Methods


    // --------------------------------------------------- Public Helper Classes


    /**
     * <p><code>Column</code> encapsulates the stored information
     * about a single "column" of data, typically corresponding to a column
     * from an individual row in an underlying relational database.</p>
     */
    public class Column implements Map.Entry, Serializable {

        /**
         * <p>Construct a new <code>DataCache.Column</code> configured
         * by the specified parameters.</p>
         *
         * @param schemaName Schema name for the table containing
         *  this column (if any)
         * @param tableName Table name containing this column
         *  (if any)
         * @param key Column name for this column
         * @param sqlType SQL type (from java.sql.Types)
         * @param original Original data value for this column
         */
        Column(String schemaName, String tableName,
            String key, int sqlType, Object original) {
            this(schemaName, tableName, key, sqlType, null, original);
        }
        
        /**
         * <p>Construct a new <code>DataCache.Column</code> configured
         * by the specified parameters.</p>
         *
         * @param schemaName Schema name for the table containing
         *  this column (if any)
         * @param tableName Table name containing this column
         *  (if any)
         * @param key Column name for this column
         * @param sqlType SQL type (from java.sql.Types)
         * @param javaType Java type as would be returned from 
         *   Class.forName(ResultSetMetaData.getColumnClassName)
         * @param original Original data value for this column
         */
        Column(String schemaName, String tableName,
            String key, int sqlType, Class javaType, Object original) {
            this.schemaName = schemaName;
            this.tableName = tableName;
            this.key = key;
            this.sqlType = sqlType;
            this.javaType = javaType;
            this.original = original;
            this.replacement = null;
            this.updated = false;
        }

        private String key;
        private Object original;
        private int sqlType;
        private Class javaType;
        private Object replacement;
        private String schemaName;
        private String tableName;
        private boolean updated;

        /**
         * <p>If this column value has been updated, copy the
         * current value to the original value and clear the
         * updated state.  Otherwise, do nothing.</p>
         */
        public void commit() {
            if (updated) {
                original = replacement;
                replacement = null;
                updated = false;
            }
        }

        /**
         * <p>Return the column name for this column as a String.
         * This is a type-safe alias for <code>getKey()</code>.</p>
         */
        public String getColumnName() {
            return (String)key;
        }

        /**
         * <p>Return the column name for this column.</p>
         */
        public Object getKey() {
            return key;
        }

        /**
         * <p>Return the SQL type for this column.
         */
        public int getSqlType() {
            return sqlType;
        }
        
        /**
         * <p>Return the Java type for this column.
         */
        public Class getJavaType() {
            return javaType;
        }

        /**
         * <p>Return the original value for this column.</p>
         */
        public Object getOriginal() {
            return original;
        }

        /**
         * <p>Return the replacement value for this column, if any.
         * This is only meaningful if <code>isUpdated()</code>
         * returns <code>true</code>.</p>
         */
        public Object getReplacement() {
            return replacement;
        }

        /**
         * <p>Return the name of the schema containing the table
         * containing this column (if any); otherwise, return
         * <code>null</code>.</p>
         */
        public String getSchemaName() {
            return schemaName;
        }

        /**
         * <p>Return the name of the table containing this column
         * (if any); otherwise, return <code>null</code>.</p>
         */
        public String getTableName() {
            return tableName;
        }

        /**
         * <p>Return the replacement value if this column has been
         * updated; else return the original value.</p>
         */
        public Object getValue() {
            if (updated) {
                return replacement;
            } else {
                return original;
            }
        }

        /**
         * <p>Return <code>true</code> if the value for this column
         * has been updated.</p>
         */
        public boolean isUpdated() {
            return updated;
        }

        /**
         * <p>Reset the updated state of this column, and throw away any
         * reference to a replacement value.</p>
         */
        public void reset() {
            replacement = null;
            updated = false;
        }

        /**
         * <p>If the specified value is different from the original
         * value, save it and mark this column (and the containing
         * row) as having been updated.  Otherwise, take no action.</p>
         *
         * @param obj Replacement value for this column
         */
        public Object setValue(Object obj) {
            if (original == null) {
                if (obj != null) {
                    /*
                     * !JK If we are updating a null to an empty String, don't do that.
                     * As it stands now, null values are getting overwritten with
                     * empty Strings.  This isn't typically the behavior desired.  Long
                     * term, we need submit to be showing null values for null objects
                     * that haven't been updated.
                     */
                    if (!(obj instanceof String && ((String)obj).length() == 0)) {
                        update(obj);
                    }
                }
            } else {
                if (obj == null) {
                    update(obj);
                } else if (!original.equals(obj)) {
                    update(obj);
                }
            }
            return original;
        }

        /**
         * <p>Save the specified replacement value, and mark this
         * column (and the associated row) as having been modified.</p>
         *
         * @param obj Replacement value for this column
         */
        private void update(Object obj) {
            replacement = obj;
            updated = true;
        }

    }

    /**
     * <p><code>Row</code> encapsulates the stored information
     * about a single "row" of data, typically corresponding to a row in
     * an underlying relational database.  The implementation methods that
     * perform comparisons against column name values are done so in
     * a case-insensitive manner.  No modification to the set of
     * <code>Column</code>s included in a <code>Row</code> is permitted
     * after construction.</p>
     */
    public class Row extends AbstractMap implements Serializable {

        /**
         * <p>Construct a new <code>Row</code> wrapping the specified
         * <code>Column</code> values.</p>
         *
         * @param columns <code>Column</code> entries for this row
         */
        Row(Column columns[]) {
            this.columns = columns;
            this.deleted = false;
        }

        private Column columns[];
        private boolean deleted;

        // ---------- Row Methods ----------


        /**
         * <p>Call <code>commit()</code> on all of the included
         * columns in order to make the current values be the
         * original ones, and reset the updated state.  Set the
         * <code>deleted</code> state of this row to <code>false</code>.</p>
         */
        public void commit() {
            for (int i = 0; i < columns.length; i++) {
                columns[i].commit();
            }
            setDeleted(false);
        }

        /**
         * <p>Return the <code>Column</code> objects representing
         * the column values in this row.</p>
         */
        Column[] getColumns() {
            return this.columns;
        }

        /**
         * <P>Return <code>true</code> if this row has been
         * marked for deletion.</p>
         */
        public boolean isDeleted() {
            return this.deleted;
        }

        /**
         * <p>Return <code>true</code> if any column value in this
         * <code>Row</code> has been updated.</p>
         */
        public boolean isUpdated() {
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].updated) {
                    return true;
                }
            }
            return false;
        }

        /**
         * <p>Reset the updated state of this row and all underlying
         * columns.  Set the <code>deleted</code> state of this row
         * to <code>false</code>.</p>
         */
        public void reset() {
            for (int i = 0; i < columns.length; i++) {
                columns[i].reset();
            }
            setDeleted(false);
        }

        /**
         * <p>Set the <code>deleted</code> flag on this row
         * to the specified value.</p>
         *
         * @param deleted The new deleted flag
         */
        public void setDeleted(boolean deleted) {
            this.deleted = deleted;
        }

        // ---------- Map Methods ----------

        public void clear() {
            throw new UnsupportedOperationException();
        }

        // Case-insensitive match on column name
        public boolean containsKey(Object key) {
            String skey = (String)key;
            for (int i = 0; i < columns.length; i++) {
                if (skey.equalsIgnoreCase(columns[i].getColumnName())) {
                    return true;
                }
            }
            return false;
        }

        public Set entrySet() {
            return new ColumnSet(this);
        }

        // Case-insensitive match on column name
        public Object get(Object key) {
            String skey = (String)key;
            for (int i = 0; i < columns.length; i++) {
                if (skey.equalsIgnoreCase(columns[i].getColumnName())) {
                    return columns[i].getValue();
                }
            }
            return null;
        }

        // Case-insensitive match on column name
        public Object put(Object key, Object value) {
            String skey = (String)key;
            for (int i = 0; i < columns.length; i++) {
                if (skey.equalsIgnoreCase(columns[i].getColumnName())) {
                    Object previous = columns[i].getValue();
                    columns[i].setValue(value);
                    return previous;
                }
            }
            throw new IllegalArgumentException(skey);
        }

        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

    }

    // -------------------------------------------------- Private Helper Classes


    /**
     * <p><code>Iterator</code> over the <code>Column</code>
     * entries for the specified <code>Row</code>.</p>
     */
    private class ColumnIterator implements Iterator {

        ColumnIterator(Row row) {
            this.row = row;
        }

        int index = 0;
        private Row row;

        public boolean hasNext() {
            return index < row.columns.length;
        }

        public Object next() {
            return row.columns[index++];
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p><code>Set</code> representing the <code>Column</code>
     * entries for the specified <code>Row</code>.</p>
     */
    private class ColumnSet extends AbstractSet {

        ColumnSet(Row row) {
            this.row = row;
        }

        private Row row;

        // ---------- Set Methods ----------

        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public Iterator iterator() {
            return new ColumnIterator(row);
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
            return row.columns.length;
        }

    }

}
