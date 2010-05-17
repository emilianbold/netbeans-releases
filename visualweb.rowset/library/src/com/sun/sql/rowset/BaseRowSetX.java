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

import javax.sql.rowset.*;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.*;
import java.io.Serializable;

/**
 * An abstract class extending <code>javax.sql.rowset.BaseRowSet</code> which contains
 * changes necessary to support using RowSets as java beans.
*/

public abstract class BaseRowSetX extends BaseRowSet {

    private static ResourceBundle rb = ResourceBundle.getBundle("com.sun.sql.rowset.Bundle",
        Locale.getDefault());

    /**
     * The maximum number of rows the reader should read.
     * @serial
     */
    private int maxRows = 0; // default is no limit

    /**
     * Supplants the fetchDir in the superclass
     * A constant used as a hint to the driver that indicates the direction in 
     * which data from this JDBC <code>RowSet</code> object  is going
     * to be fetched. The following <code>ResultSet</code> constants are
     * possible values:
     * <code>FETCH_FORWARD</code>, 
     * <code>FETCH_REVERSE</code>, 
     * <code>FETCH_UNKNOWN</code>.
     * <P>
     * Unused at this time.
     * @serial
     */
    private int fetchDir = ResultSet.FETCH_FORWARD; // default fetch direction

    /**
     * A hint to the driver that indicates the expected number of rows
     * in this JDBC <code>RowSet</code> object .
     * <P>
     * Unused at this time.
     * @serial
     */
    private int fetchSize = 0; // default fetchSize
 
    /**
     * Sets this <code>RowSet</code> object's <code>command</code> property to
     * the given <code>String</code> object and clears the parameters, if any,
     * that were set for the previous command.
     * <P>
     * The <code>command</code> property may not be needed if the <code>RowSet</code>
     * object gets its data from a source that does not support commands,
     * such as a spreadsheet or other tabular file.
     * Thus, this property is optional and may be <code>null</code>.
     *
     * @param cmd a <code>String</code> object containing an SQL query
     *            that will be set as this <code>RowSet</code> object's command
     *            property; may be <code>null</code> but may not be an empty string
     * @throws SQLException if an empty string is provided as the command value
     * @see #getCommand
     */
    public void setCommand(String cmd) throws SQLException {
        super.setCommand((cmd == null || cmd.length() == 0)? null: cmd);
    }

    /**
     * Sets the Url property for this <code>RowSet</code> object
     * to the given <code>String</code> object and sets the dataSource name
     * property to <code>null</code>. The Url property is a
     * JDBC URL that is used when
     * the connection is created using a JDBC technology-enabled driver
     * ("JDBC driver") and the <code>DriverManager</code>.
     * The correct JDBC URL for the specific driver to be used can be found
     * in the driver documentation.  Although there are guidelines for for how
     * a JDBC URL is formed,
     * a driver vendor can specify any <code>String</code> object except
     * one with a length of <code>0</code> (an empty string).
     * <P>
     * Setting the Url property is optional if connections are established using
     * a <code>DataSource</code> object instead of the <code>DriverManager</code>.
     * The driver will use either the URL property or the
     * dataSourceName property to create a connection, whichever was
     * specified most recently. If an application uses a JDBC URL, it
     * must load a JDBC driver that accepts the JDBC URL before it uses the
     * <code>RowSet</code> object to connect to a database.  The <code>RowSet</code>
     * object will use the URL internally to create a database connection in order
     * to read or write data.
     *
     * @param url a <code>String</code> object that contains the JDBC URL
     *     that will be used to establish the connection to a database for this
     *     <code>RowSet</code> object; may be <code>null</code> but must not
     *     be an empty string
     * @throws SQLException if an error occurs setting the Url property or the
     *     parameter supplied is a string with a length of <code>0</code> (an
     *     empty string)
     * @see #getUrl
     */
    public void setUrl(String url) throws SQLException {
        super.setUrl((url == null || url.length() == 0)? null: url);
    }

