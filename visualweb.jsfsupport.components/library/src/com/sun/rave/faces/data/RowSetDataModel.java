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

import java.beans.Beans;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;
import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import com.sun.rave.faces.util.ComponentBundle;

/**
 * <p>Runtime implementation of <code>javax.faces.model.DataModel</code>
 * that caches retrieved database data in memory (in a session scope
 * attribute of type <code>DataCache</code>), even when the underlying
 * rowset is closed.</p>
 *
 * @author  craigmcc
 */
public class RowSetDataModel extends DataModel {

    // ------------------------------------------------------------ Constructors


    /**
     * <p>Create a new {@link RowSetDataModel} instance not connected to
     * any underlying <code>RowSet</code>.</p>
     */
    public RowSetDataModel() {

        this(null);

    }

    /**
     * <p>Create a new {@link RowSetDataModel} instance wrapping the
     * specified <code>RowSet</code>.</p>
     *
     * @param rowSet <code>RowSet</code> to be mapped
     */
    public RowSetDataModel(RowSet rowSet) {

        super();
        setWrappedData(rowSet);

    }

    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The number of fake data rows we will compose at
     * design time.</p>
     */
    private static final int DESIGN_TIME_ROWS = 5;

    /**
     * <p>Localization resources for this package.</p>
     */
    private static final ComponentBundle bundle =
        ComponentBundle.getBundle(RowSetDataModel.class);

    /**
     * <p>List of column names for the <code>RowSet</code> we are
     * currently connected to (if any).</p>
     */
    private List columnNames = new ArrayList();

    /**
     * <p>identifies whether or not the underlying database can support
     * the conditonal clause used to handle nulls
     * (see compooseUpdateAndDeleteStatements).</p>
     */
    private boolean useConditionalWhereClause = true;

    /**
     * <p>List of column SQL types for the <code>RowSet</code> we are
     * currently connected to (if any).</p>
     */
    private List columnTypes = new ArrayList();
    
    /**
     * <p>List of column Java types (<code>Class</code> objects) 
     * for the <code>RowSet</code> we are
     * currently connected to (if any).</p>
     */
    private List columnJavaTypes = new ArrayList();

    /**
     * <p>The {@link DataCache} containing our cached row and
     * column information.  A new instance (saved in session scope)
     * is created on demand if not already present.</p>
     */
    private DataCache dataCache = null;

    /**
     * <p>The session attribute key under which our
     * <code>DataCache</code> instance will be stored.
     */
    private String dataCacheKey = null;

    /**
     * <p><code>RowSetListener</code> for significant events
     * on the <code>RowSet</code> we are currently connected to (if any).</p>
     */
    private RowSetListener listener = new CachedRowSetListener();

    /**
     * <p>The <code>ResultSetMetaData</code> associated with the
     * <code>RowSet</code> we are currently connected to (if any).</p>
     */
    private ResultSetMetaData metadata = null;

    /**
     * <p>The zero relative index of the currently positioned row,
     * or -1 if we are not positioned on a row.</p>
     */
    private int rowIndex;

    /**
     * <p>The <code>RowSet</code> that we are currently connected to
     * (if any).</p>
     */
    private RowSet rowSet = null;

    /**
     * <p>The schema name on which we will perform updates when committing
     * changes to the database.  If specified, this will be included in the
     * UPDATE statement synthesized by the <code>commit()</code> method.</p>
     */
    private String schemaName = null;

    /**
     * <p>List of schema names for the columns of the <code>RowSet</code> we are
     * currently connected to (if any).</p>
     */
    private List schemaNames = new ArrayList();

    /**
     * <p>The table name on which we will perform updates when committing
     * changes to the database.</p>
     */
    private String tableName = null;

    /**
     * <p>List of table names for the columns of the <code>RowSet</code> we are
     * currently connected to (if any).</p>
     */
    private List tableNames = new ArrayList();

    // -------------------------------------------------------------- Properties


    /**
     * <p>Return the {@link DataCache} containing our cached row and
     * column data, creating one if necessary.</p>
     */
    public DataCache getDataCache() {

        // Create a new cache if one does not already exist
        Map sessionMap =
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        dataCache = (DataCache)
            sessionMap.get(getDataCacheKey());
        if (dataCache == null) {
            dataCache = new DataCache();
            sessionMap.put(getDataCacheKey(), dataCache);
        }
        return dataCache;

    }

    /**
     * <p>Return the session attribute key under which our {@link DataCache}
     * instance will be stored.
     */
    public String getDataCacheKey() {

        return this.dataCacheKey;

    }

    /**
     * <p>Set the session attribute key under which our
     * {@link DataCache} instance will be stored.
     *
     * @param dataCacheKey The new key
     */
    public void setDataCacheKey(String dataCacheKey) {

        this.dataCacheKey = dataCacheKey;

    }

    /**
     * <p>Return the <code>RowSet</code> we are connected with,
     * if any; otherwise, return <code>null</code>.  This is a
     * type=safe alias for <code>getWrappedData()</code>.</p>
     */
    public RowSet getRowSet() {

        return ((RowSet)getWrappedData());

    }

