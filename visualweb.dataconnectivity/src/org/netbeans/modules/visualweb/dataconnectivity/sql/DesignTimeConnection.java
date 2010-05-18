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
package org.netbeans.modules.visualweb.dataconnectivity.sql;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Struct;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

// Added in Java 1.6
import java.sql.NClob;
import java.sql.SQLXML;
import java.sql.SQLClientInfoException;

/**
 * A design time only wrapper class for Connection objects.  Used to
 * facilitate logging and to make design time connection pooling
 * possible in the future.  Use getWrappedConnection() to get the
 * underlying Connection object.
 *
 * @author John Kline
 */
public class DesignTimeConnection implements Connection {

    protected static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.sql.Bundle", //NOI18N
                                                                  Locale.getDefault());

    static private int           nextId = 0;

    private int                  id;
    private Connection           wrappedConnection;
    private String               url;
    private String               driverClassName;
    private String               username;

    public DesignTimeConnection(DesignTimeDataSource dataSource, Connection wrappedConnection) {
        id                     = getNextId();
        Log.getLogger().entering(getClass().getName(), "DesignTimeConnection(" + id + ")",//NOI18N
            new Object[] {dataSource, wrappedConnection});
//         if (Log.getLogger().getLevel() == java.util.logging.Level.FINEST) {
//             Thread.currentThread().dumpStack();
//        }
        this.wrappedConnection = wrappedConnection;
        driverClassName        = dataSource.getDriverClassName();
        url                    = dataSource.getUrl();
        username               = dataSource.getUsername();
    }

    public Connection getWrappedConnection() {
        return wrappedConnection;
    }

    private synchronized int getNextId() {
        return nextId++;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("DesignTimeConnection("); //NOI18N
        s.append(id);
        s.append(',');
        s.append(wrappedConnection);
        s.append(',');
        s.append(driverClassName);
        s.append(',');
        s.append(url);
        s.append(',');
        s.append(username);
        s.append(')');
        return s.toString();
    }

    public void clearWarnings() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".clearWarnings()"); //NOI18N
        wrappedConnection.clearWarnings();
    }

    public void close() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".close()"); //NOI18N
        wrappedConnection.close();
    }

    public void commit() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".commit()"); //NOI18N
        wrappedConnection.commit();
    }

    public Statement createStatement() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".createStatement()"); //NOI18N
        return wrappedConnection.createStatement();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency)
        throws SQLException {

        Log.getLogger().entering(getClass().getName(), toString()+".createStatement()", //NOI18N
            new Object[] {new Integer(resultSetType), new Integer(resultSetConcurrency)});
        return wrappedConnection.createStatement(resultSetType, resultSetConcurrency);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency,
        int resultSetHoldability) throws SQLException {

        Log.getLogger().entering(getClass().getName(), toString()+".createStatement()", //NOI18N
            new Object[] {new Integer(resultSetType), new Integer(resultSetConcurrency),
            new Integer(resultSetHoldability)});
        return wrappedConnection.createStatement(resultSetType, resultSetConcurrency,
            resultSetHoldability);
    }

    public boolean getAutoCommit() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".getAutoCommit()"); //NOI18N
        return wrappedConnection.getAutoCommit();
    }

    public String getCatalog() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".getCatalog()"); //NOI18N
        return wrappedConnection.getCatalog();
    }

    public int getHoldability() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".getHoldability()"); //NOI18N
        return wrappedConnection.getHoldability();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".getMetaData()"); //NOI18N
        return wrappedConnection.getMetaData();
    }

    public int getTransactionIsolation() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".getTransactionIsolation()"); //NOI18N
        return wrappedConnection.getTransactionIsolation();
    }

    public Map getTypeMap() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".getTypeMap()"); //NOI18N
        return wrappedConnection.getTypeMap();
    }

    public SQLWarning getWarnings() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".getWarnings()"); //NOI18N
        return wrappedConnection.getWarnings();
    }

    public boolean isClosed() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".isClosed()"); //NOI18N
        return wrappedConnection.isClosed();
    }

    public boolean isReadOnly() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".isReadOnly()"); //NOI18N
        return wrappedConnection.isReadOnly();
    }

    public String nativeSQL(String sql) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".nativeSQL()", sql); //NOI18N
        return wrappedConnection.nativeSQL(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".prepareCall()", sql); //NOI18N
        return wrappedConnection.prepareCall(sql);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
        throws SQLException {

        Log.getLogger().entering(getClass().getName(), toString()+".prepareCall()", new Object[] //NOI18N
            {sql, new Integer(resultSetType), new Integer(resultSetConcurrency)});
        return wrappedConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
        int resultSetHoldability) throws SQLException {

        Log.getLogger().entering(getClass().getName(), toString()+".prepareCall()", new Object[] //NOI18N
            {sql, new Integer(resultSetType), new Integer(resultSetConcurrency),
            new Integer(resultSetHoldability)});
        return wrappedConnection.prepareCall(sql, resultSetType, resultSetConcurrency,
            resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".prepareStatement()", sql); //NOI18N
        return wrappedConnection.prepareStatement(sql);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
        throws SQLException {

        Log.getLogger().entering(getClass().getName(), toString()+".prepareStatement()", //NOI18N
            new Object[] {sql, new Integer(autoGeneratedKeys)});
        return wrappedConnection.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".prepareStatement()", //NOI18N
            new Object[] {sql, columnIndexes});
        return wrappedConnection.prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
        int resultSetConcurrency) throws SQLException {

        Log.getLogger().entering(getClass().getName(), toString()+".prepareStatement()", //NOI18N
            new Object[] {sql, new Integer(resultSetType), new Integer(resultSetConcurrency)});
        return wrappedConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
        int resultSetConcurrency, int resultSetHoldability) throws SQLException {

        Log.getLogger().entering(getClass().getName(), toString()+".prepareStatement()", //NOI18N
            new Object[] {sql, new Integer(resultSetType), new Integer(resultSetConcurrency),
            new Integer(resultSetHoldability)});
        return wrappedConnection.prepareStatement(sql, resultSetType, resultSetConcurrency,
            resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames)
        throws SQLException {

        Log.getLogger().entering(getClass().getName(), toString()+".prepareStatement()", //NOI18N
            new Object[] {sql, columnNames});
        return wrappedConnection.prepareStatement(sql, columnNames);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".releaseSavepoint()", savepoint); //NOI18N
        wrappedConnection.releaseSavepoint(savepoint);
    }

    public void rollback() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".rollback()"); //NOI18N
        wrappedConnection.rollback();
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".rollback()", savepoint); //NOI18N
        wrappedConnection.rollback(savepoint);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".setAutoCommit()", //NOI18N
            new Boolean(autoCommit));
        wrappedConnection.setAutoCommit(autoCommit);
    }

    public void setCatalog(String catalog) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".setCatalog()", catalog); //NOI18N
        wrappedConnection.setCatalog(catalog);
    }

    public void setHoldability(int holdability) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".setHoldability()", //NOI18N
            new Integer(holdability));
        wrappedConnection.setHoldability(holdability);
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".setReadOnly()", //NOI18N
            new Boolean(readOnly));
        wrappedConnection.setReadOnly(readOnly);
    }

    public Savepoint setSavepoint() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".setSavepoint()"); //NOI18N
        return wrappedConnection.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".setSavepoint()", name); //NOI18N
        return wrappedConnection.setSavepoint(name);
    }

    public void setTransactionIsolation(int level) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".setTransactionIsolation()", //NOI18N
            new Integer(level));
        wrappedConnection.setTransactionIsolation(level);
    }

    public void setTypeMap(Map map) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".setTypeMap()", map); //NOI18N
        wrappedConnection.setTypeMap(map);
    }

    // Methods added for compliance with Java 1.6

    public Clob createClob() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".createClob()"); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public Blob createBlob() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".createBlob()"); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public NClob createNClob() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".createNClob()"); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public SQLXML createSQLXML() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".createSQLXML()"); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public boolean isValid(int timeout) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".isValid()", timeout); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        Log.getLogger().entering(getClass().getName(), toString()+".setClientInfo()", new Object[] { name, value}); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        Log.getLogger().entering(getClass().getName(), toString()+".setClientInfo()", properties); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public String getClientInfo(String name) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".getClientInfo()", name); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public Properties getClientInfo() throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".getClientInfo()"); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".createArrayOf()", new Object[] { typeName, elements }); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".createStruct()", new Object[] { typeName, attributes }); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public boolean isWrapperFor(Class iface) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".isWrapperFor()", iface); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }

    public Object unwrap(Class iface) throws SQLException {
        Log.getLogger().entering(getClass().getName(), toString()+".unwrap()", iface); //NOI18N
        throw new RuntimeException(rb.getString("NOT_IMPLEMENTED"));
    }
}
