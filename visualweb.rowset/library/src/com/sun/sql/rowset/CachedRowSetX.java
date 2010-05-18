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
package com.sun.sql.rowset;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ParameterMetaData;
import java.sql.Savepoint;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;

/**
 * The extened interface that all implementations of {@link com.sun.sql.rowset.CachedRowSetX} must implement.
 *
 * <h3>1.0 Overview</h3>
 * An extension to the <code>CachedRowSet</code> interface.
 * <P>
 * A {@link com.sun.sql.rowset.CachedRowSetX} object differs from a CachedRowSet object
 * in the following ways:
 * <ul>
 *   <li> An <code>isExecuted()</code> method will return <code>true</code> if
 *        the {@link com.sun.sql.rowset.CachedRowSetX} is in a state where navigation and getters
 *        and setters can be called on rows returned from the database (i.e., returns
 *        true if execute or populate has been called.
 *   </li>
 *   <li> The following "advanced" properties have been added.  These properties are
 *        useful in the case of incomplete or incorrect information being supplied by
 *        JDBC drivers.<br/>
 *        Note: In most cases, these advanced properties do not need to be set (i.e., they can be
 *              left set to their default <code>null</code> value (or <code>false</code> in the
 *              case of <code>printStatements</code>).  For the cases where the advanced properties
 *              must be used, it is helpful to set the <code>printStatements</code> property to
 *              <code>true</code> first.  In that way, one can then determine what advanced
 *              properties must be set.  By setting the catalog and schema for the table to be
 *              upadted and by explicitly setting cataloag, table, schema and column names for 
 *              each column, one can explicitly set what columns will participate in inserts,
 *              updates and deletes.  Futher control can be gained by setting the updatableColumns
 *              and insertableColumns properties.  Note:  if updatableColumns is set and
 *              insertableColumns is not set, it is assumed that updateableColumns applies to
 *              both updates and inserts.
 *        <ul>
 *            <li><code>catalogName</code>: the name of the catalog that contains the table referred to by <code>tableName</code></li>
 *            <li><code>schemaName</code>: the name of the schema that contains the table referred to by <code>tableName</code></li>
 *            <li><code>columnCatalogNames</code>:  an array of {@link String}, one for each column, which contains the name of catalog of the corresponding column (each element can also be <code>null</code></li>
 *            <li><code>columnSchemaNames</code>:  an array of {@link String}, one for each column, which contains the name of the schema of the corresponding column (each element can also be <code>null</code></li>
 *            <li><code>columnTableNames</code>:  an array of {@link String}, one for each column, which contains the name of the table of the corresponding column (each element can also be <code>null</code></li>
 *            <li><code>columnNames</code>:  an array of {@link String}, one for each column, which contains the name of the corresponding column (each element can also be <code>null</code></li>
 *            <li><code>insertableColumns</code>:  an array of <code>boolean</code>, one for each column, which contains <code>true</code> if the corresponding column can be inserted, else it contains <code>false/code></li>
 *            <li><code>updatableColumns</code>:  an array of <code>boolean</code>, one for each column, which contains <code>true</code> if the corresponding column can be updated, else it contains <code>false/code></li>
 *            <li><code>printStatements</code>:  a <code>boolean{/code> that when <code>true</code> will result in internally generated <code>INSERT</code>, <code>UPDATE</code>, and <code>DELETE</code> SQL statements being written to <code>System.out</code>.  This output is useful in determining why inserts,updates and/or deletes are falling and in deciding what advanced properties to set in order to address the problems.</li>
 *        </ul>
 *   </li>
 *   <li> <code>PropertyChangeListener</code> support<br/>
 *        The following are bound properties:
 *        <ul>
 *            <li><code>command</code></li>
 *            <li><code>dataSourceName</code></li>
 *            <li><code>maxRows</code></li>
 *            <li><code>password</code></li>
 *            <li><code>type</code></li>
 *            <li><code>url</code></li>
 *            <li><code>username</code></li>
 *        </ul>
 *   </li>
 *   <li> support to use the {@link com.sun.sql.rowset.CachedRowSetX} after calling
 *        <code>close</code> on it.
 *   </li>
 *   <li> by default, a {@link com.sun.sql.rowset.CachedRowSetX}'s properties are set to:
 *        <ul>
 *            <li><code>command</code>: <code>null</code></li>
 *            <li><code>escapeProcessing</code>: <code>true</code></li>
 *            <li><code>keyColumns</code>: <code>null</code></li> (ignored)
 *            <li><code>maxFieldSize</code>: <code>0</code></li>
 *            <li><code>maxRows</code>: <code>0</code></li>
 *            <li><code>password</code>: <code>null</code></li>
 *            <li><code>readOnly</code>: <code>true</code> (ignored)</li>
 *            <li><code>showDeleted</code>: <code>false</code></li>
 *            <li><code>typeMap</code>: <code>null</code></li>
 *            <li><code>url</code>: <code>null</code></li>
 *            <li><code>username</code>: <code>null</code></li>
 *        </ul>
 *   </li>
 *   <li> the <code>readOnly</code> property is currently not used
 *   </li>
 *   <li> it is well defined how property changes affect the state of a {@link com.sun.sql.rowset.CachedRowSetX}
 *        <ul>
 *            <li><code>autoCommit</code>: nothing is invalidated</li>
 *            <li><code>command</code>: <code>release()</code> is called</li>
 *            <li><code>concurrency</code>: nohting is invalidated</li>
 *            <li><code>escapeProcessing</code>: nothing is invalidated</li>
 *            <li><code>maxFieldSize</code>: nothing is invalidated</li>
 *            <li><code>maxRows</code>: nothing is invalidated</li>
 *            <li><code>password</code>: nothing is invalidated</li>
 *            <li><code>readOnly</code>: nothing is invalidated</li>
 *            <li><code>showDeleted</code>: nothing is invalidated</li>
 *            <li><code>transactionIsolation</code>: nothing is invalidated</li>
 *            <li><code>type</code>: nothing is invalidated</li>
 *            <li><code>typeMap</code>: nothing is invalidated</li>
 *            <li><code>url</code>: <code>release()</code> is called</li>
 *            <li><code>username</code>: <code>release()</code> is called</li>
 *        </ul>
 *   </li>
 *   <li> calling <code>close</code> will never throw a SQLException
 *   </li>
 *        
 * </ul>
 */

