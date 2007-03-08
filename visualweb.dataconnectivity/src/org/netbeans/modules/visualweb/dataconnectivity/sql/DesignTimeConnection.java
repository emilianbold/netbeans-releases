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
package org.netbeans.modules.visualweb.dataconnectivity.sql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Map;

/**
 * A design time only wrapper class for Connection objects.  Used to
 * facilitate logging and to make design time connection pooling
 * possible in the future.  Use getWrappedConnection() to get the
 * underlying Connection object.
 *
 * @author John Kline
 */
public class DesignTimeConnection implements Connection {

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
}