    /**
     * Sets the <code>DataSource</code> name property for this <code>RowSet</code>
     * object to the given logical name and sets this <code>RowSet</code> object's
     * Url property to <code>null</code>. The name must have been bound to a
     * <code>DataSource</code> object in a JNDI naming service so that an
     * application can do a lookup using that name to retrieve the
     * <code>DataSource</code> object bound to it. The <code>DataSource</code>
     * object can then be used to establish a connection to the data source it
     * represents.
     * <P>
     * Users should set either the Url property or the dataSourceName property.
     * If both properties are set, the driver will use the property set most recently.
     *
     * @param name a <code>String</code> object with the name that can be supplied
     *        to a naming service based on JNDI technology to retrieve the
     *        <code>DataSource</code> object that can be used to get a connection;
     *        may be <code>null</code> but must not be an empty string
     * @throws SQLException if there is a problem setting the
     *          <code>dataSourceName</code> property or <i>name</i> is an empty string
     * @see #getDataSourceName
     */
    public void setDataSourceName(String name) throws SQLException {        
        super.setDataSourceName((name == null || name.length() == 0)? null: name);
    }

    /**
     * Sets the maximum number of rows that this <code>RowSet</code> object may contain to
     * the given number. If this limit is exceeded, the excess rows are
     * silently dropped.
     *
     * @param max an <code>int</code> indicating the current maximum number
     *     of rows; zero means that there is no limit
     * @throws SQLException if an error occurs internally setting the
     *     maximum limit on the number of rows that a JDBC <code>RowSet</code> object
     *     can contain; or if <i>max</i> is less than <code>0</code>; or
     *     if <i>max</i> is less than the <code>fetchSize</code> of the
     *     <code>RowSet</code>
     */
    public void setMaxRows(int max) throws SQLException {
        if (max < 0) {
            throw new SQLException(rb.getString("MAX_ROWS_INVALID") + " " + max);
        }
        this.maxRows = max;
    }

    /**
     * Retrieves the maximum number of rows that this <code>RowSet</code> object may contain. If 
     * this limit is exceeded, the excess rows are silently dropped.
     *
     * @return an <code>int</code> indicating the current maximum number of
     *     rows; zero means that there is no limit
     * @throws SQLException if an error occurs internally determining the
     *     maximum limit of rows that a <code>Rowset</code> object can contain
     */ 
    public int getMaxRows() throws SQLException {         
        return maxRows;
    }

    /**
     * Gives the driver a performance hint as to the direction in
     * which the rows in this <code>RowSet</code> object will be
     * processed.  The driver may ignore this hint.
     * <P>
     * A <code>RowSet</code> object inherits the default properties of the
     * <code>ResultSet</code> object from which it got its data.  That
     * <code>ResultSet</code> object's default fetch direction is set by
     * the <code>Statement</code> object that created it.
     * <P>
     * This method applies to a <code>RowSet</code> object only while it is
     * connected to a database using a JDBC driver.
     * <p>
     * A <code>RowSet</code> object may use this method at any time to change
     * its setting for the fetch direction.
     *
     * @param direction one of <code>ResultSet.FETCH_FORWARD</code>,
     *                  <code>ResultSet.FETCH_REVERSE</code>, or
     *                  <code>ResultSet.FETCH_UNKNOWN</code>
     * @throws SQLException if (1) the <code>RowSet</code> type is
     *     <code>TYPE_FORWARD_ONLY</code> and the given fetch direction is not
     *     <code>FETCH_FORWARD</code> or (2) the given fetch direction is not
     *     one of the following:
     *        ResultSet.FETCH_FORWARD,
     *        ResultSet.FETCH_REVERSE, or
     *        ResultSet.FETCH_UNKNOWN
     * @see #getFetchDirection
     */
    public void setFetchDirection(int direction) throws SQLException {
        // Changed the condition checking to the below as there were two
        // conditions that had to be checked
        // 1. RowSet is TYPE_FORWARD_ONLY and direction is not FETCH_FORWARD
        // 2. Direction is not one of the valid values

        /* !JK This violates the java beans rules that properties can be set in any order
        if (((getType() == ResultSet.TYPE_FORWARD_ONLY) && (direction != ResultSet.FETCH_FORWARD)) ||
         */
         if (
            ((direction != ResultSet.FETCH_FORWARD) &&
            (direction != ResultSet.FETCH_REVERSE) &&
            (direction != ResultSet.FETCH_UNKNOWN))) {
            throw new SQLException(rb.getString("INVALID_FETCH_DIRECTION"));
        }
        fetchDir = direction;
    }