public interface CachedRowSetX extends CachedRowSet {
    /**
     * Returns <code>true</code> if this rowset is in an executed state
     *
     * @return a <code>boolean</code> <code>true</code> if the {@link com.sun.sql.rowset.CachedRowSetX}
     * is in a state where navigation and getters and setters can be called on rows
     * returned from the database (i.e., returns <code>true</code> if <code>execute()</code>
     * or <code>populate</code> has been called.
     *
     * @exception SQLException if a database access error occurs
     */
    public boolean isExecuted() throws SQLException;

    /**
     * Add a <code>PropertyChangeListener</code> to the listener list.
     * The listener is registered for all bound properties.
     *
     * @param listener  The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);
 

    /**
     * Returns the catalog in which the table referred to by the <code>tableName</code> property
     * resides.
     * <P>
     * @return a {@link String} object giving the name of the catalog that contains
     *         the table referred to by tableName
     *<p>
     * Note:  This method is called by {@link com.sun.sql.rowset.internal.CachedRowSetXWriter}.
     *</p>
     *
     * @see javax.sql.RowSetMetaData#getCatalogName
     */
    public String getCatalogName();

    /**
     * Sets the identifier for catalog of the table referred to in the <code>tableName</code>
     * property.
     * The writer uses this name to determine which table to use when comparing the values in
     * the data source with the {@link com.sun.sql.rowset.CachedRowSetX} object's values during a
     * synchronization attempt.
     * The <code>catalogName</code> property also indicates where modified values from this
     * {@link com.sun.sql.rowset.CachedRowSetX} object should be written.
     * <P>
     * The implementation of this {@link com.sun.sql.rowset.CachedRowSetX} object may obtain the
     * the name internally from the {@link com.sun.sql.rowset.RowSetMetaDataXImpl} object.
     *<p>
     * Note:  This method is called by {@link com.sun.sql.rowset.internal.CachedRowSetXWriter}.
     *</p>
     *
     * @param catalogName a {@link String} object identifying the catalog of the table
     * from which this {@link com.sun.sql.rowset.CachedRowSetX} object was derived; can be
     * <code>null</code> or an empty {@link String}
     *
     * @see javax.sql.RowSetMetaData#setCatalogName
     * @see javax.sql.RowSetWriter
     */
    public void setCatalogName(String catalogName);

