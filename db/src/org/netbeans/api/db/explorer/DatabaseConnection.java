/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.db.explorer;

import java.sql.Connection;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnectionAccessor;

/**
 * Encapsulates a database connection. Each DatabaseConnection instance
 * represents a connection to a database in the Database Explorer.
 * 
 * <p>This class provides access to the properties of a database connection,
 * such as the connection name, URL, user or default schema. New connections
 * can be created using the {@link #create} method (these connection can be
 * added to the Database Explorer using the 
 * {@link ConnectionManager#addConnection} method.</p>
 * 
 * <p>It is also possible to retrieve the JDBC {@link java.sql.Connection}
 * using the {@link #getJDBCConnection} method (the connection can be connected
 * or disconnected using the {@link ConnectionManager#showConnectionDialog} 
 * and {@link ConnectionManager#disconnect} methods.</p>
 * 
 * @author Andrei Badea
 * @see ConnectionManager
 */
public final class DatabaseConnection {
    
    private org.netbeans.modules.db.explorer.DatabaseConnection delegate;

    /*
     * DatabaseConnection's methods delegate to 
     * org.netbeans.modules.db.explorer.DatabaseConnection. Each instance of
     * org.netbeans.modules.db.explorer.DatabaseConnection
     * creates and maintains an instance of
     * DatabaseConnection. Since the constructor of 
     * DatabaseConnection is package-protected, an accessor is needed
     * to create instances of DatabaseConnection from 
     * org.netbeans.modules.db.explorer.DatabaseConnection.
     *
     * See org.netbeans.modules.db.explorer.DatabaseConnectionAccessor
     */ 
    
    static {
        DatabaseConnectionAccessor.DEFAULT = new DatabaseConnectionAccessor() {
            public DatabaseConnection createDatabaseConnection(org.netbeans.modules.db.explorer.DatabaseConnection conn) {
                return new DatabaseConnection(conn);
            }    
        };
    }
    
    /**
     * Package-protected constructor.
     */
    DatabaseConnection(org.netbeans.modules.db.explorer.DatabaseConnection delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    /**
     * Returns the org.netbeans.modules.db.explorer.DatabaseConnection which this instance delegates to.
     */
    org.netbeans.modules.db.explorer.DatabaseConnection getDelegate() {
        return delegate;
    }
    
    /**
     * Creates a new DatabaseConnection instance. 
     * 
     * 
     * @param driver the JDBC driver the new connection uses; cannot be null.
     * @param connectionURL the URL of this connection; cannot be null.
     * @param user the username.
     * @param password the password.
     * @param rememberPassword whether to remeber the password for the current session.
     * @return the new instance.
     * @throws NullPointerException if driver or database are null.
     */
    public static DatabaseConnection create(JDBCDriver driver, String connectionURL, 
            String user, String schema, String password, boolean rememberPassword) {
        if (driver == null || connectionURL == null) {
            throw new NullPointerException();
        }
        org.netbeans.modules.db.explorer.DatabaseConnection conn = new org.netbeans.modules.db.explorer.DatabaseConnection();
        conn.setDriverName(driver.getName());
        conn.setDriver(driver.getClassName());
        conn.setDatabase(connectionURL);
        conn.setUser(user);
        conn.setSchema(schema);
        conn.setPassword(password);
        conn.setRememberPassword(true);
        
        return new DatabaseConnection(conn);
    }
    
    /**
     * Returns the JDBC driver class that this connection uses.
     *
     * @return the JDBC driver class
     */
    public String getDriverClass() {
        return delegate.getDriver();
    }

    /**
     * Returns this connection's URL.
     *
     * @return the connection's URL
     */
    public String getConnectionUrl() {
        return delegate.getDatabase();
    }
    
    /**
     * Returns this connection's default schema.
     *
     * @return the schema
     */
    public String getSchema() {
        return delegate.getSchema();
    }
    
    /**
     * Returns the username used to connect to the database.
     *
     * @return the username
     */
    public String getUser() {
        return delegate.getUser();
    }
    
    /**
     * Returns the password used to connect to the database.
     *
     * @return the password
     */
    public String getPassword() {
        return delegate.getPassword();
    }
    
    /**
     * Returns the programmatic name of this connection in the Database Explorer.
     *
     * @return the programmatic name
     */
    public String getName() {
        return delegate.getName();
    }
    
    /**
     * Returns the name used to display this connection in the UI.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return delegate.getName();
    }
    
    /**
     * Returns the {@link java.sql.Connection} instance which encapsulates 
     * the physical connection to the database. 
     *
     * @return the physical connection or null if not connected.
     *
     * @throws IllegalStateException if this connection is not added to the
     *         ConnectionManager.
     */
    public Connection getJDBCConnection() {
        if (!ConnectionList.getDefault().contains(delegate)) {
            throw new IllegalStateException("This connection is not added to the ConnectionManager."); // NOI18N
        }
        return delegate.getJDBCConnection();
    }

    /**
     * Returns a string representation of the database connection.
     */
    public String toString() {
        return "DatabaseConnection[name='" + getName() + "']"; // NOI18N
    }
}