    /**
     * <p>Set the <code>RowSet</code> we are connected with,
     * or pass <code>null</code> to disconnect.  This is a
     * type-safe alias for <code>setWrappedData()</code>.</p>
     *
     * @param rowSet The <code>RowSet</code> we are connected to,
     *  or <code>null</code> to disconnect
     */
    public void setRowSet(RowSet rowSet) {

        setWrappedData(rowSet);

    }

    /**
     * <p>Return the name of the database schema containing the table
     * we will update when <code>commit()</code> is called.</p>
     */
    public String getSchemaName() {

        return this.schemaName;

    }

    /**
     * <p>Set the name of the database schema containing the table
     * we will update when <code>commit()</code> is called.</p>
     *
     * @param schemaName The schema name to be updated
     */
    public void setSchemaName(String schemaName) {

        this.schemaName = schemaName;

    }

    /**
     * <p>Return the name of the database table we will update when
     * <code>commit()</code> is called.</p>
     */
    public String getTableName() {

        return this.tableName;

    }

    /**
     * <p>Set the name of the database table we will update when
     * <code>commit()</code> is called.</p>
     *
     * @param tableName The table name to be updated
     */
    public void setTableName(String tableName) {

        this.tableName = tableName;

    }

    // --------------------------------------------------------- Public Methods


    /**
     * <p>Clear any cached row-specific data.  This method may be called by
     * application logic when it is known that the next page to be processed
     * will not involve the <code>RowSet</code> we are connected to, or
     * when the application wants to refresh the cached data to reflect
     * any changes on the underlying database contents.</p>
     *
     * <p><strong>NOTE</strong> - Calling <code>execute()</code> on this
     * <code>RowSetDataModel</code> will implicitly call <code>clear()</code>
     * for you.</p>
     */
    public void clear() {

        // System.out.println("RSDM: clear()");
        getDataCache().clear();

    }