    /**
     * Returns the schema in which the table referred to by the <code>tableName</code> property
     * resides.
     * <P>
     * @return a {@link String} object giving the name of the catalog that contains
     *         the table referred to by tableName
     *<p>
     * Note:  This method is called by {@link com.sun.sql.rowset.internal.CachedRowSetXWriter}.
     *</p>
     *
     * @see javax.sql.RowSetMetaData#getCatalogName
     */
    public String getSchemaName();


    /**
     * Sets the identifier for schema of the table referred to in the <code>tableName</code>
     * property.
     * The writer uses this name to determine which table to use when comparing the values in
     * the data source with the {@link com.sun.sql.rowset.CachedRowSetX} object's values during a
     * synchronization attempt.
     * The <code>schemaName</code> property also indicates where modified values from this
     * {@link com.sun.sql.rowset.CachedRowSetX} object should be written.
     * <P>
     * The implementation of this {@link com.sun.sql.rowset.CachedRowSetX} object may obtain the
     * the name internally from the {@link com.sun.sql.rowset.RowSetMetaDataXImpl} object.
     *
     * @param schemaName a {@link String} object identifying the schema of the table
     * from which this {@link com.sun.sql.rowset.CachedRowSetX} object was derived; can be
     * <code>null</code> or an empty {@link String}
     *
     * @see javax.sql.RowSetMetaData#setSchemaName
     * @see javax.sql.RowSetWriter
     */
    public void setSchemaName(String schemaName);


    /**
     * Returns an array of {@link String}.  If <code>setColumnCatalogNames</code> was never called,
     * <code>null</code> is returned, else the value set when calling
     * <code>setColumnCatalogNames{/code> is returned.  See <code>setColumnCatalogNames</code>
     * for details.
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     *<p>
     * Note:  This method is called by {@link com.sun.sql.rowset.internal.CachedRowSetXWriter}.
     *</p>
     * <p>
     * @return an array of {@link String} objects or <code>null</code>
     */
    public String[] getColumnCatalogNames();


    /**
     * Returns a {@link String} which contains the catalog name set for the column or
     * <code>null</code>.
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     * @return a {@link String} object or <code>null</code>
     *
     * @throws NullPointerException if <code>columnCatalogNames</code> was never set.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is >=
     *  <code>columnCatalogName.length</code>
     */
    public String getColumnCatalogNames(int index);


    /**
     * Set the <code>columnCatalogNames</code> property.
     *
     * @param columnCatalogNames an array of {@link String}, one for each column, which contains
     *        the name of catalog of the corresponding column (each element can also be
     *        <code>null</code>
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     */
    public void setColumnCatalogNames(String[] columnCatalogNames);


    /**
     * Set the <code>columnCatalogNames</code> property.
     *
     * @param index the index of the array to set (zero based)
     * @param columnCatalogName the name of catalog for the column (can also be <code>null</code>
     * @throws NullPointerException if <code>columnCatalogNames</code> was never set.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is >=
     *  <code>columnCatalogName.length</code>
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     */
    public void setColumnCatalogNames(int index, String columnCatalogName);


    /**
     * Returns an array of {@link String}.  If <code>setColumnSchemaNames</code> was never called,
     * <code>null</code> is returned, else the value set when calling
     * <code>setColumnSchemaNames{/code> is returned.  See <code>setColumnSchemaNames</code>
     * for details.
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     *<p>
     * Note:  This method is called by {@link com.sun.sql.rowset.internal.CachedRowSetXWriter}.
     *</p>
     * <p>
     * @return an array of {@link String} objects or <code>null</code>
     */
    public String[] getColumnSchemaNames();


    /**
     * Returns a {@link String} which contains the schema name set for the column or
     * <code>null</code>.
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     * @return a {@link String} object or <code>null</code>
     *
     * @throws NullPointerException if <code>columnSchemaNames</code> was never set.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is >=
     *  <code>columnSchemaName.length</code>
     */
    public String getColumnSchemaNames(int index);


    /**
     * Set the <code>columnSchemaNames</code> property.
     *
     * @param columnSchemaNames an array of {@link String}, one for each column, which contains
     *        the name of schema of the corresponding column (each element can also be
     *        <code>null</code>
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     */
    public void setColumnSchemaNames(String[] columnSchemaNames);


