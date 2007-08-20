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

import java.beans.Beans;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;
import java.io.FileFilter;
import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.spi.SyncResolver;
import javax.sql.rowset.spi.SyncProviderException;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.FilterCriteria;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.RefreshableDataProvider;
import com.sun.data.provider.RefreshableDataListener;
import com.sun.data.provider.SortCriteria;
import com.sun.data.provider.TableCursorVetoException;
import com.sun.data.provider.TableDataFilter;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.TableDataSorter;
import com.sun.data.provider.TransactionalDataListener;
import com.sun.data.provider.TransactionalDataProvider;
import com.sun.sql.rowset.CachedRowSetX;
import com.sun.sql.rowset.SyncResolverX;

/**
 * <p>{@link TableDataProvider} implementation that wraps a <code>CachedRowSet</code>.
 * </p>
 *
 * <p>Note: valueChanged events will only fire for column changes made via the
 * CachedRowSetDataProvider</p>
 *
 * <p>Note: The lifetime of the <code>RowKey</code>s handed out
 * <code>CachedRowSetDataProvider</code> is until the underlying <code>CachedRowSet</code>
 * is closed or reexecuted.</p>
 */
public class CachedRowSetDataProvider extends AbstractTableDataProvider
    implements TableDataProvider, TransactionalDataProvider, RefreshableDataProvider {


    // ----------------------------------------------------------- Constructors


    /**
     * <p>Construct an unconfigured {@link CachedRowSetDataProvider}.</p>
     */
    public CachedRowSetDataProvider() {}


    /**
     * <p>Construct a {@link CachedRowSetDataProvider} that wraps the
     * specified <code>CachedRowSet</code>.</p>
     *
     * @param cachedRowSet The <code>CachedRowSet</code> to be wrapped
     */
    public CachedRowSetDataProvider(CachedRowSet cachedRowSet) {
        setCachedRowSet(cachedRowSet);
    }


    // ------------------------------------------------------- Static Variables

    /**
     * <p>Localized resources for this implementation.</p>
     */
    private static ResourceBundle bundle =
      ResourceBundle.getBundle("com.sun.data.provider.impl.Bundle", //NOI18N
                               Locale.getDefault(),
                               CachedRowSetDataProvider.class.getClassLoader());

    // ----------------------------------------------------- Instance Variables

    /**
     * <p>The array of {@link FieldKey}s that correspond to the columns
     * represented in the <code>ResultSetMetaData</code> for this
     * request.</p>
     */
    private FieldKey fieldKeys[] = null;


    /**
     * <p>Map of the {@link FieldKey}s that correspond to the columns
     * represented in the <code>ResultSetMetaData</code> for this
     * request, keyed (case-insensitive) by column name.</p>
     */
    private Map fieldKeysMap = null;


    /**
     * <p>The <code>ResultSetMetaData</code> associated with the
     * <code>CachedRowSet</code> that we are wrapping.</p>
     */
    private transient ResultSetMetaData metaData = null;


    /**
     * <p>The <code>CachedRowSet</code> that we are wrapping.</p>
     */
    private CachedRowSet cachedRowSet = null;


    /**
     * <p>indicator if we are on the inserted row.
     * Note: in the semantics of dataprovider, this
     * would be called onAppendRow.
     * FIXME: It seems like there should be a CachedRowSet
     * method to detect this, but I don't see it.</p>
     */
    private boolean onInsertRow = false;


    /**
     * <p>{@link PropertyChangeListener} registered with the {@link CachedRowSetX}.</p>
     */
    private RowSetPropertyChangeListener propertyChangeListener = null;


    /**
     * <p>{@link PropertyChangeListener} registered with the {@link CachedRowSetX}.</p>
     */
    private RowSetListener rowSetListener = null;


    // ------------------------------------------------------------ Properties

    /**
     * <p>Return the <code>CachedRowSet</code> that we are wrapping.</p>
     */
    public CachedRowSet getCachedRowSet() {
        return cachedRowSet;

    }


    /**
     * <p>Set the <code>CachedRowSet</code> that we are wrapping.  In addition,
     * ensure that the <code>CachedRowSet</code> has been executed so that
     * subseuqent calls accessing it will work.</p>
     *
     * @param cachedRowSet The new <code>CachedRowSet</code>
     */
    public void setCachedRowSet(CachedRowSet cachedRowSet) {

        // Initialize our internal state information
        if (this.cachedRowSet != null && this.cachedRowSet instanceof CachedRowSetX
            && this.propertyChangeListener != null) {
            ((CachedRowSetX)this.cachedRowSet).removePropertyChangeListener(propertyChangeListener);
            propertyChangeListener = null;
        }
        if (this.cachedRowSet != null && this.rowSetListener != null) {
            this.cachedRowSet.removeRowSetListener(rowSetListener);
            rowSetListener = null;
        }
        try {
            cursorFirst();
        } catch (Exception e) {
        }
        this.cachedRowSet = cachedRowSet;
        metaData = null;
        fieldKeys = null;
        fieldKeysMap = null;
        if (cachedRowSet != null && cachedRowSet instanceof CachedRowSetX) {
            propertyChangeListener = new RowSetPropertyChangeListener();
            ((CachedRowSetX)cachedRowSet).addPropertyChangeListener(propertyChangeListener);
        }
        if (cachedRowSet != null) {
            rowSetListener = new CachedRowSetListener();
            cachedRowSet.addRowSetListener(rowSetListener);
        }
        fireProviderChanged();
    }

    // --------------------------------------- CachedRowSetDataProvider Methods

    /**
     * free resources used by this instance
     *
     * Close is guaranteed  not to throw an exception.
     */
    public void close() {
        try {
            setCachedRowSet(null);
        } catch (Exception e) {
            // attempted to cleanup, contract is close() will silently fail
        }
    }

    // ---------------------------------------------------------- RowKey Methods

    /** {@inheritDoc} */
    public RowKey[] getRowKeys(int count, RowKey afterRow) throws DataProviderException {

        /*
         * Only hand out RowKeys for rows that have not been deleted
         */
        if (getCachedRowSet() == null) {
            return new CachedRowSetRowKey[0];
        }
        int cursorIndexSave = getCursorIndex();
        List keys = new ArrayList();
        try {
            int startIndex = 0;
            if (afterRow instanceof CachedRowSetRowKey) {
                startIndex = ((CachedRowSetRowKey)afterRow).getIndex() + 1;
            }
            while (absolute(startIndex+1) && keys.size() < count) {
                try {
                    if (!(isUpdatable() && getCachedRowSet().rowDeleted())) {
                        keys.add(new CachedRowSetRowKey(startIndex));
                    }
                } catch (SQLException e) {
                    // if sqlexception, we can't hand out a rowkey for this row
                }
                startIndex++;
            }
            return (RowKey[])keys.toArray(new CachedRowSetRowKey[0]);
        } finally {
            try {
                setCursorIndex(cursorIndexSave);
            } catch (IllegalArgumentException e) {
                // This can happen if the row at the cursorIndex was deleted
            }
        }
    }

    /** {@inheritDoc} */
    public RowKey[] getAllRows() throws DataProviderException {
        return getRowKeys(Integer.MAX_VALUE, null);
    }

    public RowKey getRowKey(String rowId) throws DataProviderException {
        return CachedRowSetRowKey.create(rowId);
    }

    // ---------------------------------------------------------- Cursor Methods

    /**
     * storage for the current cursor row
     */
    protected RowKey cursorRow = new CachedRowSetRowKey(0);

    protected int getCursorIndex() {
        if (cursorRow instanceof CachedRowSetRowKey) {
            return ((CachedRowSetRowKey)cursorRow).getIndex();
        }
        return -1;
    }

    private boolean absolute(int index) {
        if (getCachedRowSet() == null) {
            return false;
        }
        if (Beans.isDesignTime()) {
            return index >= 1 && index <= 3;
        }
        try {
            checkExecute();
            boolean saveShowDeleted = getCachedRowSet().getShowDeleted();
            try {
                getCachedRowSet().setShowDeleted(true);
                return getCachedRowSet().absolute(index);
            } catch (SQLException e) {
                return false;
            } finally {
                getCachedRowSet().setShowDeleted(saveShowDeleted);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    protected boolean setCursorIndex(int index) {
        try {
            setCursorRow(new CachedRowSetRowKey(index));
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

        if (Beans.isDesignTime()) {
            if (!isRowAvailable(row)) {
                throw new IllegalArgumentException(bundle.getString("ROW_NOT_AVAILABLE")); //NOI18N
            }
            RowKey oldRow = this.cursorRow;
            fireCursorChanging(oldRow, row);
            this.cursorRow = row;
            fireCursorChanged(oldRow, cursorRow);
            return;
        }

        if (getCachedRowSet() != null && row instanceof CachedRowSetRowKey) {
            try {
                checkExecute();
                if (absolute(((CachedRowSetRowKey)row).getIndex() + 1)) {
                    if (isUpdatable() && getCachedRowSet().rowDeleted()) {
                        throw new IllegalArgumentException("" + row); //NOI18N
                    }
                    RowKey oldRow = this.cursorRow;
                    fireCursorChanging(oldRow, row);
                    this.cursorRow = row;
                    fireCursorChanged(oldRow, cursorRow);
                    return;
                } else {
                    throw new IllegalArgumentException("" + row); //NOI18N
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /** {@inheritDoc} */
    public boolean cursorFirst() throws DataProviderException {

        RowKey[] keys = getRowKeys(1, null);
        if (keys.length == 0) {
            return false;
        }
        try {
            setCursorRow((CachedRowSetRowKey)keys[0]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** {@inheritDoc} */
    public boolean cursorNext() throws DataProviderException {
        RowKey[] keys = getRowKeys(1, getCursorRow());
        if (keys.length == 0) {
            return false;
        }
        try {
            setCursorRow((CachedRowSetRowKey)keys[0]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    /** {@inheritDoc} */
    public boolean cursorPrevious() throws DataProviderException {
        // attempt to set cursor index until sucessfull or < 0
        int idx = getCursorIndex() - 1;
        while (idx >= 0) {
            try {
                setCursorIndex(idx);
                return true;
            } catch (IllegalArgumentException e) {
            }
            idx--;
        }
        return false;
    }


    /** {@inheritDoc} */
    public boolean cursorLast() throws DataProviderException {
        if (Beans.isDesignTime()) {
            setCursorIndex(2);
            return true;
        }
        boolean saveShowDeleted = false;
        try {
            saveShowDeleted = getCachedRowSet().getShowDeleted();
            getCachedRowSet().setShowDeleted(true);
            try {
                getCachedRowSet().last();
                do {
                    int key = getCachedRowSet().getRow() - 1;
                    if (!(isUpdatable() && getCachedRowSet().rowDeleted())) {
                        setCursorRow(new CachedRowSetRowKey(key));
                        return true;
                    }
                } while (getCachedRowSet().previous());
                return false;
            } catch (SQLException e2) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        } finally {
            try {
                getCachedRowSet().setShowDeleted(saveShowDeleted);
            } catch (SQLException e) {
            }
        }
    }


    // --------------------------------------------------- DataProvider Methods

    /** {@inheritDoc} */
    public FieldKey getFieldKey(String fieldId) throws DataProviderException {

        /*
         * Bug 6275441: mssqlserver is handing back different metadata based on whether
         * the query has been executed or not.
         *
         */
        try {
            return getFieldKeyInternal(fieldId);
        } catch (IllegalArgumentException e) {
            /*
             * let's see if we hit bug #6275441
             */
            if (fieldId.indexOf('.') == -1) {
                // fieldId is not prepended with a table, so we may have hit 6275441
                if (fieldKeysMap != null) {
                    for (Iterator i = fieldKeysMap.values().iterator(); i.hasNext();) {
                        FieldKey fieldKey = (FieldKey)i.next();
                        String val = fieldKey.getFieldId() ;
                        int loc = val.lastIndexOf('.') ;
                        if ( loc >= 0 && loc+1 < val.length() ) {
                            val = val.substring(loc+1) ;
                        }
                        if ( val.equalsIgnoreCase(fieldId)) {
                            return fieldKey;
                        }
                    }
                }
            }
            throw e;
        }
    }

    private FieldKey getFieldKeyInternal(String fieldId) throws DataProviderException {
        if (fieldKeysMap == null) {
            if (fieldKeys == null) {
                getFieldKeys();
                if (fieldKeys == null) {
                    return null;
                }
            }
            fieldKeysMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            for (int i = 0; i < fieldKeys.length; i++) {
                fieldKeysMap.put(fieldKeys[i].getFieldId(), fieldKeys[i]);
            }
        }
        FieldKey fieldKey = (FieldKey) fieldKeysMap.get(fieldId);
        if (fieldKey != null) {
            return fieldKey;
        } else {
            throw new IllegalArgumentException(fieldId);
        }
    }


    /** {@inheritDoc} */
    public FieldKey[] getFieldKeys() throws DataProviderException {
        if (fieldKeys == null) {
            ResultSetMetaData metaData = getMetaData();
            if (metaData == null) {
                return FieldKey.EMPTY_ARRAY;
            }
            try {
                fieldKeys = new FieldKey[metaData.getColumnCount()];
                for (int i = 0; i < fieldKeys.length; i++) {
                    String tableName = "";
                    if (metaData.getTableName(i + 1) != null && !metaData.getTableName(i + 1).equals("")) {
                        tableName = metaData.getTableName(i + 1) + ".";
                    }
                    fieldKeys[i] = new FieldKey(tableName + metaData.getColumnName(i + 1));
                }
            } catch (SQLException e) {
                fieldKeys = null;
                throw new RuntimeException(e);
            }
        }
        if (fieldKeys != null) {
            return fieldKeys;
        }
        return FieldKey.EMPTY_ARRAY;
    }


    /** {@inheritDoc} */
    public Class getType(FieldKey fieldKey) throws DataProviderException {

        ResultSetMetaData metaData = getMetaData();
        if (metaData != null) {
            try {
                int column = column(fieldKey);
                if (column > 0) {
                    String className = metaData.getColumnClassName(column);
		    // System.out.println("column: "+column + "   className: "+className + "   type: "+metaData.getColumnType(column));

		    if ( Beans.isDesignTime() && (className.equals("oracle.sql.TIMESTAMP")) ) {
                        // Special processing for non-standard Oracle classes @ design time
                        // return Class.forName(className,
                        //                      true,
                        //                      getDriverClassLoader(getClass().getClassLoader()));
                        // Return the appropriate Java class instead
                        return java.sql.Timestamp.class;
                    } else if ( Beans.isDesignTime() && (className.equals("oracle.sql.CLOB")) ) {
			// Convert Oracle class into standard Java class. Note that no oracle.sql.CLOB extends java.sql.Clob, so only the
			// type needs to be converted, not the value.
			return java.sql.Clob.class;
		    } else {
                        return Class.forName(className);
                    }
                } else {
                    throw new IllegalArgumentException(fieldKey.getFieldId());
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;

    }


    // ---------------------------------------------- TableDataProvider Methods


    /** {@inheritDoc} */
    public RowKey appendRow() throws DataProviderException {
        if (!canAppendRow()) {
            return null; // spec silent on what to do here
        }
        boolean saveShowDeleted = false;
        try {
            saveShowDeleted = getCachedRowSet().getShowDeleted();
            getCachedRowSet().setShowDeleted(true);
            try {
                getCachedRowSet().last();
                int newRow = getCachedRowSet().getRow(); // will be correct for new row (zero-based)
                getCachedRowSet().moveToInsertRow();
                ResultSetMetaData rsmd = getMetaData();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    if (rsmd.isNullable(i) == ResultSetMetaData.columnNoNulls &&
                        rsmd.isWritable(i) == true) {
                        getCachedRowSet().updateObject(i, manufacturePlaceholder(rsmd.getColumnClassName(i), false, rsmd.getColumnType(i)));
                    } 
                }
                getCachedRowSet().insertRow();
                return new CachedRowSetRowKey(newRow); // zero based
            } catch (SQLException e2) {
                throw new RuntimeException(e2);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                getCachedRowSet().setShowDeleted(saveShowDeleted);
            } catch (SQLException e) {
            }
        }
    }


    /** {@inheritDoc} */
    public boolean canInsertRow(RowKey beforeRow) throws DataProviderException {
        return false;

    }

    /** {@inheritDoc} */
    public boolean canAppendRow() throws DataProviderException {
        try {
            return isUpdatable();
        } catch (SQLException e) {
            return false;
        }
    }

    /** {@inheritDoc} */
    public boolean canRemoveRow(RowKey row) throws DataProviderException {
        try {
            return isUpdatable() && isRowAvailable(row);
        } catch (SQLException e) {
            return false;
        }
    }


    /** {@inheritDoc} */
    public int getRowCount() throws DataProviderException {
        if (getCachedRowSet() == null) {
            return 0;
        }
        int rowCount = 0;
        int cursorIndexSave = getCursorIndex();
        try {
            int startIndex = 0;
            while (absolute(startIndex+1)) {
                try {
                    if (!(isUpdatable() && getCachedRowSet().rowDeleted())) {
                        rowCount++;
                    }
                } catch (SQLException e) {
                    return -1;
                }
                startIndex++;
            }
            return rowCount;
        } finally {
            try {
                setCursorIndex(cursorIndexSave);
            } catch (IllegalArgumentException e) {
                // This can happen if the row at the cursorIndex was deleted
            }
        }
    }


    /** {@inheritDoc} */
    public Object getValue(FieldKey fieldKey, RowKey row) throws DataProviderException {

	// try {
        //      System.out.println("Entering CRSDP.getValue, fieldKey: " + fieldKey + "  rowKey: " + row +
        //                         "colClassName: " + getMetaData().getColumnClassName(column(fieldKey)) +
        //                         "  colType: " + getMetaData().getColumnType(column(fieldKey)));
        // } catch (java.sql.SQLException e) {}

        if (Beans.isDesignTime()) {
            try {
                return manufacturePlaceholder(
                    getMetaData().getColumnClassName(column(fieldKey)),
                    true,
                    getMetaData().getColumnType(column(fieldKey)));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (getCachedRowSet() != null && row instanceof CachedRowSetRowKey) {
            try {
                checkExecute();
                if (absolute(((CachedRowSetRowKey)row).getIndex() + 1)) {

                    Object obj = getCachedRowSet().getObject(column(fieldKey));
                    if (getMetaData().getColumnClassName(column(fieldKey)).equals("oracle.sql.TIMESTAMP"))
                    {
                        try {
                            // If we have an Oracle TIMESTAMP object, convert to standard JDBC class
                            // Use reflection to avoid runtime dependency
                            Class c = Class.forName("oracle.sql.TIMESTAMP");
                            if (obj.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
                                 Method m = c.getMethod("toJdbc", (Class[]) null);
                                 Object newObj = m.invoke(obj, (Object[]) null);
                                 return newObj;
                             } else {
                                 return obj;
                             }
                        } catch (Exception e) {
                            // ClassNotFoundException, NoSuchMethodException, IllegalAccessException
                            return obj;
                        }

                    } else {
                        return obj;
                    }
                } else {
                    throw new IndexOutOfBoundsException("" + row);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;

    }


    /** {@inheritDoc} */
    public RowKey insertRow(RowKey beforeRow) throws DataProviderException {
        return null; // spec is silent on what to do here

    }


    /** {@inheritDoc} */
    public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException {

        try {
            if (!isUpdatable()) {
                return true;
            }
            ResultSetMetaData metaData = getMetaData();
            if (metaData.isReadOnly(column(fieldKey))) {
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }


    /** {@inheritDoc} */
    public boolean isRowAvailable(RowKey row) throws DataProviderException {
        if (!(row instanceof CachedRowSetRowKey)) {
            return false;
        }
        int index = ((CachedRowSetRowKey)row).getIndex();
        if (Beans.isDesignTime()) {
            return (index >= 0) && (index <= 2);
        }
        if (getCachedRowSet() != null) {
            try {
                checkExecute();
                if (absolute(index + 1)) {
                    if (isUpdatable() && getCachedRowSet().rowDeleted()) {
                        return false;
                    } else {
                        return true;
                    }
                }
            } catch (SQLException e) {
                return false;
            }
        }
        return false;

    }


    /** {@inheritDoc} */
    public void removeRow(RowKey row) throws DataProviderException {
        int index = ((CachedRowSetRowKey)row).getIndex();
        if (getCachedRowSet() != null) {
            try {
                if (!isUpdatable()) {
                    throw new IllegalArgumentException("" + row); // What should we throw?
                }
                if (absolute(index + 1)) {
                    if (!getCachedRowSet().rowDeleted()) {
                        getCachedRowSet().deleteRow();
                    } // FIXME: else do we throw an exception if already deleted?
                } else {
                    throw new IllegalArgumentException("" + row);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalStateException();
        }

    }


    /** {@inheritDoc} */
    public void setValue(FieldKey fieldKey, RowKey row, Object value) throws DataProviderException {

	// System.out.println("Entering setValue, fieldKey: " + fieldKey + "  rowKey: " + row + "  value: " + value);

        if (getCachedRowSet() != null && row instanceof CachedRowSetRowKey) {
            try {
                if (!isUpdatable()) {
                    throw new IllegalStateException("" + fieldKey.getFieldId());
                }
                if (absolute(((CachedRowSetRowKey)row).getIndex() + 1)) {
                    if (getCachedRowSet().rowDeleted()) {
                        throw new IllegalStateException("" + fieldKey.getFieldId());
                    }
                    if (isReadOnly(fieldKey)) {
                        throw new IllegalStateException("" + fieldKey.getFieldId());
                    }
                    int column = column(fieldKey);

//                      if (getMetaData().getColumnClassName(column(fieldKey)).equals("oracle.sql.TIMESTAMP")) {
//                          // Extra code to create oracle.sql.TIMESTAMP objects from java.sql.Timestamp
//                          // May not be required
//                      }
                    
                    Object old = getCachedRowSet().getObject(column);
                    boolean changed = false;
                    if (old == null) {
                        changed = value != null;
                    } else if (value == null) {
                        changed = true;
                    } else {
                        changed = !old.equals(value);
                    }

                    if (changed) {
                        getCachedRowSet().updateObject(column, value);
                        getCachedRowSet().updateRow();
                        fireValueChanged(fieldKey, row, old, value);
                        if (row != null && row.equals(getCursorRow())) {
                            fireValueChanged(fieldKey, old, value);
                        }
                    }
                } else {
                    throw new IndexOutOfBoundsException("" + row);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    // -------------------------------------- RefreshableDataProvider Methods

    /** {@inheritDoc} */
    public void addRefreshableDataListener(RefreshableDataListener listener) {
        super.addDataListener(listener);
    }

    /** {@inheritDoc} */
    public void removeRefreshableDataListener(RefreshableDataListener listener) {
        super.removeDataListener(listener);
    }

    /** {@inheritDoc} */
    public RefreshableDataListener[]  getRefreshableDataListeners() {
        if (dpListeners == null) {
            return new RefreshableDataListener[0];
        } else {
            ArrayList rdList = new ArrayList();
            for (int i = 0; i < dpListeners.length; i++) {
                if (dpListeners[i] instanceof RefreshableDataListener) {
                    rdList.add(dpListeners[i]);
                }
            }
            return (RefreshableDataListener[])rdList.toArray(
                new RefreshableDataListener[rdList.size()]);
        }
    }

    /** {@inheritDoc} */
    public void refresh() throws DataProviderException {
        if (getCachedRowSet() != null && getCachedRowSet() instanceof CachedRowSet) {
            try {
                ((CachedRowSet)getCachedRowSet()).release();
                fireRefreshed();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    // -------------------------------------- TransactionalDataProvider Methods


    /** {@inheritDoc} */
    public void commitChanges() throws DataProviderException {
        if (getCachedRowSet() != null) {
            try {
                checkExecute();
                try {
                    getCachedRowSet().acceptChanges();
                    fireChangesCommitted();
                } catch (SyncProviderException spe) {
                    SyncResolver resolver = spe.getSyncResolver();
                    String message = null ;
                    if ( resolver == null ) {
                        message = spe.getMessage() ;
                    } else {
                        while(  resolver.nextConflict())  {
                            int row = resolver.getRow();
                            
                            //TODO: use MessageFormat for these error strings
                            String pattern = "{0} {1} {2}" ; // NOI18N
                            switch (resolver.getStatus()) {
                            case SyncResolver.DELETE_ROW_CONFLICT:
                                pattern = bundle.getString("DELETE_ROW_CONFLICT") ; //NOI18N
                                break;
                            case SyncResolver.INSERT_ROW_CONFLICT:
                                pattern = bundle.getString("INSERT_ROW_CONFLICT") ; // NOI18N
                                break;
                            case SyncResolver.UPDATE_ROW_CONFLICT:
                                pattern = bundle.getString("UPDATE_ROW_CONFLICT") ; // NOI18N
                                break;
                            }
                            String sqlExceptionText = "" ;
                            if (resolver instanceof SyncResolverX) {
                                sqlExceptionText = ((SyncResolverX)resolver).getSQLException().getLocalizedMessage() ;
                            }
                            String[] args = new String[] { spe.getMessage(), Integer.toString(row-1), sqlExceptionText } ;
                            message = MessageFormat.format(pattern, args) ;
                            absolute(row) ;
                            int colCount = getCachedRowSet().getMetaData().getColumnCount();
                            for(int i = 1; i <= colCount; i++) {
                                try {
                                    if (resolver.getConflictValue(i) != null)  {
                                        message += ": " + resolver.getConflictValue( //NOI18N
                                            getCachedRowSet().getMetaData().getColumnName(i));
                                    }
                                } catch (SQLException se) {
                                    // should never be here.
                                    message += ": <unknown>" ; // NOI18N
                                }
                            }
                        }
                    }
                    throw new RuntimeException(message, spe);
                }
            } catch (SQLException sqle) {
                throw new RuntimeException(sqle);

            }
        }
    }


    /** {@inheritDoc} */
    public void revertChanges() throws DataProviderException {
        if (getCachedRowSet() != null) {
            try {
                checkExecute();
                ((CachedRowSet) getCachedRowSet()).restoreOriginal();
                fireChangesReverted();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /** {@inheritDoc} */
    public void addTransactionalDataListener(TransactionalDataListener listener) {
        super.addDataListener(listener);
    }


    /** {@inheritDoc} */
    public void removeTransactionalDataListener(TransactionalDataListener listener) {
        super.removeDataListener(listener);
    }

    /** {@inheritDoc} */
    public TransactionalDataListener[] getTransactionalDataListeners() {
        if (dpListeners == null) {
            return new TransactionalDataListener[0];
        } else {
            ArrayList tdList = new ArrayList();
            for (int i = 0; i < dpListeners.length; i++) {
                if (dpListeners[i] instanceof TransactionalDataListener) {
                    tdList.add(dpListeners[i]);
                }
            }
            return (TransactionalDataListener[])tdList.toArray(
                new TransactionalDataListener[tdList.size()]);
        }
    }


    // -------------------------------------------------------- Private Classes

    /**
     * <p>CachedRowSetDataProvider's private RowKey class
     */
    static private class CachedRowSetRowKey extends RowKey implements Serializable {

        /**
         * Constructs a new CachedRowSetRowKey from the passed rowId String, or returns
         * null if the passed rowId cannot be parsed into an int index.
         *
         * @param rowId The canonical row ID string to parse into an int
         * @return An CachedRowSetRowKey representing the passed rowId, or null if one
         *         could not be created.
         */
        public static CachedRowSetRowKey create(String rowId) {
            try {
                return new CachedRowSetRowKey(Integer.parseInt(rowId));
            } catch (NumberFormatException nfx) {
                return null;
            }
        }

        /**
         * Constructs an CachedRowSetRowKey using the specified index
         *
         * @param index The desired index
         */
        CachedRowSetRowKey(int index) {
            super(String.valueOf(index));
            this.index = index;
        }

        /**
         * Returns the index of this CachedRowSetRowKey
         *
         * @return This CachedRowSetRowKey's index value
         */
        int getIndex() {
            return index;
        }

        /**
         * Standard equals implementation.  This method compares the CachedRowSetRowKey
         * index values for == equality.  If the passed Object is not an
         * CachedRowSetRowKey instance, the superclass (RowKey) gets a chance to evaluate
         * the Object for equality.
         *
         * @param o the Object to check equality
         * @return true if equal, false if not
         * @see Object#equals(Object)
         */
        public boolean equals(Object o) {
            if (o instanceof CachedRowSetRowKey) {
                return ((CachedRowSetRowKey)o).getIndex() == getIndex();
            }
            return super.equals(o);
        }

        private int index;

        /**
         * <p>Return a printable version of this instance.</p>
         */
        public String toString() {
            return "CachedRowSetRowKey[" + index + "]";
        }
    }

    static private class ColumnNotSet implements Serializable {
        public String toString() {
            return null;
        }
    }

    private class RowSetPropertyChangeListener implements PropertyChangeListener, Serializable {
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equals("command") ||                 //NOI18N
                event.getPropertyName().equals("dataSourceName") ||          //NOI18N
                event.getPropertyName().equals("url") ||                     //NOI18N
                event.getPropertyName().equals("username")) {               //NOI18N
                    try {
                        setCursorIndex(0);
                    } catch (Exception e) {
                    }
                    metaData = null;
                    fieldKeys = null;
                    fieldKeysMap = null;
                    fireProviderChanged();
                }
        }
    }

    private class CachedRowSetListener implements RowSetListener, Serializable {
        public void cursorMoved(RowSetEvent event) {
        }

        public void rowChanged(RowSetEvent event) {
            try {
                int row = ((RowSet)event.getSource()).getRow();
                //Don't know what column changed
                //fireValueChanged(fieldKey, new CachedRowSetRowKey(row+1), old, value);
            } catch (SQLException e) {
                //FIXME: What to do?
            }
        }

        public void rowSetChanged(RowSetEvent event) {
            fireProviderChanged();
        }
    }


    // -------------------------------------------------------- Private Methods

    private void fireRefreshed() {
        RefreshableDataListener[] rdList = getRefreshableDataListeners();
        for (int i = 0; i < rdList.length; i++) {
            rdList[i].refreshed(this);
        }
    }


    /**
     * <p>Manufacture an empty (or as close to empty as we can get) instance
     * of class className (to be used a placeholder when inserting a row)</p>
     */
    static private Object manufacturePlaceholder(String className, boolean fakeData, int columnType)
        throws SQLException
    {
        // System.out.println("Entering manufacturePlaceholder, className: "+className + "  columnType: "+columnType+"  fakeData: "+fakeData);
        /* 
         * for classes we know don't have null constructors, we'll create them explicitly.
         */
        if (className.equals("java.sql.Date")) { // NOI18N
            return new Date(new java.util.Date().getTime());
        } else if (className.equals("java.sql.Time")) { // NOI18N
            return new Time(new java.util.Date().getTime());
        } else if (className.equals("java.sql.Timestamp")) { // NOI18N
            return new Timestamp(new java.util.Date().getTime());
        } else if (className.equals("java.math.BigDecimal")) { // NOI18N
            return new BigDecimal(fakeData? BigInteger.ONE: BigInteger.ZERO);
        } else if (className.equals("java.math.BigInteger")) { // NOI18N
            return fakeData? BigInteger.ONE: BigInteger.ZERO;
        } else if (className.equals("java.lang.Boolean")) { // NOI18N
            return fakeData? Boolean.TRUE: Boolean.FALSE;
        } else if (className.equals("java.lang.Byte")) { // NOI18N
            return new Byte(fakeData? (byte)123: (byte)0);
        } else if (className.equals("java.lang.Character")) { // NOI18N
            return new Character(fakeData? 'c': ' '); // NOI18N
        } else if (className.equals("java.lang.Double")) { // NOI18N
            return new Double(fakeData? 123.0: 0.0);
        } else if (className.equals("java.lang.Float")) { // NOI18N
            return new Float(fakeData? 123.0: 0.0);
        } else if (className.equals("java.lang.Integer")) { // NOI18N
            return new Integer(fakeData? 123: 0);
        } else if (className.equals("java.lang.Long")) { // NOI18N
            return new Long(fakeData? 123: 0);
        } else if (className.equals("java.lang.Short")) { // NOI18N
            return new Short((short)(fakeData? 123: 0));
        } else if (className.equals("java.lang.String")) { // NOI18N
            return fakeData? bundle.getString("arbitraryCharData"): ""; //NOI18N
        } else if (className.equals("java.sql.Blob")) { // NOI18N
           try {
            return new javax.sql.rowset.serial.SerialBlob(
                fakeData? (new byte[] {1, 2, 3, 4, 5}): (new byte[0]));
            } catch (SQLException e) {
                return new Object();
            }
        } else if (className.equals("javax.sql.SerialClob")) { // NOI18N
            try {
                return new javax.sql.rowset.serial.SerialClob(
                    fakeData? (bundle.getString("arbitraryClobData").toCharArray()): //NOI18N
                              new char[0]);
            } catch (SQLException e) {
                return new Object();
            }
        } else if (className.equals("java.net.URL")) { // NOI18N
            try {
                if (fakeData) {
                    return new java.net.URL("http://www.sun.com"); //NOI18N
                } else {
                    return new java.net.URL(""); // NOI18N
                }
            } catch (java.net.MalformedURLException e) {
                return new Object();
            }
        } else if (className.equals("java.sql.Array")) { // NOI18N
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
                };
        } else if (className.equals("char[]")) { // NOI18N
                return fakeData? new char[] { 'a', 'b', 'c', 'd', 'e'}: new char[0];
        } else if (className.equals("byte[]")) { // NOI18N
                return fakeData? new byte[] { 1, 2, 3, 4, 5}: new byte[0];
        } else if (className.equals("java.sql.Ref")) { // NOI18N
            return new java.sql.Ref() {
                private Object data = bundle.getString("arbitraryCharData"); //NOI18N
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
            };
        } else if (className.equals("java.sql.Struct")) { // NOI18N
            return new java.sql.Struct() {
                private String[] data = {
                    bundle.getString("arbitraryCharData"),   //NOI18N
                    bundle.getString("arbitraryCharData2"),  //NOI18N
                    bundle.getString("arbitraryCharData3")}; //NOI18N
                    public Object[] getAttributes() {
                    return data;
                }
                public Object[] getAttributes(Map map) {
                    return data;
                }
                public String getSQLTypeName() {
                    return "CHAR"; //NOI18N
                }
            };
        }

        if (fakeData) {
            // At least for now, let's try to use the old getFakeData
            //System.out.println("JK: temporary: fell through to use getFakeData()");
            return getFakeData(columnType);
        }

        try {
            return Class.forName(className).newInstance();
        } catch (ClassNotFoundException e) {
            return new ColumnNotSet();
        } catch (InstantiationException e) {
            return new ColumnNotSet();
        } catch (IllegalAccessException e) {
            return new ColumnNotSet();
        }
    }


    /**
     * <p>Check if rowset, if so, execute if necessary.</p>
     */
    protected void checkExecute() throws SQLException {
        if (!Beans.isDesignTime() && getCachedRowSet() != null) {
            try {
                getCachedRowSet().isBeforeFirst();
            } catch (SQLException e) {
                getCachedRowSet().execute();
                getCachedRowSet().first();
            }
        }
    }

    /**
     * <p>Check whether resultset is updatable or not.</p>
     */
    private boolean isUpdatable() throws SQLException {
        if (Beans.isDesignTime() || getCachedRowSet() == null) {
            return false;
        }
        checkExecute();
        if (getCachedRowSet().getConcurrency() == ResultSet.CONCUR_READ_ONLY) {
            return false;
        } else {
            return true;
         }
    }


    /**
     * <p>Return the one-relative column number corresponding to the
     * specified {@link FieldKey}, or zero if there is no such column
     * available.</p>
     *
     * @param fieldKey {@link FieldKey} representing this column
     */
    private int column(FieldKey fieldKey) throws DataProviderException {

        if (fieldKeys == null) {
            getFieldKeys();
        }
        if (fieldKeys != null) {
            for (int i = 0; i < fieldKeys.length; i++) {
                if (fieldKey.getFieldId().equals(fieldKeys[i].getFieldId())) {
                    return i + 1;
                }
            }
            /*
             * Here we are also matching on fieldKey,
             * so bug #6275441 workaround applies here too.
             * let's see if we hit bug #6275441
             */
            if (fieldKey.getFieldId().indexOf('.') == -1) {
                // fieldId is not prepended with a table (and schema), 
                // so we may have hit 6275441
                for (int i = 0; i < fieldKeys.length; i++) {
                    String val = fieldKeys[i].getFieldId() ;
                    int loc = val.lastIndexOf('.') ;
                    if ( loc >= 0 && loc+1 < val.length() ) {
                        val = val.substring(loc+1) ;
                    }
                    if ( val.equalsIgnoreCase(fieldKey.getFieldId() ) ) {
                        return i + 1;
                    }
                }
            }
        }
        return 0;
    }


    /**
     * <p>Fires a changesCommtted event to each registered {@link
     * TransactionalDataListener}.</p>
     */
    protected void fireChangesCommitted() {
        TransactionalDataListener[] tdList = getTransactionalDataListeners();
        for (int i = 0; i < tdList.length; i++) {
            tdList[i].changesCommitted(this);
        }
    }


    /**
     * <p>Fires a changesReverted event to each registered {@link
     * TransactionalDataListener}.</p>
     */
    protected void fireChangesReverted() {
        TransactionalDataListener[] tdList = getTransactionalDataListeners();
        for (int i = 0; i < tdList.length; i++) {
            tdList[i].changesReverted(this);
        }
    }


    /**
     * <p>Return the <code>ResultSetMetaData</code> instance for the
     * <code>ResultSet</code> we are wrapping, caching it the first
     * time it is requested.</p>
     */
    private ResultSetMetaData getMetaData() {

        if (metaData == null) {
            if (getCachedRowSet() != null) {
                try {
                    metaData = getCachedRowSet().getMetaData();
                } catch (SQLException e) {
                    throw new RuntimeException(e); // FIXME - exception type?
                }
            }
        }
        return metaData;

    }


    private static URLClassLoader getDriverClassLoader(ClassLoader parent) {

        File libDir = new File(System.getProperty("netbeans.user"), "jdbc-drivers"); // NOI18N

        File[] files = libDir.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith("jar") // NOI18N
                    || f.getName().toLowerCase().endsWith("zip"); // NOI18N
            }
        });
        
        int len = (files == null)? 0: files.length;

        URL[] urls = new URL[len];

        for (int i = 0; i < len; i++) {
            try {
                urls[i] = files[i].toURL();
            } catch (MalformedURLException e) {
                // ignore
            }
        }
        return URLClassLoader.newInstance(urls, parent);
    }

    // --------------------------------------------- Design Time Related Methods

    /**
     * <p>Return design time data of a type corresponding to that of the
     * specified column.</p>
     *
     * @param rsmd <code>ResultSetMetaData</code> for this result set
     * @param columnName Name of the requested column
     */
    private static Object getFakeData(int columnType)
      throws SQLException {

        switch (columnType) {

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
                };

            case Types.BIGINT:
                return new Long(123);

            case Types.BINARY:
                return new byte[] { 1, 2, 3, 4, 5};

            case Types.BIT:
                return new Boolean(true);

            case Types.BLOB:
                return new javax.sql.rowset.serial.SerialBlob(new byte[]
                  {1, 2, 3, 4, 5});

            case Types.BOOLEAN:
                return new Boolean(true);

            case Types.CHAR:
                return bundle.getString("arbitraryCharData"); //NOI18N

            case Types.CLOB:
                return new javax.sql.rowset.serial.SerialClob
                  (bundle.getString("arbitraryClobData").toCharArray()); //NOI18N

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
                return new Double(123);

            case Types.FLOAT:
                return new Double(123);

            case Types.INTEGER:
                return new Integer(123);

            case Types.JAVA_OBJECT:
                return bundle.getString("arbitraryCharData"); //NOI18N

            case Types.LONGVARBINARY:
                return new byte[] { 1, 2, 3, 4, 5};

            case Types.LONGVARCHAR:
                return bundle.getString("arbitraryCharData"); //NOI18N

            case Types.NULL:
                return null;

            case Types.NUMERIC:
                return new java.math.BigDecimal(java.math.BigInteger.ONE);

            case Types.OTHER:
                return null;

            case Types.REAL:
                return new Float(123);

            case Types.REF:
                return new java.sql.Ref() {

                    private Object data = bundle.getString("arbitraryCharData"); //NOI18N

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
                return new Short((short)123);

            case Types.STRUCT:
		return new java.sql.Struct() {

                    private String[] data = {
                        bundle.getString("arbitraryCharData"),   //NOI18N
                        bundle.getString("arbitraryCharData2"),  //NOI18N
                        bundle.getString("arbitraryCharData3")}; //NOI18N

                        public Object[] getAttributes() {
                        return data;
                    }

                    public Object[] getAttributes(Map map) {
                        return data;
                    }

                    public String getSQLTypeName() {
                        return "CHAR"; //NOI18N
                    }
                };

            case Types.TIME:
                return new java.sql.Time(new java.util.Date().getTime());

            case Types.TIMESTAMP:
                return new java.sql.Timestamp(new java.util.Date().getTime());

            case Types.TINYINT:
                return new Byte((byte)123);

            case Types.VARBINARY:
                return new byte[] { 1, 2, 3, 4, 5};

            case Types.VARCHAR:
                return bundle.getString("arbitraryCharData"); //NOI18N

            default:
                return null;

        }

    }
}