    /**
     * <p>Push any deleted or updated cached rows to the specified table,
     * then call <code>commit()</code> on the JDBC <code>Connection</code>
     * underlying our current <code>RowSet</code>, as well as our associated
     * <code>DataCache</code>.  If any <code>SQLException</code> occurs, call
     * <code>rollback()</code> on the <code>Connection()</code> and
     * <code>reset()</code> on the <code>DataCache</code>.</p>
     *
     * @exception IllegalArgumentException if the <code>tableName</code>
     *  property has not yet been set
     * @exception IllegalStateException if this method is called when
     *  not connected to an underlying rowset
     * @exception SQLException if an error occurs while committing
     */
    public void commit() throws SQLException {

        // System.out.println("RSDM: commit()");

        // Validate our preconditions
        if (tableName == null) {
            throw new IllegalArgumentException(bundle.getMessage("noTableName")); // NOI18N
        }
        if (!connected()) {
            throw new IllegalStateException(bundle.getMessage("notConnected")); // NOI18N
        }

        // Local variables for resources we will need
        Connection conn = null;
        SQLException exception = null;
        PreparedStatement ustmt = null;
        PreparedStatement dstmt = null;
        String extracted = null; // Table name extracted from command
        List names = null; // Column names extracted from DBMD

        // Acquire the JDBC resources we will need
        try {

            // Extract a table name from the command if we do not have
            // one (because JDBC metadata did not include it).
            if ((getTableName() == null) || (getTableName().length() < 1)) {
                extracted = extract(getRowSet().getCommand());
            }

            // Acquire the Connection we will be using
            try {
                conn = getRowSet().getStatement().getConnection();
            } catch (NullPointerException e) {
                exception = new SQLException
                    (bundle.getMessage("noConnection"));
                throw new FacesException(exception);
            }

            // Retrieve the database metadata for this connection
            DatabaseMetaData dbmd = conn.getMetaData();

            // Set useConditionalWhereClause based on driver
            String driverName = dbmd.getDriverName();
            if (driverName != null && driverName.equals("DB2")) { // NOI18N
                useConditionalWhereClause = false;
            } else {
                useConditionalWhereClause = true;
            }

            // Always get column names that match the table, else we have no way
            // to throw away columns not in this table (if the driver does not
            // provide the tablename in ResultSetMetaData
            names = columns(dbmd, (extracted != null)? extracted: getTableName(), columnNames);

            String[] statements = composeStatements(extracted, names, useConditionalWhereClause, null);
            ustmt = conn.prepareStatement(statements[0]);
            dstmt = conn.prepareStatement(statements[1]);
        } catch (SQLException e) {

            exception = e;

        }

        // Scan cached rows, performing deletes and updates as needed
        try {

            if (exception == null) {
                Iterator keys = getDataCache().iterator();
                while (keys.hasNext()) {
                    Integer key = (Integer)keys.next();
                    DataCache.Row row = getDataCache().get(key.intValue());
                    DataCache.Column columns[] = row.getColumns();
                    if (row.isDeleted()) {
                        PreparedStatement tmpStmt = dstmt;
                        boolean requiresCustomDeleteStatement = false;
                        int dindex = 1;
                        if (useConditionalWhereClause) {
                            // Delete this row in the database
                            // System.out.println("RSDM: delete " + row);
                            for (int i = 0; i < columns.length; i++) {
                                if (match(i, names)) {
                                    // each column is set twice, see composeStatements
                                    tmpStmt.setObject(dindex++,
                                        columns[i].getOriginal(), columns[i].getSqlType());
                                    tmpStmt.setObject(dindex++,
                                        columns[i].getOriginal(), columns[i].getSqlType());
                                }
                            }
                        } else {
                            /*
                             * First, let's see if we can use the PreparedStatement or not.
                             * If the original values contain a null, we cannot use it since
                             * WHERE column-name = null does not work.  We'll need to prepare
                             * another statement that contains column-name IS NULL for all
                             * columns that are null
                             */
                            for (int i = 0; i < columns.length; i++) {
                                if (match(i, names)) {
                                    if (columns[i].getOriginal() == null) {
                                        requiresCustomDeleteStatement = true;
                                        break;
                                    }
                                }
                            }
                            if (requiresCustomDeleteStatement) {
                                tmpStmt = conn.prepareStatement(composeStatements(
                                    extracted, names, false, columns)[1]);
                            }
                            for (int i = 0; i < columns.length; i++) {
                                if (match(i, names)) {
                                    if (columns[i].getOriginal() != null) {
                                        tmpStmt.setObject(dindex++, columns[i].getOriginal());
                                    }
                                }
                            }
                        }
                        int dresult = tmpStmt.executeUpdate();
                        if (requiresCustomDeleteStatement) {
                            try {
                                tmpStmt.close();
                            } catch (SQLException e) {
                                // We'll do nothing if we can't close the statement
                            }
                        }
                        if (dresult < 1) {
                            exception = new SQLException
                                (bundle.getMessage("deleteFailedMissing", key)); // NOI18N
                            break;
                        } else if (dresult > 1) {
                            exception = new SQLException
                                (bundle.getMessage("deleteFailedMultiple", key)); // NOI18N
                            break;
                        }
                    } else if (row.isUpdated()) {
                        PreparedStatement tmpStmt = ustmt;
                        boolean requiresCustomUpdateStatement = false;
                        if (!useConditionalWhereClause) {
                            /*
                             * First, let's see if we can use the PreparedStatement or not.
                             * If the original values contain a null, we cannot use it since
                             * WHERE column-name = null does not work.  We'll need to prepare
                             * another statement that contains column-name IS NULL for all
                             * columns that are null
                             */
                            for (int i = 0; i < columns.length; i++) {
                                if (match(i, names)) {
                                    if (columns[i].getOriginal() == null) {
                                        requiresCustomUpdateStatement = true;
                                        break;
                                    }
                                }
                            }
                            if (requiresCustomUpdateStatement) {
                                tmpStmt = conn.prepareStatement(composeStatements(extracted, names, false, columns)[0]);
                            }
                        }
                        // Update this row in the database
                        // System.out.println("RSDM: update " + row);
                        int uindex = 1;
                        for (int i = 0; i < columns.length; i++) {
                            if (match(i, names)) {
                                tmpStmt.setObject(uindex++,
                                    columns[i].getValue(), columns[i].getSqlType());
                            }
                        }
                        // Where clause
                        if (useConditionalWhereClause) {
                            for (int i = 0; i < columns.length; i++) {
                                if (match(i, names)) {
                                    /*
                                     * This is the where clause, so we set every column
                                     * twice, see composeStatements
                                     */
                                    tmpStmt.setObject(uindex++,
                                        columns[i].getOriginal(), columns[i].getSqlType());
                                    tmpStmt.setObject(uindex++,
                                        columns[i].getOriginal(), columns[i].getSqlType());
                                }
                            }
                        } else {
                            /*
                             * This is the where clause, we only set the property if the
                             * column is non-null.  If it is null, a custom PreparedStatement
                             * was created with the "<column-name> IS NULL" syntax.
                             */
                            for (int i = 0; i < columns.length; i++) {
                                if (match(i, names)) {
                                    if (columns[i].getOriginal() != null) {
                                        ustmt.setObject(uindex++, columns[i].getOriginal());
                                    }
                                }
                            }
                        }
                        int uresult = tmpStmt.executeUpdate();
                        if (requiresCustomUpdateStatement) {
                            try {
                                tmpStmt.close();
                            } catch (SQLException e) {
                                // We'll do nothing if we can't close the statement
                            }
                        }
                        if (uresult < 1) {
                            exception = new SQLException
                                (bundle.getMessage("updateFailedMissing", key)); // NOI18N
                            break;
                        } else if (uresult > 1) {
                            exception = new SQLException
                                (bundle.getMessage("updateFailedMultiple", key)); // NOI18N
                            break;
                        }
                    }
                }
            }

        } catch (SQLException e) {

            exception = e;

        }

        // Commit on the database and associated cache
        try {
            if (exception == null) {
                conn.commit();
                getDataCache().commit();
            }
        } catch (SQLException e) {
            exception = e;
        }

        // Roll back if necessary
        if (exception != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                ;
            }
            getDataCache().reset();
        }