    /**
     * Set the <code>columnSchemaNames</code> property.
     *
     * @param index the index of the array to set (zero based)
     * @param columnSchemaName the name of schema for the column (can also be <code>null</code>
     * @throws NullPointerException if <code>columnSchemaNames</code> was never set.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is >=
     *  <code>columnSchemaName.length</code>
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     */
    public void setColumnSchemaNames(int index, String columnSchemaName);


    /**
     * Returns an array of {@link String}.  If <code>setColumnTableNames</code> was never called,
     * <code>null</code> is returned, else the value set when calling
     * <code>setColumnTableNames{/code> is returned.  See <code>setColumnTableNames</code>
     * for details.
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     *<p>
     * Note:  This method is called by {@link com.sun.sql.rowset.internal.CachedRowSetXWriter}.
     *</p>
     * <p>
     * @return an array of {@link String} objects or <code>null</code>
     */
    public String[] getColumnTableNames();


    /**
     * Returns a {@link String} which contains the table name set for the column or
     * <code>null</code>.
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     * @return a {@link String} object or <code>null</code>
     *
     * @throws NullPointerException if <code>columnTableNames</code> was never set.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is >=
     *  <code>columnTableName.length</code>
     */
    public String getColumnTableNames(int index);


    /**
     * Set the <code>columnTableNames</code> property.
     *
     * @param columnTableNames an array of {@link String}, one for each column, which contains
     *        the name of table of the corresponding column (each element can also be
     *        <code>null</code>
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     */
    public void setColumnTableNames(String[] columnTableNames);


    /**
     * Set the <code>columnTableNames</code> property.
     *
     * @param index the index of the array to set (zero based)
     * @param columnTableName the name of table for the column (can also be <code>null</code>
     * @throws NullPointerException if <code>columnTableNames</code> was never set.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is >=
     *  <code>columnTableName.length</code>
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     */
    public void setColumnTableNames(int index, String columnTableName);


    /**
     * Returns an array of {@link String}.  If <code>setColumnNames</code> was never called,
     * <code>null</code> is returned, else the value set when calling
     * <code>setColumnNames{/code> is returned.  See <code>setColumnNames</code>
     * for details.
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     *<p>
     * Note:  This method is called by {@link com.sun.sql.rowset.internal.CachedRowSetXWriter}.
     *</p>
     * <p>
     * @return an array of {@link String} objects or <code>null</code>
     */
    public String[] getColumnNames();


    /**
     * Returns a {@link String} which contains the column name set for the column or
     * <code>null</code>.
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     * @return a {@link String} object or <code>null</code>
     *
     * @throws NullPointerException if <code>columnNames</code> was never set.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is >=
     *  <code>columnName.length</code>
     */
    public String getColumnNames(int index);


    /**
     * Set the <code>columnNames</code> property.
     *
     * @param columnNames an array of {@link String}, one for each column, which contains
     *        the name of column (each element can also be <code>null</code>
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     */
    public void setColumnNames(String[] columnNames);


    /**
     * Set the <code>columnNames</code> property.
     *
     * @param index the index of the array to set (zero based)
     * @param columnName for the column (can also be <code>null</code>
     * @throws NullPointerException if <code>columnNames</code> was never set.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is >=
     *  <code>columnName.length</code>
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     */
    public void setColumnNames(int index, String columnName);


    /**
     * Returns an array of <code>boolean{/code>.  If <code>setInsertableColumns</code> was never called,
     * <code>null</code> is returned, else the value set when calling
     * <code>setInsertableColumns{/code> is returned.  See <code>setInsertableColumns</code>
     * for details.
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     *<p>
     * Note:  This method is called by {@link com.sun.sql.rowset.internal.CachedRowSetXWriter}.
     *</p>
     * <p>
     * @return an array of <code>boolean{/code> objects or <code>null</code>
     */
    public boolean[] getInsertableColumns();


    /**
     * Returns a <code>boolean{/code> of <code>true</code> if the column should be inserted when
     * when adding rows or <code>null</code>.
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     * @return a <code>boolean/code> object or <code>null</code>
     *
     * @throws NullPointerException if <code>insertableColumns</code> was never set.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is >=
     *  <code>insertableColumns.length</code>
     */
    public boolean getInsertableColumns(int index);