    /**
     * Retrieves this <code>RowSet</code> object's current setting for the 
     * fetch direction. The default type is <code>ResultSet.FETCH_FORWARD</code>
     *
     * @return one of <code>ResultSet.FETCH_FORWARD</code>,
     *                  <code>ResultSet.FETCH_REVERSE</code>, or
     *                  <code>ResultSet.FETCH_UNKNOWN</code>
     * @throws SQLException if an error occurs in determining the 
     *     current fetch direction for fetching rows
     * @see #setFetchDirection
     */ 
    public int getFetchDirection() throws SQLException {
        return (fetchDir);
    }

    /**
     * Sets the fetch size for this <code>RowSet</code> object to the given number of
     * rows.  The fetch size gives a JDBC technology-enabled driver ("JDBC driver")
     * a hint as to the
     * number of rows that should be fetched from the database when more rows
     * are needed for this <code>RowSet</code> object. If the fetch size specified
     * is zero, the driver ignores the value and is free to make its own best guess
     * as to what the fetch size should be.
     * <P>
     * A <code>RowSet</code> object inherits the default properties of the
     * <code>ResultSet</code> object from which it got its data.  That
     * <code>ResultSet</code> object's default fetch size is set by
     * the <code>Statement</code> object that created it.
     * <P>
     * This method applies to a <code>RowSet</code> object only while it is
     * connected to a database using a JDBC driver.
     * For connected <code>RowSet</code> implementations such as
     * <code>JdbcRowSet</code>, this method has a direct and immediate effect
     * on the underlying JDBC driver.
     * <P>
     * A <code>RowSet</code> object may use this method at any time to change
     * its setting for the fetch size.
     * <p>
     * For <code>RowSet</code> implementations such as
     * <code>CachedRowSet</code>, which operate in a disconnected environment,
     * the <code>SyncProvider</code> object being used
     * may leverage the fetch size to poll the data source and
     * retrieve a number of rows that do not exceed the fetch size and that may
     * form a subset of the actual rows returned by the original query. This is
     * an implementation variance determined by the specific <code>SyncProvider</code>
     * object employed by the disconnected <code>RowSet</code> object.
     * <P>
     *
     * @param rows the number of rows to fetch; <code>0</code> to let the
     *        driver decide what the best fetch size is; must not be less
     *        than <code>0</code> or more than the maximum number of rows
     *        allowed for this <code>RowSet</code> object (the number returned
     *        by a call to the method {@link #getMaxRows})
     * @throws SQLException if the specified fetch size is less than <code>0</code>
     *        or more than the limit for the maximum number of rows
     * @see #getFetchSize
     */
    public void setFetchSize(int rows) throws SQLException {
        //Added this checking as maxRows can be 0 when this function is called
        //maxRows = 0 means rowset can hold any number of rows, os this checking
        // is needed to take care of this condition.
        if (rows < 0) {
            throw new SQLException(rb.getString("INVALID_FETCH_SIZE") + " " + rows);
        }
        fetchSize = rows;
    }

    /**
     * Returns the fetch size for this <code>RowSet</code> object. The default 
     * value is zero.
     *
     * @return the number of rows suggested as the fetch size when this <code>RowSet</code> object 
     *     needs more rows from the database
     * @throws SQLException if an error occurs determining the number of rows in the
     *     current fetch size
     * @see #setFetchSize
     */ 
    public int getFetchSize() throws SQLException {
        return fetchSize;
    }
}