        // Free allocated resources
        if (ustmt != null) {
            try {
                ustmt.close();
            } catch (SQLException e) {
                ;
            }
            ustmt = null;
        }
        if (dstmt != null) {
            try {
                dstmt.close();
            } catch (SQLException e) {
                ;
            }
            dstmt = null;
        }

        // Rethrow any saved exception
        if (exception != null) {
            throw exception;
        }

    }

    /**
     * <p>Clear any cached data, then re-execute the
     * query for the rowset we are connected to.  <strong>WARNING</strong> -
     * this method should <strong>ONLY</strong> be called when you
     * have changed the query parameters for your query, or your
     * application has performed other database transaction(s) on a
     * different page that should be reflected in the query results.</p>
     *
     * @exception IllegalStateException if this method is called when
     *  not connected to an underlying rowset
     * @exception SQLException if an error occurs executing the rowset
     */
    public void execute() throws SQLException {

        // System.out.println("RSDM: execute()");

        // Validate our preconditions
        if (!connected()) {
            throw new IllegalStateException(bundle.getMessage("notConnected")); // NOI18N
        }

        // Call clear() on the underlying cache
        getDataCache().clear();

        // Call execute() on the underlying rowset
        getRowSet().execute();

    }

    /**
     * <p>Reset any deleted or updated values in the cache, so that any cached
     * rows no longer appear to have been modified.  This method has no
     * effect on the connected rowset (if any).</p>
     */
    public void reset() {

        // System.out.println("RSDM: reset()");
        getDataCache().reset();

    }

    /**
     * <p>Reset any deleted or updated values in the cache (as is done by
     * the <code>reset()</code> method), then call <code>rollback()</code>
     * on the JDBC <code>Connectino</code> underlying our current
     * <code>RowSet</code>.</p>
     *
     * @exception IllegalStateException if this method is called when
     *  not connected to an underlying rowset
     * @exception SQLException if an error occurs while committing
     */
    public void rollback() throws SQLException {

        // System.out.println("RSDM: rollback()");

        // Validate our preconditions
        if (!connected()) {
            throw new IllegalStateException(bundle.getMessage("notConnected")); // NOI18N
        }

        // Reset the cache
        getDataCache().reset();

        // Roll back the database connection
        getRowSet().getStatement().getConnection().rollback();

    }

    /**
     * <p>Set the designated parameter on the <code>RowSet</code> to which
     * we are connected.  This parameter will have no effect on any currently
     * selected rows; it takes effect the next time you call
     * <code>execute()</codew>.</p>
     *
     * @param index One-relative parameter index
     * @param value Value to be set
     *
     * @exception IllegalStateException if this method is called when
     *  not connected to an underlying rowset
     * @exception SQLException if an error occurs setting the parameter value
     */
    public void setObject(int index, Object value) throws SQLException {

        // Validate our preconditions
        if (!connected()) {
            throw new IllegalStateException(bundle.getMessage("notConnected")); // NOI18N
        }

        // Set the parameter value
        getRowSet().setObject(index, value);

    }

    /**
     * <p>Set the designated parameter on the <code>RowSet</code> to which
     * we are connected.  This parameter will have no effect on any currently
     * selected rows; it takes effect the next time you call
     * <code>execute()</codew>.</p>
     *
     * @param index One-relative parameter index
     * @param value Value to be set
     * @param type Destination SQL type (as defined in <code>java.sql.Types</code>)
     *
     * @exception IllegalStateException if this method is called when
     *  not connected to an underlying rowset
     * @exception SQLException if an error occurs setting the parameter value
     */
    public void setObject(int index, Object value, int type) throws SQLException {

        // Validate our preconditions
        if (!connected()) {
            throw new IllegalStateException(bundle.getMessage("notConnected")); // NOI18N
        }

        // Set the parameter value
        getRowSet().setObject(index, value, type);

    }

    /**
     * <p>Set the designated parameter on the <code>RowSet</code> to which
     * we are connected.  This parameter will have no effect on any currently
     * selected rows; it takes effect the next time you call
     * <code>execute()</codew>.</p>
     *
     * @param index One-relative parameter index
     * @param value Value to be set
     * @param type Destination SQL type (as defined in <code>java.sql.Types</code>)
     * @param scale For <code>java.sql.Types.DECIMAL</code> or
     *  <code>java.sql.Types.NUMERIC</code> types, the number of digits
     *  after the decimal point (ignored for all other types)
     *
     * @exception IllegalStateException if this method is called when
     *  not connected to an underlying rowset
     * @exception SQLException if an error occurs setting the parameter value
     */
    public void setObject(int index, Object value, int type, int scale) throws SQLException {

        // Validate our preconditions
        if (!connected()) {
            throw new IllegalStateException(bundle.getMessage("notConnected")); // NOI18N
        }

        // Set the parameter value
        getRowSet().setObject(index, value, type, scale);

    }

    // ------------------------------------------------------- DataModel Methods


    /**
     * <p>Return -1 to indicate that the number of rows available is
     * unknown.</p>
     */
    public int getRowCount() {

        if (Beans.isDesignTime()) {
            return DESIGN_TIME_ROWS;
        }

        return ( -1);

    }

    /**
     * <p>Return a <code>Map</code> representing the column values for
     * the row specified by the current <code>rowIndex</code>.  The
     * returned Map supports case-insensitive matching on column names,
     * and records any updates to the column values for later transfer
     * to the database when <code>commit()</code> is called.  It does
     * not allow column names (and corresponding values) to be added
     * or removed.</p>
     *
     * @exception IllegalArgumentException if there is no cached
     *  data for the current <code>rowIndex</code>
     */
    public Object getRowData() {

        // System.out.println("RSDM: getRowData(" + rowIndex + ") --> " + getDataCache().get(rowIndex));

        DataCache.Row row = getDataCache().get(rowIndex);
        if (row == null) {
            throw new IllegalArgumentException("" + rowIndex);
        }
        return row;

    }

    /**
     * <p>Return the zero-relative index of the currently positioned row,
     * or -1 if we are not positioned on a row.</p>
     */
    public int getRowIndex() {

        return (rowIndex);

    }

    /**
     * <p>Return the <code>RowSet</code> we are currently wrapping,
     * if any.</p>
     */
    public Object getWrappedData() {

        return (rowSet);

    }

    /**
     * <p>Return <code>true</code> if there is a cache entry for the
     * current <code>rowIndex</code> value.</p>
     */
    public boolean isRowAvailable() {

        //designtime check only
        if (Beans.isDesignTime()) {
            if (rowIndex < 0 || rowIndex >= DESIGN_TIME_ROWS) {
                return false;
            }
        }

        //designtime and runtime
        if (rowIndex >= 0) {
            return getDataCache().get(rowIndex) != null;
        } else {
            return false;
        }

    }
    
    //to be called at designtime only as part of fix for 6333068
    private boolean shouldRowBeAvailable() {
        return rowIndex >= 0 && rowIndex < DESIGN_TIME_ROWS;
    }

    /**
     * <p>Set the zero relative index for the newly positioned row, or
     * set to -1 for no currently selected row.  If there is no current
     * cache entry for this row, but we are currently connected, position
     * the underlying <code>RowSet</code> to the corresponding one-relative
     * row number, and create a new cache entry.  In addition, fire a
     * <code>DataModelEvent</code> if needed, per the Javadocs for
     * this method on <code>javax.faces.model.DataModel</code>.</p>
     *
     * @param rowIndex The row index, or -1 for no selected row
     *
     * @exception FacesException if an error occurs setting the row index
     * @exception IllegalArgumentException if rowIndex is less than -1
     */
    public void setRowIndex(int rowIndex) {

        // System.out.println("RSDM: setRowIndex(" + rowIndex + ")");

        // Bounds check on the incoming argument
        if (rowIndex < -1) {
            throw new IllegalArgumentException
                (bundle.getMessage("invalidRowIndex", new Integer(rowIndex))); // NOI18N
        }

        // Update the current row index
        int oldIndex = this.rowIndex;
        this.rowIndex = rowIndex;

        // If we are not connected, nothing else to do
        if (!connected()) {
            return;
        }

        // Construct a new cache item if necessary
        if ((rowIndex >= 0) && connected() &&
            !Beans.isDesignTime() &&
            (getDataCache().get(rowIndex) == null)) {
            DataCache.Row row = create();
            if (row != null) {
                getDataCache().add(rowIndex, row);
            }
        }

        // Broadcast an event to interested listeners if we changed rows
        DataModelListener listeners[] = getDataModelListeners();
        if ((oldIndex != rowIndex) && (listeners != null)) {
            Object rowData = null;
            if (Beans.isDesignTime()) {
                if (shouldRowBeAvailable() && !isRowAvailable()) {
                    synchronize();
                }
            }
            if (isRowAvailable()) {
                rowData = getRowData();
            }
            DataModelEvent event =
                new DataModelEvent(this, rowIndex, rowData);
            int n = listeners.length;
            for (int i = 0; i < n; i++) {
                if (null != listeners[i]) {
                    listeners[i].rowSelected(event);
                }
            }
        }

    }

    /**
     * <p>Set the <code>RowSet</code> wrapped by this
     * <code>RowSetDataModel</code>, or <code>null</code> to disconnect
     * from the previously connected <code>RowSet</code>.</p>
     *
     * @param rowSet <code>RowSet</code> to be wrapped, or <code>null</code>
     *  to disconnect
     *
     * @exception ClassCastException if this object is not of the
     *  correct type
     */
    public void setWrappedData(Object rowSet) {

        if (rowSet == null) {
            disconnect();
        } else {
            disconnect();
            connect((RowSet)rowSet);
        }

    }

    // --------------------------------------------------------- Private Methods


    /**
     * <p>Return the subset of the specified set of column names (from the
     * original query) that are part of the specified table.  This is useful
     * on databases whose JDBC driver does not include column names in the
     * <code>ResultSetMetaData</code>.</p>
     *
     * @param conn <code>Connection</code> from which we can acquire
     *  database metadata
     * @param extracted The table or schema.table identifier extracted
     *  from our query
     * @param columns List of column names included in the query (of which
     *  a subset will be returned)
     *
     * @exception SQLException if an error occurs processing the metadata
     */
    private List columns(DatabaseMetaData dbmd, String extracted, List columns) throws SQLException {

        // Set useConditionalWhereClause based on driver
        String driverName = dbmd.getDriverName();
        if (driverName != null && driverName.equals("DB2")) {
            useConditionalWhereClause = false;
        } else {
            useConditionalWhereClause = true;
        }

        // Narrow our results down to the table of interest
        String schemaName = null;
        String tableName = extracted;
        int period = tableName.lastIndexOf('.');
        if (period >= 0) {
            schemaName = tableName.substring(0, period);
            tableName = tableName.substring(period + 1);
        }
        ResultSet trs = dbmd.getTables(null, schemaName, tableName, null);
        // if more than one table, take first one (what else can we do?)
        if (!trs.next()) {
            throw new SQLException(tableName + " not found");
        }

        // Retrieve the column names for the table of interest
        // and save the ones that match
        ResultSet crs =
            dbmd.getColumns(trs.getString("TABLE_CAT"),
            trs.getString("TABLE_SCHEM"),
            trs.getString("TABLE_NAME"), "%");
        trs.close();
        List results = new ArrayList();
        int n = columns.size();
        while (crs.next()) {
            String name = crs.getString("COLUMN_NAME");
            for (int i = 0; i < n; i++) {
                if (name.equalsIgnoreCase((String)columns.get(i))) {
                    results.add(name);
                    break;
                }
            }
        }
        crs.close();
        return results;

    }

    /**
     * <p>Connect ourselves to the specified new <code>RowSet</code>.</p>
     *
     * @param rowSet The new <code>RowSet</code> to connect to
     */
    private void connect(RowSet rowSet) {

        this.rowSet = rowSet;
        getRowSet().addRowSetListener(listener);
        synchronize();

    }

    /**
     * <p>Return <code>true</code> if we are currently connected to an
     * underlying <code>RowSet</code>.</p>
     */
    private boolean connected() {

        return (rowSet != null);

    }

    /**
     * <p>Create and return a new {@link DataCache.Row} representing the
     * row at the currently set <code>rowIndex</code>, if there is
     * actually such a row in the underlying rowset.  If there is no
     * such row, return <code>null</code>.  Assumes that we
     * are connected, and that rowIndex is non-negative.</p>
     *
     * @exception FacesException if an error occurs creating this item
     */
    private DataCache.Row create() {

        initialize();

        try {

            // Position based on the current row index
            if (!getRowSet().absolute(rowIndex + 1)) { // One relative
                return (null);
            }

            // Create a CacheItem for the contents of the current row
            DataCache.Column columns[] =
                new DataCache.Column[columnNames.size()];
            for (int i = 0; i < columnNames.size(); i++) {
                columns[i] =
                    getDataCache().createColumn((String)schemaNames.get(i),
                    (String)tableNames.get(i),
                    (String)columnNames.get(i),
                    ((Integer)columnTypes.get(i)).intValue(),
                    (Class)columnJavaTypes.get(i),
                    getRowSet().getObject(i + 1));
            }
            DataCache.Row row = getDataCache().createRow(columns);
            return row;

        } catch (SQLException e) {
            throw new FacesException(e);
        }

    }

    /**
     * <p>Disconnect from the currently connected <code>RowSet</code>
     * (if any).  If we are not currently connected, do nothing.</p>
     */
    private void disconnect() {

        if (!connected()) {
            return;
        }

        getRowSet().removeRowSetListener(listener);
        metadata = null;
        rowSet = null;
        // NOTE - leave columnNames etc. because the cache is still alive
        // NOTE - leave rowIndex where it was for persistence
        // (but that won't help much behind a Data Table, because
        // the component will move the cursor around on its own)

    }

    /**
     * <p>Extract and return a table (or schema.table) name from
     * the specified command, which should be an SQL select statement.</p>
     *
     * @param command SQL command for this rowset
     */
    private String extract(String command) {

        boolean next = false;
        command = command.trim();
        String word;
        while (command.length() > 0) {
            int space = command.indexOf(' ');
            if (space >= 0) {
                word = command.substring(0, space).trim();
                command = command.substring(space + 1).trim(); ;
            } else {
                word = command.trim();
                command = "";
            }
            if (next) {
                int comma = word.indexOf(',');
                if (comma >= 0) {
                    word = word.substring(0, comma);
                }
                return word;
            } else if ("FROM".equalsIgnoreCase(word)) {
                next = true;
            }
        }
        return "";

    }

    /**
     * <p>Execute the rowset if needed, to avoid the need
     * to manually execute it, if we not at design time.</p>
     */
    private void initialize() {

        if (Beans.isDesignTime()) {
            return;
        }

        RowSet rowSet = getRowSet();
        try {
            if (rowSet.isBeforeFirst()) {
                try {
                    rowSet.first();
                } catch (SQLException x) {
                }
            }
        } catch (SQLException x1) {
            try {
                rowSet.execute();
            } catch (SQLException x2) {
                throw new FacesException(x2);
            }
        }

    }

    /**
     * <p>Return <code>true</code> if this column belongs to
     * the table we will be updating during a <code>commit()</code>.
     *
     * @param index Zero-relative index of the column to check
     * @param names Column names extracted from database metadata, or
     *  <code>null</code> to check the specific column information
     */
    private boolean match(int index, List names) {

        if (names != null) {
            String name = (String)columnNames.get(index);
            for (int i = 0; i < names.size(); i++) {
                if (name.equalsIgnoreCase((String)names.get(i))) {
                    return true;
                }
            }
            return false;
        }

        if ((schemaName != null) &&
            !schemaName.equalsIgnoreCase((String)schemaNames.get(index))) {
            return false;
        }
        if ((tableName != null) &&
            !tableName.equalsIgnoreCase((String)tableNames.get(index))) {
            return false;
        }
        return true;

    }

    /**
     * <p>Return the specified string in single quotes if it contains
     * any whitespace characters, or unchanged otherwise.</p>
     *
     * @param value String value to be optionally quoted
     */
    private String quoted(String value) {
        boolean required = false;
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                required = true;
                break;
            }
        }
        if (required) {
            return '"' + value + '"';
        } else {
            return value;
        }
    }

    /**
     * <p>Synchronize the metadata associated with the current
     * <code>RowSet</code>.  If the set of columns has changed
     * from the previous contents (if any), the cache will be
     * cleared.</p>
     */
    private void synchronize() {

        // System.out.println("RSDM: synchronize()");

        // Update the column metadata, and detect any changes
        boolean changed = false;
        try {
            DataCache.Column previous[] = new DataCache.Column[0];
            DataCache.Row row = null;
            Iterator keys = getDataCache().iterator();
            if (keys.hasNext()) {
                row = getDataCache().get(((Integer)keys.next()).intValue());
                previous = row.getColumns();
            }
            int m = previous.length;
            metadata = getRowSet().getMetaData();
            int n = metadata.getColumnCount();
            if (m != n) {
                changed = true;
            }
            columnNames.clear();
            columnTypes.clear();
            columnJavaTypes.clear();
            schemaNames.clear();
            tableNames.clear();
            for (int i = 1; i <= n; i++) {
                // Has this column changed?
                if (previous.length >= i) {
                    if (!previous[i - 1].getColumnName().equals(metadata.getColumnName(i)) ||
                        !previous[i - 1].getSchemaName().equals(metadata.getSchemaName(i)) ||
                        !previous[i - 1].getTableName().equals(metadata.getTableName(i))) {
                        changed = true;
                    }
                }
                // Save the new column names
                columnNames.add(metadata.getColumnName(i));
                columnTypes.add(new Integer(metadata.getColumnType(i)));
                Class javaType = null;
                try {
                    String javaTypeName = metadata.getColumnClassName(i);
                    javaType = Class.forName(javaTypeName);
                }
                catch (Exception jte) {
                    //let javaType be null
                }
                columnJavaTypes.add(javaType);
                schemaNames.add(metadata.getSchemaName(i));
                tableNames.add(metadata.getTableName(i));
            }
        } catch (SQLException e) {
            throw new FacesException(e);
        }

        // If the set of columns has changed, clear the cache
        if (!Beans.isDesignTime() && changed) {
            // System.out.println("RSDM: columns have changed, clear cache");
            getDataCache().clear();
        }

        // At design time only, cache some fake data
        // with the appropriate data types
        if (!Beans.isDesignTime()) {
            return;
        }
        for (int i = 0; i < DESIGN_TIME_ROWS; i++) {
            DataCache.Column columns[] =
                new DataCache.Column[columnNames.size()];
            for (int j = 0; j < columnNames.size(); j++) {
                String schemaName = "";
                String tableName = "";
                String columnName = (String)columnNames.get(j);
                int columnType = ((Integer)columnTypes.get(j)).intValue();
                Class columnJavaType = (Class)columnJavaTypes.get(j);
                Object fakeData;
                try {
                    fakeData = getFakeData(metadata, columnName);
                } catch (SQLException e) {
                    throw new FacesException(e);
                }
                columns[j] =
                    getDataCache().createColumn(schemaName, tableName,
                    columnName, columnType, columnJavaType, fakeData);
            }
            DataCache.Row row = getDataCache().createRow(columns);
            getDataCache().add(i, row);
        }

    }

    // --------------------------------------------------------- Private Classes


    /**
     * <p>Listener for significant changes on the <code>RowSet</code> our
     * parent is connected to.</p>
     */
    private class CachedRowSetListener implements RowSetListener {

        public void cursorMoved(RowSetEvent event) {
            ; // No action required
        }

        public void rowChanged(RowSetEvent event) {
            ; // No action required
        }

        public void rowSetChanged(RowSetEvent event) {
            // System.out.println("RSDM: rowSetChanged()");
            RowSetDataModel.this.synchronize();
        }

    }

    // -------------------------------------------------- Private Static Methods


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

    /**
     * <p>Compose update and delete statements.  This method returns an array of 2 Strings,
     * the first being an update statement and the second being a delete statement.
     *
     * If useConditionalWhereClause is true, the statements are composed in such a way that
     * they will work even if some columns are null.  This is accomplished by checking for
     * null in the where clause.  As a consequence of this, the column parameter in the where
     * clause must be set twice.
     * For example:
     * UPDATE table 1
     * SET col1 = ?, col2 = ?
     * WHERE ((? IS NULL AND col1 IS NULL) OR col1 = ?)
     * AND   ((? IS NULL AND col2 IS NULL) OR col2 = ?)
     *
     * If useConditionalWhereClause is false, when called with null as the value of the
     * columns argument, these returned statements will be the "standard" statements that
     * are prepared and used for all rows where no original values are null.  When called
     * with a non-null columns argument, this method returns statements that correctly deal
     * with null values in the where clause (that is, the where caluse contains a
     * "<column-name> IS NULL"
     */
    private String[] composeStatements(String extracted, List names,
        boolean useConditionalWhereClause, DataCache.Column[] columns) {

        // Construct the delete and update statements we will be using
        StringBuffer dsb = new StringBuffer("DELETE FROM "); // NOI18N
        if (extracted != null) {
            dsb.append(extracted);
        } else {
            if ((getSchemaName() != null) &&
                (getSchemaName().length() > 0)) {
                dsb.append(quoted(getSchemaName()));
                dsb.append("."); // NOI18N
            }
            dsb.append(quoted(getTableName()));
        }


        StringBuffer usb = new StringBuffer("UPDATE "); // NOI18N
        if (extracted != null) {
            usb.append(extracted);
        } else {
            if ((getSchemaName() != null) &&
                (getSchemaName().length() > 0)) {
                usb.append(quoted(getSchemaName()));
                usb.append("."); // NOI18N
            }
            usb.append(quoted(getTableName()));
        }

        usb.append(" SET "); // NOI18N
        int m = 0; // included columns
        int n = columnNames.size();
        for (int i = 0; i < n; i++) {
            if (!match(i, names)) {
                continue;
            }
            if (m > 0) {
                usb.append(", "); // NOI18N
            }
            usb.append(quoted((String)columnNames.get(i)));
            usb.append(" = ?"); // NOI18N
            m++;
        }

        dsb.append(" WHERE "); // NOI18N
        usb.append(" WHERE "); // NOI18N
        m = 0;
        for (int i = 0; i < n; i++) {
            if (!match(i, names)) {
                continue;
            }
            if (m > 0) {
                dsb.append(" AND "); // NOI18N
                usb.append(" AND "); // NOI18N
            }
            if (useConditionalWhereClause) {
                dsb.append("((? IS NULL AND ");
                usb.append("((? IS NULL AND ");
                dsb.append(quoted((String)columnNames.get(i)));
                usb.append(quoted((String)columnNames.get(i)));
                dsb.append(" IS NULL) OR ");
                usb.append(" IS NULL) OR ");
                dsb.append(quoted((String)columnNames.get(i)));
                usb.append(quoted((String)columnNames.get(i)));
                dsb.append(" = ?)");
                usb.append(" = ?)");
            } else if (columns != null && columns[i].getOriginal() == null) {
                dsb.append(quoted((String)columnNames.get(i)));
                usb.append(quoted((String)columnNames.get(i)));
                dsb.append(" IS NULL"); // NOI18N
                usb.append(" IS NULL"); // NOI18N
            } else {
                dsb.append(quoted((String)columnNames.get(i)));
                usb.append(quoted((String)columnNames.get(i)));
                dsb.append(" = ?"); // NOI18N
                usb.append(" = ?"); // NOI18N
            }

            m++;
        }

        String[] statements = new String[2];
        statements[0] = usb.toString();
        statements[1] = dsb.toString();
        //System.out.println("RSDM: " + ((columns == null)? "standard": "custom") + " update: " + statements[0]); //NOI18N
        //System.out.println("RSDM: " + ((columns == null)? "standard": "custom") + " delete: " + statements[1]); //NOI18N

        return statements;
    }
}