    /**
     * Set the <code>insertableColumns</code> property.
     *
     * @param insertableColumns an array of <code>boolean</code> one for each column, which contains
     *        a boolean indicating whether or not the column should be inserted when adding
     *        rows to the {@link com.sun.sql.rowset.CachedRowSetX}
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     */
    public void setInsertableColumns(boolean[] insertableColumns);


    /**
     * Set the <code>insertableColumns</code> property.
     *
     * @param index the index of the array to set (zero based)
     * @param insertableColumn true if column should be inserted for new rows
     * @throws NullPointerException if <code>insertableColumns</code> was never set.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is >=
     *  <code>insertableColumns.length</code>
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     */
    public void setInsertableColumns(int index, boolean insertableColumn);


    /**
     * Returns an array of <code>boolean{/code>.  If <code>setUpdatableColumns</code> was never called,
     * <code>null</code> is returned, else the value set when calling
     * <code>setUpdatebleColumns{/code> is returned.  See <code>setUpdatableColumns</code>
     * for details.
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     *<p>
     * Note:  This method is called by {@link com.sun.sql.rowset.internal.CachedRowSetXWriter}.
     *</p>
     * <p>
     * @return an array of <code>boolean{/code> objects or <code>null</code>
     */
    public boolean[] getUpdatableColumns();


    /**
     * Returns a <code>boolean{/code> of <code>true</code> if the column should be updated when
     * when updating rows or <code>null</code>.
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     * @return a <code>boolean{/code> object or <code>null</code>
     *
     * @throws NullPointerException if <code>updatableColumns</code> was never set.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is >=
     *  <code>updatableColumns.length</code>
     */
    public boolean getUpdatableColumns(int index);


    /**
     * Set the <code>updatableColumns</code> property.
     *
     * @param updatableColumns an array of <code>boolean</code>, one for each column, which contains
     *        a boolean indicating whether or not the column should be updated when updating
     *        rows to the {@link com.sun.sql.rowset.CachedRowSetX}
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     */
    public void setUpdatableColumns(boolean[] updatableColumns);


    /**
     * Set the <code>updatableColumns</code> property.
     *
     * @param index the index of the array to set (zero based)
     * @param updatableColumn true if column should be updated when rows are updated
     * @throws NullPointerException if <code>updatableColumns</code> was never set.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is >=
     *  <code>updatableColumns.length</code>
     * <p>
     * Note, the column is zero based whereas most jdbc calls are one based.
     * <p>
     */
    public void setUpdatableColumns(int index, boolean updatableColumn);


    /**
     * Returns the <code>printStatements</code> property.
     *
     * @return a <code>boolean{/code> object which, if true, causes SQL statements to be written to
     *         <code>System.out</code>.
     *<p>
     * Note:  This method is called by {@link com.sun.sql.rowset.internal.CachedRowSetXWriter}.
     *</p>
     */
    public boolean getPrintStatements();


    /**
     * Sets the <code>printStatements</code> property.
     *
     *  If this property is <code>true</code>,
     * SQL <code>Insert</code>, <code>UPDATE</code> and <code>DELETE</code> statements will
     * be written to <code>System.out</code>.  This property is intended to be set when debugging
     * problems inserting, updating and deleting.  With the information gained from examining the
     * output, it is intended that one can set the other advanced properties on the
     * {@link com.sun.sql.rowset.CachedRowSetX} to "fix" the SQL statements being generated.  In this way,
     * some JDBC driver problems can be overcome.
     *</p>
     * @param printStatements a <code>boolean{/code> object which determines whether
     * SQL <code>Insert</code>, <code>UPDATE</code> and <code>DELETE</code> statements are
     * written to <code>System.out</code>
     */
    public void setPrintStatements(boolean printStatements);

    /**
     * Add a PropertyChangeListener for a specific property.  The listener
     * will be invoked only on a change of the specific property.
     *
     * @param propertyName  The name of the property to listen on.
     * @param listener  The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Remove a <code>PropertyChangeListener</code> from the listener list.
     *
     * This removes a <code>PropertyChangeListener</code> that was registered
     * for all bound properties.
     *
     * @param listener  The PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove a PropertyChangeListener for a specific property.
     *
     * @param propertyName  The name of the property that was listened on.
     * @param listener  The PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
}
